package com.communote.common.util;

import java.awt.Color;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ColorUtilsTest {

    private void assertDecodingFailes(String colorString) {
        try {
            ColorUtils.decodeRGB(colorString);
        } catch (IllegalArgumentException e) {
            // expected
            return;
        }
        Assert.fail("The string " + colorString + " should not be decodable");
    }

    private void assertValidRGB(Color color, int r, int g, int b) {
        assertValidRGBA(color, r, g, b, 255);
    }

    private void assertValidRGBA(Color color, int r, int g, int b, int a) {
        Assert.assertEquals(color.getRed(), r, "Red has not the expected value:");
        Assert.assertEquals(color.getGreen(), g, "Green has not the expected value:");
        Assert.assertEquals(color.getBlue(), b, "Blue has not the expected value:");
        Assert.assertEquals(color.getAlpha(), a, "Alpha has not the expected value:");
    }

    @Test
    public void testDecodingFailure() {
        assertDecodingFailes("");
        assertDecodingFailes(null);
        assertDecodingFailes("    ");
        assertDecodingFailes("Hello World");
        // test silent decoding
        Assert.assertNull(ColorUtils.decodeRGBSilently(""));
        Assert.assertNull(ColorUtils.decodeRGBSilently(null));
        Assert.assertNull(ColorUtils.decodeRGBSilently("       "));
        Assert.assertNull(ColorUtils.decodeRGBSilently("Hello World"));
    }

    @Test
    public void testEncoding() {
        Assert.assertEquals(ColorUtils.encodeRGBHexString(new Color(0, 255, 250), false), "#00fffa");
        Assert.assertEquals(ColorUtils.encodeRGBHexString(new Color(0, 15, 250), false), "#000ffa");
        Assert.assertEquals(ColorUtils.encodeRGBHexString(new Color(0, 255, 250), true),
                "#00fffaff");
        Assert.assertEquals(ColorUtils.encodeRGBHexString(new Color(0, 255, 250, 3), true),
                "#00fffa03");
        Assert.assertEquals(ColorUtils.encodeRGBHexString(new Color(0, 255, 250, 32), true),
                "#00fffa20");

        Assert.assertEquals(ColorUtils.encodeRGBIntString(new Color(0, 255, 250), false),
                "0,255,250");
        Assert.assertEquals(ColorUtils.encodeRGBIntString(new Color(0, 15, 250), false), "0,15,250");
        Assert.assertEquals(ColorUtils.encodeRGBIntString(new Color(0, 255, 250), true),
                "0,255,250,255");
        Assert.assertEquals(ColorUtils.encodeRGBIntString(new Color(0, 255, 250, 3), true),
                "0,255,250,3");
        Assert.assertEquals(ColorUtils.encodeRGBIntString(new Color(0, 255, 250, 32), true),
                "0,255,250,32");
    }

    @Test
    public void testHexStringDecoding() {
        Color color = ColorUtils.decodeRGB("#000000");
        assertValidRGB(color, 0, 0, 0);
        color = ColorUtils.decodeRGB("#FFf500");
        assertValidRGB(color, 255, 245, 0);
        color = ColorUtils.decodeRGB("#aaAdcc");
        assertValidRGB(color, 170, 173, 204);
        color = ColorUtils.decodeRGB("  #aaAdcc       ");
        assertValidRGB(color, 170, 173, 204);
        color = ColorUtils.decodeRGB("#FFf50022");
        assertValidRGBA(color, 255, 245, 0, 34);
        color = ColorUtils.decodeRGB("#FFf500F2");
        assertValidRGBA(color, 255, 245, 0, 242);
        color = ColorUtils.decodeRGB("#FFf500dE");
        assertValidRGBA(color, 255, 245, 0, 222);
        color = ColorUtils.decodeRGB("  #FFf500dE   ");
        assertValidRGBA(color, 255, 245, 0, 222);
        // test silently decoding invalid
        Assert.assertNull(ColorUtils.decodeRGBSilently("#000000;"));
        Assert.assertNull(ColorUtils.decodeRGBSilently("#FFEE0"));
        Assert.assertNull(ColorUtils.decodeRGBSilently("#FG00ef"));
        Assert.assertNull(ColorUtils.decodeRGBSilently("#EEffCCFF0"));
    }

    @Test
    public void testHexStringDecodingFailure() {
        assertDecodingFailes("#000000;");
        assertDecodingFailes("#FFEE0");
        assertDecodingFailes("#FE0");
        assertDecodingFailes("#FG00ef");
        assertDecodingFailes("#EEffCC0");
        assertDecodingFailes("#EEffCCFF0");
        assertDecodingFailes("EEffCC");
    }

    @Test
    public void testIntStringDecoding() {
        Color color = ColorUtils.decodeRGB("0,0,0");
        assertValidRGB(color, 0, 0, 0);
        color = ColorUtils.decodeRGB("255,245,0");
        assertValidRGB(color, 255, 245, 0);
        color = ColorUtils.decodeRGB("170, 173, 204");
        assertValidRGB(color, 170, 173, 204);
        color = ColorUtils.decodeRGB("  170  ,  173,   204       ");
        assertValidRGB(color, 170, 173, 204);
        color = ColorUtils.decodeRGB("255,245,0,34");
        assertValidRGBA(color, 255, 245, 0, 34);
        color = ColorUtils.decodeRGB("255, 245,  0  , 242  ");
        assertValidRGBA(color, 255, 245, 0, 242);
        // test silently decoding invalid
        Assert.assertNull(ColorUtils.decodeRGBSilently("256, 3000, 400"));
        Assert.assertNull(ColorUtils.decodeRGBSilently("255, -1, 0"));
        Assert.assertNull(ColorUtils.decodeRGBSilently("200.5, 23, 0"));
    }

    @Test
    public void testIntStringDecodingFailure() {
        assertDecodingFailes("256,3000,400");
        assertDecodingFailes("-1,255,0");
        assertDecodingFailes("255,-1,0");
        assertDecodingFailes("255,0,-1");
        assertDecodingFailes("200.5,23,0");
        assertDecodingFailes("200,23,0,-1");
        assertDecodingFailes("200,256,0,1");
        assertDecodingFailes("200,200,256,1");
        assertDecodingFailes("200,200,255,256");
        assertDecodingFailes("200");
        assertDecodingFailes("200,");
        assertDecodingFailes("200,200");
        assertDecodingFailes("200,200,");
        assertDecodingFailes("200,200,200,");
        assertDecodingFailes(",,,");
        assertDecodingFailes(",,,,");
    }

}
