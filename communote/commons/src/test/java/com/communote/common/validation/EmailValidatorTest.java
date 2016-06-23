package com.communote.common.validation;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.validation.EmailValidator;

/**
 * This tests the {@link EmailValidator}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class EmailValidatorTest {

    private final static String[] VALID_ADDRESSES = new String[] { "adrian_@mydomain.de",
            "test@test.de", "a@b.de", "FredBloggs@example.com", "$A12345@example.com"
            , "!def!xyz%abc@example.com", "_somename@example.com", "_home@rest.scrobble.me" };
    private final static String[] INVALID_ADDRESSES = new String[] {
            ".notvalid@server.de",
            "nothing",
            "test.@rsdf.de",
            "too_long_too_long_too_long_too_long_too_long_too_long_too_long_ab@blubbla.de",
            "a@tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_"
                    + "tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_"
                    + "long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_long_tooo_long" };

    /**
     * Tests the valid addresses.
     */
    @Test
    public void testInvalidAddresses() {
        for (String address : INVALID_ADDRESSES) {
            if (EmailValidator.validateEmailAddressByRegex(address)) {
                Assert.fail("Address should be invalid: " + address);
            }
        }
    }

    /**
     * Tests the valid addresses.
     */
    @Test
    public void testValidAddresses() {
        for (String address : VALID_ADDRESSES) {
            if (!EmailValidator.validateEmailAddressByRegex(address)) {
                Assert.fail("Address should be valid: " + address);
            }
        }
    }
}
