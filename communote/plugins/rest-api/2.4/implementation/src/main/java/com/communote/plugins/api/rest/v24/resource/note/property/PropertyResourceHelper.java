package com.communote.plugins.api.rest.v24.resource.note.property;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class PropertyResourceHelper {

    /**
     * Get set of string properties of property resources
     * 
     * @param properties
     *            array of {@link PropertyResource}
     * @return set of {@link NotePropertyTO}
     */
    public static Set<StringPropertyTO> convertPropertyResourcesToStringPropertyTOs(
            PropertyResource[] properties) {
        Set<StringPropertyTO> stringPropertyTOs = new HashSet<StringPropertyTO>();
        if (properties != null) {
            for (PropertyResource propertyResource : properties) {
                StringPropertyTO stringPropertyTO = new StringPropertyTO();
                stringPropertyTO.setKeyGroup(propertyResource.getKeyGroup());
                stringPropertyTO.setPropertyKey(propertyResource.getKey());
                stringPropertyTO.setPropertyValue(propertyResource.getValue());
                stringPropertyTO.setLastModificationDate(new Date());
                stringPropertyTOs.add(stringPropertyTO);
            }
        }
        return stringPropertyTOs;
    }

    /**
     * Converts the property TOs into an array of property resource objects
     * 
     * @param properties
     *            collection of TOs to convert
     * @return an array that contains the converted resources or null if the input was null
     */
    public static PropertyResource[] convertToPropertyResources(
            Collection<StringPropertyTO> properties) {
        if (properties == null) {
            return null;
        }
        PropertyResource[] propertyResources = new PropertyResource[properties.size()];
        int i = 0;
        for (StringPropertyTO property : properties) {
            propertyResources[i] = new PropertyResource();
            propertyResources[i].setKeyGroup(property.getKeyGroup());
            propertyResources[i].setKey(property.getPropertyKey());
            propertyResources[i].setValue(property.getPropertyValue());
            i++;
        }
        return propertyResources;
    }

    /**
     * Set the properties of an note.
     * 
     * @param noteId
     *            identifier of note
     * @param properties
     *            array of {@link PropertyResource}
     * @throws NotFoundException
     *             can not found property
     * @throws AuthorizationException
     *             user is not allowed to access property
     */
    public static void setProperties(Long noteId, PropertyResource[] properties)
            throws NotFoundException, AuthorizationException {
        ServiceLocator
                .instance()
                .getService(PropertyManagement.class)
                .setObjectProperties(PropertyType.NoteProperty, noteId,
                        convertPropertyResourcesToStringPropertyTOs(properties));
    }

    /**
     * Default Constructor
     */
    private PropertyResourceHelper() {

    }

}
