package com.communote.server.core.vo.query.java.note;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.user.UserData;

/**
 * Matcher for
 * {@link com.communote.server.core.vo.query.TaggingCoreItemUTPExtension#getUserToBeNotified()}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UsersToBeNotifiedMatcher extends Matcher<NoteData> {

    private final Set<Long> userToBeNotifiedIds = new HashSet<Long>();

    /**
     * Constructor.
     * 
     * @param userToBeNotifiedIds
     *            Ids of notified users, might be null or empty, where every note matches.
     */
    public UsersToBeNotifiedMatcher(Collection<Long> userToBeNotifiedIds) {
        if (userToBeNotifiedIds != null) {
            this.userToBeNotifiedIds.addAll(userToBeNotifiedIds);
        }
    }

    /**
     * Constructor.
     * 
     * @param userToBeNotifiedIds
     *            Ids of notified users, might be null or empty, where every note matches.
     */
    public UsersToBeNotifiedMatcher(Long... userToBeNotifiedIds) {
        if (userToBeNotifiedIds != null) {
            for (Long id : userToBeNotifiedIds) {
                this.userToBeNotifiedIds.add(id);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @return True, if one of the notified users is in the given list of userToBeNotifiedIds, an
     * @@-notation was used and the note is for me or the list is empty.
     * 
     */
    @Override
    public boolean matches(NoteData entity) {
        if (userToBeNotifiedIds.isEmpty()) {
            return true; // all authors are allowed.
        }

        for (UserData notifiedUser : entity.getNotifiedUsers()) {
            if (userToBeNotifiedIds.contains(notifiedUser.getId())) {
                return true;
            }
        }
        return entity.isForMe()
                && (entity.isMentionDiscussionAuthors() || entity.isMentionTopicAuthors()
                        || entity.isMentionTopicReaders() || entity.isMentionTopicManagers());
    }
}
