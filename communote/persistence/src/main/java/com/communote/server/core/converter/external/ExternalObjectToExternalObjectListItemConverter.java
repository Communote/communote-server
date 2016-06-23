package com.communote.server.core.converter.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.converter.Converter;
import com.communote.common.util.Pair;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.core.filter.listitems.blog.ExternalObjectListItem;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.external.ExternalObject;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ExternalObjectToExternalObjectListItemConverter implements
Converter<Pair<Blog, ExternalObject>, ExternalObjectListItem> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ExternalObjectToExternalObjectListItemConverter.class);

    private final boolean fillObjectProperties;

    /**
     * Create a converter
     * 
     * @param fillObjectProperties
     *            whether to add the object properties to the result
     */
    public ExternalObjectToExternalObjectListItemConverter(boolean fillObjectProperties) {
        this.fillObjectProperties = fillObjectProperties;
    }

    @Override
    public ExternalObjectListItem convert(Pair<Blog, ExternalObject> source) {
        Blog topic = source.getLeft();
        ExternalObject externalObject = source.getRight();
        if (topic != null && externalObject != null) {
            ExternalObjectListItem result = new ExternalObjectListItem(externalObject.getId(),
                    externalObject.getExternalId(),
                    externalObject.getExternalSystemId(),
                    externalObject.getExternalName(),
                    topic.getId(),
                    topic.getNameIdentifier());
            if (fillObjectProperties) {
                if (!fillProperties(externalObject, result)) {
                    return null;
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Fill the properties of the target with the properties of the external object to convert.
     *
     * @param source
     *            the external object to convert
     * @param targetItem
     *            the target list item
     * @return true if the filling the properties succeeded, false otherwise
     */
    private boolean fillProperties(ExternalObject source, ExternalObjectListItem targetItem) {
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        try {
            targetItem.setObjectProperties(propertyManagement.getAllObjectProperties(
                    PropertyType.ExternalObjectProperty, source.getId()));
            return true;
        } catch (Exception e) {
            LOGGER.warn(
                    "Unexpected exception processing external object {}. Ignoring this external object. Exception: {}",
                    source.getId(), e.getMessage());
        }
        return false;
    }

}
