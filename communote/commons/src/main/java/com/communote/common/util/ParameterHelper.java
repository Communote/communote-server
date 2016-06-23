package com.communote.common.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.communote.common.string.StringHelper;

/**
 * Helper class to retrieve parameters in a given format. The parameter map can be values being
 * either {@link String} or String[] (in case of ServletRequest#getParameterMap()).
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class ParameterHelper {

    /**
     * Gets the parameter as boolean.
     *
     * @param parameters
     *            the parameters
     * @param parameter
     *            the parameter
     * @return the parameter as boolean
     */
    public static Boolean getParameterAsBoolean(Map<String, ? extends Object> parameters,
            String parameter) {
        Boolean result = null;

        Object value = parameters.get(parameter);
        if (value != null) {
            if (value instanceof Boolean) {
                return (Boolean) value;

            }

            String str = getValue(parameters, parameter);
            if (str != null) {
                // never throws an exception
                result = Boolean.valueOf(str);
            }
        }
        return result;
    }

    /**
     * Gets the parameter as boolean.
     *
     * @param parameters
     *            the parameters
     * @param parameter
     *            the parameter
     * @param fallback
     *            the fallback
     * @return the parameter as boolean
     */
    public static boolean getParameterAsBoolean(Map<String, ? extends Object> parameters,
            String parameter, boolean fallback) {
        Boolean b = getParameterAsBoolean(parameters, parameter);

        return b == null ? fallback : b.booleanValue();

    }

    /**
     * Gets the parameter as date. The parameters value must be in Milliseconds!
     *
     * @param parameters
     *            the parameters
     * @param parameter
     *            the parameter for the timestamp
     * @return the parameter as date or null if the parameter is not set
     */
    public static Date getParameterAsDate(Map<String, ? extends Object> parameters, String parameter) {
        Date result = null;
        Long timeStamp = getParameterAsLong(parameters, parameter);
        if (timeStamp != null) {
            result = new Date(timeStamp);
        }
        return result;
    }

    /**
     * Gets the parameter as double.
     *
     * @param parameters
     *            the parameters
     * @param parameter
     *            the parameter
     * @return the parameter as double, null if not set or on invalid input
     */
    public static Double getParameterAsDouble(Map<String, ? extends Object> parameters,
            String parameter) {
        Double result = null;
        Object value = parameters.get(parameter);
        if (value != null) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            String str = getValue(parameters, parameter);
            if (str != null) {
                try {
                    result = Double.valueOf(str);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return result;
    }

    /**
     * Gets the parameter as integer.
     *
     * @param parameters
     *            the parameters
     * @param parameter
     *            the parameter
     * @return the parameter as integer
     */
    public static Integer getParameterAsInteger(Map<String, ? extends Object> parameters,
            String parameter) {
        Integer result = null;
        Object value = parameters.get(parameter);
        if (value != null) {
            if (value instanceof Number
                    && (value instanceof Integer || value instanceof Short || value instanceof Byte)) {
                return ((Number) value).intValue();
            }
            String str = getValue(parameters, parameter);
            if (str != null) {
                try {
                    result = Integer.valueOf(str);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return result;
    }

    /**
     * Gets the parameter as integer.
     *
     * @param parameters
     *            the parameters
     * @param parameter
     *            the parameter
     * @param fallback
     *            the fallback
     * @return the parameter as integer
     */
    public static int getParameterAsInteger(Map<String, ? extends Object> parameters,
            String parameter, int fallback) {
        Integer result = getParameterAsInteger(parameters, parameter);

        return result == null ? fallback : result.intValue();
    }

    /**
     * Gets the parameter as long.
     *
     * @param parameters
     *            the parameters
     * @param parameter
     *            the parameter
     * @return the parameter as long
     */
    public static Long getParameterAsLong(Map<String, ? extends Object> parameters, String parameter) {
        Long result = null;

        // do type checking since operating on untyped map
        Object value = parameters.get(parameter);
        if (value != null) {
            if (isLongType(value)) {
                return ((Number) value).longValue();

            }
            // check whether it is a string
            String str = getValue(parameters, parameter);
            if (str != null) {
                try {
                    result = Long.parseLong(str);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return result;
    }

    /**
     * Gets the parameter as long.
     *
     * @param parameters
     *            the parameters
     * @param parameter
     *            the parameter
     * @param fallback
     *            the fallback
     * @return the parameter as long
     */
    public static Long getParameterAsLong(Map<String, ? extends Object> parameters,
            String parameter, Long fallback) {
        Long result = getParameterAsLong(parameters, parameter);
        if (result == null) {
            result = fallback;
        }
        return result;
    }

    /**
     * Gets the parameter as long array.
     *
     * @param parameters
     *            the parameters
     * @param parameter
     *            the parameter name
     * @return the parameter as long array
     */
    public static Long[] getParameterAsLongArray(Map<String, ? extends Object> parameters,
            String parameter) {
        Object value = parameters.get(parameter);
        if (value != null && !(value instanceof String)) {
            if (isLongType(value)) {
                return new Long[] { new Long(((Number) value).longValue()) };
            } else if (value instanceof Number[]) {
                ArrayList<Long> result = new ArrayList<Long>();
                Number[] valueArray = (Number[]) value;
                for (Number entry : valueArray) {
                    result.add(entry.longValue());
                }
                return result.toArray(new Long[] { });
            }
        }
        return StringHelper.getStringAsLongArray(getValues(parameters, parameter));
    }

    /**
     * Gets the parameter as long array.
     *
     * @param parameters
     *            the parameter map with the values being either {@link String} or String[] (in case
     *            of ServletRequest#getParameterMap(). The values can be a single number or a number
     *            array, but the numbers must not be floats or doubles.
     * @param parameterName
     *            the parameter name
     * @param fallback
     *            the fallback
     * @return
     * @return the parameter splitted by "," as longs
     */
    public static Long[] getParameterAsLongArray(Map<String, ? extends Object> parameters,
            String parameterName, Long[] fallback) {
        Long[] result = getParameterAsLongArray(parameters, parameterName);
        if (result == null) {
            result = fallback;
        }
        return result;
    }

    /**
     * Gets the parameter as list.
     *
     * @param parameters
     *            the parameters
     * @param parameter
     *            the parameter name
     * @return the parameter as list of Long objects
     */
    public static List<Long> getParameterAsLongList(Map<String, ? extends Object> parameters,
            String parameter) {
        Object value = parameters.get(parameter);
        if (value != null && !(value instanceof String)) {
            if (isLongType(value)) {
                ArrayList<Long> result = new ArrayList<Long>();
                result.add(((Number) value).longValue());
                return result;
            } else if (value instanceof Number[]) {
                ArrayList<Long> result = new ArrayList<Long>();
                Number[] valueArray = (Number[]) value;
                for (Number entry : valueArray) {
                    result.add(entry.longValue());
                }
                return result;
            }
        }
        return StringHelper.getStringAsLongList(getValues(parameters, parameter));
    }

    /**
     * @param parameters
     *            the parameter map with the values being either {@link String} or String[] (in case
     *            of ServletRequest#getParameterMap()
     * @param parameterName
     *            the name of the parameter
     * @return the parameter as string
     */
    public static String getParameterAsString(Map<String, ? extends Object> parameters,
            String parameterName) {
        return getValue(parameters, parameterName);
    }

    /**
     * @param parameters
     *            the parameter map with the values being either {@link String} or String[] (in case
     *            of ServletRequest#getParameterMap()
     * @param parameterName
     *            the name of the parameter
     * @param fallback
     *            the fallback
     * @return the parameter as string or the fallback if the parameter is null
     */
    public static String getParameterAsString(Map<String, ? extends Object> parameters,
            String parameterName, String fallback) {
        String result = getParameterAsString(parameters, parameterName);
        if (result == null) {
            return fallback;
        }
        return result;
    }

    /**
     * @param parameters
     *            the parameter map with the values being either {@link String} or String[] (in case
     *            of ServletRequest#getParameterMap()
     * @param parameterName
     *            the name of the parameter
     * @param separator
     *            the separator by which the value will be splitted
     * @return the parameter value splitted by the separator as strings
     */
    public static String[] getParameterAsStringArray(Map<String, ? extends Object> parameters,
            String parameterName, String separator) {
        String value = getValue(parameters, parameterName);
        return StringUtils.split(value, separator);
    }

    /**
     * Get the value of the parameter and split it by the separator. Return the values as set. If
     * the parameter value is empty or not existing null is returned.
     *
     * @param parameters
     *            the parameter map with the values being either {@link String} or String[] (in case
     *            of ServletRequest#getParameterMap()
     * @param parameterName
     *            the name of the parameter
     * @param separator
     *            the separator by which the value will be splitted
     * @return the parameter value splitted by the separator as a {@link Set} of Strings (or null)
     */
    public static Set<String> getParameterAsStringSet(Map<String, ? extends Object> parameters,
            String parameterName, String separator) {
        String[] values = getParameterAsStringArray(parameters, parameterName, separator);
        Set<String> set = null;
        if (values != null && values.length > 0) {
            set = new HashSet<String>(values.length);
            CollectionUtils.addAll(set, values);
        }
        return set;
    }

    /**
     * Get the value of the parameter
     *
     * @param parameters
     *            the parameter map with the values being either {@link String} or String[] (in case
     *            of ServletRequest#getParameterMap()
     * @param key
     *            the key to get
     * @return the value of the parameter
     */
    private static String getValue(Map<String, ? extends Object> parameters, String key) {
        Object parameter = parameters.get(key);
        String result = null;
        if (parameter instanceof String) {
            result = (String) parameter;
        } else if (parameter instanceof String[]) {
            // servlet request parameter values (of getParameterMap) are string[]
            String[] sa = (String[]) parameter;
            if (sa.length > 0) {
                result = sa[0];
            }
        }
        return result;
    }

    /**
     * Get the value of the parameter
     *
     * @param parameters
     *            the parameter map with the values being either {@link String} or String[] (in case
     *            of ServletRequest#getParameterMap()
     * @param key
     *            the key to get
     * @return the value of the parameter
     */
    private static String[] getValues(Map<String, ? extends Object> parameters, String key) {
        String result = getValue(parameters, key);
        if (result != null) {
            return new String[] { result };
        }
        int i = 0;
        List<String> results = new ArrayList<String>();
        while ((result = getValue(parameters, key + "[" + i + "]")) != null) {
            results.add(result);
            i++;
        }
        return results.toArray(new String[results.size()]);
    }

    /**
     * Test whether a value is a long or a compatible whole number.
     *
     * @param value
     *            the value to test
     * @return true if it is a number that is not float or double
     */
    private static boolean isLongType(Object value) {
        return value instanceof Number && !(value instanceof Double) && !(value instanceof Float);
    }

    /**
     * Instantiates a new parameter helper. never used.
     */
    private ParameterHelper() {

    }

}
