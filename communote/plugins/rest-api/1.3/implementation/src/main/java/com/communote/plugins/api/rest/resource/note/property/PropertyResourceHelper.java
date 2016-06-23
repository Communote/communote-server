package com.communote.plugins.api.rest.resource.note.property;

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
     * Get note properties of property resources
     * 
     * @param properties
     *            array of {@link PropertyResource}
     * @return set of {@link StringPropertyTO}
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
