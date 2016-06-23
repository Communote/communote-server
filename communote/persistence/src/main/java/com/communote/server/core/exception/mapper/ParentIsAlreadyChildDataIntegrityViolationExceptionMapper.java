package com.communote.server.core.exception.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.persistence.blog.ParentIsAlreadyChildDataIntegrityViolationException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class ParentIsAlreadyChildDataIntegrityViolationExceptionMapper implements
        ExceptionMapper<ParentIsAlreadyChildDataIntegrityViolationException> {

    @Autowired
    private BlogManagement topicManagement;

    /**
     * @return ParentIsAlreadyChildDataIntegrityViolationException.class
     */
    @Override
    public Class<ParentIsAlreadyChildDataIntegrityViolationException> getExceptionClass() {
        return ParentIsAlreadyChildDataIntegrityViolationException.class;
    }

    /**
     * Maps the given exception to a status.
     * 
     * @param exception
     *            The exception to map.
     * @return The status for the exception.
     */
    @Override
    public Status mapException(ParentIsAlreadyChildDataIntegrityViolationException exception) {
        String parentTopicName = "";
        String childTopicName = "";
        try {
            parentTopicName = topicManagement.getBlogById(
                    exception.getViolatingParentTopicId(), false).getTitle();
            childTopicName = topicManagement.getBlogById(exception.getViolatingChildTopicId(),
                    false).getTitle();
        } catch (BlogNotFoundException e) {
            // ignore
        } catch (BlogAccessException e) {
            // ignore
        }
        return new Status(
                "error.topic.structure.ParentIsAlreadyChildDataIntegrityViolationException",
                new Object[] { parentTopicName, childTopicName }, BAD_REQUEST);
    }
}
