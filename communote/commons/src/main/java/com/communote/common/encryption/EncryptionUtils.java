package com.communote.common.encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

import com.communote.common.util.Base64Utils;


/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class EncryptionUtils {

    // use 128 bit, 192 and 256 bits may not be available
    private static final int KEY_LENGTH = 128;

    private static final int PBE_ITERATION_COUNT = 1024;
    private static final String SEPARATOR = ":";
    private static final int SALT_DEFAULT_MIN_LENGTH = 6;
    private static final int SALT_DEFAULT_MAX_LENGTH = 20;

    /**
     * Decrypts text and returns the readable clear text. If the encrypted input is blank then it is
     * returned without processing.
     * 
     * @param encryptedText
     *            the encrypted string (required)
     * @param password
     *            the password (required)
     * @return the decrypted version of text
     * @throws EncryptionException
     *             in the event of an encryption failure
     * 
     */
    public static String decrypt(String encryptedText, String password) throws EncryptionException {
        if (StringUtils.isBlank(encryptedText)) {
            return encryptedText;
        }
        byte[] recoveredBytes = null;
        String salt = getSalt(encryptedText);
        byte[] saltDecoded = Base64Utils.decode(salt);
        try {
            SecretKey aesKey = getSecretKey(password, saltDecoded);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] encryptedTextAsByte = Base64Utils.decode(StringUtils.substringBeforeLast(
                    encryptedText, SEPARATOR));
            recoveredBytes = cipher.doFinal(encryptedTextAsByte);
        } catch (InvalidKeyException e) {
            throw new EncryptionException("Invalid key for encrypt password", e);
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("The encryption algorithm was not found", e);
        } catch (NoSuchPaddingException e) {
            throw new EncryptionException("Padding exception", e);
        } catch (IllegalBlockSizeException e) {
            throw new EncryptionException("Block size exception", e);
        } catch (BadPaddingException e) {
            throw new EncryptionException("Bad padding exception", e);
        } catch (IllegalArgumentException e) {
            throw new EncryptionException("Illegal argument exception", e);
        } catch (InvalidKeySpecException e) {
            throw new EncryptionException("Invalid key spec exception", e);
        }

        return new String(recoveredBytes);
    }

    /**
     * Encrypt the clear text and adds a random salt. If the input text is blank then it is returned
     * without processing.
     * 
     * @param clearText
     *            the string to encrypt (required)
     * @param password
     *            the password (required)
     * @return the encrypted version of the text
     * @throws EncryptionException
     *             in the event of an encryption failure
     */
    public static String encrypt(String clearText, String password) throws EncryptionException {
        return encrypt(clearText, password, generateSalt());
    }

    /**
     * Encrypt the clear text with a defined salt. If the input text is blank then it is returned
     * without processing.
     * 
     * @param clearText
     *            the string to encrypt (required)
     * @param salt
     *            a predefined salt added to the clear text (required)
     * @param password
     *            the password (required)
     * @return the encrypted version of the text
     * @throws EncryptionException
     *             in the event of an encryption failure
     */
    public static String encrypt(String clearText, String password, byte[] salt)
            throws EncryptionException {
        if (StringUtils.isBlank(clearText)) {
            return clearText;
        }

        byte[] encryptedBytes = null;

        if (salt == null) {
            salt = generateSalt();
        }

        try {
            SecretKey aesKey = getSecretKey(password, salt);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            byte[] inputAsByte = clearText.getBytes();
            encryptedBytes = cipher.doFinal(inputAsByte);
        } catch (InvalidKeyException e) {
            throw new EncryptionException("Invalid key for encrypt password", e);
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("The encryption algorithm was not found", e);
        } catch (NoSuchPaddingException e) {
            throw new EncryptionException("Padding exception", e);
        } catch (IllegalBlockSizeException e) {
            throw new EncryptionException("Block size exception", e);
        } catch (BadPaddingException e) {
            throw new EncryptionException("Bad padding exception", e);
        } catch (IllegalArgumentException e) {
            throw new EncryptionException("Illegal argument exception", e);
        } catch (InvalidKeySpecException e) {
            throw new EncryptionException("Invalid key spec exception", e);
        }

        return Base64Utils.encode(encryptedBytes) + SEPARATOR + Base64Utils.encode(salt);
    }

    /**
     * Generates random bits that are used to encrypt a text. The lower and upper bound of the
     * length of the salt is set to default.
     * 
     * @return a secure and random salt
     */
    public static byte[] generateSalt() {
        return generateSalt(SALT_DEFAULT_MIN_LENGTH, SALT_DEFAULT_MAX_LENGTH);
    }

    /**
     * Generates random bits that are used to encrypt a text. The lower and upper bound of the
     * length of the salt can set.
     * 
     * @param minLength
     *            the min length for the salt
     * @param maxLength
     *            the max length for the salt
     * @return a secure and random salt
     */
    public static byte[] generateSalt(int minLength, int maxLength) {
        int range = maxLength - minLength;

        SecureRandom r = new SecureRandom();
        int saltSize = r.nextInt(range) + minLength;
        byte[] salt = new byte[saltSize];
        r.nextBytes(salt);

        return salt;
    }

    /**
     * returns the stored salt (encoded) of a encrypted value
     * 
     * @param encryptedText
     *            the encrypted text
     * @return the salt of the encryption
     */
    public static String getSalt(String encryptedText) {
        return StringUtils.substringAfterLast(encryptedText, SEPARATOR);
    }

    /**
     * @param salt
     *            the salt to generate the secret key
     * @param password
     *            The password.
     * @return the "secret" key
     * @throws NoSuchAlgorithmException
     *             in case of an error
     * 
     * @throws InvalidKeySpecException
     *             in case of an error
     */
    private static SecretKey getSecretKey(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        int rounds = PBE_ITERATION_COUNT;
        int keyLength = KEY_LENGTH;
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, rounds, keyLength);
        SecretKey key = factory.generateSecret(spec);
        SecretKey secretKey = new SecretKeySpec(key.getEncoded(), "AES");
        return secretKey;
    }

    /**
     * Instantiates a new Encryption.
     */
    private EncryptionUtils() {
        // Do nothing.
    }
}