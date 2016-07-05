package com.shareplaylearn;

import java.io.IOException;

/**
 * Unit test for simple App.
 */
public class TokenValidatorTest
{
    /**
     * Create the test case
     *
     */
    public TokenValidatorTest() {
    }

    /**
     * This serves as a separate e2e test harness.
     * @param args
     */
    public static void main( String[] args ) throws IOException {
        /**
         * TODO:
         * Working - but auth service actually cares whether there is a trailing slash,
         * and should be modified to return JSON with expires_in
         */
        TokenValidator tokenValidator = new TokenValidator(
                "https://www.shareplaylearn.com/auth_api/oauthToken_validation",
                5, 30 );
        String token = "ya29.CjMWA9JJGoU4vHj9OMQHvO7CLShFoODXxYrI3RcTHhWIIIhC-aA8bH0dLW5GLgkWGMMkGyg";
        System.out.println(tokenValidator.isValid(token));
    }
}
