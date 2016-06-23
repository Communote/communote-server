package com.communote.common.util;

import org.apache.commons.codec.binary.Base64;

/**
 * Utility class to provide same Base64 decoding and encoding mechanism.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class Base64Utils {

    /**
     * 
     * @param bytes
     *            encoded string.
     * @return decoded byte[].
     */
    public static byte[] decode(byte[] bytes) {
        return Base64.decodeBase64(bytes);
    }

    /**
     * 
     * @param string
     *            encoded string.
     * @return decoded byte[].
     */
    public static byte[] decode(String string) {
        return Base64.decodeBase64(string);
    }

    /**
     * 
     * @param bytes
     *            decoded bytes.
     * @return encoded String.
     */
    public static String encode(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes, false, true));
    }

    /**
     * 
     * @param bytes
     *            decoded bytes.
     * @return encoded String.
     */
    public static byte[] encodeToBytes(byte[] bytes) {
        return Base64.encodeBase64(bytes, false, true);
    }

    /**
     * Utility class constructor.
     */
    private Base64Utils() {
        // Do nothing.
    }
}
