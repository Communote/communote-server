package com.communote.common.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for working with URLs.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UrlHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlHelper.class);

    private static final Pattern ABSOLUTE_URL_PATTERN = Pattern.compile("\\A[a-z0-9.+-]+://.*",
            Pattern.CASE_INSENSITIVE);

    private static final String QUERY_STRING_PREFIX = "?";

    // RegEx class of unreserved characters in an URL, without leading [ to allow prepending more
    // characters
    private static final String URL_UNRESERVED = "A-Z0-9_.!~*'()-]";
    // RegEx class of unreserved characters in an URL that differs from RFC in that it allows any
    // letter and not just A-Z, without leading [ to allow prepending more characters
    private static final String URL_UNRESERVED_EXTENDED = "\\p{L}0-9_.!~*'()-]";

    private static final String URL_USERINFO = "[%;:&=+$," + URL_UNRESERVED + "+@";
    // slash in the pattern is not quite correct because would treat '//////' as legal, but is
    // faster
    private static final String URL_PATH_CHAR = "[/%:@&=+$," + URL_UNRESERVED_EXTENDED + "*";
    // departure from RFC because we do not want to match trailing parenthesis and typical line end
    // characters as part of the URL and also support any unicode character from letter class
    private static final String URL_PATH = "(?:" + URL_PATH_CHAR + "[\\p{L}0-9_+%/~$@-])";
    private static final String URL_SIMPLIFIED_URIC = "[%;/?:@&=+,$" + URL_UNRESERVED
            + "*[A-Z0-9_+%/~$=-]";
    // query part of the URL similar to URL_PATH in that it does not match line end characters
    private static final String URL_QUERY_PART = "\\?(?:#!)?" + URL_SIMPLIFIED_URIC;
    // fragment identifier within an URL similar to URL_PATH in that it does not match line end
    // characters
    private static final String URL_FRAGMENT_IDENTIFIER = "#(?:" + URL_SIMPLIFIED_URIC + ")?";
    // regex pattern for finding URLs in text-nodes (close to RFC2396, but only allowing one segment
    // with ';'-separated (simplified) param and not supporting arbitrary schemes)
    private static final String URL_RECOGNITION = "(?<![\\w/])((?:(?:ht|f)tp(?:s?)://(?:"
            + URL_USERINFO + ")?(?:www\\.)?)|(?:www\\.))"
            + "([\\p{L}0-9.-]*[\\p{L}0-9]+(?::[0-9]+)?)(/" + URL_PATH + "?)?(;[A-Z0-9=.]+)?("
            + URL_QUERY_PART + ")?(" + URL_FRAGMENT_IDENTIFIER + ")?";
    /**
     * Pattern for recognition of URLs. The pattern tries to find URLs as defined in RFC2396.
     * However there are some differences
     * <ul>
     * <li>The protocol is limited to HTTP, HTTPS, FTP and FTPS</li>
     * <li>The protocol can be omitted if URL starts with www. and will then default to HTTP</li>
     * <li>The domain can contain any unicode character of class letter and not just a-z</li>
     * <li>The path component can contain any unicode character of class letter and not just a-z</li>
     * <li>The path component can only contain one segment</li>
     * </ul>
     */
    public static final Pattern URL_PATTERN = Pattern.compile(URL_RECOGNITION,
            Pattern.CASE_INSENSITIVE);
    private static final int URL_MAX_LENGTH = 30;
    private static final String URL_SHORTENING_INDICATOR = "...";

    private static final String HTML_ANCHOR = "<a [^>]*href=\"([^\"]+)\"[^>]*>(.*?)</a>";
    private static final Pattern ANCHOR_PATTERN = Pattern.compile(HTML_ANCHOR,
            Pattern.CASE_INSENSITIVE);

    private static final char[] LINK_TERMINATING_CHARS = { '.', '?', '!', ')', ',', ';', '"', '\'',
            ']', '}' };

    /**
     * Checks if url is well formed. If not an {@link IllegalArgumentException} is thrown
     *
     * @param url
     *            the url to check
     */
    public static void assertIsValidUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("url=" + url + " is not well formed! e="
                    + e.getMessage(), e);
        }
    }

    /**
     * Parses a string for HTML anchor elements and replaces a shortened URL (suffixed by ...)
     * within the the anchor with the href value. If the anchor does not contain any URL the href
     * value is appended to the anchor content (encapsulated by parentheses). The anchor tag and
     * attributes will be removed.
     *
     * @param content
     *            the content to parse
     * @return the processed content
     */
    public static String convertAnchorsToString(String content) {
        StringBuilder processedContent = new StringBuilder();
        int start = 0;
        Matcher matcher = ANCHOR_PATTERN.matcher(content);
        while (matcher.find()) {
            processedContent.append(content.substring(start, matcher.start(0)));
            insertSpaceBeforeAfterLink(content, matcher.start(0) - 1, processedContent, true);
            // test if the innerHTML has a shortened URL
            String innerHTML = matcher.group(2);
            String href = matcher.group(1);
            if (innerHTML != null && innerHTML.length() > 0) {
                innerHTML = innerHTML.replace("&amp;", "&");
                Matcher urlMatcher = URL_PATTERN.matcher(innerHTML);
                int innerStart = 0;
                boolean anchorProcessed = false;
                loop: while (urlMatcher.find()) {
                    processedContent.append(innerHTML.substring(innerStart, urlMatcher.start(0)));
                    String remainingText = innerHTML.substring(urlMatcher.end(0));
                    String foundUrl = urlMatcher.group(0);
                    if (urlMatcher.group(1).startsWith("www.")) {
                        foundUrl = "http://" + foundUrl;
                    }
                    // TODO should we urlEncodeUrlPath the path component before comparing with HREF
                    // value?
                    if ((remainingText.startsWith(URL_SHORTENING_INDICATOR)
                            || remainingText.startsWith("&" + URL_SHORTENING_INDICATOR) || remainingText
                                .startsWith("?" + URL_SHORTENING_INDICATOR))
                            && href.startsWith(foundUrl)) {
                        // replace with href value
                        processedContent.append(href);
                        int symbolCount = remainingText.startsWith(URL_SHORTENING_INDICATOR) ? 3
                                : 4;
                        // append value right of match and break
                        processedContent
                                .append(innerHTML.substring(urlMatcher.end(0) + symbolCount));
                        anchorProcessed = true;
                        break loop;
                    }
                    // test if href value matches the found URL
                    if (foundUrl.equals(href)) {
                        processedContent.append(href);
                        processedContent.append(remainingText);
                        anchorProcessed = true;
                        break loop;
                    }
                    processedContent.append(urlMatcher.group(0));
                    innerStart = urlMatcher.end(0);
                }
                if (!anchorProcessed) {
                    processedContent.append(innerHTML.substring(innerStart));
                    // append url in parentheses
                    processedContent.append(" (");
                    processedContent.append(href);
                    processedContent.append(")");
                }
            } else {
                processedContent.append(href);
            }
            start = matcher.end(0);
            // add space if next char is an alpha-num char, happens someone removed the space after
            // the anchor
            insertSpaceBeforeAfterLink(content, start, processedContent, false);
        }
        if (start == 0) {
            return content;
        }

        processedContent.append(content.substring(start));
        return processedContent.toString();
    }

    /**
     * Get a substring of an absolute URL from beginning up to the path, excluding the path and
     * without a trailing slash.
     *
     * @param url
     *            the absolute URL to process
     * @param stripUserInfo
     *            whether to remove the user info if contained
     * @return the substring or null if the provided URL is not absolute, has an unsupported
     *         protocol or no host.
     */
    public static String getProtocolHostPort(String url, boolean stripUserInfo) {
        try {
            URL urlObj = new URL(url);
            if (urlObj.getHost() != null && urlObj.getHost().length() > 0) {
                StringBuilder result = new StringBuilder(urlObj.getProtocol());
                result.append("://");
                if (!stripUserInfo && urlObj.getUserInfo() != null) {
                    result.append(urlObj.getUserInfo());
                    result.append("@");
                }
                result.append(urlObj.getHost());
                if (urlObj.getPort() != -1) {
                    result.append(":");
                    result.append(urlObj.getPort());
                }
                return result.toString();
            }
        } catch (MalformedURLException e) {
            // not absolute or unknown protocol
        }
        return null;
    }

    /**
     * Insert the session ID in the URL in the form ";jessionid=SESSION_ID". Takes care of query
     * strings.
     *
     * @param url
     *            the url to modify
     * @param sessionId
     *            the sessionId to insert
     * @return the url/uri with the session id
     */
    public static String insertSessionIdInUrl(String url, String sessionId) {
        String jSessionId = ";jsessionid=" + sessionId;
        if (url.indexOf(QUERY_STRING_PREFIX) > 0) {
            url = url.replace(QUERY_STRING_PREFIX, jSessionId + QUERY_STRING_PREFIX);
        } else {
            url += jSessionId;
        }
        return url;
    }

    /**
     * Inserts a space character into the text if the character at index is not a space character.
     * This is useful for anchor to text conversion if the character before or after the anchor is
     * not a separating character.
     *
     * @param src
     *            the source string to analyze
     * @param index
     *            the index of the character of the source string to check
     * @param target
     *            the target object to which a space character will be appended
     * @param before
     *            if index references a character before the link
     */
    private static void insertSpaceBeforeAfterLink(String src, int index, StringBuilder target,
            boolean before) {
        if (index >= 0 && src.length() > index) {
            char ch = src.charAt(index);
            // left side of link has a smaller set of terminating chars
            if (before) {
                if (!Character.isSpaceChar(ch) && ch != '(' && ch != '[' && ch != '{') {
                    target.append(" ");
                }
            } else {
                if (!isLinkTerminatingChar(ch)) {
                    target.append(" ");
                }
            }
        }
    }

    /**
     * Test whether the given URL is an absolute URL using the HTTP or HTTPS protocol.
     *
     * @param url
     *            the URL to check
     * @return true if the URL is not null and absolute
     */
    public static boolean isAbsoluteHttpUrl(String url) {
        if (url != null) {
            url = url.toLowerCase(Locale.ENGLISH);
            if (url.startsWith("https://")) {
                return url.length() > 8;
            } else if (url.startsWith("http://")) {
                return url.length() > 7;
            }
        }
        return false;
    }

    /**
     * Test whether the provided URL is absolute.
     *
     * @param url
     *            the URL to test
     * @return true if the URL starts with a scheme name as defined in RFC 1738
     */
    public static boolean isAbsoluteUrl(String url) {
        return ABSOLUTE_URL_PATTERN.matcher(url).matches();
    }

    /**
     * Checks if a character is a space character or one of {@link #LINK_TERMINATING_CHARS}
     *
     * @param ch
     *            the character to check
     * @return true if the character is a space character or one of {@link #LINK_TERMINATING_CHARS}
     */
    private static boolean isLinkTerminatingChar(char ch) {
        boolean terminatingChar = false;
        if (!Character.isSpaceChar(ch)) {
            for (int i = 0; i < LINK_TERMINATING_CHARS.length; i++) {
                if (ch == LINK_TERMINATING_CHARS[i]) {
                    terminatingChar = true;
                    break;
                }
            }
        } else {
            terminatingChar = true;
        }
        return terminatingChar;
    }

    /**
     * Shortens a URL.
     *
     * @param protocolHostPart
     *            the protocol and host part of the URL including the port
     * @param pathPart
     *            the path part of the URL; can be null
     * @param queryPart
     *            the query part of the URL; can be null
     * @param fragmentIdentifier
     *            the fragment identifier of the URL; can be null
     * @return the shortened URL
     */
    public static String shortenUrl(String protocolHostPart, String pathPart, String queryPart,
            String fragmentIdentifier) {
        // always full protocol host part
        StringBuilder url = new StringBuilder();
        url.append(protocolHostPart);
        StringBuilder pathQueryFragmentPart = new StringBuilder();
        if (pathPart != null) {
            pathQueryFragmentPart.append(pathPart);
        }
        if (queryPart != null) {
            pathQueryFragmentPart.append(queryPart);
        }
        if (fragmentIdentifier != null) {
            pathQueryFragmentPart.append(fragmentIdentifier);
        }
        int pathQueryFragmentLength = pathQueryFragmentPart.length();
        if (protocolHostPart.length() + pathQueryFragmentLength - URL_MAX_LENGTH > URL_SHORTENING_INDICATOR
                .length()) {
            int pathCharLimit = URL_MAX_LENGTH - protocolHostPart.length();
            if (pathCharLimit <= 0) {
                int limit = 0;
                // take at least one character from path part
                if (pathQueryFragmentLength > 0) {
                    limit = pathQueryFragmentLength > URL_SHORTENING_INDICATOR.length() + 1 ? 1
                            : pathQueryFragmentLength;
                }
                pathCharLimit = limit;
            }
            url.append(pathQueryFragmentPart.substring(0, pathCharLimit));
            if (pathCharLimit < pathQueryFragmentLength) {
                url.append(URL_SHORTENING_INDICATOR);
            }
        } else {
            url.append(pathQueryFragmentPart);
        }

        return url.toString();
    }

    /**
     * <p>
     * URL encode (application/x-www-form-urlencoded format) the path component of a URL. The method
     * uses the UTF-8 encoding scheme.
     * </p>
     *
     * Note: the application/x-www-form-urlencoded format encodes spaces with the plus character.
     * For proper URI encoding better use {@link UriUtils#encodeUriComponent(String)}!
     *
     * @param path
     *            the path component of a URL. If parts of the path are already URL encoded these
     *            will be omitted
     * @return the encoded path component
     */
    public static String urlEncodeUrlPath(String path) {
        String decodePath;
        try {
            // avoid double encoding by decoding first
            decodePath = URLDecoder.decode(path, "UTF-8");
            // split at '/' to not encode them
            String[] splitted = decodePath.split("/");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < splitted.length; i++) {
                if (splitted[i].length() == 0) {
                    result.append('/');
                } else {
                    result.append(URLEncoder.encode(splitted[i], "UTF-8"));
                    result.append('/');
                }
            }
            if (path.charAt(path.length() - 1) != '/') {
                return result.substring(0, result.length() - 1);
            }
            return result.toString();
        } catch (UnsupportedEncodingException e) {
            // should not occur as UTF-8 is supported
            LOGGER.error("Unexpected exception while encoding URL path", e);
        }
        return path;
    }

    /**
     * Helper class do not need public constructor
     */
    private UrlHelper() {
        // Do nothing.
    }
}
