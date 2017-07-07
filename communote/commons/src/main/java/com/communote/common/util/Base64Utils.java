package com.communote.common.util;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;

/**
 * Utility class to provide same Base64 decoding and encoding mechanism.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class Base64Utils {

    /**
     * Decodes base64 encoded bytes. The encoding can be in URL-safe or the standard form. Padding
     * is supported but not required.
     *
     * @param bytes
     *            encoded bytes
     * @return decoded bytes
     */
    public static byte[] decode(byte[] bytes) {
        return Base64.decodeBase64(bytes);
    }

    /**
     * Decodes a base64 encoded string. The encoding can be in URL-safe or the standard form.
     * Padding is supported but not required.
     *
     * @param string
     *            encoded string
     * @return decoded bytes
     */
    public static byte[] decode(String string) {
        return Base64.decodeBase64(string);
    }

    /**
     * Base64 encodes the bytes with the URL-safe table. Padding is not applied.
     *
     * @param bytes
     *            bytes to encode
     * @return encoded String
     */
    public static String encode(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes, false, true), StandardCharsets.ISO_8859_1);
    }

    /**
     * Base64 encodes the bytes with the URL-safe table. Padding is not applied.
     *
     * @param bytes
     *            bytes to encode
     * @return encoded bytes
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
