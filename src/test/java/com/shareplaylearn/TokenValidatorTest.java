package com.shareplaylearn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

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

        /**
         * Plug in a valid user token & user id for gmail
         * (you can use the developer console to log into the site, and look at the request headers
         * & url).
         * Don't check values in...!
         */
        String token = "ya29.CjGPA4A-FmHhNSqEcaHi-gFME4zwRk-TQKf5dDuobmBFvVVO6utjvIskofZeHfi1J4Yn";
        String gmailId = "114145198865195405983";

        assertTrue(tokenValidator.isValid(token, gmailId));
        //this should be from valid cache
        assertTrue(tokenValidator.isValid(token, gmailId));
        //this will also be from valid token cache, but will not be valid (wrong user)
        assertFalse(tokenValidator.isValid(token, "ABC"));
        assertFalse(tokenValidator.isValid("ABC", gmailId));
        //this will be from teh invalid cache.
        assertFalse(tokenValidator.isValid("ABC", gmailId));
    }
}
