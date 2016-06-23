package com.communote.server.web.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.properties.PropertiesUtils;
import com.communote.server.core.osgi.CommunoteBundleListener;
import com.communote.server.core.osgi.OSGiHelper;
import com.communote.server.web.commons.view.velocity.DynamicVelocityViewResolver;
import com.communote.server.web.commons.view.velocity.VelocityTemplateManager;

/**
 * This resolver handles template name to template file location mappings defined in
 * vm.tiles-mappings.properties files contained in a plugin.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class VelocityTemplateMappingResolver extends CommunoteBundleListener {

    /** Logger. */
    private final static Logger LOG = LoggerFactory
            .getLogger(VelocityTemplateMappingResolver.class);

    @Autowired
    private VelocityTemplateManager templateManager;

    private boolean bundleResourceBasePathAdded;

    @Autowired
    private DynamicVelocityViewResolver resolver;

    /**
     * Registers the templates.
     *
     * {@inheritDoc}
     */
    @Override
    protected void fireBundleStarted(Bundle bundle) {
        try {
            Enumeration<URL> resources = bundle.findEntries("META-INF",
                    "vm.tiles-mappings.properties", true);
            if (resources == null || !resources.hasMoreElements()) {
                return;
            }
            String bundleResourcePath = OSGiHelper.getBundleResourcesBasePath().getAbsolutePath();
            if (!bundleResourceBasePathAdded) {
                templateManager.addTemplateBasePath(bundleResourcePath);
                bundleResourceBasePathAdded = true;
            }
            String symbolicName = bundle.getSymbolicName();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                Properties properties = PropertiesUtils.load(resource);
                for (Entry<Object, Object> mapping : properties.entrySet()) {
                    String viewName = mapping.getKey().toString();
                    templateManager.registerTemplate(symbolicName, viewName, OSGiHelper
                            .getRelativeBundleResourceLocation(symbolicName, mapping.getValue()
                                    .toString()));
                }
                // inform the resolver about the changed templates. ugly workaround for the
                // installer problem described in resolver class
                resolver.templatesChanged();
            }
        } catch (IOException e) {
            LOG.error("Error retrieving content out of a bundle: " + bundle.getSymbolicName(), e);
        }
    }

    /**
     * Removes the templates.
     *
     * {@inheritDoc}
     */
    @Override
    protected void fireBundleStopped(Bundle bundle) {
        templateManager.unregisterTemplates(bundle.getSymbolicName());
        // inform the resolver about the changed templates. ugly workaround for the
        // installer problem described in resolver's class (DynamicVelocityViewResolver)
        resolver.templatesChanged();
    }

}
