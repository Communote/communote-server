package com.communote.plugins.activity.base.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.communote.common.util.Pair;
import com.communote.plugins.activity.base.data.ActivityConfiguration;
import com.communote.plugins.activity.base.data.ActivityDefinition;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.security.AuthorizationException;

/**
 * Service to manage activity definitions and configurations
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @see ActivityDefinition
 * @see ActivityConfiguration
 */
public interface ActivityService {

    /**
     * Key of the note property which marks a note as an activity.
     */
    public static final String NOTE_PROPERTY_KEY_ACTIVITY = "contentTypes.activity";

    /** Key of the note property which marks an activity as not deletable. */
    public static final String NOTE_PROPERTY_KEY_ACTIVITY_UNDELETABLE = NOTE_PROPERTY_KEY_ACTIVITY
            + ".undeletable";

    /** Value of the note property which marks a note as an activity. */
    public static final String NOTE_PROPERTY_VALUE_ACTIVITY = "activity";

    /**
     * the key group to be used for properties related to activities
     */
    // set to the symbolic name of the bundle. You should be careful when changing the value as it
    // might break persisted data (note properties, plugin properties, ...)!
    public static final String PROPERTY_KEY_GROUP = "com.communote.plugins.communote-plugin-activity-base";

    /**
     * Activate and deactivate activities for a given topic. Provided activities which cannot be
     * configured per topic or whose definition was removed will be ignored. This operation replaces
     * any previously stored active or inactive activities of the given topic, with the exception of
     * those whose definition was removed.
     * 
     * @param topicId
     *            the ID of the topic for which the activities should be stored
     * @param settings
     *            a mapping from activity ID (template ID) to the active state where true means the
     *            activity should be active and false inactive
     * @throws NotFoundException
     *             if the topic does not exist
     * @throws AuthorizationException
     *             if the calling user is not manager of the topic
     * @throws ActivityServiceException
     *             in case the existing settings couldn't be read or the new settings couldn't be
     *             stored
     * 
     * @see ActivityService#isActivityActive(String, Long)
     */
    public abstract void activateDeactivateActivitiesForTopic(Long topicId,
            Map<ActivityDefinition, Boolean> settings) throws NotFoundException,
            AuthorizationException, ActivityServiceException;

    /**
     * Add an activity definition. A definition is identified by its templateID. If there is already
     * a definition with that ID the new one will be ignored.
     * 
     * @param definition
     *            the activity definition to add
     * @return whether the definition was added
     */
    public abstract boolean addDefinition(ActivityDefinition definition);

    /**
     * Add the required properties to mark the note as an activity of the provided type. This method
     * won't check whether the activity exists or is active. This will be done by a note storing
     * pre-processor.
     * 
     * @param noteTO
     *            the transfer object to enhance
     * @param templateId
     *            the ID of the activity
     * @param templatePropertiesJSON
     *            optional string in JSON which should be passed to the template engine and can
     *            contain any additional data the template might need. Can be null.
     */
    public abstract void convertToActivityNote(NoteStoringTO noteTO, String templateId,
            String templatePropertiesJSON);

    /**
     * Return pairs of associated activity definitions and configurations. For activities which were
     * not configured yet, the pair will contain a default configuration which is derived from the
     * definition. In case the definition of an activity was already removed the pair will only
     * contain the configuration and the left side will be null.
     * 
     * @param locale
     *            if not null the result will be sorted alphabetically by the name (or static name
     *            if the definition is missing) and by internal
     * @return a list of available activities
     * @throws ActivityServiceException
     *             in case the stored activity configurations couldn't be loaded
     */
    public abstract List<Pair<ActivityDefinition, ActivityConfiguration>> getActivities(
            Locale locale)
            throws ActivityServiceException;

    /**
     * Return a list of activities that can be configured per topic. The result is a list of pairs
     * where the left part is the definition and the right denotes whether the activity is active
     * for the topic. For activities that were not yet configured for the topic the right part
     * contains a default value. Activities which are deactivated by configuration will not be
     * included.
     * 
     * @param topicId
     *            the ID of the topic for which the activities should be retrieved
     * @return the activities with a flag denoting whether they are active or not
     * @throws NotFoundException
     *             in case the topic was not found
     * @throws AuthorizationException
     *             in case the current user has no read access to the topic
     * @throws ActivityServiceException
     *             in case the stored activity configurations couldn't be loaded
     */
    public abstract List<Pair<ActivityDefinition, Boolean>> getActivitySettingsForTopic(Long topicId)
            throws NotFoundException, AuthorizationException, ActivityServiceException;

    /**
     * Return whether an activity is active for a given topic. An activity is active if the
     * definition was not removed and the configuration states that the activity is active. If the
     * activity supports a configuration per topic it is considered as active if the previous
     * conditions hold and the activity was marked as active for the given topic or was not yet
     * configured for that topic at all.
     * 
     * @param templateId
     *            the ID of the activity definition
     * @param topicId
     *            the ID of the topic, can be null but the return value will be false if the
     *            activity can be configured per topic
     * @return whether an activity is active for the topic
     * @throws AuthorizationException
     *             if the user is not authorized to read the topic
     * @throws NotFoundException
     *             if the topic does not exist
     * @throws ActivityServiceException
     *             in case the stored activity configurations couldn't be loaded
     */
    public abstract boolean isActivityActive(String templateId, Long topicId)
            throws AuthorizationException,
            NotFoundException, ActivityServiceException;

    /**
     * Return whether an activity is defined or configured to be deletable by the owner of the
     * activity message or the manager of the topic. In case there is no configuration and no
     * definition it is assumed the activity existed but the definition was removed and no
     * configuration had been created. Therefore the default value defined by
     * {@link ActivityDefinition#DEFAULT_DELETABLE_BY_USER} is returned.
     * 
     * @param templateId
     *            the ID of the activity
     * @return true if the users can delete activity messages, false otherwise
     * @throws ActivityServiceException
     *             in case the configuration couldn't be loaded
     */
    public abstract boolean isActivityDeletableByUser(String templateId)
            throws ActivityServiceException;

    /**
     * Remove an activity definition. The configuration associated with the definition will not be
     * removed.
     * 
     * @param templateId
     *            the ID of the definition to remove. In case there is no definition for the ID,
     *            nothing will happen.
     */
    public abstract void removeDefinition(String templateId);

    /**
     * Stores the provided activity configurations. This replaces all previously stored
     * configurations. So if for instance the provided list does not contain some of the previously
     * stored configurations these configurations will be removed. To remove all configurations an
     * empty list can be used.
     * 
     * @param configurations
     *            the configurations to store
     * @throws ActivityServiceException
     *             in case the storing of the configurations failed or a configuration was invalid
     */
    public abstract void storeActivityConfigurations(List<ActivityConfiguration> configurations)
            throws ActivityServiceException;

}