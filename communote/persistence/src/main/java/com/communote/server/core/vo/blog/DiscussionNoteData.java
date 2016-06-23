package com.communote.server.core.vo.blog;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.tag.TagData;

/**
 * NoteData which additionally provides the comments of the note.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DiscussionNoteData extends NoteData {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    private List<NoteData> comments = new ArrayList<NoteData>();
    private String[] allTags;

    /**
     * Extracts the tags from a note list data object
     * 
     * @param noteData
     *            the object to process
     * @param tags
     *            the collection for storing the extracted tags
     */
    private void extractTags(NoteData noteData, Collection<String> tags) {
        if (noteData.getTags() != null) {
            for (TagData tag : noteData.getTags()) {
                tags.add(tag.getDefaultName());
            }
        }
    }

    /**
     * @param locale
     *            The locale to use. If this is null, English will be used.
     * @return A locale-sensitive ordered array of all tags of the note and all its comments.
     */
    // TODO saves allTags only once, first locale wins (locale only used for sorting)
    public String[] getAllTags(Locale locale) {
        locale = locale != null ? locale : Locale.ENGLISH;
        if (allTags == null) {
            Collection<String> tags = new TreeSet<String>(Collator.getInstance(locale));
            extractTags(this, tags);
            if (comments != null) {
                for (NoteData child : comments) {
                    extractTags(child, tags);
                }
            }
            allTags = tags.toArray(new String[tags.size()]);
        }
        return allTags;
    }

    /**
     * @return all comments to the note
     * @see DiscussionNoteData#setComments(List)
     */
    public List<NoteData> getComments() {
        return comments;
    }

    /**
     * @param comments
     *            the comments to this note. Depending on the use case, the passed in collection
     *            could also contain comments on comments and could be sorted for instance
     *            chronologically or in threaded view style. Comments a user is not allowed to read
     *            should not be in the list.
     */
    public void setComments(List<NoteData> comments) {
        this.comments = comments;
    }
}
