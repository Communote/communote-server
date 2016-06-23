package com.communote.server.core.vo.blog;

import java.util.ArrayList;
import java.util.List;

import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.note.NoteData;

/**
 * Note data object to be used for autosaves.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AutosaveNoteData extends NoteData {

    /**
     * default serial version uid
     */
    private static final long serialVersionUID = 1L;
    private Long originalNoteId;

    private List<BlogData> crosspostBlogs = new ArrayList<BlogData>();;

    /**
     * @return the blogs to create crossposts in when publishing the autosave
     */
    public List<BlogData> getCrosspostBlogs() {
        return this.crosspostBlogs;
    }

    /**
     * When the autosave results from an edit operation, this method returns the ID of the note to
     * be edited. In all other cases null will be returned.
     *
     * @return the note ID of the note to be edited
     */
    public Long getOriginalNoteId() {
        return originalNoteId;
    }

    /**
     * Sets the blogs to create crossposts in when publishing the autosave
     *
     * @param crosspostBlogs
     *            the crosspost blogs
     */
    public void setCrosspostBlogs(List<BlogData> crosspostBlogs) {
        this.crosspostBlogs = crosspostBlogs;
    }

    /**
     * Sets the ID of the note that is edited by the operation that created this autosave
     *
     * @param originalNoteId
     *            the ID of the note to be edited
     */
    public void setOriginalNoteId(Long originalNoteId) {
        this.originalNoteId = originalNoteId;
    }

}
