package com.communote.server.core.vo.query.java.note;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;


/**
 * Matcher for {@link com.communote.server.core.vo.query.TimelineQueryParameters#getUserIds()}
 * and {@link com.communote.server.core.vo.query.TaggingCoreItemUTPExtension#getUserId()}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserIdsMatcher extends Matcher<NoteData> {

    private final Long[] userIds;

    /**
     * Constructor.
     * 
     * @param userIds
     *            Array of user ids, might be null or empty, where every note matches.
     */
    public UserIdsMatcher(Long... userIds) {
        this.userIds = userIds;
    }

    /**
     * {@inheritDoc}
     * 
     * @return True, if the author is in the list of user ids or no list was given (all authors are
     *         allowed).
     * 
     */
    @Override
    public boolean matches(NoteData entity) {
        if (userIds == null || userIds.length == 0) {
            return true; // all authors are allowed.
        }
        for (Long userAlias : userIds) {
            if (userAlias.equals(entity.getUser().getId())) {
                return true;
            }
        }
        return false;
    }
}
