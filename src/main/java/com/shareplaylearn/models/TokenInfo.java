package com.shareplaylearn.models;

/**
 /**
 * small class with possible fields holding timeouts
 * GSON should fill them out if they exist, null otherwise.
 * Also used in the AuthService as the model to return in JSON
 */
@SuppressWarnings("WeakerAccess")
public class TokenInfo {
    public String token;
    public Long expires_in;
    public Long expiration;

    public TokenInfo() {
    }

    public TokenInfo( String token ) {
        this.token = token;
    }
    public TokenInfo( String token, long expires_in ) {
        this.token = token;
        this.expires_in = expires_in;
        this.expiration = System.currentTimeMillis() + expires_in;
    }
}