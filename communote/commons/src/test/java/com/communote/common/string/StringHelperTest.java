package com.communote.common.string;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link StringHelper}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StringHelperTest {

    private static final String REPLACEMENT_1 = "[..]";
    private static final String REPLACEMENT_2 = "...";
    private static final String REPLACEMENT_3 = " [..] ";

    @Test
    public void testStringAsLongList() {
        List<Long> longs = StringHelper.getStringAsLongList("1,2,3,4,5,6,7,8");
        Assert.assertEquals(longs.size(), 8);
        for (long i = 1; i <= 8; i++) {
            Assert.assertTrue(longs.contains(i));
        }

        longs = StringHelper.getStringAsLongList("1,2,3,-4,5,6,-7,8,0", true);
        Assert.assertEquals(longs.size(), 6);
        for (long i = 0; i <= 8; i++) {
            if (i == 0 || i == 4 || i == 7) {
                Assert.assertFalse(longs.contains(i));
                Assert.assertFalse(longs.contains(-1 * i));
            } else {
                Assert.assertTrue(longs.contains(i));
            }
        }

    }

    /**
     * Test for {@link StringHelper#truncateMiddle(String, int, String)}
     */
    @Test
    public void testTruncateMiddle() {
        // string to abbreviate with 56 characters
        String testString00 = "In Deutschland gibt es rund 120.000 Verm\u00F6gensmillion\u00E4re.";

        // string to abbreviate with 132 characters
        String testString01 = "Dies ist ein Blindtext. Blindtexte sind zumeist "
                + "weder informativ noch interessant, sondern ausgesprochen "
                + "langweilig. So auch dieser.";

        // string to abbreviate with 200 characters
        String testString02 = "Ich bin nur ein kleiner Blindtext. Wenn ich gross "
                + "bin, will ich Ulysses von James Joyce werden. Aber jetzt lohnt es "
                + "sich noch nicht, mich weiterzulesen. Denn vorerst bin ich nur ein "
                + "kleiner Blindtext.";

        // result should be the unmodified string
        String result00 = StringHelper.truncateMiddle(testString00, 56, null);
        Assert.assertEquals(testString00, result00);
        Assert.assertFalse(StringUtils.contains(result00, REPLACEMENT_2));

        // result should be the abbreviated to an length of 20 char (an even number of chars)
        String result01 = StringHelper.truncateMiddle(testString00, 20, null);
        Assert.assertEquals(result01.length(), 20);
        Assert.assertTrue(StringUtils.contains(result01, REPLACEMENT_2));

        // result should be the abbreviated to an length of 15 chars (odd number of chars)
        String result02 = StringHelper.truncateMiddle(testString00, 15, null);
        Assert.assertEquals(result02.length(), 15);
        Assert.assertTrue(StringUtils.contains(result02, REPLACEMENT_2));

        // using an custom replacement
        String result03 = StringHelper.truncateMiddle(testString01, 45, REPLACEMENT_1);
        Assert.assertEquals(result03.length(), 45);
        Assert.assertTrue(StringUtils.contains(result03, REPLACEMENT_1));

        // using an custom replacement and a long test string
        String result04 = StringHelper.truncateMiddle(testString02, 156, REPLACEMENT_3);
        Assert.assertEquals(result04.length(), 156);
        Assert.assertTrue(StringUtils.contains(result04, REPLACEMENT_3));
    }
}
