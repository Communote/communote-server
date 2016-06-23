package com.communote.server.web.fe.portal.user.client.controller.integration;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.model.config.ExternalSystemConfiguration;

/**
 * Form for
 * {@link com.communote.server.web.fe.portal.user.client.controller.integration.IntegrationOverviewController}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IntegrationOverviewForm {

    private String selectedAuthenticationType = "default";
    private boolean allowAuthOverDbOnExternal = false;

    /**
     * maps from external system id to the number of active admins in that system.
     */
    private final Map<String, Long> numberOfAdminsForExternalSystems = new HashMap<>();

    /**
     * maps from external system id to configuration. Contains a configuration for all available
     * repos. If it not configured the config is empty.
     */
    private final Map<String, ExternalSystemConfiguration> configurationsForExternalSystems = new HashMap<>();

    /**
     * Maps the external system id to a human readable name
     */
    private final Map<String, String> configurationNamesForExternalSystems = new HashMap<>();

    /**
     * Maps the external system id to the configuration url
     */
    private final Map<String, String> configurationUrlsForExternalSystems = new HashMap<>();

    private String userServiceRepositoryMode;

    /**
     * Maps the external system id to a human readable name
     *
     * @return
     */
    public Map<String, String> getConfigurationNamesForExternalSystems() {
        return configurationNamesForExternalSystems;
    }

    public Map<String, ExternalSystemConfiguration> getConfigurationsForExternalSystems() {
        return configurationsForExternalSystems;
    }

    /**
     * get the URL pointing to the configuration site of the external system
     *
     * @param externalSystemId
     *            the ID of the external system
     * @return the URL
     */
    public String getConfigurationUrlOfExternalSystem(String externalSystemId) {
        ExternalSystemConfiguration config = configurationsForExternalSystems.get(externalSystemId);
        if (config != null) {
            return config.getConfigurationUrl();
        }
        return "";
    }

    /**
     * Maps the external system id to the configuration url
     *
     * @return
     */
    public Map<String, String> getConfigurationUrlsForExternalSystems() {
        return configurationUrlsForExternalSystems;
    }

    public Map<String, Long> getNumberOfAdminsForExternalSystems() {
        return numberOfAdminsForExternalSystems;
    }

    /**
     * @return the selectedAuthenticationType
     */
    public String getSelectedAuthenticationType() {
        return selectedAuthenticationType;
    }

    /**
     * @return the userServiceRepositoryMode
     */
    public String getUserServiceRepositoryMode() {
        return userServiceRepositoryMode;
    }

    /**
     * @return the allowAuthOverDbOnExternal
     */
    public boolean isAllowAuthOverDbOnExternal() {
        return allowAuthOverDbOnExternal;
    }

    /**
     * @param allowAuthOverDbOnExternal
     *            the allowAuthOverDbOnExternal to set
     */
    public void setAllowAuthOverDbOnExternal(boolean allowAuthOverDbOnExternal) {
        this.allowAuthOverDbOnExternal = allowAuthOverDbOnExternal;
    }

    public void setConfiguration(String externalSystemId,
            ExternalSystemConfiguration externalSystemConfiguration, String label) {
        this.configurationsForExternalSystems.put(externalSystemId, externalSystemConfiguration);
        this.configurationNamesForExternalSystems.put(externalSystemId, label);
        this.configurationUrlsForExternalSystems.put(externalSystemId,
                externalSystemConfiguration.getConfigurationUrl());
    }

    /**
     * @param externalSystemId
     * @param numberOfAdmins
     *            for external system id
     */
    public void setNumberOfAdmins(String externalSystemId, long numberOfAdmins) {
        this.numberOfAdminsForExternalSystems.put(externalSystemId, numberOfAdmins);
    }

    /**
     * @param selectedAuthenticationType
     *            the selectedAuthenticationType to set
     */
    public void setSelectedAuthenticationType(String selectedAuthenticationType) {
        this.selectedAuthenticationType = StringUtils.trim(selectedAuthenticationType);
    }

    /**
     * @param userServiceRepositoryMode
     *            the userServiceRepositoryMode to set
     */
    public void setUserServiceRepositoryMode(String userServiceRepositoryMode) {
        this.userServiceRepositoryMode = StringUtils.trim(userServiceRepositoryMode);
    }
}