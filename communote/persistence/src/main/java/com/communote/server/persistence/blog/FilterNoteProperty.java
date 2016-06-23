package com.communote.server.persistence.blog;

import com.communote.server.model.note.NoteProperty;

/**
 * NoteProperty used for filtering. This filter allows fetching notes which have a certain property
 * or don't have a specific property. It is also possible to fetch notes which have (or don't have)
 * properties with a given group and key but an arbitrary value. For the latter the value can be
 * omitted.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class FilterNoteProperty extends NoteProperty {

    private static final long serialVersionUID = 3090380773194495776L;

    private boolean include = true;

    /**
     * @return Whether a note only matches if it has or does not have this property. Defaults to
     *         true and thus the note needs have this property.
     */
    public boolean isInclude() {
        return include;
    }

    /**
     * Set whether to only match a note if it has or does not have this property. The value of the
     * property can be omitted to filter only for properties by their group and key. By default the
     * note needs to have the property.
     *
     * @param include
     *            true to require the note to have the property
     */
    public void setInclude(boolean include) {
        this.include = include;
    }
}
