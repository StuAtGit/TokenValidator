package com.shareplaylearn;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.shareplaylearn.models.GoogleUserEntity;
import com.shareplaylearn.models.TokenInfo;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 *  Checks tokens and caches them once a valid response is received.
 *  Initially, attempted to extract expiration from the token validation response,
 *  but it's not present in the response from google's validation.
 *  Since who ever is asking for this can decide whatever they want about the token,
 *  it shouldn't hurt to let them ask for it to be cached.
 *
 *   TODO: Perhaps make this work with multiple resources, for particular access token domains/types
 *   TODO: (e.g. OAuth or internal auth. Or multiple Oauth providers.
 *   TODO: Another option is to have clients keep multiple TokenValidators around
 *   TODO: Or have a composite class that tracks all of them for the shareplaylearn API.
 *
 */
public class TokenValidator
{
    private String validationResource;
    private Cache<String,TokenInfo> tokenCache;
    private Cache<String,TokenInfo> invalidCache;
    private HttpClient httpClient;
    private Logger log;
    private Gson gson;
    private long cacheTime;

    public TokenValidator( String validationResource,
                           int cacheSize,
                           long cacheTime) {
        initialize(validationResource, cacheSize, cacheTime, HttpClients.createDefault());
    }

    public TokenValidator(String validationResource,
                          int cacheSize,
                          long cacheTime,
                          CloseableHttpClient httpClient ) {
        initialize(validationResource, cacheSize, cacheTime, httpClient);
    }

    private void initialize(String validationResource,
                            int cacheSize,
                            long cacheTime,
                            CloseableHttpClient httpClient) {
        this.validationResource = validationResource;
        tokenCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheTime, TimeUnit.SECONDS)
                .maximumSize(cacheSize).build();
        invalidCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheTime, TimeUnit.SECONDS)
                .maximumSize(cacheSize).build();
        this.httpClient = httpClient;
        this.cacheTime = cacheTime;
        log = LoggerFactory.getLogger(TokenValidator.class);
        log.debug("token validator created.");
        this.gson = new Gson();
    }

    /**
     * This sets the expireIn for the token to be equal to the cache expiration time.
     * @param token ephemeral token that to verify
     * @param userId the userId that should be associated with the token
     * @return true if the given token is valid and associated with the given userId
     * @throws IOException
     */

    @SuppressWarnings({"unused", "WeakerAccess"})
    public boolean isValid(String token, String userId) throws IOException {
        return isValid(token, userId, this.cacheTime);
    }

    /**
     * If you want a specific token to have an expiresIn shorter than the cache
     * expiresIn, you can use this method. If you set the expireIn to be longer
     * than the cache expiration, it will have no effect.
     * @param token ephemeral token that to verify
     * @param userId the userId that should be associated with the token
     * @param expiresIn how long to cache the response, if this is not retrieved from the cache
     *                  (in seconds)
     * @return true if the given token is valid and associated with the given userId
     * @throws IOException
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public boolean isValid(String token, String userId, Long expiresIn) throws IOException {
        TokenInfo tokenInfo;

        if( (tokenInfo = this.invalidCache.getIfPresent(token)) != null ) {
            if( tokenInfo.getExpiration() < System.currentTimeMillis() * 1000 ) {
                log.debug("Token was in invalid cache, returning false.");
                return false;
            }
        }

        GoogleUserEntity googleUserEntity = null;

        if( (tokenInfo = this.tokenCache.getIfPresent(token)) != null ) {
            if( tokenInfo.getExpiration() < System.currentTimeMillis() * 1000 ) {
                log.debug("Retrieved token validation from cache.");
                googleUserEntity = new GoogleUserEntity().setId(tokenInfo.getUserId());
            } else {
                this.tokenCache.invalidate(token);
            }
        }

        if( googleUserEntity == null ){
            HttpGet get = new HttpGet(this.validationResource);
            get.addHeader("Authorization", "Bearer " + token);
            try (CloseableHttpResponse response =
                         (CloseableHttpResponse) this.httpClient.execute(get)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    log.info("Authorization for token: " + token + " failed: " + response.getStatusLine().getStatusCode());
                    log.info(response.getStatusLine().getReasonPhrase());
                    this.invalidCache.put(token, new TokenInfo(token, userId, expiresIn));
                    return false;
                }
                if (response.getEntity() != null) {
                    String responseEntity = EntityUtils.toString(response.getEntity());
                    log.debug(responseEntity);
                    googleUserEntity = gson.fromJson(responseEntity, GoogleUserEntity.class);
                } else {
                    log.error("Entity from auth service was null.");
                    return false;
                }
            }
        }

        if(googleUserEntity.getId().equals(userId)) {
            this.tokenCache.put(token, new TokenInfo(token, userId, expiresIn));
            return true;
        } else {
            this.invalidCache.put(token, new TokenInfo(token,userId,expiresIn));
            log.info("Someone gave a valid token: " + token +
                    ", but asked for another user, the other user was: " + userId);
            return false;
        }
    }

    public static void main(String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
