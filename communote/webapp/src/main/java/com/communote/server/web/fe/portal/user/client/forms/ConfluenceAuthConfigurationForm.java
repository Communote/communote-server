package com.communote.server.web.fe.portal.user.client.forms;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.communote.server.model.config.ConfluenceConfiguration;

/**
 * Form for the confluence authentication.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfluenceAuthConfigurationForm {

    /** The config. */
    private ConfluenceConfiguration config;

    /** The form action. */
    private String action;

    private Boolean useConfluenceImages;

    private boolean passwordChanged = false;

    /**
     * Instantiates a new ConfluenceAuthConfigurationForm.
     * 
     * @param config
     *            the config
     */
    public ConfluenceAuthConfigurationForm(ConfluenceConfiguration config) {
        this.setConfig(config);
    }

    /**
     * Gets the action.
     * 
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Gets the config.
     * 
     * @return the config
     */
    public ConfluenceConfiguration getConfig() {
        return config;
    }

    /**
     * @return the passwordChanged
     */
    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    /**
     * Wrapper for primary flag.
     * 
     * @return whether the external configuration is the primary configuration
     */
    public boolean isPrimary() {
        return config.isPrimaryAuthentication();
    }

    /**
     * @return the useConfluenceImages
     */
    public boolean isUseConfluenceImages() {
        if (useConfluenceImages == null) {
            return StringUtils.isNotBlank(config.getImageApiUrl());
        }
        return useConfluenceImages;
    }

    /**
     * Sets the action.
     * 
     * @param action
     *            the new action
     */
    public void setAction(String action) {
        this.action = action == null ? null : action.trim();
    }

    /**
     * Sets the config.
     * 
     * @param config
     *            the config
     */
    public void setConfig(ConfluenceConfiguration config) {
        Assert.notNull(config, "ConfluenceConfiguration must be set");
        this.config = config;
    }

    /**
     * @param passwordChanged
     *            the passwordChanged to set
     */
    public void setPasswordChanged(boolean passwordChanged) {
        this.passwordChanged = passwordChanged;
    }

    /**
     * Wrapper for setting the primary flag.
     * 
     * @param primary
     *            whether the external configuration is the primary configuration
     */
    public void setPrimary(boolean primary) {
        config.setPrimaryAuthentication(primary);
    }

    /**
     * @param useConfluenceImages
     *            the useConfluenceImages to set
     */
    public void setUseConfluenceImages(boolean useConfluenceImages) {
        this.useConfluenceImages = useConfluenceImages;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return config.attributesToString();
    }
}
