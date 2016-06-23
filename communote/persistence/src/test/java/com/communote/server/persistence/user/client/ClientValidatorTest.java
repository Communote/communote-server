package com.communote.server.persistence.user.client;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Testing the user management functionality
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientValidatorTest {

    /**
     * @param id
     *            The clients id.
     * @param isValid
     *            True, if this is a valid client id.
     */
    private void checkClientId(String id, boolean isValid) {
        if (isValid) {
            Assert.assertTrue(ClientValidator.validateClientId(id), id + " must validate to true");
        } else {
            Assert.assertFalse(ClientValidator.validateClientId(id), id
                    + " must validate to false");
        }
    }

    /**
     * Validate the client id
     */
    @Test
    public void testValidateClientId() {
        checkClientId("abc", true);
        checkClientId("abc787", true);
        checkClientId("ABc3783", false);
        checkClientId("abc_3333____", true);
        checkClientId("_abc_3333____", true);
        checkClientId("_abc_3333_", true);
        checkClientId("22_12d", true);
        checkClientId("dd", true);

        checkClientId("_ _", false);
        checkClientId("f S", false);
        checkClientId("343.fsdfsdf", false);
        checkClientId("343&f", false);
        checkClientId("juche,", false);

    }
}
