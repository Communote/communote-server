package com.communote.plugins;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.communote.plugins.contenttypes.ContentTypes;
import com.communote.plugins.mediaparser.RichMediaLinkRendererPreProcessor;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorManager;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;

/**
 * OSGi Activator for RichMediaLink plugin.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ContentTypeExtractorActivator implements BundleActivator {

    private RichMediaLinkRendererPreProcessor richMediaLinkRendererPreProcessor;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(BundleContext context) throws Exception {
        richMediaLinkRendererPreProcessor = new RichMediaLinkRendererPreProcessor();
        ServiceLocator.findService(NoteRenderingPreProcessorManager.class).addProcessor(
                richMediaLinkRendererPreProcessor);
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        for (ContentTypes type : ContentTypes.values()) {
            propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                    PropertyManagement.KEY_GROUP, type.getPropertyKey());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        ServiceLocator.findService(NoteRenderingPreProcessorManager.class).removeProcessor(
                richMediaLinkRendererPreProcessor);
        richMediaLinkRendererPreProcessor = null;
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        for (ContentTypes type : ContentTypes.values()) {
            propertyManagement.removeObjectPropertyFilter(PropertyType.NoteProperty,
                    PropertyManagement.KEY_GROUP, type.getPropertyKey());
        }
    }
}
