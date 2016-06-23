package com.communote.server.web.commons.view.velocity;

import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.ServletContext;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.FactoryConfiguration;
import org.apache.velocity.tools.config.ToolboxConfiguration;
import org.apache.velocity.tools.view.ViewToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tool manager which can be extended with additional tools.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExtendableViewToolManager extends ViewToolManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendableViewToolManager.class);
    private final HashMap<String, HashSet<ClassBasedToolConfiguration>> applicationTools;
    private final HashMap<String, HashSet<ClassBasedToolConfiguration>> requestTools;
    private final ToolboxConfiguration applicationToolboxConfiguration;
    private final ToolboxConfiguration requestToolboxConfiguration;
    private FactoryConfiguration factoryConfig;
    private boolean configModified;

    public ExtendableViewToolManager(ServletContext servletContext, boolean autoConfig,
            boolean includeDefaults) {
        super(servletContext, autoConfig, includeDefaults);
        this.applicationToolboxConfiguration = new ToolboxConfiguration();
        this.applicationToolboxConfiguration.setScope(Scope.APPLICATION);
        this.requestToolboxConfiguration = new ToolboxConfiguration();
        this.requestToolboxConfiguration.setScope(Scope.REQUEST);
        applicationTools = new HashMap<>();
        requestTools = new HashMap<>();
    }

    /**
     * Add the provided application scope velocity tools to the toolbox. For the changes to become
     * effective {@link #refresh()} has to be called.
     *
     * @param providerId
     *            the ID of the provider whose tools should be added
     * @param toolClasses
     *            the velocity tools to add
     */
    public void addApplicationScopeTools(String providerId, Class<?>... toolClasses) {
        addTools(providerId, applicationTools, applicationToolboxConfiguration, toolClasses);
    }

    /**
     * Add the provided request scope velocity tools to the toolbox. For the changes to become
     * effective {@link #refresh()} has to be called.
     *
     * @param providerId
     *            the ID of the provider whose tools should be added
     * @param toolClasses
     *            the velocity tools to add
     */
    public void addRequestScopeTools(String providerId, Class<?>... toolClasses) {
        addTools(providerId, requestTools, requestToolboxConfiguration, toolClasses);
    }

    private ClassBasedToolConfiguration addToolConfig(String providerId,
            HashMap<String, HashSet<ClassBasedToolConfiguration>> scopedTools, String scopeName,
            Class<?> toolClass) {

        HashSet<ClassBasedToolConfiguration> toolsOfProvider = scopedTools.get(providerId);
        if (toolsOfProvider == null) {
            toolsOfProvider = new HashSet<>();
            scopedTools.put(providerId, toolsOfProvider);
        }
        ClassBasedToolConfiguration config = new ClassBasedToolConfiguration(toolClass);
        if (toolsOfProvider.add(config)) {
            LOGGER.debug("Added {} scope velocity tool {} of provider {}", scopeName,
                    toolClass.getName(), providerId);
            return config;
        }
        return null;
    }

    private synchronized void addTools(String providerId,
            HashMap<String, HashSet<ClassBasedToolConfiguration>> scopedTools,
            ToolboxConfiguration scopedToolboxConfig, Class<?>[] toolClasses) {
        if (toolClasses != null) {
            if (this.factoryConfig != null) {
                // if the scoped toolbox already exists we can directly add to that toolbox
                scopedToolboxConfig = this.factoryConfig.getToolbox(scopedToolboxConfig.getScope());
            }
            boolean addedSomething = false;
            for (Class<?> toolClass : toolClasses) {
                ClassBasedToolConfiguration toolConfig = addToolConfig(providerId, scopedTools,
                        scopedToolboxConfig.getScope(), toolClass);
                if (toolConfig != null) {
                    scopedToolboxConfig.addTool(toolConfig);
                    addedSomething = true;
                }
            }
            if (!this.configModified) {
                this.configModified = this.factoryConfig != null && addedSomething;
            }
        }
    }

    @Override
    public synchronized void configure(FactoryConfiguration config) {
        if (config != this.factoryConfig) {
            this.factoryConfig = config;
            // add our custom toolbox configurations to the provided toolbox config (which typically
            // contains the built-in tools). The tools of the configurations are added directly to
            // an existing toolbox with that scope. If there is no such toolbox our toolboxes are
            // added and we can later on directly add to these toolboxes.
            config.addToolbox(applicationToolboxConfiguration);
            config.addToolbox(requestToolboxConfiguration);
        }
        this.configModified = false;
        super.configure(config);
    }

    public synchronized void refresh() {
        if (this.configModified) {
            this.configure(this.factoryConfig);
        }
    }

    /**
     * Remove all application scope tools of the given provider. For the changes to become effective
     * {@link #refresh()} has to be called.
     *
     * @param providerId
     *            the ID of the provider whose tools should be removed
     */
    public void removeApplicationScopeTools(String providerId) {
        removeTools(providerId, applicationTools, applicationToolboxConfiguration);
    }

    /**
     * Remove all request scope tools of the given provider. For the changes to become effective
     * {@link #refresh()} has to be called.
     *
     * @param providerId
     *            the ID of the provider whose tools should be removed
     */
    public void removeRequestScopeTools(String providerId) {
        removeTools(providerId, requestTools, requestToolboxConfiguration);
    }

    private synchronized void removeTools(String providerId,
            HashMap<String, HashSet<ClassBasedToolConfiguration>> scopedTools,
            ToolboxConfiguration scopedToolboxConfig) {
        HashSet<ClassBasedToolConfiguration> tools = scopedTools.get(providerId);
        if (this.factoryConfig != null) {
            // if the scoped toolbox already exists we can directly remove from that toolbox
            scopedToolboxConfig = this.factoryConfig.getToolbox(scopedToolboxConfig.getScope());
        }
        boolean removedSomething = false;
        if (tools != null && tools.size() > 0) {
            for (ClassBasedToolConfiguration tool : tools) {
                scopedToolboxConfig.removeTool(tool);
            }
            scopedTools.remove(providerId);
            removedSomething = true;
            LOGGER.debug("Removed all {} scope tools of provider {}",
                    scopedToolboxConfig.getScope(), providerId);
        }
        if (!this.configModified) {
            this.configModified = this.factoryConfig != null && removedSomething;
        }
    }

    @Override
    protected void unpublishApplicationTools() {
        // parent class removes the application scope tools from the servlet context which will
        // cause problems if other threads are currently rendering template. So we directly set the
        // new application scope tools here
        if (hasApplicationTools()) {
            this.servletContext.setAttribute(getToolboxKey(), getApplicationToolbox());
        } else {
            this.servletContext.removeAttribute(getToolboxKey());
        }
    }

}
