package com.communote.common.encryption;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generator class to provide the functionality to compute hash codes
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class HashCodeGenerator {

    /** The Constant HASH_CODE_ALGORITHM_MD5. */
    private static final String HASH_CODE_ALGORITHM_MD5 = "MD5";

    /** The Constant DEFAULT_HASHCODE_CHARSET. */
    public static final String DEFAULT_HASHCODE_CHARSET = "UTF-8";

    /**
     * Hash the text using MD5 and the default charset.
     * 
     * @param clearText
     *            The plain text to hash
     * @return the hash code
     */
    public static String generateMD5HashCode(String clearText) {
        return generateMD5HashCode(clearText, DEFAULT_HASHCODE_CHARSET);
    }

    /**
     * Hash the text using MD5 and the given charset
     * 
     * @param clearText
     *            The plain text to hash
     * @param charset
     *            The charset of the text
     * @return The hash code
     */
    public static String generateMD5HashCode(String clearText, String charset) {
        String dstr = null;
        byte[] digest;
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_CODE_ALGORITHM_MD5);
            md.update(clearText.getBytes(charset));
            digest = md.digest();
            dstr = new BigInteger(1, digest).toString(16);

            /* this is important, toString leaves out initial 0 */
            if (dstr.length() % 2 > 0) {
                dstr = "0" + dstr;
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Error hashing text! NoSuchAlgorithmException: " + ex,
                    ex);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error hashing text!", e);
        }

        return dstr;
    }

    /**
     * Private constructor for this helper classs
     */
    private HashCodeGenerator() {
    }

}
