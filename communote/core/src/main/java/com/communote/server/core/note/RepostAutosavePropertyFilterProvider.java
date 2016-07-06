package com.communote.server.core.note;

import java.util.ArrayList;
import java.util.Collection;

import com.communote.server.api.core.note.AutosavePropertyFilterProvider;
import com.communote.server.api.core.property.PropertyHelper;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyFilter;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.blog.notes.processors.RepostNoteStoringPreProcessor;

/**
 * Get the filters to get the autosave of a repost.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class RepostAutosavePropertyFilterProvider implements AutosavePropertyFilterProvider {

    @Override
    public Collection<StringPropertyFilter> getFiltersForComment(Long parentNoteId,
            Collection<StringPropertyTO> properties) {
        // nothing to do for comments
        return null;
    }

    @Override
    public Collection<StringPropertyFilter> getFiltersForCreate(
            Collection<StringPropertyTO> properties) {
        StringPropertyTO repostProperty = PropertyHelper.getPropertyTO(properties,
                PropertyManagement.KEY_GROUP, RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID);
        StringPropertyFilter filter = new StringPropertyFilter();
        filter.setKeyGroup(PropertyManagement.KEY_GROUP);
        filter.setPropertyKey(RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID);
        if (repostProperty == null) {
            // ensure that a repost autosave is not retrieved
            filter.setInclude(false);
        } else {
            filter.setPropertyValue(repostProperty.getPropertyValue());
        }
        ArrayList<StringPropertyFilter> filters = new ArrayList<>();
        filters.add(filter);
        return filters;
    }

    @Override
    public Collection<StringPropertyFilter> getFiltersForUpdate(Long noteId,
            Collection<StringPropertyTO> properties) {
        // nothing to do for comments
        return null;
    }

}
