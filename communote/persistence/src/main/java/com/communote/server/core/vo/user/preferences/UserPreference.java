package com.communote.server.core.vo.user.preferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.util.JsonHelper;

/**
 * Abstract class, all concrete user preferences should implement.
 * <p>
 * <b>Note:</b> Implementing class must have an empty constructor to allow instantiation while
 * runtime.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserPreference {

    private final Map<String, String> preferences = new HashMap<String, String>(getDefaults());

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserPreference.class);

    /**
     * Default constructor.
     */
    public UserPreference() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param preferences
     *            The preferences to load.
     */
    public UserPreference(Map<String, String> preferences) {
        if (preferences != null) {
            this.preferences.putAll(preferences);
        }
    }

    /**
     * Return the default values of this preference as a map. This implementation returns an empty
     * map.
     * 
     * @return Map containing default values. The return value must not be null.
     */
    protected Map<String, String> getDefaults() {
        return new HashMap<String, String>();
    }

    /**
     * @return The preferences as map.
     */
    // TODO is it intended that the returned map is editable? A caller could extend it with
    // arbitrary key/value pairs (also null values!) which will be persisted. If this is intended,
    // why is this class abstract - only because of the registration stuff?
    public Map<String, String> getPreferences() {
        return preferences;
    }

    /**
     * @param key
     *            The key.
     * @param defaultValue
     *            The default value to return.
     * @return The value as boolean (false, when empty).
     */
    public boolean getValue(String key, boolean defaultValue) {
        String value = preferences.get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * @param key
     *            The key.
     * @param defaultValue
     *            The default value to return.
     * @return The value as byte or the default value.
     */
    public byte getValue(String key, byte defaultValue) {
        String value = preferences.get(key);
        return value == null ? defaultValue : Byte.parseByte(value);
    }

    /**
     * @param key
     *            The key.
     * @param defaultValue
     *            The default value to return.
     * @return The value as double.
     */
    public double getValue(String key, double defaultValue) {
        String value = preferences.get(key);
        return value == null ? defaultValue : Double.parseDouble(value);
    }

    /**
     * @param key
     *            The key.
     * @param defaultValue
     *            The default value to return.
     * @return The value as float.
     */
    public float getValue(String key, float defaultValue) {
        String value = preferences.get(key);
        return value == null ? defaultValue : Float.parseFloat(value);
    }

    /**
     * @param key
     *            The key.
     * @param defaultValue
     *            The default value to return.
     * @return The value as int.
     */
    public int getValue(String key, int defaultValue) {
        String value = preferences.get(key);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    /**
     * @param key
     *            The key.
     * @param defaultValue
     *            The default value to return.
     * @return The value as long.
     */
    public long getValue(String key, long defaultValue) {
        String value = preferences.get(key);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    /**
     * @param key
     *            The key.
     * @param defaultValue
     *            The default value to return.
     * @return The value as short or the default value.
     */
    public short getValue(String key, short defaultValue) {
        String value = preferences.get(key);
        return value == null ? defaultValue : Short.parseShort(value);
    }

    /**
     * @param key
     *            The key.
     * @param defaultValue
     *            The default value to return.
     * @return The value as String or empty String if null.
     */
    public String getValue(String key, String defaultValue) {
        String value = preferences.get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Method to get a more complex object from a JSON string.
     * <p>
     * Note, that parsing is done on every request you do, so this might be slow on large objects.
     * Also all parsing exceptions are logged and not thrown.
     * 
     * @param key
     *            The key.
     * @param defaultValue
     *            The default value.
     * @param type
     *            The type of the object.
     * @param <T>
     *            Type of the object.
     * @return The object itself.
     */
    public <T> T getValueFromJson(String key, T defaultValue, Class<T> type) {
        String value = preferences.get(key);
        if (value != null) {
            ObjectMapper sharedObjectMapper = JsonHelper.getSharedObjectMapper();
            try {
                return sharedObjectMapper.readValue(value, type);
            } catch (JsonParseException e) {
                LOGGER.error(e.getMessage());
            } catch (JsonMappingException e) {
                LOGGER.error(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return defaultValue;
    }

    /**
     * Method to set the preferences.
     * 
     * @param preferences
     *            The preferences to set. These might overwrite existing ones.
     */
    public void setPreferences(Map<String, String> preferences) {
        if (preferences != null) {
            for (Entry<String, String> entry : preferences.entrySet()) {
                if (entry.getValue() != null) {
                    this.preferences.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Method to set the given object as value. Use this only, if the object is some kind of
     * primitive type and can easily be restored from a simple String.
     * 
     * @param key
     *            The key.
     * @param value
     *            The value to set. Use null to remove the value.
     */
    public void setValue(String key, Object value) {
        if (value == null) {
            preferences.remove(key);
        } else {
            preferences.put(key, value.toString());
        }
    }

    /**
     * Method to set a more complex object explicitly as JSON.
     * <p>
     * Note, that parsing is done on every request you do, so this might be slow on large objects.
     * Also all parsing exceptions are logged and not thrown.
     * 
     * @param key
     *            The key.
     * @param value
     *            The value to be set, if null, the key will be removed.
     */
    public void setValueToJson(String key, Object value) {
        if (value == null) {
            preferences.remove(key);
        } else {
            ObjectMapper sharedObjectMapper = JsonHelper.getSharedObjectMapper();
            try {
                preferences.put(key, sharedObjectMapper.writeValueAsString(value));
            } catch (JsonGenerationException e) {
                LOGGER.error(e.getMessage());
            } catch (JsonMappingException e) {
                LOGGER.error(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
