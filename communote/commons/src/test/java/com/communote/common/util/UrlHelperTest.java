package com.communote.common.util;

import java.util.regex.Matcher;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the UrlHelper
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UrlHelperTest {

    /**
     * Tests that the URL is matched correctly.
     *
     * @param content
     *            the content that contains the URL
     * @param expectedProtocolPrefix
     *            expected protocol prefix including www if present (e.g. http://www.)
     * @param expectedHostPart
     *            expect host part including port if necessary
     * @param expectedPathPart
     *            expected path part. Can be null.
     * @param expectedPathParameter
     *            expected path parameter including ; character. Can be null.
     * @param expectedQueryPart
     *            expected query part including ? character. Can be null.
     * @param expectedFragmentIdentifer
     *            expected fragment identifier including # character. Can be null.
     */
    private void assertUrlPatternDoesMatch(String content, String expectedProtocolPrefix,
            String expectedHostPart, String expectedPathPart, String expectedPathParameter,
            String expectedQueryPart, String expectedFragmentIdentifer) {
        Matcher matcher = UrlHelper.URL_PATTERN.matcher(content);
        Assert.assertTrue(matcher.find());
        String group1 = matcher.group(1);
        String group2 = matcher.group(2);
        String group3 = matcher.group(3);
        String group4 = matcher.group(4);
        String group5 = matcher.group(5);
        String group6 = matcher.group(6);
        Assert.assertEquals(expectedProtocolPrefix, group1);
        Assert.assertEquals(expectedHostPart, group2);
        if (expectedPathPart != null) {
            Assert.assertEquals(group3, expectedPathPart);
        } else {
            Assert.assertNull(group3);
        }
        if (expectedPathParameter != null) {
            Assert.assertEquals(group4, expectedPathParameter);
        } else {
            Assert.assertNull(group4);
        }
        if (expectedQueryPart != null) {
            Assert.assertEquals(group5, expectedQueryPart);
        } else {
            Assert.assertNull(group5);
        }
        if (expectedFragmentIdentifer != null) {
            Assert.assertEquals(group6, expectedFragmentIdentifer);
        } else {
            Assert.assertNull(group6);
        }
    }

    /**
     * Asserts that the content does not contain a matchable URL.
     *
     * @param content
     *            the content to check
     * @param fullmatch
     *            whether to check the content for a full match (complete content represents a URL)
     *            or only containment of an URL
     */
    private void assertUrlPatternDoesNotMatch(String content, boolean fullmatch) {
        Matcher matcher = UrlHelper.URL_PATTERN.matcher(content);
        if (fullmatch) {
            Assert.assertFalse(matcher.matches());
        } else {
            Assert.assertFalse(matcher.find());
        }
    }

    /**
     * Tests that the shortened URLs are correctly converted into text.
     */
    @Test
    public void testAnchorToTextConversion() {
        String content = "<a href=\"http://www.test.de\">blah blah</a> inhalt";
        String convertedContent = UrlHelper.convertAnchorsToString(content);
        Assert.assertEquals("blah blah (http://www.test.de) inhalt", convertedContent);
        content = "<a href=\"http://www.test.de\">http://www.test.de...</a> inhalt";
        convertedContent = UrlHelper.convertAnchorsToString(content);
        Assert.assertEquals("http://www.test.de inhalt", convertedContent);
        content = "<a href=\"http://www.test.de\">...</a> inhalt";
        convertedContent = UrlHelper.convertAnchorsToString(content);
        Assert.assertEquals("... (http://www.test.de) inhalt", convertedContent);
        content = "<a href=\"http://www.test.de\">www.test.de inner stuff</a> inhalt";
        convertedContent = UrlHelper.convertAnchorsToString(content);
        Assert.assertEquals("http://www.test.de inner stuff inhalt", convertedContent);
        content = "<a href=\"http://www.test.de\">blah blah www.test.de inner stuff</a> inhalt";
        convertedContent = UrlHelper.convertAnchorsToString(content);
        Assert.assertEquals("blah blah http://www.test.de inner stuff inhalt", convertedContent);
        content = "<a href=\"http://www.test.de\">http://www.test2.de... inner stuff</a> inhalt";
        convertedContent = UrlHelper.convertAnchorsToString(content);
        Assert.assertEquals("http://www.test2.de... inner stuff (http://www.test.de) inhalt",
                convertedContent);
    }

    /**
     * Test {@link UrlHelper#getProtocolHostPort(String, boolean)}
     */
    @Test
    public void testGetProtocolHostPort() {
        String url = "http://test.de/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test.de");
        url = "http://test.de:400/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test.de:400");
        url = "http://test.de/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, true), "http://test.de");
        url = "http://test.de:400/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, true), "http://test.de:400");
        url = "http://test/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test");
        url = "http://test:400/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test:400");
        url = "https://test.de/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "https://test.de");
        url = "https://test.de:400/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "https://test.de:400");
        url = "http://test.de";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test.de");
        url = "http://test.de:400";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test.de:400");
        url = "http://test.de/";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test.de");
        url = "http://test.de:400/";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test.de:400");
        url = "http://user:pwd@test.de/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://user:pwd@test.de");
        url = "http://user:pwd@test.de:400/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false),
                "http://user:pwd@test.de:400");
        url = "http://user:pwd@test.de/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, true), "http://test.de");
        url = "http://user:pwd@test.de:400/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, true), "http://test.de:400");
        url = "ftp://test.de/";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "ftp://test.de");
        url = "http://test/blah?query=true#id";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test");
        url = "http://test:400/blah?query=true#id";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test:400");
        url = "test.de/blah";
        Assert.assertNull(UrlHelper.getProtocolHostPort(url, false));
        url = "/blah?query=true#id";
        Assert.assertNull(UrlHelper.getProtocolHostPort(url, false));
        url = "http:";
        Assert.assertNull(UrlHelper.getProtocolHostPort(url, false));
        url = "http://";
        Assert.assertNull(UrlHelper.getProtocolHostPort(url, false));
        url = "http://test.de:80/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test.de:80");
        url = "HTTP://test.de:80/blah";
        Assert.assertEquals(UrlHelper.getProtocolHostPort(url, false), "http://test.de:80");
    }

    /**
     * Test for {@link UrlHelper#isAbsoluteHttpUrl(String)}
     */
    @Test
    public void testIsAbsoluteHttpUrl() {
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("http://test.de"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("http://test.de/de"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("http://test.de/de?q=4#dje"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("http://test.de:8080/de"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("http://test"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("http://user:pwd@test.de/de"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("https://test.de"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("https://test.de/de"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("https://test.de/de?q=4#dje"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("https://test.de:8080/de"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("https://test"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("https://user:pwd@test.de/de"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("HTTP://test.de"));
        Assert.assertTrue(UrlHelper.isAbsoluteHttpUrl("HTTPS://test.de"));
        Assert.assertFalse(UrlHelper.isAbsoluteHttpUrl("ftp://test.de"));
        Assert.assertFalse(UrlHelper.isAbsoluteHttpUrl("http://"));
        Assert.assertFalse(UrlHelper.isAbsoluteHttpUrl("https://"));
        Assert.assertFalse(UrlHelper.isAbsoluteHttpUrl("test.de:80/te"));
        Assert.assertFalse(UrlHelper.isAbsoluteHttpUrl("/blah/?dje"));
        Assert.assertFalse(UrlHelper.isAbsoluteHttpUrl(""));
        Assert.assertFalse(UrlHelper.isAbsoluteHttpUrl(null));
    }

    /**
     * Test for {@link UrlHelper#urlEncodeUrlPath(String)}
     */
    @Test
    public void testUrlEncodeUrlPath() {
        // test that slashes are kept
        String urlPath = "/abc/test/";
        Assert.assertEquals(UrlHelper.urlEncodeUrlPath(urlPath), urlPath);
        urlPath = "/abc/test";
        Assert.assertEquals(UrlHelper.urlEncodeUrlPath(urlPath), urlPath);
        urlPath = "abc/test/";
        Assert.assertEquals(UrlHelper.urlEncodeUrlPath(urlPath), urlPath);
        urlPath = "abc/test";
        Assert.assertEquals(UrlHelper.urlEncodeUrlPath(urlPath), urlPath);
        urlPath = "/abc";
        Assert.assertEquals(UrlHelper.urlEncodeUrlPath(urlPath), urlPath);
        urlPath = "abc/";
        Assert.assertEquals(UrlHelper.urlEncodeUrlPath(urlPath), urlPath);
        urlPath = "abc";
        Assert.assertEquals(UrlHelper.urlEncodeUrlPath(urlPath), urlPath);
        // test encoding
        Assert.assertEquals(UrlHelper.urlEncodeUrlPath("/abc\u00f6_ro/~"), "/abc%C3%B6_ro/%7E");
        Assert.assertEquals(UrlHelper.urlEncodeUrlPath("/abc\u3072\u3069\u3044_ro/~"),
                "/abc%E3%81%B2%E3%81%A9%E3%81%84_ro/%7E");
        // assert that there is no double encoding
        Assert.assertEquals(UrlHelper.urlEncodeUrlPath("/abc%C3%B6_ro/~"), "/abc%C3%B6_ro/%7E");
    }

    /**
     * Tests that the UrlHelper.URL_PATTERN, which is used to find URLs in text, matches correctly
     */
    @Test
    public void testUrlRecognitionNegativeCases() {
        // negative tests
        assertUrlPatternDoesNotMatch("httq://www.test.de/abc/moo.html?q=p", false);
        assertUrlPatternDoesNotMatch("blieh blah", false);
        assertUrlPatternDoesNotMatch("test.de", false);
        assertUrlPatternDoesNotMatch("peter@test.de", false);
        // don't match unicode characters that are not letters (ideographic full stop)
        assertUrlPatternDoesNotMatch("http://www.boo.com/test/abc\u3002bc/", true);
        assertUrlPatternDoesNotMatch("http://www.boo\u3002com/test/abc", true);
        // don't match unicode characters that are not letters (ethiopic question mark)
        assertUrlPatternDoesNotMatch("http://www.boo.com/test/abc\u1367", true);
        // don't match non-ascii letters in query string
        assertUrlPatternDoesNotMatch("http://www.boo.com/test/abc?topic=\u00f6test", true);
        assertUrlPatternDoesNotMatch("http://www.boo.com/test/abc?topic=test\u00f6", true);
        assertUrlPatternDoesNotMatch("http://www.boo.com/test/abc?topic=t\u00f6st", true);
    }

    /**
     * Tests that the UrlHelper.URL_PATTERN, which is used to find URLs in text, matches correctly
     */
    @Test
    public void testUrlRecognitionPositiveCases() {
        String url = "http://www.test.de/abc/moo.html?q=p";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/abc/moo.html", null, "?q=p",
                null);
        url = "https://www.test.de/abc/moo.html?q=p";
        assertUrlPatternDoesMatch(url, "https://www.", "test.de", "/abc/moo.html", null, "?q=p",
                null);
        url = "www.test.de/abc/moo.html?q=p";
        assertUrlPatternDoesMatch(url, "www.", "test.de", "/abc/moo.html", null, "?q=p", null);
        url = "www.communote-12.test.de/abc/moo.html?q=p";
        assertUrlPatternDoesMatch(url, "www.", "communote-12.test.de", "/abc/moo.html", null,
                "?q=p", null);
        url = "http://com30/abc/moo.html?q=p";
        assertUrlPatternDoesMatch(url, "http://", "com30", "/abc/moo.html", null, "?q=p", null);
        url = "http://com30:8080/abc/moo.html?q=p";
        assertUrlPatternDoesMatch(url, "http://", "com30:8080", "/abc/moo.html", null, "?q=p", null);
        url = "ftp://com30/abc/moo.html?q=p";
        assertUrlPatternDoesMatch(url, "ftp://", "com30", "/abc/moo.html", null, "?q=p", null);
        url = "http://pete:jk23_sh;f-$uz@www.test.de/abc/moo.html?q=p";
        assertUrlPatternDoesMatch(url, "http://pete:jk23_sh;f-$uz@www.", "test.de",
                "/abc/moo.html", null, "?q=p", null);
        url = " http://com30/abc/moo.html? more text";
        assertUrlPatternDoesMatch(url, "http://", "com30", "/abc/moo.html", null, null, null);
        url = "http://test.de/~abc/lo_op(ing/mo)o.html/wgatev*er)";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/~abc/lo_op(ing/mo)o.html/wgatev*er",
                null, null, null);
        url = "blah http://test.de/~abc/lo_op(ing/mo)o.html/wgatev*er. text";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/~abc/lo_op(ing/mo)o.html/wgatev*er",
                null, null, null);
        url = "blah http://test.de/~abc/lo_op(ing/mo)o.html/wgatev*er? text";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/~abc/lo_op(ing/mo)o.html/wgatev*er",
                null, null, null);
        url = "http://www.test.de/?q=p";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/", null, "?q=p", null);
        url = "http://www.test.de?q=p";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", null, null, "?q=p", null);
        url = "http://www.test.de";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", null, null, null, null);
        url = "http://www.test.de)";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", null, null, null, null);
        url = "http://www.test.de.";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", null, null, null, null);
        url = "http://www.test.de/moo";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/moo", null, null, null);
        url = "http://www.test.de/moo /tools/";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/moo", null, null, null);
        url = "url (http://www.test.de/moo) blah blah";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/moo", null, null, null);
        url = "http://www.test.de/moo%20pool/def%3Aleft";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/moo%20pool/def%3Aleft", null,
                null, null);
        url = "http://www.test.de/moo;jsessionid=12324324?q=suche";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/moo", ";jsessionid=12324324",
                "?q=suche", null);
        url = "http://www.blah.de/test?query=test&check=true";
        assertUrlPatternDoesMatch(url, "http://www.", "blah.de", "/test", null,
                "?query=test&check=true", null);
        url = "http://www.blah.de/test;jsessionid=BAEF231123424.jvm0?query=test&check=true";
        assertUrlPatternDoesMatch(url, "http://www.", "blah.de", "/test",
                ";jsessionid=BAEF231123424.jvm0", "?query=test&check=true", null);
        url = "http://www.test.de/abc/moo.html?q=p&l=23,23&test=no(n)e";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/abc/moo.html", null,
                "?q=p&l=23,23&test=no(n)e", null);
        url = "http://www.test.de/abc/moo.html?q=p&l=23,23&test=no(n)e)";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/abc/moo.html", null,
                "?q=p&l=23,23&test=no(n)e", null);
        url = "http://www.test.de/abc/moo.html?q=p&l=23,23&test=no(n)e.";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/abc/moo.html", null,
                "?q=p&l=23,23&test=no(n)e", null);
        url = "post http://www.test.de/abc/moo.html?q=p.er&l=23,23&test=no(n)e? dfgj";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/abc/moo.html", null,
                "?q=p.er&l=23,23&test=no(n)e", null);
        url = "http://www.test.de/abc/moo.html?q=p&l=23,23&test=no(n)e]";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/abc/moo.html", null,
                "?q=p&l=23,23&test=no(n)e", null);
        url = "http://www.test.de/abc/moo.html?q=p&l=23,23&test=no(n)e!";
        assertUrlPatternDoesMatch(url, "http://www.", "test.de", "/abc/moo.html", null,
                "?q=p&l=23,23&test=no(n)e", null);
        url = "hTTp://WWw.test.DE/abc/mOo.html?q=p&l=23,23&tesT=no(n)e!";
        assertUrlPatternDoesMatch(url, "hTTp://WWw.", "test.DE", "/abc/mOo.html", null,
                "?q=p&l=23,23&tesT=no(n)e", null);
        // trailing slash in query
        url = "http://test.de/node/dumb.html?w=12&t=/abc/dir/";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/node/dumb.html", null,
                "?w=12&t=/abc/dir/", null);
        url = "http://www.facebook.com/home.php?#!/clubdertoechter.muc";
        assertUrlPatternDoesMatch(url, "http://www.", "facebook.com", "/home.php", null,
                "?#!/clubdertoechter.muc", null);
        url = "http://www.handelsblatt.com/technologie/it-internet/"
                + "download-charts-die-erfolgreichsten-iphone-apps;2698288";
        assertUrlPatternDoesMatch(url, "http://www.", "handelsblatt.com",
                "/technologie/it-internet/download-charts-die-erfolgreichsten-iphone-apps",
                ";2698288", null, null);
    }

    /**
     * Tests that the UrlHelper.URL_PATTERN, which is used to find URLs in text, matches correctly.
     * Focuses on fragment identifier.
     */
    @Test
    public void testUrlRecognitionPositiveCasesFragmentPart() {
        String url = "http://test.de/#core";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/", null, null, "#core");
        url = "http://test.de#core";
        assertUrlPatternDoesMatch(url, "http://", "test.de", null, null, null, "#core");
        url = "http://test.de/?w=12&q=3%204#core";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/", null, "?w=12&q=3%204", "#core");
        url = "http://test.de/?w=12&q=3%204#co(r)e=3";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/", null, "?w=12&q=3%204",
                "#co(r)e=3");
        url = "http://test.de/?w=12&q=3%204#co(r)e=3. more text";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/", null, "?w=12&q=3%204",
                "#co(r)e=3");
        url = "http://test.de/?w=12&q=3%204#co-(r)e=3) more text";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/", null, "?w=12&q=3%204",
                "#co-(r)e=3");
        url = "http://test.de/node/dumb.html?w=12&q=3%204#mode,damn, ";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/node/dumb.html", null,
                "?w=12&q=3%204", "#mode,damn");
        url = "http://test.de/node/dumb.html?w=12&q=3%204#/path/test/dir ";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/node/dumb.html", null,
                "?w=12&q=3%204", "#/path/test/dir");
        url = "http://test.de/node/dumb.html?w=12&q=3%204#/path/test/dir/ ";
        assertUrlPatternDoesMatch(url, "http://", "test.de", "/node/dumb.html", null,
                "?w=12&q=3%204", "#/path/test/dir/");
    }

    /**
     * Tests that the UrlHelper.URL_PATTERN, which is used to find URLs in text, matches correctly.
     * Focuses on unicode in domain and path component of URL.
     */
    @Test
    public void testUrlRecognitionPositiveCasesUnicode() {
        // test unicode letters (German o-umlaut) in domain
        String url = "http://www.l\u00f6we.com/test/x123_z/~";
        assertUrlPatternDoesMatch(url, "http://www.", "l\u00f6we.com", "/test/x123_z/~", null,
                null, null);
        // test unicode letters (German o-umlaut and katakana a) in domain
        url = "http://www.l\u00f6\u30A1e.com/test/x123_z/~";
        assertUrlPatternDoesMatch(url, "http://www.", "l\u00f6\u30A1e.com", "/test/x123_z/~", null,
                null, null);
        // test unicode letters (umlaut) in URL path
        url = "http://www.test.com/test/x123_z\u00f6/~";
        assertUrlPatternDoesMatch(url, "http://www.", "test.com", "/test/x123_z\u00f6/~", null,
                null, null);
        url = "http://www.test.com/test/x123_z\u00f6";
        assertUrlPatternDoesMatch(url, "http://www.", "test.com", "/test/x123_z\u00f6", null, null,
                null);
        // test unicode letters (hiragana) in URL path
        url = "http://www.test.com/t\u3072\u3069\u3044/x123_z/~";
        assertUrlPatternDoesMatch(url, "http://www.", "test.com", "/t\u3072\u3069\u3044/x123_z/~",
                null, null, null);
        // test for stop at non letter character outside of ASCII (ideographic full stop)
        url = "http://www.test.com/test/abc\u3002bc";
        assertUrlPatternDoesMatch(url, "http://www.", "test.com", "/test/abc", null, null, null);
    }

    /**
     * Tests the {@link UrlHelper#shortenUrl(String, String, String, String)} which shortens a URL.
     * The Max
     */
    @Test
    public void testUrlShortener() {
        // expects max length to be 30
        String shortUrl;
        // test long host which must not be shortened
        shortUrl = UrlHelper.shortenUrl("http://westinmyhouse-here-even-more-de.de", "/", null,
                null);
        Assert.assertEquals("http://westinmyhouse-here-even-more-de.de/", shortUrl);
        shortUrl = UrlHelper.shortenUrl("http://westinmyhouse-here-even-more-de.de", "/what", null,
                null);
        Assert.assertEquals("http://westinmyhouse-here-even-more-de.de/...", shortUrl);
        shortUrl = UrlHelper.shortenUrl("http://westinmyhouse-here-even-more-de.de", "/wha", null,
                null);
        Assert.assertEquals("http://westinmyhouse-here-even-more-de.de/wha", shortUrl);
        shortUrl = UrlHelper.shortenUrl("http://westinmyhouse-here-even-more-de.de", null,
                "?huhu=value", null);
        Assert.assertEquals("http://westinmyhouse-here-even-more-de.de?...", shortUrl);
        shortUrl = UrlHelper.shortenUrl("http://westinmyhouse-here-even-more-de.de", null, null,
                null);
        Assert.assertEquals("http://westinmyhouse-here-even-more-de.de", shortUrl);
        shortUrl = UrlHelper.shortenUrl("http://westinmyhouse.de:8080", "/path/file", null, null);
        Assert.assertEquals("http://westinmyhouse.de:8080/p...", shortUrl);
        shortUrl = UrlHelper.shortenUrl("http://westinmyhouse.de:8080", "/path/file",
                "?abc=12&abd=d", null);
        Assert.assertEquals("http://westinmyhouse.de:8080/p...", shortUrl);
        shortUrl = UrlHelper.shortenUrl("http://westinmyhouse.de:8080", "/path", null, null);
        Assert.assertEquals("http://westinmyhouse.de:8080/path", shortUrl);
        shortUrl = UrlHelper.shortenUrl("http://westinmyhouse.de:8080", null, "?a=12", null);
        Assert.assertEquals("http://westinmyhouse.de:8080?a=12", shortUrl);
        shortUrl = UrlHelper.shortenUrl("http://westinmyhouse.de:8080", "/path", "?abc=12&abd=d",
                null);
        Assert.assertEquals("http://westinmyhouse.de:8080/p...", shortUrl);
        shortUrl = UrlHelper.shortenUrl("http://westinmyhouse.de:8080", "/path", null,
                ";identifier=2321");
        Assert.assertEquals("http://westinmyhouse.de:8080/p...", shortUrl);
    }
}
