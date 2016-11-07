package com.shareplaylearn.models;

/**
 /**
 * small class with possible fields holding timeouts
 * GSON should fill them out if they exist, null otherwise.
 * Also used in the AuthService as the model to return in JSON.
 */
@SuppressWarnings("WeakerAccess")
public class TokenInfo {
    private String token;
    private String userId;
    private Long expiration;

    public TokenInfo() {
    }

    public TokenInfo( String token, String userId ) {
        this.token = token;
        this.userId = userId;
    }

    public TokenInfo( String token, String userId, long expiresIn ) {
        this.token = token;
        this.userId = userId;
        this.expiration = System.currentTimeMillis() + expiresIn;
    }

    public String getToken() {
        return token;
    }

    public TokenInfo setToken(String token) {
        this.token = token;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public TokenInfo setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Long getExpiration() {
        return expiration;
    }

    public TokenInfo setExpiration(Long expiration) {
        this.expiration = expiration;
        return this;
    }
}