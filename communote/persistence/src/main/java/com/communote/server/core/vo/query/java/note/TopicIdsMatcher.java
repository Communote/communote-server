package com.communote.server.core.vo.query.java.note;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;


/**
 * Matcher to test for the topics.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicIdsMatcher extends Matcher<NoteData> {

    private final Long[] topicIds;

    /**
     * Constructor.
     * 
     * @param blogIds
     *            IDs of blogs into which the note should have been created. Can be null or empty to
     *            match all blogs.
     */
    public TopicIdsMatcher(Long[] blogIds) {
        this.topicIds = blogIds;
    }

    /**
     * {@inheritDoc}
     * 
     * @return True, if no topic IDs were set or the ID of the topic of the entity matches one of
     *         them.
     * 
     */
    @Override
    public boolean matches(NoteData entity) {
        if (topicIds == null || topicIds.length == 0) {
            return true;
        }
        for (Long blogId : topicIds) {
            if (blogId.equals(entity.getBlog().getId())) {
                return true;
            }
        }
        return false;
    }
}