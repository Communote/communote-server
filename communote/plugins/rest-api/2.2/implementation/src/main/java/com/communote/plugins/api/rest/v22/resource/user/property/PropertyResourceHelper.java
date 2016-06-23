package com.communote.plugins.api.rest.v22.resource.user.property;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.communote.server.api.core.property.StringPropertyTO;

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
    public static Set<StringPropertyTO> convertPropertyResourcesToStringProperties(
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
     * Default Constructor
     */
    private PropertyResourceHelper() {

    }

}
