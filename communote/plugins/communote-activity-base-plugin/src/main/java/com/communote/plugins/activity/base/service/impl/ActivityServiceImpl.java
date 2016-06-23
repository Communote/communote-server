package com.communote.plugins.activity.base.service.impl;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.Pair;
import com.communote.common.util.PairComparator;
import com.communote.plugins.activity.base.data.ActivityConfiguration;
import com.communote.plugins.activity.base.data.ActivityDefinition;
import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.plugins.activity.base.service.ActivityServiceException;
import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.plugins.core.services.PluginPropertyServiceException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.template.NoteTemplateService;
import com.communote.server.model.property.StringProperty;

/**
 * Service to manage activity definitions and configurations
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @see ActivityDefinition
 * @see ActivityConfiguration
 */
@Component
@Instantiate(name = "ActivityService")
@Provides
public class ActivityServiceImpl implements ActivityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityServiceImpl.class);
    private static final String PLUGIN_PROPERTY_KEY_ACTIVITY_CONFIGS = "activity-configs";
    private static final String BLOG_PROPERTY_KEY_ACTIVE_ACTIVITIES = "active-activities";
    private static final String BLOG_PROPERTY_KEY_INACTIVE_ACTIVITIES = "inactive-activities";
    private static final boolean DEFAULT_ACTIVE_PER_BLOG = true;

    private final ConcurrentHashMap<String, ActivityDefinition> activityDefinitions =
            new ConcurrentHashMap<String, ActivityDefinition>();

    private NoteTemplateService noteTemplateService;
    private PropertyManagement propertyManagement;

    @Requires
    private PluginPropertyService propertyService;

    @Override
    public void activateDeactivateActivitiesForTopic(Long topicId,
            Map<ActivityDefinition, Boolean> settings) throws NotFoundException,
            AuthorizationException, ActivityServiceException {
        List<String> activeActivities = new ArrayList<String>();
        List<String> inactiveActivities = new ArrayList<String>();
        // get all active and disabled activities for that blog for which no activities exist
        // anymore and keep them
        extractRemovedActivitiesFromTopicActivities(topicId, true, activeActivities);
        extractRemovedActivitiesFromTopicActivities(topicId, false, inactiveActivities);
        // add new activities, but ensure that they are configurable per topic and still exist
        for (Map.Entry<ActivityDefinition, Boolean> setting : settings.entrySet()) {
            ActivityDefinition existingDefinition = activityDefinitions.get(setting.getKey()
                    .getTemplateId());
            if (existingDefinition != null && existingDefinition.isConfigurablePerTopic()) {
                // external activities cannot be deactivated
                if (Boolean.TRUE.equals(setting.getValue()) || existingDefinition.isExternal()) {
                    activeActivities.add(existingDefinition.getTemplateId());
                } else {
                    inactiveActivities.add(existingDefinition.getTemplateId());
                }
            }
        }
        HashSet<StringPropertyTO> properties = new HashSet<StringPropertyTO>();
        properties.add(convertToTopicPropterty(activeActivities, true));
        properties.add(convertToTopicPropterty(inactiveActivities, false));
        getPropertyManagement().setObjectProperties(PropertyType.BlogProperty, topicId, properties);
    }

    @Override
    public synchronized boolean addDefinition(ActivityDefinition definition) {
        if (!activityDefinitions.contains(definition.getTemplateId())) {
            if (getNoteTemplateService().addDefinition(definition)) {
                activityDefinitions.put(definition.getTemplateId(), definition);
                // note: we are not creating and storing a configuration with default values here
                // because it might result in race conditions when the activity providing plugin is
                // deployed in a clustered environment
                return true;
            }
        }
        LOGGER.debug("Ignored activity with ID {} because it already exists",
                definition.getTemplateId());
        return false;
    }

    @Override
    public void convertToActivityNote(NoteStoringTO noteTO, String templateId,
            String templatePropertiesJSON) {
        StringPropertyTO property = new StringPropertyTO();
        property.setKeyGroup(PropertyManagement.KEY_GROUP);
        property.setPropertyKey(NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_ID);
        property.setPropertyValue(templateId);
        noteTO.getProperties().add(property);
        if (templatePropertiesJSON != null) {
            property = new StringPropertyTO();
            property.setKeyGroup(PropertyManagement.KEY_GROUP);
            property.setPropertyKey(NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_PROPERTIES);
            property.setPropertyValue(templatePropertiesJSON);
            noteTO.getProperties().add(property);
        }
        property = new StringPropertyTO();
        property.setKeyGroup(PROPERTY_KEY_GROUP);
        property.setPropertyKey(NOTE_PROPERTY_KEY_ACTIVITY);
        property.setPropertyValue(NOTE_PROPERTY_VALUE_ACTIVITY);
        noteTO.getProperties().add(property);
    }

    /**
     * Convert the provided activity IDs into a topic property that holds the explicitly activated
     * or deactivated activities.
     * 
     * @param templateIds
     *            the IDs of the activities to store in the property
     * @param active
     *            whether the provided activities are activated or deactivated activities
     * @return the topic property
     * @throws ActivityServiceException
     *             in case the property value couldn't be created
     */
    private StringPropertyTO convertToTopicPropterty(List<String> templateIds, boolean active)
            throws ActivityServiceException {
        StringPropertyTO property = new StringPropertyTO();
        property.setKeyGroup(PROPERTY_KEY_GROUP);
        property.setPropertyKey(active ? BLOG_PROPERTY_KEY_ACTIVE_ACTIVITIES
                : BLOG_PROPERTY_KEY_INACTIVE_ACTIVITIES);
        String propertyValue = null;
        // pass null for empty templateIds so that the property is removed
        if (templateIds.size() > 0) {
            try {
                propertyValue = JsonHelper.getSharedObjectMapper().writeValueAsString(
                        templateIds.toArray(new String[templateIds.size()]));
            } catch (IOException e) {
                LOGGER.error(
                        "Failed to convert property " + property.getPropertyKey() + " into JSON", e);
                throw new ActivityServiceException("Failed to create topic property "
                        + property.getPropertyKey(), e);
            }
        }

        property.setPropertyValue(propertyValue);
        return property;
    }

    /**
     * Create an activity configuration for the provided definition filled with default values taken
     * from the definition.
     * 
     * @param definition
     *            the activity definition
     * @return the configuration built from the definition
     */
    private ActivityConfiguration createDefaultConfiguration(ActivityDefinition definition) {
        ActivityConfiguration configuration;
        // create from definition
        configuration = new ActivityConfiguration(definition.getTemplateId(),
                definition.getStaticName(), definition.isExternal());
        configuration.setActive(true); // Active by default
        configuration.setDeletableByUser(definition.isDeletableByUser());
        configuration.setExpirationTimeout(definition.getExpirationTimeout());
        return configuration;
    }

    /**
     * Retrieve the activities that were explicitly activated or deactivated for a given topic and
     * extract those activities for which no definition is registered.
     * 
     * @param topicId
     *            the ID of the topic to consider
     * @param active
     *            whether the activated or the deactivated activities should be retrieved
     * @param extractedActivities
     *            list for storing the IDs of the extracted activities
     * @throws NotFoundException
     *             in case the topic was not found
     * @throws AuthorizationException
     *             in case the current user is not authorized to read the settings (topic
     *             properties)
     * @throws ActivityServiceException
     *             in case the activities couldn't be in case the parsing of the stored setting
     *             failed
     */
    private void extractRemovedActivitiesFromTopicActivities(Long topicId, boolean active,
            List<String> extractedActivities) throws NotFoundException, AuthorizationException,
            ActivityServiceException {
        String[] currentTopicActivities = loadSettingsForTopic(topicId, active);
        for (String templateId : currentTopicActivities) {
            if (!activityDefinitions.contains(templateId)) {
                extractedActivities.add(templateId);
            }
        }
    }

    @Override
    public List<Pair<ActivityDefinition, ActivityConfiguration>> getActivities(Locale locale)
            throws ActivityServiceException {
        List<Pair<ActivityDefinition, ActivityConfiguration>> activities =
                new ArrayList<Pair<ActivityDefinition, ActivityConfiguration>>();
        List<ActivityConfiguration> configurations = loadConfigurations();
        HashSet<String> processedActivityDefinitions = new HashSet<String>();
        for (ActivityConfiguration configuration : configurations) {
            ActivityDefinition definition = activityDefinitions.get(configuration.getTemplateId());
            if (definition != null) {
                processedActivityDefinitions.add(definition.getTemplateId());
            }
            activities.add(new Pair<ActivityDefinition, ActivityConfiguration>(definition,
                    configuration));
        }
        // add all activity definitions which do not have a configuration yet and create a default
        // config for them
        for (ActivityDefinition definition : activityDefinitions.values()) {
            if (!processedActivityDefinitions.contains(definition.getTemplateId())) {
                ActivityConfiguration defaultConfiguration = createDefaultConfiguration(definition);
                activities.add(new Pair<ActivityDefinition, ActivityConfiguration>(definition,
                        defaultConfiguration));
            }
        }
        if (locale != null) {
            activities = sortByName(activities, locale);
        }
        return activities;
    }

    @Override
    public List<Pair<ActivityDefinition, Boolean>> getActivitySettingsForTopic(Long topicId)
            throws NotFoundException, AuthorizationException, ActivityServiceException {
        List<ActivityConfiguration> configurations = loadConfigurations();
        ArrayList<Pair<ActivityDefinition, Boolean>> result = new ArrayList<Pair<ActivityDefinition, Boolean>>();
        HashSet<String> processedActivities = new HashSet<String>();
        loadFilteredSettingsForTopic(topicId, true, result, processedActivities, configurations);
        loadFilteredSettingsForTopic(topicId, false, result, processedActivities, configurations);

        // add remaining with default value for active state
        for (ActivityDefinition definition : activityDefinitions.values()) {
            if (definition.isConfigurablePerTopic()
                    && !processedActivities.contains(definition.getTemplateId())
                    && getConfiguration(configurations, definition).isActive()) {
                result.add(new Pair<ActivityDefinition, Boolean>(definition,
                        DEFAULT_ACTIVE_PER_BLOG));
            }
        }
        // TODO sort by name
        return result;
    }

    /**
     * Get an activity configuration that is associated with a definition. If there is no stored
     * configuration yet a default one will be derived from the definition.
     * 
     * @param storedConfigurations
     *            the stored activity configurations, can be null
     * @param definition
     *            the definition for which a configuration should be returned
     * @return the stored or created configuration
     */
    private ActivityConfiguration getConfiguration(
            List<ActivityConfiguration> storedConfigurations, ActivityDefinition definition) {
        ActivityConfiguration configuration = null;
        if (storedConfigurations != null) {
            for (ActivityConfiguration storedConfig : storedConfigurations) {
                if (storedConfig.getTemplateId().equals(definition.getTemplateId())) {
                    configuration = storedConfig;
                    break;
                }
            }
        }
        if (configuration == null) {
            configuration = createDefaultConfiguration(definition);
        }
        return configuration;
    }

    /**
     * @return the lazily initialized template service
     */
    private NoteTemplateService getNoteTemplateService() {
        if (this.noteTemplateService == null) {
            this.noteTemplateService = ServiceLocator.instance().getService(
                    NoteTemplateService.class);
        }
        return this.noteTemplateService;
    }

    /**
     * @return the lazily initialized property management
     */
    private PropertyManagement getPropertyManagement() {
        if (this.propertyManagement == null) {
            this.propertyManagement = ServiceLocator.instance().getService(
                    PropertyManagement.class);
        }
        return this.propertyManagement;
    }

    /**
     * Return whether an activity that is configured as active is also activated for a given topic.
     * 
     * @param definition
     *            the activity definition
     * @param topicId
     *            the ID of the topic
     * @return true if the activity cannot be configured per topic or it was marked as active for
     *         the topic or hasn't been configured for the topic at all
     * @throws NotFoundException
     *             in case the topic was not found
     * @throws AuthorizationException
     *             in case the user is not authorized to read the topic
     * @throws ActivityServiceException
     *             in case the topic configuration couldn't be read
     */
    private boolean isActiveForTopic(ActivityDefinition definition, Long topicId
            ) throws NotFoundException, AuthorizationException, ActivityServiceException {
        if (!definition.isConfigurablePerTopic()) {
            return true;
        }
        boolean active;
        if (topicId == null) {
            // we are pessimistic here
            active = false;
        } else {
            if (ArrayUtils.contains(loadSettingsForTopic(topicId, false),
                    definition.getTemplateId())) {
                active = false;
            } else {
                if (ArrayUtils.contains(loadSettingsForTopic(topicId, true),
                        definition.getTemplateId())) {
                    active = true;
                } else {
                    active = DEFAULT_ACTIVE_PER_BLOG;
                }
            }
        }
        return active;
    }

    @Override
    public boolean isActivityActive(String templateId, Long topicId) throws AuthorizationException,
            NotFoundException, ActivityServiceException {
        boolean active = false;
        // if the definition was removed the activity is not active anymore
        ActivityDefinition definition = activityDefinitions.get(templateId);
        if (definition != null) {
            // check configuration if there is any
            ActivityConfiguration configuration = getConfiguration(
                    loadConfigurations(), definition);
            if (configuration.isActive()) {
                active = isActiveForTopic(definition, topicId);
            }
        }
        return active;
    }

    @Override
    public boolean isActivityDeletableByUser(String templateId) throws ActivityServiceException {
        List<ActivityConfiguration> configurations = loadConfigurations();
        for (ActivityConfiguration config : configurations) {
            if (config.getTemplateId().equals(templateId)) {
                return config.isDeletableByUser();
            }
        }
        // check if there's a definition which wasn't configured yet
        ActivityDefinition definition = activityDefinitions.get(templateId);
        if (definition != null) {
            return definition.isDeletableByUser();
        }
        LOGGER.debug("No configuraion for ID {} found, return default for deletable by user",
                templateId);
        return ActivityDefinition.DEFAULT_DELETABLE_BY_USER;
    }

    /**
     * Load the previously stored activity configurations from the plugin properties.
     * 
     * @return the loaded configurations. The list will be empty if no configurations have been
     *         stored yet.
     * @throws ActivityServiceException
     *             in case loading or parsing the serialized configurations failed
     */
    private List<ActivityConfiguration> loadConfigurations() throws ActivityServiceException {
        try {
            ActivityConfiguration[] configurations = propertyService.getClientPropertyAsObject(
                    PLUGIN_PROPERTY_KEY_ACTIVITY_CONFIGS, ActivityConfiguration[].class);
            if (configurations != null) {
                return Arrays.asList(configurations);
            }
            return Collections.emptyList();
        } catch (PluginPropertyServiceException e) {
            LOGGER.error("Loading the activity configrations failed", e);
            throw new ActivityServiceException("Loading the activity configrations failed", e);
        }
    }

    /**
     * Load the active or inactive activities assigned to a topic and filter the found activities so
     * that only activities with an existing definition and active configuration are retrieved.
     * Activities that are not configurable per topic will be ignored too.
     * 
     * @param topicId
     *            the ID of the topic to check
     * @param active
     *            whether to get active or inactive activities
     * @param loadedSettings
     *            container to add the loaded activities to. The right part of the pair will be the
     *            value of the active parameter.
     * @param processedActivityIds
     *            the IDs of the templates that were processed by this method
     * @param configurations
     *            the current activity configurations
     * @throws NotFoundException
     *             in case the topic does not exist
     * @throws AuthorizationException
     *             in case the current user is not allowed to read the settings
     * @throws ActivityServiceException
     *             in case the topic configuration couldn't be loaded
     */
    private void loadFilteredSettingsForTopic(Long topicId, boolean active,
            List<Pair<ActivityDefinition, Boolean>> loadedSettings,
            HashSet<String> processedActivityIds, List<ActivityConfiguration> configurations)
            throws NotFoundException, AuthorizationException, ActivityServiceException {
        String[] activeActivities = loadSettingsForTopic(topicId, active);
        for (String templateId : activeActivities) {
            ActivityDefinition definition = activityDefinitions.get(templateId);
            if (definition != null && definition.isConfigurablePerTopic()
                    && getConfiguration(configurations, definition).isActive()) {
                loadedSettings.add(new Pair<ActivityDefinition, Boolean>(definition, active));
            }
            // always add the ID to the processed IDs for consistency reasons (e.g. if an activity
            // is added after this method has finished but before calling method completed)
            processedActivityIds.add(templateId);
        }
    }

    /**
     * Retrieve the IDs (templateIds) of the activities which were activated or deactivated for the
     * provided topic.
     * 
     * @param topicId
     *            the topic whose activities should be retrieved
     * @param active
     *            whether to get the activated or the deactivated activities
     * @return an array of template IDs representing the activities that were activated or
     *         deactivated for the topic
     * @throws NotFoundException
     *             if the topic does not exist
     * @throws AuthorizationException
     *             in case the current user is not authorized to access the settings
     * @throws ActivityServiceException
     *             in case the parsing of the stored setting failed
     */
    private String[] loadSettingsForTopic(Long topicId, boolean active) throws NotFoundException,
            AuthorizationException, ActivityServiceException {
        StringProperty property = getPropertyManagement().getObjectProperty(
                PropertyType.BlogProperty, topicId,
                PROPERTY_KEY_GROUP, active ? BLOG_PROPERTY_KEY_ACTIVE_ACTIVITIES
                        : BLOG_PROPERTY_KEY_INACTIVE_ACTIVITIES);
        if (property != null) {
            try {
                String[] templateIds = JsonHelper.getSharedObjectMapper().readValue(
                        property.getPropertyValue(),
                        String[].class);
                return templateIds;
            } catch (IOException e) {
                LOGGER.error(
                        "Failed to convert JSON value of property " + property.getPropertyKey(), e);
                throw new ActivityServiceException("Failed to process topic property "
                        + property.getPropertyKey(), e);
            }
        }
        return new String[0];
    }

    @Override
    public synchronized void removeDefinition(String templateId) {
        getNoteTemplateService().removeDefinition(templateId);
        activityDefinitions.remove(templateId);
    }

    /**
     * Sort the activities alphabetically by taking the name or static name into account and by
     * internal activities
     * 
     * @param activities
     *            the activities to sort
     * @param locale
     *            the locale to use for sorting
     * @return the sorted list of activities
     */
    private List<Pair<ActivityDefinition, ActivityConfiguration>> sortByName(
            List<Pair<ActivityDefinition, ActivityConfiguration>> activities,
            Locale locale) {
        String nameToSort;
        List<Pair<String, Object>> listToSort = new ArrayList<Pair<String, Object>>();
        for (Pair<ActivityDefinition, ActivityConfiguration> activity : activities) {
            if (activity.getLeft() != null) {
                nameToSort = activity.getLeft().getTemplateName().toString(locale);
            } else {
                nameToSort = activity.getRight().getStaticName();
            }
            Pair<String, Object> pair = new Pair<String, Object>(activity.getRight()
                    .getTemplateId(), nameToSort);
            listToSort.add(pair);
        }
        PairComparator<String, Object> pairComparator = PairComparator
                .createRightSidePairComparator(Collator.getInstance(locale));
        Collections.sort(listToSort, pairComparator);

        List<Pair<ActivityDefinition, ActivityConfiguration>> sortedActivities =
                new ArrayList<Pair<ActivityDefinition, ActivityConfiguration>>();
        List<Pair<ActivityDefinition, ActivityConfiguration>> externalActivities =
                new ArrayList<Pair<ActivityDefinition, ActivityConfiguration>>();
        for (Pair<String, Object> sortedActivity : listToSort) {
            for (Pair<ActivityDefinition, ActivityConfiguration> activity : activities) {
                if (activity.getRight().getTemplateId()
                        .equals(sortedActivity.getLeft())) {
                    if (activity.getRight().isExternal()) {
                        externalActivities.add(activity);
                    } else {
                        sortedActivities.add(activity);
                    }
                    break;
                }
            }
        }
        sortedActivities.addAll(externalActivities);
        return sortedActivities;
    }

    @Override
    public void storeActivityConfigurations(List<ActivityConfiguration> configurations)
            throws ActivityServiceException {
        ActivityConfiguration[] configs = null;
        if (configurations != null && configurations.size() > 0) {
            configs = new ActivityConfiguration[configurations.size()];
            // convert to array and validate that no external activity is deactivated
            for (int i = 0; i < configurations.size(); i++) {
                ActivityConfiguration configuration = configurations.get(i);
                if (configuration.isExternal() && !configuration.isActive()) {
                    // TODO define specific InvalidActivityConfigurationException?
                    throw new ActivityServiceException("External activities cannot be deactivated",
                            null);
                }
                configs[i] = configuration;
            }
        }
        try {
            // store en-bloc instead of per templateId because we don't know the template IDs after
            // definitions were removed, additionally the FE provides the configurations that way
            propertyService.setClientPropertyAsObject(
                    PLUGIN_PROPERTY_KEY_ACTIVITY_CONFIGS,
                    configurations.toArray(configs));
        } catch (PluginPropertyServiceException e) {
            LOGGER.error("Storing the activity configrations failed", e);
            throw new ActivityServiceException("Storing the activity configrations failed", e);
        }
    }
}
