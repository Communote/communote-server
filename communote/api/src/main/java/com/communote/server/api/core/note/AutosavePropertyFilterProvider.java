package com.communote.server.api.core.note;

import java.util.Collection;

import com.communote.server.api.core.property.StringPropertyFilter;
import com.communote.server.api.core.property.StringPropertyTO;

/**
 * Provider to get PropertyFilters for retrieving an existing autosave. This is for example useful
 * if there should be a unique autosave for notes that have a specific note property.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface AutosavePropertyFilterProvider {

    /**
     * Get the filter for retrieving an autosave for a comment to a note.
     *
     * @param parentNoteId
     *            the ID of the note for which the comment is created
     * @param properties
     *            the properties which should be stored with the comment
     * @return the filters for getting the autosave. Can be null if no filter is needed.
     */
    Collection<StringPropertyFilter> getFiltersForComment(Long parentNoteId,
            Collection<StringPropertyTO> properties);

    /**
     * Get the filter for retrieving an autosave for a new note.
     *
     * @param properties
     *            the properties which should be stored with the note
     * @return the filters for getting the autosave. Can be null if no filter is needed.
     */
    Collection<StringPropertyFilter> getFiltersForCreate(Collection<StringPropertyTO> properties);

    /**
     * Get the filter for retrieving an autosave for editing a note.
     *
     * @param noteId
     *            the ID of the note which is updated
     * @param properties
     *            the properties which should be stored with the note
     * @return the filters for getting the autosave. Can be null if no filter is needed.
     */
    Collection<StringPropertyFilter> getFiltersForUpdate(Long noteId,
            Collection<StringPropertyTO> properties);
}
