package com.communote.common.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UriUtilsTest {

    /**
     * Tests for {@link UriUtils#encodeUriComponent(String)}
     */
    @Test
    public void testEncodeUriComponent() {
        Assert.assertNull(UriUtils.encodeUriComponent(null));
        Assert.assertEquals(UriUtils.encodeUriComponent(""), "");
        // unreserved
        Assert.assertEquals(UriUtils.encodeUriComponent("abcDe'fZz*_1(2)-0.~9"),
                "abcDe'fZz*_1(2)-0.~9");
        // space
        Assert.assertEquals(UriUtils.encodeUriComponent("Hello world!"), "Hello%20world!");
        // slash, plus and percent
        Assert.assertEquals(UriUtils.encodeUriComponent("a/b+c30%"), "a%2Fb%2Bc30%25");
        // UTF-8 character
        Assert.assertEquals(UriUtils.encodeUriComponent("euro \u20AC"), "euro%20%E2%82%AC");
        Assert.assertEquals(UriUtils.encodeUriComponent("katakana ka \u30AB"),
                "katakana%20ka%20%E3%82%AB");
        // UTF-16 supplementary character
        Assert.assertEquals(UriUtils.encodeUriComponent("emoji smile open mouth \uD83D\uDE04"),
                "emoji%20smile%20open%20mouth%20%F0%9F%98%84");
    }
}
