package com.communote.common.util;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for converting color strings to Color objects.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ColorUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColorUtils.class);

    private static void appendHexString(StringBuilder builder, int value) {
        String hex = Integer.toHexString(value);
        if (hex.length() == 1) {
            builder.append("0");
        }
        builder.append(hex);
    }

    private static Color decodeHexColor(String hexString) {
        if (hexString.length() == 6 || hexString.length() == 8) {
            // RGB color
            int r = parseHexColorComponent(hexString, 0);
            int g = parseHexColorComponent(hexString, 2);
            int b = parseHexColorComponent(hexString, 4);
            if (hexString.length() == 8) {
                // RGBA color
                int a = parseHexColorComponent(hexString, 6);
                return new Color(r, g, b, a);
            }
            return new Color(r, g, b);
        } else {
            throw new IllegalArgumentException("Color is not a valid hex string: #" + hexString);
        }
    }

    /**
     * Decode a string with an RGB color into a Color object. If the color cannot be decoded an
     * IllegalArgumentException will be thrown.
     *
     * @param colorString
     *            the string to decode. Supported formats are hex and integer notations. The hex
     *            notation has to have the format <code>#RRGGBB</code> or <code>#RRGGBBAA</code>
     *            with an alpha channel. The integer notation has to have the format
     *            <code>R,G,B</code> or <code>R,G,B,A</code> with an alpha channel and values of the
     *            components have to be in the range 0-255.
     * @return the color
     */
    public static Color decodeRGB(String colorString) {
        if (StringUtils.isBlank(colorString)) {
            throw new IllegalArgumentException("Color not valid: " + colorString);
        }
        colorString = colorString.trim();
        if (colorString.startsWith("#")) {
            String hexString = colorString.substring(1);
            return decodeHexColor(hexString);
        } else {
            if (colorString.endsWith(",")) {
                throw new IllegalArgumentException("Color is not valid: " + colorString);
            }
            String[] rgbComponents = colorString.split(",");
            if (rgbComponents.length == 3 || rgbComponents.length == 4) {
                int r = parseIntColorComponent(rgbComponents[0]);
                int g = parseIntColorComponent(rgbComponents[1]);
                int b = parseIntColorComponent(rgbComponents[2]);
                if (rgbComponents.length == 4) {
                    int a = parseIntColorComponent(rgbComponents[3]);
                    return new Color(r, g, b, a);
                }
                return new Color(r, g, b);
            } else {
                throw new IllegalArgumentException("Color not valid: " + colorString);
            }
        }
    }

    /**
     * Like {@link #decodeRGB(String)} but does not throw an exception if the color cannot be
     * parsed.
     *
     * @param colorString
     *            the string to decode
     * @return the color or null if the color cannot be parsed
     */
    public static Color decodeRGBSilently(String colorString) {
        try {
            return decodeRGB(colorString);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Decoding the color string '{}' failed.", colorString, e);
        }
        return null;
    }

    /**
     * Create a string representation of the color in the format <code>#RRGGBB</code> or
     * <code>#RRGGBBAA</code> if withAlpha is true. The RR is the 2 byte hex value of the red
     * component of the color.
     *
     * @param color
     *            the color to encode
     * @param withAlpha
     *            whether to include the alpha value in the result
     * @return the string or null if color was null
     */
    public static String encodeRGBHexString(Color color, boolean withAlpha) {
        if (color == null) {
            return null;
        }
        StringBuilder colorString = new StringBuilder(withAlpha ? 9 : 7);
        colorString.append("#");
        appendHexString(colorString, color.getRed());
        appendHexString(colorString, color.getGreen());
        appendHexString(colorString, color.getBlue());
        if (withAlpha) {
            appendHexString(colorString, color.getAlpha());
        }
        return colorString.toString();
    }

    /**
     * Create a string of the integer values of the RGB components of the color. The components are
     * separated by comma.
     *
     * @param color
     *            the color to encode
     * @param withAlpha
     *            whether the alpha value should be appended
     * @return the string or null if color was null
     */
    public static String encodeRGBIntString(Color color, boolean withAlpha) {
        if (color == null) {
            return null;
        }
        StringBuilder colorString = new StringBuilder();
        colorString.append(color.getRed()).append(",").append(color.getGreen()).append(",")
        .append(color.getBlue());
        if (withAlpha) {
            colorString.append(",").append(color.getAlpha());
        }
        return colorString.toString();
    }

    private static int parseHexColorComponent(String hexString, int offset) {
        try {
            return Integer.parseInt(hexString.substring(offset, offset + 2), 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Color is not a valid hex string: #" + hexString);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Color is not a valid hex string: #" + hexString, e);
        }
    }

    private static int parseIntColorComponent(String intString) {
        try {
            return Integer.parseInt(intString.trim(), 10);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Color component is not a valid integer: "
                    + intString);
        }
    }

    private ColorUtils() {
    }
}
