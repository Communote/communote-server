package com.communote.plugins.activity.base.data;

import java.util.Locale;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.core.template.NoteTemplateDefinition;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;

/**
 * <p>
 * An ActivityDefinition is a special kind of a template definition which defines an activity or
 * activity type.
 * </p>
 * <p>
 * Activities result from actions which were conducted by a user and are visualized by a template
 * note with that user being the author. The different types of actions like creating a topic or
 * editing the permissions are modeled by distinctive activities which are instances of the activity
 * definition with a unique ID (the templateId).
 * </p>
 * <p>
 * As soon as an ActivityDefiniton is added to the
 * {@link com.communote.plugins.activity.base.service.ActivityService} activity messages for that
 * type can be created. Activity messages are template notes which have the templateId of the
 * definition and additionally have a note property that marks the note as an activity.
 * </p>
 * <p>
 * The ActivityDefinition defines the default properties of an activity and the activity messages.
 * Some of these settings can be overwritten by an {@link ActivityConfiguration} with the same
 * identifier (templateId). The definition is only available as long as it is registered to the
 * ActivityService which is usually the time the providing plugin is deployed. The configuration on
 * the other hand can be persisted.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ActivityDefinition extends NoteTemplateDefinition {

    /**
     * the default value for all definitions that do not set the deletableByUser flag
     * 
     * @see #isDeletableByUser()
     */
    public static final boolean DEFAULT_DELETABLE_BY_USER = true;

    private final boolean configurablePerTopic;
    private LocalizedMessage description;
    private String staticName;
    // 180 days
    private static final long DEFAULT_EXPIRATION_TIMEOUT = 15552000000L;
    private boolean deletableByUser = DEFAULT_DELETABLE_BY_USER;
    private boolean isDeactivatableByManager = true;
    private final boolean external;
    private boolean active;

    private boolean isDeletable = true;

    /**
     * Create a new activity template definition. This assumes the keys for the activities name and
     * template are the activities id suffixed with ".name" and ".template"
     * 
     * @param templateId
     *            an ID which uniquely identifies the activity
     * @param external
     *            whether activity messages of this type are created by external systems
     * @param configurablePerTopic
     *            whether the activity can be activated or deactivated per topic by the topic
     *            manager
     * @param isDeactivatableByManager
     *            Set to true, if this activity is deactivatable.
     */
    public ActivityDefinition(String templateId, boolean external, boolean configurablePerTopic,
            boolean isDeactivatableByManager) {
        super(templateId, new MessageKeyLocalizedMessage(templateId + ".name"),
                new MessageKeyLocalizedMessage(templateId + ".template"), 15552000000L);
        this.external = external;
        this.isDeactivatableByManager = isDeactivatableByManager;
        this.configurablePerTopic = configurablePerTopic;
    }

    /**
     * Create a new activity template definition with a default expiration timeout of 180 days.
     * 
     * @param templateId
     *            an ID which uniquely identifies the activity
     * @param templateName
     *            a localizable name of the activity
     * @param template
     *            the actual template. This will be the default template.
     * @param external
     *            whether activity messages of this type are created by external systems
     * @param configurablePerTopic
     *            whether the activity can be activated or deactivated per topic by the topic
     *            manager
     */
    public ActivityDefinition(String templateId, LocalizedMessage templateName,
            LocalizedMessage template, boolean external, boolean configurablePerTopic) {
        this(templateId, templateName, template, DEFAULT_EXPIRATION_TIMEOUT, external,
                configurablePerTopic);
    }

    /**
     * Create a new activity template definition with a default expiration timeout of 180 days.
     * 
     * @param templateId
     *            an ID which uniquely identifies the activity
     * @param templateName
     *            a localizable name of the activity
     * @param template
     *            the actual template. This will be the default template.
     * @param external
     *            whether activity messages of this type are created by external systems
     * @param configurablePerTopic
     *            whether the activity can be activated or deactivated per topic by the topic
     *            manager
     * @param isDeactivatableByManager
     *            Set to true, if this activity is deactivatable.
     */
    public ActivityDefinition(String templateId, LocalizedMessage templateName,
            LocalizedMessage template, boolean external, boolean configurablePerTopic,
            boolean isDeactivatableByManager) {
        this(templateId, templateName, template, DEFAULT_EXPIRATION_TIMEOUT, external,
                configurablePerTopic, isDeactivatableByManager);
    }

    /**
     * Create a new activity template definition.
     * 
     * @param templateId
     *            an ID which uniquely identifies the activity
     * @param templateName
     *            a localizable name of the activity
     * @param template
     *            the actual template. This will be the default template.
     * @param expirationTimeout
     *            a timeout in milliseconds after which activity messages should expire
     * @param external
     *            whether activity messages of this type are created by external systems
     * @param configurablePerTopic
     *            whether the activity can be activated or deactivated per topic by the topic
     *            manager
     */
    public ActivityDefinition(String templateId, LocalizedMessage templateName,
            LocalizedMessage template, long expirationTimeout, boolean external,
            boolean configurablePerTopic) {
        this(templateId, templateName, template, DEFAULT_EXPIRATION_TIMEOUT, external,
                configurablePerTopic, true);
    }

    /**
     * Create a new activity template definition.
     * 
     * @param templateId
     *            an ID which uniquely identifies the activity
     * @param templateName
     *            a localizable name of the activity
     * @param template
     *            the actual template. This will be the default template.
     * @param expirationTimeout
     *            a timeout in milliseconds after which activity messages should expire
     * @param external
     *            whether activity messages of this type are created by external systems
     * @param configurablePerTopic
     *            whether the activity can be activated or deactivated per topic by the topic
     *            manager
     * @param isDeactivatableByManager
     *            Set to true, if this activity is deactivatable.
     */
    public ActivityDefinition(String templateId, LocalizedMessage templateName,
            LocalizedMessage template, long expirationTimeout, boolean external,
            boolean configurablePerTopic, boolean isDeactivatableByManager) {
        super(templateId, templateName, template, expirationTimeout);
        this.external = external;
        this.isDeactivatableByManager = isDeactivatableByManager;
        this.configurablePerTopic = configurablePerTopic;
    }

    /**
     * Create a new activity template definition. This assumes the keys for the activities name and
     * template are the activities id suffixed with ".name" and ".template"
     * 
     * @param templateId
     *            an ID which uniquely identifies the activity
     * @param expirationTimeout
     *            a timeout in milliseconds after which activity messages should expire
     * @param external
     *            whether activity messages of this type are created by external systems
     * @param configurablePerTopic
     *            whether the activity can be activated or deactivated per topic by the topic
     *            manager
     * @param isDeactivatableByManager
     *            Set to true, if this activity is deactivatable.
     */
    public ActivityDefinition(String templateId, long expirationTimeout, boolean external,
            boolean configurablePerTopic, boolean isDeactivatableByManager) {
        super(templateId, new MessageKeyLocalizedMessage(templateId + ".name"),
                new MessageKeyLocalizedMessage(templateId + ".template"), expirationTimeout);
        this.external = external;
        this.isDeactivatableByManager = isDeactivatableByManager;
        this.configurablePerTopic = configurablePerTopic;
    }

    /**
     * @return the localized description of the activity, can be null
     */
    public LocalizedMessage getDescription() {
        return description;
    }

    /**
     * @return display name of the activity which should be shown when the localized name is not
     *         accessible anymore
     */

    public String getStaticName() {
        // fall back for null
        if (staticName == null) {
            staticName = getTemplateName().toString(Locale.ENGLISH);
        }
        return staticName;
    }

    /**
     * @return whether the activity should be active, defaults to false
     * @see ActivityConfiguration#isActive()
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * @return whether the activity can be activated or deactivated per topic by the topic manager
     */
    public boolean isConfigurablePerTopic() {
        return configurablePerTopic;
    }

    /**
     * Return whether the client manager should be allowed to deactivate the activity. In case the
     * activity is an external activity this method returns always false.
     * 
     * @return True (default), if this activity can be deactivated by the client manager.
     */
    public boolean isDeactivatableByManager() {
        return isDeactivatableByManager && !isExternal();
    }

    /**
     * @return True, if activities of this type can be generally be deleted. Return false, if it
     *         should'nt be possible to delete the activities.
     */
    public boolean isDeletable() {
        return isDeletable;
    }

    /**
     * @return whether authorized users are allowed to delete the messages of this activity. Whether
     *         a user is authorized or not is defined by standard note access control rules. This
     *         setting can be overridden by the activity configuration. This value defaults to
     *         {@link #DEFAULT_DELETABLE_BY_USER}
     */
    public boolean isDeletableByUser() {
        return deletableByUser;
    }

    /**
     * @return whether activity messages of this type are created by external systems. In case this
     *         method returns true an activity cannot be deactivated.
     */
    public boolean isExternal() {
        return external;
    }

    /**
     * Set whether the activity should be active. This value can be overridden in the configuration
     * and defaults to false.
     * 
     * @param active
     *            true if the activity should be active, false otherwise
     * @see ActivityConfiguration#isActive()
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Set whether the client manager should be allowed to deactivate the activity. This does not
     * apply to external activities which cannot be deactivated.
     * 
     * @param deactivatable
     *            true if the client manager should be allowed to deactivate the activity, false
     *            otherwise
     */
    public void setDeactivatableByManager(boolean deactivatable) {
        this.isDeactivatableByManager = deactivatable;
    }

    /**
     * @param isDeletable
     *            the isDeletable to set
     */
    public void setDeletable(boolean isDeletable) {
        this.isDeletable = isDeletable;
    }

    /**
     * Set whether authorized users are allowed to delete the messages of this activity. Whether a
     * user is authorized or not is defined by standard note access control rules. This setting can
     * be overridden by the activity configuration. The default value is
     * {@link #DEFAULT_DELETABLE_BY_USER}
     * 
     * @param deletableByUser
     *            true if authorized users are allowed to delete the messages of the activity, false
     *            otherwise
     */
    public void setDeletableByUser(boolean deletableByUser) {
        this.deletableByUser = deletableByUser;
    }

    /**
     * Set the description of the activity.
     * 
     * @param description
     *            the localized description of the activity
     */
    public void setDescription(LocalizedMessage description) {
        this.description = description;
    }

    /**
     * Set the display name of the activity which should be shown when the localized name is not
     * accessible anymore, for example after the definition was removed. This value defaults to the
     * name in English locale.
     * 
     * @param staticName
     *            the display name
     */
    public void setStaticName(String staticName) {
        this.staticName = staticName;
    }
}
