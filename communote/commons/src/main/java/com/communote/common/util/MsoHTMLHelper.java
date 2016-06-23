package com.communote.common.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class providing some utility functions for processing MS office generated HTML.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MsoHTMLHelper {

    private static final Pattern MSO_LIST_PATTERN = Pattern
            .compile(
                    "<p class=\"MsoListParagraph.*?mso-list:l([0-9]+).*?level([0-9]+).*?endif\\]>(.*?)</p>",
                    Pattern.DOTALL);
    private static final Pattern WORD_CHARACTER_PATTERN = Pattern.compile("\\W*\\w");

    /**
     * Close all open lists up to target level by writing the close tags to the convertedContent.
     * 
     * @param convertedContent
     *            The converted content.
     * @param targetLevel
     *            The target level.
     * @param openLevels
     *            The open levels.
     * @param listTypes
     *            The list types.
     */
    private static void closeOpenLists(StringBuilder convertedContent, int targetLevel,
            List<Integer> openLevels, Map<Integer, String> listTypes) {
        for (int i = openLevels.size() - 1; i >= 0; i--) {
            Integer l = openLevels.get(i);
            if (l >= targetLevel) {
                convertedContent.append("</li></");
                convertedContent.append(listTypes.get(l));
                convertedContent.append(">");
                openLevels.remove(i);
            } else {
                break;
            }
        }
    }

    /**
     * Converts paragraphs of class MsoListParagraph into equivalent HTML lists.
     * 
     * @param htmlContent
     *            the HTML to convert
     * @return the converted content
     */
    public static String convertListParagraphsToHTMLList(String htmlContent) {
        StringBuilder convertedContent = new StringBuilder();
        int start = 0;
        int currentMSOListNumber = -1;
        int currentLevel = 0;
        // the open levels of the current mso list
        LinkedList<Integer> openLevels = new LinkedList<Integer>();
        // the list types per level
        HashMap<Integer, String> listTypes = new HashMap<Integer, String>();
        Matcher matcher = MSO_LIST_PATTERN.matcher(htmlContent);
        while (matcher.find()) {
            String betweenListContent = htmlContent.substring(start, matcher.start());
            start = matcher.end();
            // test whether the content between two list paragraphs contains
            // some word characters
            Matcher wordCharMatcher = WORD_CHARACTER_PATTERN.matcher(betweenListContent);
            if (!wordCharMatcher.find()) {
                betweenListContent = "";
            }
            // get level and list number of this paragraph
            // define fallbacks
            int listNumber = currentMSOListNumber == -1 ? 0 : currentMSOListNumber;
            int level = 1;
            String listNumberStr = matcher.group(1);
            String levelStr = matcher.group(2);
            try {
                listNumber = Integer.parseInt(listNumberStr);
                level = Integer.parseInt(levelStr);
            } catch (NumberFormatException e) {
            }
            if (currentMSOListNumber != listNumber) {
                // close open HTML lists of current msoList
                closeOpenLists(convertedContent, level, openLevels, listTypes);
                // insert content found between list paragraphs
                convertedContent.append(betweenListContent);
                // save first level and list type
                String listType = extractListTypeFromComment(htmlContent, listNumber, level);
                openHTMLList(convertedContent, level, listType, openLevels, listTypes, matcher
                        .group(3));
                currentMSOListNumber = listNumber;
                currentLevel = level;
            } else {
                // insert content found between list paragraphs
                convertedContent.append(betweenListContent);
                if (currentLevel < level) {
                    // open new HTML list
                    String listType = extractListTypeFromComment(htmlContent, listNumber, level);
                    openHTMLList(convertedContent, level, listType, openLevels, listTypes, matcher
                            .group(3));
                } else {
                    if (currentLevel != level) {
                        // close HTML list of all open levels > new level
                        closeOpenLists(convertedContent, level + 1, openLevels, listTypes);
                    }
                    convertedContent.append("</li><li>");
                    convertedContent.append(matcher.group(3) == null ? "" : matcher.group(3));
                }
                currentLevel = level;
            }
        }
        // close all open lists (i.e. up to level 1 which is lowest possible)
        closeOpenLists(convertedContent, 1, openLevels, listTypes);
        if (start < htmlContent.length()) {
            convertedContent.append(htmlContent.substring(start));
        }
        return convertedContent.toString();
    }

    /**
     * Returns ul or ol depending on the list styling in comment.
     * 
     * @param htmlContent
     *            The content in html.
     * @param listNumber
     *            The list numver.
     * @param level
     *            The level.
     * @return ul or ol.
     */
    private static String extractListTypeFromComment(String htmlContent, int listNumber, int level) {
        String pattern = "@list l" + listNumber + ":level" + level
                + "\r\n\t{mso-level-number-format:";
        int idx = htmlContent.indexOf(pattern);
        // number format is not defined for normal ordered lists thus ol is
        // default
        String listType = "ol";
        if (idx != -1) {
            idx += pattern.length();
            if (htmlContent.startsWith("bullet", idx) || htmlContent.startsWith("image", idx)) {
                listType = "ul"; // all other cases are ordered lists
            }
        }
        return listType;
    }

    /**
     * 
     * @param convertedContent
     *            The converted content.
     * @param level
     *            The level.
     * @param listType
     *            The list type.
     * @param openLevels
     *            The open levels.
     * @param listTypes
     *            The list types.
     * @param listElementContent
     *            The content.
     */
    private static void openHTMLList(StringBuilder convertedContent, int level, String listType,
            List<Integer> openLevels, Map<Integer, String> listTypes, String listElementContent) {
        Integer newLevel = new Integer(level);
        openLevels.add(newLevel);

        listTypes.put(newLevel, listType);

        // open list and write content
        convertedContent.append("<");
        convertedContent.append(listType);
        convertedContent.append("><li>");
        convertedContent.append(listElementContent == null ? "" : listElementContent);
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private MsoHTMLHelper() {
        // Do nothing
    }
}
