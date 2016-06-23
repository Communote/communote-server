package com.communote.server.web.commons.view.velocity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.view.ViewToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.web.commons.view.TemplateManager;

/**
 * Template manager optimized for velocity templates.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class VelocityTemplateManager extends TemplateManager implements ServletContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(VelocityTemplateManager.class);

    private static final String FILE_PROTO = "file:";
    private final String velocityToolConfigLocation;
    private ExtendableViewToolManager toolManager;
    private final VelocityConfigurer velocityEngineConfigurer;
    private ServletContext servletContext;
    private List<String> basePaths;

    /**
     * Create a new manager
     *
     * @param engineConfig
     *            the VelocityEngine configuration that provides the engine
     * @param toolConfigLocation
     *            classpath or filesystem location of the toolbox configuration
     */
    public VelocityTemplateManager(VelocityConfigurer engineConfig, String toolConfigLocation) {
        super();
        this.velocityToolConfigLocation = toolConfigLocation;
        this.velocityEngineConfigurer = engineConfig;
        extractInitialBasePaths();
    }

    /**
     * Add the provided velocity tools to the application scope toolbox.
     *
     * @param providerId
     *            the ID of the provider of the tools. This is usually the ID of a plugin.
     * @param toolClass
     *            the tools to add
     */
    public void addApplicationScopeTools(String providerId, Class<?>... toolClass) {
        ExtendableViewToolManager toolManager = getOrBuildToolManager();
        toolManager.addApplicationScopeTools(providerId, toolClass);
        toolManager.refresh();
    }

    @Override
    protected void addCoreTemplates(Map<String, String> templateMapping) {
        // add the core velocity templates
        Properties coreViewMappings = new Properties();
        try {
            coreViewMappings.load(new FileInputStream(CommunoteRuntime.getInstance()
                    .getApplicationInformation().getApplicationRealPath()
                    + "WEB-INF/config/core.vm.tiles-mappings.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Object key : coreViewMappings.keySet()) {
            templateMapping.put(key.toString(), coreViewMappings.getProperty(key.toString()));
        }
    }

    /**
     * Add the provided velocity tools to the request scope toolbox.
     *
     * @param providerId
     *            the ID of the provider of the tools. This is usually the ID of a plugin.
     * @param toolClass
     *            the tools to add
     */
    public void addRequestScopeTools(String providerId, Class<?>... toolClass) {
        ExtendableViewToolManager toolManager = getOrBuildToolManager();
        toolManager.addRequestScopeTools(providerId, toolClass);
        toolManager.refresh();
    }

    /**
     * Add a base directory for searching templates which are registered with a relative path.
     *
     * @param basePath
     *            absolute path to a directory for searching for templates
     */
    public synchronized void addTemplateBasePath(String basePath) {
        if (!this.basePaths.contains(basePath)) {
            File basePathDir = new File(basePath);
            // spring resource loader is attached which needs the file protocol to support the URL
            basePaths.add(0, basePathDir.toURI().toString());
            String newPath = StringUtils.join(basePaths, ",");
            velocityEngineConfigurer.setResourceLoaderPath(newPath);
            toolManager = null;
            velocityEngineConfigurer.setVelocityEngine(null);
            try {
                velocityEngineConfigurer.afterPropertiesSet();
                LOGGER.info("Added new template path to velocity engine: " + basePath);
            } catch (Exception e) {
                LOGGER.error("There was an error reconfiguring the velocity engine.", e);
            }
        }
    }

    /**
     * Add the provided velocity tools to the application and request scope toolboxes. This method
     * is more efficient if tools should be added to both toolboxes than calling the specific
     * add-methods individually.
     *
     * @param providerId
     *            the ID of the provider of the tools. This is usually the ID of a plugin.
     * @param applicationTools
     *            the application scope tools to add
     * @param requestTools
     *            the request scope tools to add
     */
    public void addTools(String providerId, Collection<Class<?>> applicationTools,
            Collection<Class<?>> requestTools) {
        ExtendableViewToolManager toolManager = getOrBuildToolManager();
        if (applicationTools != null) {
            toolManager.addApplicationScopeTools(providerId,
                    applicationTools.toArray(new Class<?>[applicationTools.size()]));
        }
        if (requestTools != null) {
            toolManager.addApplicationScopeTools(providerId,
                    requestTools.toArray(new Class<?>[requestTools.size()]));
        }
        toolManager.refresh();
    }

    private synchronized ExtendableViewToolManager buildToolManger() {
        if (toolManager == null) {
            ExtendableViewToolManager newToolManager = new ExtendableViewToolManager(
                    this.servletContext, false, false);
            newToolManager.setVelocityEngine(getVelocityEngine());
            newToolManager.configure(this.velocityToolConfigLocation);
            this.toolManager = newToolManager;
        }
        return this.toolManager;
    }

    private void extractInitialBasePaths() {
        basePaths = new ArrayList<>();
        VelocityEngine velocityEngine = velocityEngineConfigurer.getVelocityEngine();
        Object originalPath = velocityEngine.getProperty("file.resource.loader.path");
        if (originalPath != null) {
            String[] splittedPathes = StringUtils.split(originalPath.toString(), ",");
            for (String path : splittedPathes) {
                if (!path.startsWith(FILE_PROTO)) {
                    path = new File(path).toURI().toString();
                }
                basePaths.add(path);
            }
        }
    }

    private ExtendableViewToolManager getOrBuildToolManager() {
        ExtendableViewToolManager manager = toolManager;
        if (manager == null) {
            manager = buildToolManger();
        }
        return manager;
    }

    public ViewToolManager getToolManager() {
        return getOrBuildToolManager();
    }

    private VelocityEngine getVelocityEngine() {
        return this.velocityEngineConfigurer.getVelocityEngine();
    }

    /**
     * Remove all tools that were added by the given provider.
     *
     * @param providerId
     *            the ID of the provider whose tools should be removed. This is usually the ID of a
     *            plugin.
     */
    public void removeTools(String providerId) {
        ExtendableViewToolManager toolManager = getOrBuildToolManager();
        toolManager.removeApplicationScopeTools(providerId);
        toolManager.removeRequestScopeTools(providerId);
        toolManager.refresh();
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

}
