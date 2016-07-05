package com.shareplaylearn;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.shareplaylearn.exceptions.Exceptions;
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
 * This library validates tokens by two means:
 *   - for cached tokens, it checks if they're expired
 *   - for brand new tokens, it queries the given resource, and checks for OK, if the token is OK, and
 *   the entity response contains a JSON entity with a expiry field named in TokenInfo,
 *   the token will be cached. There is a global expiration for the token cache,
 *   but tokens which are expired will be evicted as well.
 *
 *   TODO: Perhaps make this work with multiple resources, for particular access token domains/types
 *   TODO: (e.g. OAuth or internal auth. Or multiple Oauth providers.
 *   TODO: Another option is to have clients keep multiple TokenValidators around
 *   TODO: Or have a composite class that tracks all of them for the shareplaylearn API.
 *
 */
public class TokenValidator
{
    /**
     * small class with possible fields holding timeouts
     * GSON should fill them out if they exist, null otherwise.
     */
    private static class TokenInfo {
        Long expires_in;
        Long expiration;
    }

    private final String validationResource;
    private final Cache<String,TokenInfo> tokenCache;
    private final HttpClient httpClient;
    private final Gson gson;
    private final Logger log;
    //if the configured resource doesn't send back JSON, don't keep trying.
    private boolean disableJsonParse;

    public TokenValidator( String validationResource,
                           int cacheSize,
                           long maxCacheTime) {
        this.validationResource = validationResource;
        tokenCache = CacheBuilder.newBuilder()
                .expireAfterAccess(maxCacheTime, TimeUnit.SECONDS)
                .maximumSize(cacheSize).build();
        httpClient = HttpClients.
                createDefault();
        gson = new Gson();
        log = LoggerFactory.getLogger(TokenValidator.class);
        disableJsonParse = false;
    }

    public TokenValidator(String validationResource,
                          int cacheSize,
                          long maxCacheTime,
                          CloseableHttpClient httpClient ) {
        this.validationResource = validationResource;
        tokenCache = CacheBuilder.newBuilder()
                .expireAfterAccess(maxCacheTime, TimeUnit.SECONDS)
                .maximumSize(cacheSize).build();
        this.httpClient = httpClient;
        gson = new Gson();
        log = LoggerFactory.getLogger(TokenValidator.class);
        disableJsonParse = false;
    }

    @SuppressWarnings("unused")
    public boolean isValid(String token ) throws IOException {
        TokenInfo tokenInfo;
        if( ( tokenInfo = this.tokenCache.getIfPresent(token)) != null ) {
            if( tokenInfo.expiration > System.currentTimeMillis() * 1000 ) {
                this.tokenCache.invalidate(token);
            } else {
                return true;
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
            if( !disableJsonParse && response.getEntity() != null ) {
                String entity = EntityUtils.toString(response.getEntity());
                try {
                    tokenInfo = gson.fromJson(entity, TokenInfo.class);
                    if( tokenInfo.expires_in != null ) {
                        //expire it a little early to play it safe on any lag.
                        //not perfect - but it never can be, really.
                        tokenInfo.expiration = (System.currentTimeMillis() * 1000 + tokenInfo.expires_in) - 5;
                    }
                    if( tokenInfo.expiration != null ) {
                        this.tokenCache.put(token, tokenInfo);
                    }
                    //GSON tends to throw runtime exceptions, some are just throwable (annoying)
                    //so this is really the best way to deal with non-JSON resources.
                } catch ( Throwable t ) {
                    log.info("Token validation resource: " + this.validationResource + " did not return" +
                            " a valid JSON entity: " + Exceptions.asString(t) + " disabling JSON parsing (this will disable caching as well)." );
                    disableJsonParse = true;
                }
            }
            return true;
        }
    }

    public boolean isDisableJsonParse() {
        return disableJsonParse;
    }

    public TokenValidator setDisableJsonParse(boolean disableJsonParse) {
        this.disableJsonParse = disableJsonParse;
        return this;
    }

    public static void main(String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
