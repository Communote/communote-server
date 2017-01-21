package com.communote.common.string;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for escaping/unescaping strings
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class StringEscapeHelper {

    /**
     * Escapes backslash, single and double quotes with a backslash.
     *
     * @param str
     *            the string to escape
     * @return the escaped string
     */
    public static String escapeJavaScript(String str) {
        int length = str.length();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            if (ch == '\'' || ch == '"' || ch == '\\') {
                result.append("\\");
            }
            result.append(ch);
        }
        return result.toString();

    }

    /**
     * Escapes strings which should be used as string literals in JavaScript code that is rendered
     * in HTML inline event handler attributes like onclick or inside a script tag. The characters
     * '&lt;\"&gt;&amp; are escaped with hex notation. This method should only be used in pages that
     * are UTF-8 encoded.
     *
     * @param str
     *            the string to escape
     * @return the escaped string
     */
    public static String escapeJavaScriptInlineHtml(String str) {
        int length = str.length();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            if (ch < 32) {
                result.append(ch < 16 ? "\\x0" : "\\x");
                result.append(Integer.toHexString(ch));
            } else {
                switch (ch) {
                case '\'':
                case '"':
                case '\\':
                case '&':
                case '>':
                case '<':
                    result.append("\\u00");
                    result.append(Integer.toHexString(ch));
                    break;
                default:
                    result.append(ch);
                }
            }
        }
        return result.toString();
    }

    /**
     * Escapes serialized JSON which should be used in JavaScript code that is rendered inside a
     * script tag. The characters &lt;&gt;&amp; are escaped with hex notation. Other characters are
     * not modified and it is assumed that they were already escaped correctly by the JSON
     * serializer. This method should only be used in pages that are UTF-8 encoded and have an HTML
     * DOCTYPE.
     *
     * @param str
     *            the serialized JSON to escape
     * @return the escaped string
     */
    public static String escapeJsonInlineHtml(String str) {
        int length = str.length();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            switch (ch) {
            case '&':
            case '>':
            case '<':
                result.append("\\u00");
                result.append(Integer.toHexString(ch));
                break;
            default:
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * Escape characters that are not in the slightly extended word regex character class with an
     * underscore. That is all characters that are not in [A-Za-z0-9_.-] will be replaced with an
     * underscore.
     *
     * @param str
     *            the string to escape
     * @return the escaped string
     */
    public static String escapeNonWordCharacters(String str) {
        int length = str.length();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            // 48 is '0'
            if (ch > 47) {
                // 122 is 'z'
                if (ch > 122) {
                    result.append('_');
                } else {
                    if (ch > 96 || (ch > 64 && ch < 91) || ch < 58) {
                        result.append(ch);
                    } else {
                        result.append('_');
                    }
                }
            } else if (ch == '.' || ch == '-') {
                result.append(ch);
            } else {
                result.append('_');
            }
        }
        return result.toString();
    }

    /**
     * Performs the following substring replacements (to facilitate output to XML/HTML pages): <br>
     * &amp; with &amp;amp;<br>
     * &lt; with &amp;lt;<br>
     * &gt; with &amp;gt;<br>
     * " with &amp;#034;<br>
     * ' with &amp;#039;
     *
     * @param buffer
     *            the string to escape XML entities
     * @return the escaped string
     */
    public static String escapeXml(String buffer) {
        int length = buffer.length();
        char[] arrayBuffer = buffer.toCharArray();
        StringBuilder escapedBuffer = new StringBuilder(length + 5);
        for (int i = 0; i < length; i++) {
            char c = arrayBuffer[i];
            // > is highest character of the special characters to escape
            if (c <= '>') {
                switch (c) {
                case '>':
                    escapedBuffer.append("&gt;");
                    break;
                case '<':
                    escapedBuffer.append("&lt;");
                    break;
                case '&':
                    escapedBuffer.append("&amp;");
                    break;
                case '"':
                    escapedBuffer.append("&#034;");
                    break;
                case '\'':
                    escapedBuffer.append("&#039;");
                    break;
                default:
                    escapedBuffer.append(c);
                    break;
                }
            } else {
                escapedBuffer.append(c);
            }
        }
        return escapedBuffer.toString();
    }

    /**
     * get the text of the xml content and replace linebreaks with a whitespace
     *
     * @param html
     *            the html text to anal
     * @return the text of the html
     */
    public static String getSingleLineTextFromXML(String html) {
        String nohtml = removeXmlMarkup(html).replaceAll("\t|[\r\n]+", " ").trim();
        return nohtml;
    }

    /**
     * Removes all tags from a string and replaces HTML 4.0 entities with the corresponding Unicode
     * character.
     *
     * @param str
     *            the string to modify
     * @return the input string with all markup (tags) removed and HTML 4.0 entities unescaped
     */
    public static String removeHtmlMarkup(String str) {
        // TODO this trim might lead to unexpected results if the str starts or ends with a space
        // intentionally
        String markupFree = str.replaceAll("(?s)\\<.*?>", StringUtils.EMPTY).trim();
        return StringEscapeUtils.unescapeHtml4(markupFree);
    }

    /**
     * Removes XML markup from a string and unescapes XML entities (only gt, lt, quot, amp, apos).
     *
     * @param str
     *            the string to modify
     * @return the input string with all markup (tags) removed and XML entities unescaped
     */
    public static String removeXmlMarkup(String str) {
        String markupFree = str.replaceAll("\\<.*?>", StringUtils.EMPTY).trim();
        return StringEscapeUtils.unescapeXml(markupFree);
    }

    /**
     * Do not construct me
     */
    private StringEscapeHelper() {

    }
}
