package com.communote.plugins.activity.base.data;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Object holding the current configuration of an activity. The configuration is directly connected
 * to an {@link ActivityDefinition} by the templateId. While an ActivityDefinition holds the default
 * settings of an activity and only exists as long the providing plugin is deployed, a configuration
 * can be used to overwrite these defaults and can be persisted to allow automatic expiration even
 * after the providing plugin has been removed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ActivityConfiguration implements Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -4708275023965508286L;
    private long currentExpirationTimeout;
    private final String staticName;
    private boolean active;
    private boolean deletableByUser;
    private boolean deletable;
    private boolean isDeactivatableByManager = true;
    private final String templateId;
    private final boolean external;

    /**
     * Create a new configuration for an activity definition
     * 
     * @param templateId
     *            the ID which uniquely identifies the associated activity definition
     * @param staticName
     *            the unlocalized display name of the associated activity definition
     * @param external
     *            whether activity messages of this type are created by external systems
     */
    @JsonCreator
    public ActivityConfiguration(@JsonProperty("templateId") String templateId,
            @JsonProperty("staticName") String staticName,
            @JsonProperty("external") boolean external) {
        this.staticName = staticName;
        this.templateId = templateId;
        this.external = external;
    }

    /**
     * @return a timeout in milliseconds after which an activity should expire automatically.
     *         Expiration for activities means that the activity messages, which are the notes with
     *         a templateId of the activity definition, should be deleted automatically if they are
     *         older than the offset. A return value of zero or less means that the activity won't
     *         expire.
     */
    public long getExpirationTimeout() {
        return currentExpirationTimeout;
    }

    /**
     * @return the display name of the activity which is directly derived from the associated
     *         activity definition
     */
    public String getStaticName() {
        return staticName;
    }

    /**
     * @return the template ID which uniquely identifies the activity. This ID is directly derived
     *         from the activity definition this configuration belongs to.
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * @return whether the activity represented by this configuration is active. When an activity is
     *         not active new activity messages should not be created. In case true is returned the
     *         activity can still be disabled if it is configurable per topic and the topic manager
     *         disabled it.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return whether the client manager is allowed to deactivate the activity.
     */
    public boolean isDeactivatableByManager() {
        return isDeactivatableByManager;
    }

    /**
     * @see ActivityDefinition#setDeletable()
     * @return the deletable
     */
    public boolean isDeletable() {
        return deletable;
    }

    /**
     * @return whether users can delete the messages of the activity. In case this method returns
     *         true an activity message will only be deletable if the user has the appropriate
     *         rights, as he would have to have for any other note.
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
     * Activate or deactivate the activity.
     * 
     * @param active
     *            true to activate the activity, false to deactivate it
     * @see #isActive()
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Whether the client manager should be allowed to deactivate the activity.
     * 
     * @param deactivatable
     *            true if the client manager should be allowed to deactivate the activity, false
     *            otherwise
     */
    public void setDeactivatableByManager(boolean deactivatable) {
        this.isDeactivatableByManager = deactivatable;
    }

    /**
     * @see ActivityDefinition#setDeletable()
     * @param deletable
     *            the deletable to set
     */
    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    /**
     * Set whether authorized users are allowed to delete the messages of the activity. Whether a
     * user is authorized or not is defined by standard note access control rules.
     * 
     * @param deletableByUser
     *            true if authorized users can delete activity messages, false otherwise
     */
    public void setDeletableByUser(boolean deletableByUser) {
        this.deletableByUser = deletableByUser;
    }

    /**
     * Set the expiration timeout for the activity.
     * 
     * @param timeoutInMillis
     *            the timeout in milliseconds after which an activity should expire automatically. A
     *            value of zero or less means that the activity won't expire.
     * @see #getCurrentExpirationTimeout()
     */
    public void setExpirationTimeout(long timeoutInMillis) {
        this.currentExpirationTimeout = timeoutInMillis;
    }

}
