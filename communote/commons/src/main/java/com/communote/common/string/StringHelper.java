package com.communote.common.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for Strings.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class StringHelper {
    /**
     * Cleans a string, removes whitespaces on start and end, and removes multiple whitespaces.
     * Handles null parameter and returns an empty string.
     * 
     * @param string
     *            the string
     * @return the cleaned string
     */
    public static String cleanString(String string) {
        return StringUtils.trimToEmpty(string).replaceAll("[\\p{Zs}]+", " ");
    }

    /**
     * Cleans a List of Strings, handle null parameter
     * 
     * @param list
     *            the list
     * @return the cleaned list
     */
    public static List<String> cleanStringList(List<String> list) {
        List<String> result = new ArrayList<String>();
        if (list != null) {
            for (String s : list) {
                String cleaned = cleanString(s);
                if (!StringUtils.isEmpty(cleaned)) {
                    result.add(cleaned);
                }
            }
        }
        return result;
    }

    /**
     * Remove the cleanUpChar from the given string by stripping it from the start and end. If it
     * still exists use the replaceChar to remove it.
     * 
     * Example: str="%bla%bla", cleanUpChar="%", replaceChar="-" will return "bla-bla"
     * 
     * @param str
     *            the string to cleanup
     * @param cleanUpChar
     *            the string to replace
     * @param replaceChar
     *            the string to replace with
     * @return
     */
    public static String cleanUpString(String str, String cleanUpChar, String replaceChar) {

        str = StringUtils.stripStart(str, cleanUpChar);
        str = StringUtils.stripEnd(str, cleanUpChar);
        str = str.replace(cleanUpChar, replaceChar);
        return str;
    }

    /**
     * Converts a string containing a decimal number into a long. If the conversion is not possible
     * the fallback will be returned.
     * 
     * @param value
     *            the value to convert
     * @param fallback
     *            the fallback to return if conversion failed
     * @return the long value
     */
    public static Long getStringAsLong(String value, Long fallback) {
        Long longValue = fallback;
        if (value != null) {
            try {
                longValue = Long.parseLong(value);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return longValue;
    }

    /**
     * Gets the string as long array.
     * 
     * @param value
     *            the value
     * @return the string as long array
     */
    public static Long[] getStringAsLongArray(String value) {
        Long[] result = null;
        List<Long> longValues = getStringAsLongList(value);
        if (longValues != null) {
            result = longValues.toArray(new Long[longValues.size()]);
        }
        return result;
    }

    /**
     * @param values
     *            The values to parse.
     * @return The values as Long array.
     */
    public static Long[] getStringAsLongArray(String[] values) {
        List<Long> result = getStringAsLongList(values);
        return result.toArray(new Long[result.size()]);
    }

    /**
     * Convert a string of longs which are separated by commas into a list of Long objects.
     * 
     * @param value
     *            the value to process
     * @return the list of Longs
     */
    public static List<Long> getStringAsLongList(String value) {
        return getStringAsLongList(value, false);
    }

    /**
     * Convert a string of longs which are separated by commas into a list of Long objects.
     * 
     * @param value
     *            the value to process
     * @param ignoreValuesEqualLessZero
     *            if true, parsed long values <= 0 will not be added to the returning list
     * @return the list of Longs
     */
    public static List<Long> getStringAsLongList(String value, boolean ignoreValuesEqualLessZero) {
        return getStringAsLongList(value, ",", ignoreValuesEqualLessZero);
    }

    /**
     * Convert a string of longs which are separated by a given string into a list of Long objects.
     * 
     * @param value
     *            the value to process
     * @param separator
     *            the separator
     * @return the list of Longs
     */
    public static List<Long> getStringAsLongList(String value, String separator) {
        return getStringAsLongList(value, separator, false);
    }

    /**
     * Convert a string of longs which are separated by a given string into a list of Long objects.
     * 
     * @param value
     *            the value to process
     * @param separator
     *            the separator
     * @param ignoreValuesEqualLessZero
     *            if true, parsed long values <= 0 will not be added to the returning list
     * @return the list of Longs
     */
    public static List<Long> getStringAsLongList(String value, String separator,
            boolean ignoreValuesEqualLessZero) {
        String[] splitted = StringUtils.split(value, separator);
        if (splitted != null && splitted.length > 0) {
            List<Long> longValues = new ArrayList<Long>();
            for (String s : splitted) {
                try {
                    s = s.trim();
                    if (s.length() > 0) {
                        Long longValue = Long.parseLong(s);
                        if (!ignoreValuesEqualLessZero || longValue > 0) {
                            longValues.add(longValue);
                        }
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            if (longValues.size() > 0) {
                return longValues;
            }
        }
        return null;
    }

    /**
     * @param values
     *            The values to parse.
     * @return The values as list of Long
     */
    public static List<Long> getStringAsLongList(String[] values) {
        if (values == null || values.length == 0) {
            return new ArrayList<Long>(0);
        }
        List<Long> result = new ArrayList<Long>();
        for (String value : values) {
            List<Long> valueAsLong = getStringAsLongList(value);
            if (valueAsLong != null) {
                result.addAll(valueAsLong);
            }
        }
        return result;
    }

    /**
     * Converts a string of the form 'k1=v1,k2=v2,k3=v3' into a map with k's as key and the v's as
     * the associated values.
     * 
     * @param mapping
     *            the string based mapping to convert
     * @return the created map
     */
    public static Map<String, String> getStringAsMap(String mapping) {
        Map<String, String> parsedMapping = new HashMap<String, String>();
        String[] entries = mapping.split(",");
        for (int i = 0; i < entries.length; i++) {
            String[] attribute = entries[i].split("=");
            // ignore all entries with incorrect syntax
            if (attribute.length == 2) {
                parsedMapping.put(attribute[0].trim(), attribute[1].trim());
            }
        }
        return parsedMapping;
    }

    /**
     * Replaces all occurrences of a character with the replacement.
     * 
     * @param source
     *            the source to process
     * @param charToReplace
     *            the character to replace
     * @param replacement
     *            the replacement for the character
     * @return the processed string
     */
    public static String replaceCharacter(String source, char charToReplace, String replacement) {
        // slightly more efficient than regex replace (replaceAll) due to regex compile overhead
        int length = source.length();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char ch = source.charAt(i);
            if (ch == charToReplace) {
                result.append(replacement);
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * Build a string containing toString for each sub element. The result will be in the form
     * [a,b,null,c ...]
     * 
     * @param coll
     *            The collection to use
     * @return The String
     */
    public static String toString(Collection<?> coll) {
        StringBuilder sb = new StringBuilder();

        if (coll == null) {
            sb.append("null");
        } else {
            sb.append("[");
            String prefix = StringUtils.EMPTY;
            for (Object o : coll) {
                sb.append(prefix);
                sb.append(o);
                prefix = ", ";
            }
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * Converts the map into a string of the form 'k1=v1,k2=v2,k3=v3'. This method does no escaping,
     * thus keys and values should not contain commas or equal signs. The counterpart of this
     * function is {@link StringHelper#getStringAsMap(String)}.
     * 
     * @param mapping
     *            the map to convert
     * @return the string
     */
    public static String toString(Map<String, String> mapping) {
        if (mapping.size() == 0) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        String separator = StringUtils.EMPTY;
        for (String key : mapping.keySet()) {
            sb.append(separator);
            sb.append(key);
            sb.append("=");
            sb.append(mapping.get(key));
            separator = ",";
        }
        return sb.toString();
    }

    /**
     * Build a string of the array in the form "o1, o2, o3" if separator is ", "
     * 
     * @param array
     *            an array of objects which should be "stringed"
     * @param separator
     *            the separator to use
     * @return null if array is null otherwise the objects strings separated by the separator
     */
    public static String toString(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String prefix = StringUtils.EMPTY;
        for (Object o : array) {
            sb.append(prefix);
            prefix = separator;
            if (o != null) {
                sb.append(o.toString());
            }

        }
        return sb.toString();
    }

    /**
     * To cut out middle part of a String.
     * 
     * @param string
     *            the string to truncate
     * @param maxLength
     *            the max length of a string
     * @param ellipses
     *            the replacement in case of cutting the string
     * 
     * @return The abbreviated string if the string exceeds the max length otherwise the original
     *         string is returned.
     */
    public static String truncateMiddle(String string, int maxLength, String ellipses) {
        int strlen = string.length();

        if (strlen <= maxLength) {
            return string;
        }

        if (StringUtils.isBlank(ellipses)) {
            ellipses = "...";
        }

        int lengthToKeep = maxLength - ellipses.length();
        int start = 0;
        int end = 0;

        if ((lengthToKeep % 2) == 0) {
            start = lengthToKeep / 2;
            end = start;
        } else {
            start = lengthToKeep / 2;
            end = start + 1;
        }

        return StringUtils.substring(string, 0, start) + ellipses
                + StringUtils.substring(string, -1 * end);

    }

    /**
     * Do not construct me
     */
    private StringHelper() {
        // Do nothing.
    }
}
