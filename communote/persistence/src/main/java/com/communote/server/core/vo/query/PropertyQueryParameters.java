package com.communote.server.core.vo.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.communote.server.core.vo.query.filter.PropertyFilter;

/**
 * Abstract Query Parameter holding Property Filters
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public abstract class PropertyQueryParameters extends QueryParameters {

    private final Collection<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();

    /**
     * Adds a new property filter.
     * 
     * @param filter
     *            The filter.
     */
    public void addPropertyFilter(PropertyFilter filter) {
        this.propertyFilters.add(filter);
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = super.getParameters();

        for (PropertyFilter propertyFilter : propertyFilters) {
            parameters.putAll(propertyFilter.getNamedQueryNamesToValuesMap());
        }

        return parameters;
    }

    /**
     * @return The property filters.
     */
    public Collection<PropertyFilter> getPropertyFilters() {
        return propertyFilters;
    }
}
