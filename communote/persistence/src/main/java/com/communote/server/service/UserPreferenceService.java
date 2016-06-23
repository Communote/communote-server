package com.communote.server.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.user.preferences.UserInterfaceUserPreference;
import com.communote.server.core.vo.user.preferences.UserPreference;
import com.communote.server.model.property.StringProperty;

/**
 * Service for providing user preferences.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class UserPreferenceService {

    private static final String JSON_EMPTY_OBJECT = "{}";

    private final static String KEY_PREFIX = "user.preference.";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserPreferenceService.class);

    @Autowired
    private PropertyManagement propertyManagement;

    private final Map<String, Class<? extends UserPreference>> registeredProperties =
            new HashMap<String, Class<? extends UserPreference>>();
    // short names for the built-in core preferences
    private final Map<String, String> shortNames = new HashMap<String, String>();

    /**
     * Get default value of a preference type as a JSON
     * 
     * @param preferencesType
     *            the type whose default is searched
     * @return the default preferences as JSON
     */
    private <T extends UserPreference> String getDefaultPreferenceAsJson(Class<T> preferencesType) {
        // TODO is there a way to cache the JSON of the defaults? Pretty difficult since defaults
        // might change like in UIPrefs where they are retrieved from editable client properties.
        try {
            T preference = preferencesType.newInstance();
            if (preference.getPreferences().isEmpty()) {
                return JSON_EMPTY_OBJECT;
            }
            return JsonHelper.getSharedObjectMapper()
                    .writeValueAsString(preference.getPreferences());
        } catch (InstantiationException e) {
            LOGGER.error("Unexpected error getting default preference values", e);
        } catch (IllegalAccessException e) {
            LOGGER.error("Unexpected error getting default preference values", e);
        } catch (IOException e) {
            LOGGER.error("Unexpected error getting default preference values", e);
        }
        return JSON_EMPTY_OBJECT;
    }

    /**
     * Method to load the given preferences for the current user.
     * 
     * @param preferencesType
     *            Type of the preferences to load.
     * @param <T>
     *            Concrete type of the user preference.
     * @return The preferences. This will never return null.
     * @throws AuthorizationException
     *             Thrown, when the current user does not have access to the given preferences.
     */
    public <T extends UserPreference> T getPreferences(Class<T> preferencesType)
            throws AuthorizationException {
        try {
            T preferences = preferencesType.newInstance();
            Long currentUserId = SecurityHelper.assertCurrentUserId();
            String preferencesAsString = getPreferencesAsString(currentUserId, preferencesType,
                    null);
            if (preferencesAsString != null) {
                HashMap<String, String> preferencesAsMap = JsonHelper.getSharedObjectMapper()
                        .readValue(preferencesAsString,
                                new TypeReference<HashMap<String, String>>() {
                                });
                preferences.setPreferences(preferencesAsMap);
            }
            return preferences;
        } catch (JsonParseException e) {
            throw new RuntimeException(e); // This may not happen.
        } catch (JsonMappingException e) {
            throw new RuntimeException(e); // This may not happen.
        } catch (IOException e) {
            throw new RuntimeException(e); // This may not happen.
        } catch (InstantiationException e) {
            throw new RuntimeException(e); // This may not happen.
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // This may not happen.
        }
    }

    /**
     * Method to load the given preferences for the current user.
     * 
     * @param className
     *            Full qualified class name of the preferences.
     * @return The preferences. This will never return null.
     * @throws AuthorizationException
     *             Thrown, when the current user does not have access to the given preferences.
     */
    public UserPreference getPreferences(String className) throws AuthorizationException {
        return getPreferences(registeredProperties.get(className));
    }

    /**
     * @return all stored preferences of the current user as JSON object. The JSON object will be
     *         empty if there are no preferences
     */
    public String getPreferencesAsJson() {
        Long currentUserId = SecurityHelper.assertCurrentUserId();
        // for performance reasons build the JSON by string concatenation
        StringBuilder jsonResult = new StringBuilder("{");
        for (Entry<String, Class<? extends UserPreference>> property : registeredProperties
                .entrySet()) {
            try {
                String value = getPreferencesAsString(currentUserId, property.getValue(),
                        getDefaultPreferenceAsJson(property.getValue()));
                jsonResult.append("\"");
                if (shortNames.containsKey(property.getKey())) {
                    jsonResult.append(shortNames.get(property.getKey()));
                } else {
                    jsonResult.append(property.getKey());
                }
                jsonResult.append("\":");
                jsonResult.append(value);
                jsonResult.append(",");
            } catch (AuthorizationException e) {
                // ignore this preference
                LOGGER.debug("Ignoring preferences for type {}", property.getKey());
            }
        }
        if (jsonResult.length() > 1) {
            jsonResult.setCharAt(jsonResult.length() - 1, '}');
        } else {
            jsonResult.append('}');
        }
        return jsonResult.toString();
    }

    /**
     * Method to load the given preferences for the current user.
     * 
     * @param preferencesType
     *            Type of the preferences to load.
     * @return The preferences in JSON format.
     * @throws AuthorizationException
     *             Thrown, when the current user does not have access to the given preferences.
     * @param <T>
     *            Concrete type of the user preference.
     */
    public <T extends UserPreference> String getPreferencesAsJson(Class<T> preferencesType)
            throws AuthorizationException {
        Long currentUserId = SecurityHelper.assertCurrentUserId();
        if (preferencesType != null) {
            return getPreferencesAsString(currentUserId, preferencesType,
                    getDefaultPreferenceAsJson(preferencesType));
        }
        return JSON_EMPTY_OBJECT;
    }

    /**
     * Method to load the given preferences for the current user.
     * 
     * @param className
     *            Full qualified class name of the preferences.
     * @return The preferences in JSON format.
     * @throws AuthorizationException
     *             Thrown, when the current user does not have access to the given preferences.
     * @param <T>
     *            Concrete type of the user preference.
     */
    public <T extends UserPreference> String getPreferencesAsJson(String className)
            throws AuthorizationException {
        return getPreferencesAsJson(registeredProperties.get(className));
    }

    /**
     * Get the stored value of a given preference type for the current user
     * 
     * @param currentUserId
     *            the ID of the current user, must not be null
     * @param preferencesType
     *            the preference type
     * @param fallbackValue
     *            the fallback value to return, if the use has no stored preferences for this type.
     *            Must be a legal JSON string or null.
     * @return the stored value of the preference type, which is a JSON string, or fallback if the
     *         did not store this preference yet
     * @throws AuthorizationException
     *             Thrown, when the current user does not have access to the given preferences.
     */
    private <T extends UserPreference> String getPreferencesAsString(Long currentUserId,
            Class<T> preferencesType, String fallbackValue) throws AuthorizationException {
        try {
            StringProperty preferencesProperty = propertyManagement.getObjectProperty(
                    PropertyType.UserProperty, currentUserId, PropertyManagement.KEY_GROUP,
                    KEY_PREFIX + preferencesType.getName());
            if (preferencesProperty != null) {
                return preferencesProperty.getPropertyValue();
            }
        } catch (NotFoundException e) {
            // shouldn't occur since the currentUser exists
            LOGGER.debug("Unexpected exception getting UserProperty of current user", e);
        }
        return fallbackValue;
    }

    /**
     * Initializer method.
     */
    @PostConstruct
    public void initialize() {
        register(UserInterfaceUserPreference.class);
        shortNames.put(UserInterfaceUserPreference.class.getName(), "uiPreferences");
    }

    /**
     * This method can be used to merge the given properties with already existing data.
     * 
     * @param className
     *            Full qualified class name of the preferences.
     * @param preferences
     *            The preferences to merge.
     * @throws NotFoundException
     *             Thrown, when there is not type for the given class name registered.
     * @throws AuthorizationException
     *             Thrown, when the current user doesn't have access to the preferences.
     */
    public void mergePreferences(String className, Map<String, String> preferences)
            throws NotFoundException, AuthorizationException {
        Class<? extends UserPreference> preferencesType = registeredProperties.get(className);
        if (preferencesType == null) {
            throw new NotFoundException("No registered preferences found for class '"
                    + className + "'");
        }
        UserPreference preferencesObject = getPreferences(preferencesType);
        preferencesObject.setPreferences(preferences);
        storePreferences(preferencesObject);
    }

    /**
     * Register a new preference type to allow storing and loading of preferences of this type.
     * 
     * @param preferencesType
     *            The type to register.
     * @param <T>
     *            Concrete type of the user preference.
     */
    // TODO when the number of preferences increases we might need a way to decide whether a
    // preference should be exposed to the FE or not. goal: reduce the number of data that needs to
    // be transfered to the client
    // TODO should allow to pass a friendly/short name on registration, so it is easier reference
    // the preferences via API or in the JSON object as returned by getPreferencesAsJson()
    // TODO not thread safe
    public <T extends UserPreference> void register(Class<T> preferencesType) {
        String typeName = preferencesType.getName();
        propertyManagement.addObjectPropertyFilter(PropertyType.UserProperty,
                PropertyManagement.KEY_GROUP, KEY_PREFIX + typeName);
        registeredProperties.put(typeName, preferencesType);
        LOGGER.debug("Added UserPreference type {}", typeName);
    }

    /**
     * Method to remove all values for the given preferences type from the current user.
     * 
     * @param preferencesType
     *            The preference type.
     * @param <T>
     *            Concrete type of the user preference.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access the given preference.
     */
    public <T extends UserPreference> void removePreferences(Class<T> preferencesType)
            throws AuthorizationException {
        Long currentUserId = SecurityHelper.assertCurrentUserId();
        try {
            propertyManagement.setObjectProperty(
                    PropertyType.UserProperty, currentUserId, PropertyManagement.KEY_GROUP,
                    KEY_PREFIX + preferencesType.getName(), null);
        } catch (NotFoundException e) {
            LOGGER.debug("Preferences not found within db for type {} and user {}",
                    preferencesType.getName(), currentUserId);
        }
    }

    /**
     * Persists the given preferences.
     * 
     * @param preferences
     *            The preferences to persist.
     */
    public void storePreferences(UserPreference preferences) {
        Long currentUserId = SecurityHelper.assertCurrentUserId();
        try {
            String valueAsString = JsonHelper.getSharedObjectMapper()
                    .writeValueAsString(preferences.getPreferences());
            propertyManagement.setObjectProperty(PropertyType.UserProperty,
                    currentUserId, PropertyManagement.KEY_GROUP, KEY_PREFIX
                            + preferences.getClass().getName(), valueAsString);
        } catch (JsonGenerationException e) {
            throw new RuntimeException(e); // This may not happen.
        } catch (JsonMappingException e) {
            throw new RuntimeException(e); // This may not happen.
        } catch (NotFoundException e) {
            throw new RuntimeException(e); // This may not happen.
        } catch (AuthorizationException e) {
            throw new RuntimeException(e); // This may not happen.
        } catch (IOException e) {
            throw new RuntimeException(e); // This may not happen.
        }
    }

    /**
     * Method to unregister the type. This should be explicitly called, when a type shall be
     * removed.
     * 
     * @param preferencesType
     *            The type to remove.
     * @param <T>
     *            Concrete type of the user preference.
     */
    // TODO not thread safe
    public <T extends UserPreference> void unregister(Class<T> preferencesType) {
        String typeName = preferencesType.getName();
        propertyManagement.removeObjectPropertyFilter(PropertyType.UserProperty,
                PropertyManagement.KEY_GROUP, KEY_PREFIX + typeName);
        registeredProperties.remove(typeName);
        LOGGER.debug("Removed UserPreference type {}", typeName);
    }
}
