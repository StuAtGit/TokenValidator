package com.shareplaylearn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Logger log = LoggerFactory.getLogger(TokenValidatorTest.class);
        log.info("Starting up e2e test...");
        TokenValidator tokenValidator = new TokenValidator(
                "https://www.shareplaylearn.com/auth_api/oauthToken_validation",
                5, 30 );

        String token = "";
        System.out.println(tokenValidator.isValid(token));
        System.out.println(tokenValidator.isValid(token));
    }
}
