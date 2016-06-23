package com.communote.server.core.vo.query;

import java.util.Collection;

import com.communote.server.core.vo.query.filter.PropertyFilter;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class PropertyQuery<R, I extends PropertyQueryParameters> extends Query<R, I> {

    /**
     * @param instance
     *            The instance.
     * @param whereQuery
     *            The where query.
     * @param wherePrefix
     *            The current prefix.
     * @param entityProperty
     *            The entity alias in the HQL Query that has the properties to filter for
     * @return The new prefix.
     */
    protected String renderPropertyFilters(I instance, StringBuilder whereQuery,
            String wherePrefix,
            String entityProperty) {
        Collection<PropertyFilter> propertyFilters = instance.getPropertyFilters();
        for (PropertyFilter propertyFilter : propertyFilters) {
            if (propertyFilter.hasProperties()) {
                whereQuery.append(wherePrefix + propertyFilter.toQueryString(entityProperty));
                wherePrefix = AND;
            }
        }
        return wherePrefix;
    }
}
