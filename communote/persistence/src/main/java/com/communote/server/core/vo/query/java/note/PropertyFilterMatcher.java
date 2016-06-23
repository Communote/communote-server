package com.communote.server.core.vo.query.java.note;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.vo.query.filter.PropertyFilter;


/**
 * Matcher for property filters like content types.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PropertyFilterMatcher extends Matcher<NoteData> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyFilterMatcher.class);

    private Collection<PropertyFilter> filters = new HashSet<PropertyFilter>();
    private final PropertyManagement propertyManagement;

    /**
     * Constructor.
     * 
     * @param propertyManagement
     *            The PropertyManagement to use.
     * 
     * @param filters
     *            The filters to match. All filters are AND combined. Might be null, which means all
     *            filters will match.
     */
    public PropertyFilterMatcher(PropertyManagement propertyManagement,
            Collection<PropertyFilter> filters) {
        this.propertyManagement = propertyManagement;
        if (filters != null) {
            this.filters = filters;
        }
    }

    /**
     * Constructor.
     * 
     * @param propertyManagement
     *            The PropertyManagement to use.
     * @param filters
     *            The filters to match. All filters are AND combined. Might be null, which means all
     *            filters will match.
     */
    public PropertyFilterMatcher(PropertyManagement propertyManagement, PropertyFilter... filters) {
        this.propertyManagement = propertyManagement;
        if (filters != null) {
            for (PropertyFilter filter : filters) {
                this.filters.add(filter);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(NoteData entity) {
        if (filters.isEmpty()) {
            return true;
        }
        try {
            Set<StringPropertyTO> noteProperties = propertyManagement.getAllObjectProperties(
                    PropertyType.NoteProperty, entity.getId());
            // Filters are AND combined,
            // Properties within filters are OR combined.
            boolean result = true;
            outer: for (PropertyFilter filter : filters) {
                for (StringPropertyTO noteProperty : noteProperties) {
                    if (filter.matches(noteProperty.getKeyGroup(), noteProperty.getPropertyKey(),
                            noteProperty.getPropertyValue())) {
                        continue outer;
                    }
                }
                result = false;
                break outer;
            }
            return result;
        } catch (NotFoundException e) {
            LOGGER.warn("The note with id {} can't be found. It might have been deleted.",
                    entity.getId());
        } catch (AuthorizationException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
