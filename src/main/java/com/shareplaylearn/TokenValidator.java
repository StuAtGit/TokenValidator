package com.shareplaylearn;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.primitives.Booleans;
import com.google.gson.Gson;
import com.shareplaylearn.exceptions.Exceptions;
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
    private HttpClient httpClient;
    private Gson gson;
    private Logger log;
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
        this.httpClient = httpClient;
        gson = new Gson();
        this.cacheTime = cacheTime;
        log = LoggerFactory.getLogger(TokenValidator.class);
        log.debug("token validator created.");
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public boolean isValid(String token) throws IOException {
        return isValid(token, this.cacheTime);
    }

    /**
     * If you want a specific token to have an expiration shorter than the cache
     * expiration, you can use this method.
     * @param token
     * @param expiration
     * @return
     * @throws IOException
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public boolean isValid(String token, Long expiration) throws IOException {
        TokenInfo tokenInfo;
        if( (tokenInfo = this.tokenCache.getIfPresent(token)) != null ) {
            if( tokenInfo.expiration < System.currentTimeMillis() * 1000 ) {
                log.debug("Retrieved token validation from cache.");
                return true;
            } else {
                this.tokenCache.invalidate(token);
            }
        }

        HttpGet get = new HttpGet( this.validationResource );
        get.addHeader("Authorization", "Bearer " + token);
        try( CloseableHttpResponse response =
                     (CloseableHttpResponse) this.httpClient.execute(get)) {
            if( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK ) {
                log.info("Authorization for token: " + token + " failed: " + response.getStatusLine().getStatusCode());
                log.info(response.getStatusLine().getReasonPhrase());
                return false;
            }
            this.tokenCache.put(token, new TokenInfo(token, expiration));
            return true;
        }
    }

    public static void main(String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
