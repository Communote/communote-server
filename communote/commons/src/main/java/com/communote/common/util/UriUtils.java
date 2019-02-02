package com.communote.common.util;

import java.io.CharArrayWriter;
import java.nio.charset.StandardCharsets;

/**
 * Utilities for working with URIs.
 *
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 * 
 * @since 3.5
 */
public class UriUtils {

    private static final boolean[] UNRESERVED = new boolean[127];
    static {
        int i;
        for (i = '0'; i <= '9'; i++) {
            UNRESERVED[i] = true;
        }
        for (i = 'A'; i <= 'Z'; i++) {
            UNRESERVED[i] = true;
        }
        for (i = 'a'; i <= 'z'; i++) {
            UNRESERVED[i] = true;
        }
        // ' ( ) *
        for (i = 39; i < 43; i++) {
            UNRESERVED[i] = true;
        }
        UNRESERVED['!'] = true;
        UNRESERVED['-'] = true;
        UNRESERVED['.'] = true;
        UNRESERVED['_'] = true;
        UNRESERVED['~'] = true;
    }

    /**
     * Encode characters of the buffer with percent encoding by converting them to UTF-8, turning
     * the UTF-8 value into a hex-string and prepending this with the % character.
     *
     * @param charsToEncode
     *            the buffer with the characters to encode. After encoding all characters the writer
     *            is reset.
     * @param target
     *            target to append the encoded characters to
     */
    private static void appendPercentEncoded(CharArrayWriter charsToEncode, StringBuilder target) {
        charsToEncode.flush();
        String toEncode = new String(charsToEncode.toCharArray());
        byte[] utf8Encoded = toEncode.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < utf8Encoded.length; i++) {
            target.append('%');
            byte utf8Byte = utf8Encoded[i];
            char hexPart = Character.forDigit(utf8Byte >> 4 & 0xF, 16);
            // if letter, convert to upper-case by subtracting the ASCII diff
            if (Character.isLetter(hexPart)) {
                hexPart -= 32;
            }
            target.append(hexPart);
            hexPart = Character.forDigit(utf8Byte & 0xF, 16);
            if (Character.isLetter(hexPart)) {
                hexPart -= 32;
            }
            target.append(hexPart);
        }
        charsToEncode.reset();
    }

    /**
     * <p>
     * Encode a URI component by replacing reserved characters by one, two, three, or four escape
     * sequences representing the UTF-8 encoding of the character. In contrast to RFC 3986, which
     * treats ! ' ( ) * as reserved, the following characters are unreserved: alphabetic, decimal
     * digits, - _ . ! ~ * ' ( )
     * </p>
     * This method behaves like the JavaScript global function encodeURIComponent, with the
     * exception that a surrogate which is not part of a high-low pair won't throw an exception.
     *
     * @param uriComponent
     *            the URI component to encode
     * @return the encoded component or null if uriComponent was null
     */
    public static String encodeUriComponent(String uriComponent) {
        if (uriComponent == null) {
            return null;
        }
        boolean containedReserved = false;
        StringBuilder encoded = new StringBuilder(uriComponent.length());
        boolean unwrittenReserved = false;
        CharArrayWriter reservedCharsBuffer = new CharArrayWriter();
        for (int i = 0; i < uriComponent.length(); i++) {
            int c = uriComponent.charAt(i);
            if (c < 127 && UNRESERVED[c]) {
                if (unwrittenReserved) {
                    appendPercentEncoded(reservedCharsBuffer, encoded);
                    unwrittenReserved = false;
                }
                encoded.append((char) c);
            } else {
                containedReserved = true;
                unwrittenReserved = true;
                reservedCharsBuffer.write(c);
                // check if character is a supplementary character. In that case UTF-16
                // uses 2 chars, a high and a low surrogate
                if (Character.isHighSurrogate((char) c)) {
                    if (i + 1 < uriComponent.length()) {
                        char lowSurrogate = uriComponent.charAt(i + 1);
                        // don't know how to handle a case where a high surrogate is not followed by
                        // a low surrogate, so just treat it as a char from BMP
                        if (Character.isLowSurrogate(lowSurrogate)) {
                            reservedCharsBuffer.append(lowSurrogate);
                            i++;
                        }
                    }
                }
            }
        }
        if (unwrittenReserved) {
            appendPercentEncoded(reservedCharsBuffer, encoded);
        }
        if (containedReserved) {
            uriComponent = encoded.toString();
        }
        return uriComponent;
    }

    private UriUtils() {
    }
}
