package com.communote.server.core.blog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.util.PageableList;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;

/**
 * Management for topics related to notification specifics.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class TopicInformationManagement {

    @Autowired
    private QueryManagement queryManagement;

    /**
     * Method to check if a specific user is an author within a given topic and optional discussion.
     * 
     * @param userId
     *            Id of the user to check.
     * @param topicId
     *            If of the topic to check.
     * @param discussionId
     *            If set, the check will also check the given discussion, can be null.
     * @return True, if the given user has at least written one message within the topic.
     */
    public boolean isAuthor(long userId, long topicId, Long discussionId) {
        NoteQueryParameters parameters = new NoteQueryParameters();
        parameters.setTypeSpecificExtension(new TaggingCoreItemUTPExtension());
        parameters.getTypeSpecificExtension().setBlogId(topicId);
        parameters.setUserIds(new Long[] { userId });
        if (discussionId != null) {
            parameters.setDiscussionId(discussionId);
        }
        parameters.setResultSpecification(new ResultSpecification(0, 1));
        PageableList<SimpleNoteListItem> notes = queryManagement.query(new NoteQuery(), parameters);
        return notes.size() > 0;
    }
}
