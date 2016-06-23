package com.communote.common.encryption;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.util.Base64Utils;

/**
 * Tests for {@link EncryptionUtils}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class EncryptionUtilsTest {

    /**
     * Tests {@link EncryptionUtils}.
     * 
     * @throws EncryptionException
     *             Exception.
     */
    @Test
    public void testEncryptionUtils() throws EncryptionException {
        String clearText = RandomStringUtils.randomAlphanumeric(new Random().nextInt(10000) + 5)
                + UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        String encrypted = EncryptionUtils.encrypt(clearText, password);
        String decrypted = EncryptionUtils.decrypt(encrypted, password);

        Assert.assertEquals(decrypted, clearText);

        String salt = EncryptionUtils.getSalt(encrypted);
        String encryptedWithSalt = EncryptionUtils.encrypt(clearText, password, Base64Utils
                .decode(salt));

        Assert.assertEquals(encrypted, encryptedWithSalt);

        String decryptedWithSalt = EncryptionUtils.decrypt(encryptedWithSalt, password);
        Assert.assertEquals(decrypted, decryptedWithSalt);

        String wrongPassword = UUID.randomUUID().toString();
        String result = null;
        try {
            result = EncryptionUtils.decrypt(encryptedWithSalt, wrongPassword);
        } catch (EncryptionException e) {
            return;
        }
        if (result != null && result.equals(clearText)) {
            Assert.fail("Could decrypt a text (" + clearText
                    + ") that was encrypted with password '"
                    + password + "' with password '" + wrongPassword + "'");
        }

    }
}
