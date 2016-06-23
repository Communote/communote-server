package com.communote.server.web.commons.helper;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;

import com.communote.server.persistence.blog.FilterNoteProperty;


/**
 * Helper class for handling properties.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class PropertyHelper {

    /**
     * @param request
     *            Request.
     * @return The property filters, which were defined within the request.
     */
    public static FilterNoteProperty[] getFilterNoteProperties(HttpServletRequest request) {
        String propertyFilterObject = ServletRequestUtils.getStringParameter(request,
                "properties", "");
        if (StringUtils.isBlank(propertyFilterObject)) {
            return new FilterNoteProperty[0];
        }
        List<FilterNoteProperty> filterNoteProperties = new ArrayList<FilterNoteProperty>();
        String[] filters = StringUtils.substringsBetween(propertyFilterObject, "[\"", "\"]");
        if (filters == null) {
            return new FilterNoteProperty[0];
        }
        for (String filter : filters) {
            // [1:Group, 2:Key, 3:Value, 4:include]
            String[] filterDefinition = filter.split("\",\"");
            if (filterDefinition.length != 4) {
                continue;
            }
            FilterNoteProperty filterNoteProperty = new FilterNoteProperty();
            filterNoteProperty.setKeyGroup(filterDefinition[0]);
            filterNoteProperty.setPropertyKey(filterDefinition[1]);
            filterNoteProperty.setInclude(Boolean.parseBoolean(filterDefinition[3]));
            String filterValue = filterDefinition[2];
            filterValue = StringUtils.isBlank(filterValue) ? null : filterValue;
            filterNoteProperty.setPropertyValue(filterValue);
            filterNoteProperties.add(filterNoteProperty);
        }
        return filterNoteProperties.toArray(new FilterNoteProperty[filterNoteProperties.size()]);
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private PropertyHelper() {
        // Do nothing
    }
}
