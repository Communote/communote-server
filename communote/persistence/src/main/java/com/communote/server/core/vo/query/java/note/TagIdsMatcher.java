package com.communote.server.core.vo.query.java.note;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.tag.TagData;


/**
 * Matcher for {@link com.communote.server.core.vo.query.TimelineQueryParameters#getTagIds()}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagIdsMatcher extends Matcher<NoteData> {

    private final Set<Long> tagIds = new HashSet<Long>();

    /**
     * Constructor.
     * 
     * @param tagIds
     *            Id's of the tags, that have to match. Might be null, which means all tags will
     *            match.
     */
    public TagIdsMatcher(Collection<Long> tagIds) {
        if (tagIds != null) {
            this.tagIds.addAll(tagIds);
        }
    }

    /**
     * Constructor.
     * 
     * @param tagIds
     *            Id's of the tags, that have to match. Might be null, which means all tags will
     *            match.
     */
    public TagIdsMatcher(Long... tagIds) {
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                this.tagIds.add(tagId);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @return True, if <b>all</b> tags match or if no tags are given to match.
     * 
     */
    @Override
    public boolean matches(NoteData entity) {
        if (tagIds.isEmpty()) {
            return true; // all tags are allowed.
        }
        Set<Long> entityTagIds = new HashSet<Long>();
        for (TagData tag : entity.getTags()) {
            entityTagIds.add(tag.getId());
        }
        for (Long tagId : tagIds) {
            if (!entityTagIds.contains(tagId)) {
                return false;
            }
        }
        return true;
    }
}
