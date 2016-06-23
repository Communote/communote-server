package com.communote.server.core.vo.query.java.note;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.follow.FollowManagement;


/**
 * A matcher, which returns true, if the current user follows the entity in one of the following
 * forms:
 * <ul>
 * <li>Topic the note was written in,</li>
 * <li>Author of the note,</li>
 * <li>Discussion of the note,</li>
 * <li>A tag of the note.</li>
 * </ul>
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FollowingMatcher extends Matcher<NoteData> {

    private final FollowManagement followManagement;

    /**
     * Constructor.
     * 
     * @param followManagement
     *            The {@link FollowManagement} to use for any check.
     */
    public FollowingMatcher(FollowManagement followManagement) {
        this.followManagement = followManagement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(NoteData entity) {
        if (followManagement.followsBlog(entity.getBlog().getId())
                || followManagement.followsUser(entity.getUser().getId())
                || followManagement.followsDiscussion(entity.getDiscussionId())) {
            return true;
        }
        if (entity.getTags() != null) {
            for (TagData tag : entity.getTags()) {
                if (followManagement.followsTag(tag.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
}
