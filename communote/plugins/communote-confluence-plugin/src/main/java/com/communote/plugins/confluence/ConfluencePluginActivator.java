package com.communote.plugins.confluence;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.communote.plugins.confluence.image.ConfluenceUserImageProvider;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.core.image.type.HttpExternalUserImageProvider;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfluencePluginActivator implements BundleActivator {

    private static final String DEFAULT_IMAGE_PATH = "/com/communote/images/default_userimage_large.jpg";
    private ConfluenceUserImageProvider imageProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(BundleContext context) throws Exception {
        String symbolicName = context.getBundle().getSymbolicName();
        if (imageProvider == null) {
            imageProvider = new ConfluenceUserImageProvider(symbolicName, DEFAULT_IMAGE_PATH);
        }
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        propertyManagement.addObjectPropertyFilter(PropertyType.UserProperty, symbolicName,
                HttpExternalUserImageProvider.USER_PROPERTY_KEY_IMAGE_LAST_MODIFIED);
        propertyManagement.addObjectPropertyFilter(PropertyType.UserProperty, symbolicName,
                HttpExternalUserImageProvider.USER_PROPERTY_KEY_IMAGE_VERSION_STRING);
        ServiceLocator.findService(ImageManager.class).registerImageProvider(
                UserImageDescriptor.IMAGE_TYPE_NAME, imageProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.imageProvider != null) {
            ServiceLocator.findService(ImageManager.class).unregisterImageProvider(
                    UserImageDescriptor.IMAGE_TYPE_NAME, imageProvider);
            imageProvider.cleanup();
        }
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        String symbolicName = context.getBundle().getSymbolicName();
        propertyManagement.removeObjectPropertyFilter(PropertyType.UserProperty, symbolicName,
                HttpExternalUserImageProvider.USER_PROPERTY_KEY_IMAGE_LAST_MODIFIED);
        propertyManagement.removeObjectPropertyFilter(PropertyType.UserProperty, symbolicName,
                HttpExternalUserImageProvider.USER_PROPERTY_KEY_IMAGE_VERSION_STRING);
    }
}
