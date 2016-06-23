package com.communote.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.communote.common.string.StringEscapeHelper;

/**
 * Helper class for HTML processing.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public final class HTMLHelper {

    private static final String PARAGRAPH_ENCAPSULATED_LINE_CONTENT = "<p>$1</p>";
    private static final Pattern NS_PATTERN = Pattern.compile("xmlns:(\\w+)=\".+?\"");
    private static final Pattern OPENING_TAG_PATTERN = Pattern.compile("<\\w[^>]*>");
    private static final Pattern LINE_CONTENT_PATTERN = Pattern.compile(
            "^(.*)(?:(?:\r\n)|[\n\u2029\u2028\u0085\r]|$)", Pattern.MULTILINE);

    private static final Pattern LIST_PATTERN = Pattern.compile("<(/)?(?:(ol|ul)|(li))[^>]*>");
    private static final Pattern NBSP_UNICODE_PATTERN = Pattern
            .compile("\u00A0|&nbsp;|&#xA0;|&#160;");
    private static String LAYER_INDENT = "    ";
    private static Integer LIST_TYPE_UL = new Integer(1);
    private static Integer LIST_TYPE_OL = new Integer(2);
    private static String[] BULLET_CHARACTERS = { "* ", "o ", "- " };

    /**
     * Names of HTML tags whose nodes should be removed when they do not have a body.
     */
    public static final Set<String> NODES_TO_REMOVE_WHEN_EMPTY = new HashSet<String>(Arrays.asList(
            "strong", "b", "em", "i", "u", "li", "ul", "ol", "a", "div", "blockquote"));

    /**
     * Regex class of line terminators. Note: using this class for line break removal/replacement is
     * not save because of '\r\n' combination which must be checked separately.
     */
    public static final String LINE_TERMINATORS_REGEX_CLASS = "[\n\u2029\u2028\u0085\r]";

    /**
     * Converts br tags to the XHTML compatible form.
     * 
     * @param htmlContent
     *            the content to process
     * @return the processed content
     */
    public static String closeBrTags(String htmlContent) {
        return htmlContent.replaceAll("<(?:br|BR)>", "<br />");
    }

    /**
     * Tests whether the provided HTML contains non-empty text nodes.
     * 
     * @param htmlContent
     *            the HTML to test
     * @return false if htmlContent is null or contains only whitespace (including Unicode codepoint
     *         160) as text, true otherwise
     */
    public static boolean containsNonEmptyTextNodes(String htmlContent) {
        if (htmlContent != null) {
            String str = StringEscapeHelper.removeHtmlMarkup(htmlContent);
            str = NBSP_UNICODE_PATTERN.matcher(str).replaceAll(StringUtils.EMPTY);
            return StringUtils.isNotBlank(str);
        }
        return false;
    }

    /**
     * Converts HTML lists to readable plain text representations. The lists can be nested ol or ul
     * elements.
     * 
     * @param htmlContent
     *            the HTML content to parse for lists
     * @return the converted content
     */
    public static String convertHTMLListToText(String htmlContent) {
        Matcher matcher = LIST_PATTERN.matcher(htmlContent);
        StringBuilder convertedContent = new StringBuilder();
        LinkedList<Integer> openLists = new LinkedList<Integer>();
        Map<Integer, Integer> olCounters = new HashMap<Integer, Integer>();
        int start = 0;
        boolean prependNewline = false;
        while (matcher.find()) {
            convertedContent.append(htmlContent.substring(start, matcher.start()));
            start = matcher.end();

            int layer = openLists.size() - 1;

            if (matcher.group(1) != null) {
                if (matcher.group(3) != null) {
                    // do nothing
                } else {
                    // remove last entry in openlists to close a list -
                    // expecting well formed content
                    openLists.removeLast();
                    if (layer == 0) {
                        // closing last list -> add newline
                        convertedContent.append("\n");
                        prependNewline = false;
                    }
                }
            } else {
                if (matcher.group(3) != null) {
                    if (prependNewline) {
                        convertedContent.append("\n");
                    } else {
                        prependNewline = true;
                    }
                    if (layer < 0) {
                        // unbalanced content -> interpret as ul
                        convertedContent.append(BULLET_CHARACTERS[0]);
                    } else {
                        convertedContent.append(StringUtils.repeat(LAYER_INDENT, layer));
                        if (openLists.get(layer).equals(LIST_TYPE_OL)) {
                            Integer layerObj = new Integer(layer);
                            Integer curCount = olCounters.get(layerObj);
                            convertedContent.append(curCount);
                            convertedContent.append(". ");
                            olCounters.put(layerObj, new Integer(curCount.intValue() + 1));
                        } else {
                            int bulIdx = layer % BULLET_CHARACTERS.length;
                            convertedContent.append(BULLET_CHARACTERS[bulIdx]);
                        }
                    }
                } else {
                    layer += 1;
                    if (matcher.group(2).equals("ul")) {
                        openLists.addLast(LIST_TYPE_UL);
                    } else {
                        openLists.addLast(LIST_TYPE_OL);
                        // add new counter
                        olCounters.put(new Integer(layer), new Integer(1));
                    }
                }
            }
        }
        if (start < htmlContent.length()) {
            convertedContent.append(htmlContent.substring(start));
        }
        return convertedContent.toString();
    }

    /**
     * Replaces all closing paragraph tags and br elements with newlines.
     * 
     * @param htmlContent
     *            The content as HTML.
     * @return The converted content.
     */
    private static String convertParagraphAndBrElements(String htmlContent) {
        // <p><br/></p> represent one line (when rendered)
        String result = htmlContent.replaceAll("(?i)<p><br\\s?/?></p>", "<LBR>");
        result = result.replaceAll("(?i)<br\\s?/?>(?=.)", "\n");
        result = result.replaceAll("(?i)</p>(?=.)", "\n");
        result = result.replace("<LBR>", "\n");
        return result;
    }

    /**
     * Does some cleanup to convert HTML generated by serializing an HTML DOM with an XML processor
     * to legal HTML.
     * 
     * @param xmlSerializedHtml
     *            the HTML that was serialized by an XML processor
     * @return the cleaned HTML
     */
    public static String convertXmlSerializedHtmlToLegalHtml(String xmlSerializedHtml) {
        // remove all directly closed nodes whose local name is in set of nodes to remove when
        // empty, because these elements can lead to strange rendering issues (like everything
        // ending up bold)
        boolean scanForEmptyNodes = true;
        while (scanForEmptyNodes) {
            int length = xmlSerializedHtml.length();
            for (String tagName : NODES_TO_REMOVE_WHEN_EMPTY) {
                xmlSerializedHtml = xmlSerializedHtml.replaceAll("<" + tagName
                        + "(?:\\s*|\\s[^>]+)(?:/\\s*>|></" + tagName + ">)", "");
            }
            // repeat if some empty nodes were removed to correctly clean stacked empty nodes (like
            // <b><i/></b>)
            scanForEmptyNodes = length != xmlSerializedHtml.length();
        }
        // remove all zero-width spaces
        xmlSerializedHtml = xmlSerializedHtml.replaceAll("&#8203;", StringUtils.EMPTY);
        xmlSerializedHtml = xmlSerializedHtml.replaceAll("\u200B", StringUtils.EMPTY);
        // fill empty p-tags with a BR (zero-width spaces are not always rendered correctly)
        xmlSerializedHtml = xmlSerializedHtml.replaceAll("<p\\s*(?:/\\s*>|>\\s*</p>)",
                "<p><br/></p>");
        // replace char-code 160 with html entity
        return xmlSerializedHtml.replaceAll("\u00A0", "&#160;");
    }

    /**
     * Some tools (e.g. M$-Outlook) create HTML where single-word attribute values are not
     * encapsulated in quotes. Although this is legal HTML, it is not parsable by an XML parser.
     * This method adds the quotes (double quotes).
     * 
     * @param htmlContent
     *            The content as HTML.
     * @return the repaired content
     */
    public static String encapsulateAttributesInQuotes(String htmlContent) {
        int startIdx = 0;
        StringBuilder cleanedContent = new StringBuilder();
        Matcher m = OPENING_TAG_PATTERN.matcher(htmlContent);
        while (m.find()) {
            cleanedContent.append(htmlContent.substring(startIdx, m.start()));
            cleanedContent.append(m.group().replaceAll("=([\\w-]+)((:?\\s+)|>)", "=\"$1\"$2"));
            startIdx = m.end();
        }
        if (startIdx < htmlContent.length()) {
            cleanedContent.append(htmlContent.substring(startIdx));
        }
        return cleanedContent.toString();
    }

    /**
     * A simple helper which converts HTML to plain text which works similarly to
     * {@link StringEscapeHelper#removeHtmlMarkup(String)}, but tries to preserve linebreaks by
     * replacing closing paragraphs and br elements with newlines. More over list items are
     * surrounded by minus and newline character.
     * 
     * @param htmlContent
     *            the HTML content
     * @return the plain text version
     */
    public static String htmlToPlaintext(String htmlContent) {
        String result = convertParagraphAndBrElements(htmlContent);
        result = result.replaceAll("(?i)</li>(?=.)", "\n");
        result = result.replaceAll("(?i)<li>(?=.)", "-");
        return StringEscapeHelper.removeHtmlMarkup(result);
    }

    /**
     * Helper method similar to {@link #htmlToPlaintext(String)} with the difference that it uses a
     * more advanced approach to replace HTML lists (namely by invoking
     * {@link #convertHTMLListToText(String)}). This method also converts hyperlinks into text
     * representation using {@link UrlHelper#convertAnchorsToString(String)}.
     * 
     * @param htmlContent
     *            the HTML content
     * @param convertLists
     *            whether to convert HTML lists. If true lists will be converted by invoking
     *            {@link #convertHTMLListToText(String)}. If false, list entries will only be
     *            separated by line breaks.
     * @return the plain text version
     */
    public static String htmlToPlaintextExt(String htmlContent, boolean convertLists) {
        String result = convertParagraphAndBrElements(htmlContent);
        result = UrlHelper.convertAnchorsToString(result);
        if (convertLists) {
            result = convertHTMLListToText(result);
        } else {
            result = result.replaceAll("(?i)</li>(?=.)", "\n");
        }
        return StringEscapeHelper.removeHtmlMarkup(result);
    }

    /**
     * A simple helper which converts a plain text string to HTML by encapsulating all lines with
     * paragraphs and escapes characters reserved in XML.
     * 
     * @param plainText
     *            the plain text
     * @return the converted string
     */
    public static String plaintextToHTML(String plainText) {
        // String result = StringEscapeUtils.escapeXml(plainText);
        String result = plainText.replace("&", "&amp;");
        result = result.replace(">", "&gt;");
        result = result.replace("<", "&lt;");
        result = result.replace("\"", "&quot;");
        result = result.replace("'", "&apos;");
        result = LINE_CONTENT_PATTERN.matcher(result).replaceAll(
                PARAGRAPH_ENCAPSULATED_LINE_CONTENT);
        return result;
    }

    /**
     * Removes all HTML comments.
     * 
     * @param htmlContent
     *            the HTML content
     * @return the content without comments
     */
    public static String removeComments(String htmlContent) {
        return htmlContent.replaceAll("(?s)<!--.*?-->", "");
    }

    /**
     * Strips the head element with all children from an HTML string.
     * 
     * @param htmlContent
     *            the content to process
     * @return the htmlContent without meta tags
     */
    public static String removeHeadElement(String htmlContent) {
        return htmlContent.replaceAll("(?is)<head[^>]*>.*?</head>", "");
    }

    /**
     * Removes all namespace definitions and the nodes using these namespaces. The body of the nodes
     * will be kept.
     * 
     * @param htmlContent
     *            the content to process
     * @return the cleaned content
     */
    public static String removeNamespaces(String htmlContent) {
        int startIdx = 0;
        StringBuilder cleanedContent = new StringBuilder();
        List<String> nsList = new ArrayList<String>();
        Matcher m = NS_PATTERN.matcher(htmlContent);
        while (m.find()) {
            cleanedContent.append(htmlContent.substring(startIdx, m.start()));
            nsList.add(m.group(1));
            startIdx = m.end();
        }
        // add rest to cleanedContent
        if (startIdx < htmlContent.length()) {
            cleanedContent.append(htmlContent.substring(startIdx));
        }
        String cleanedContentString = cleanedContent.toString();
        // remove all tags with qualified names
        for (String nsPrefix : nsList) {
            cleanedContentString = cleanedContentString
                    .replaceAll("</?" + nsPrefix + ":[^>]+>", "");
        }
        return cleanedContentString;
    }

    /**
     * Remove open meta elements.
     * 
     * @param htmlContent
     *            the HTML content
     * @return the cleaned content
     */
    public static String removeUnclosedMetaElements(String htmlContent) {
        return htmlContent.replaceAll("(?i)<META[^>]*[^/]?>", "");
    }

    public static String stripLinebreaks(String htmlContent) {
        // remove linebreaks between tags
        htmlContent = htmlContent.replaceAll(">(?:(?:\r\n)|[\n\u2029\u2028\u0085\r])+<", "><");
        // replace all other linebreaks with whitespace
        return htmlContent.replaceAll("(?:(?:\r\n)|[\n\u2029\u2028\u0085\r])+", " ");
    }

    /**
     * Private constructor.
     */
    private HTMLHelper() {

    }
}
