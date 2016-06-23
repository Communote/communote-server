package com.communote.server.core.exception.mapper;

import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.core.user.group.GroupNotFoundException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GroupNotFoundExceptionMapper implements
        ExceptionMapper<GroupNotFoundException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<GroupNotFoundException> getExceptionClass() {
        return GroupNotFoundException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(GroupNotFoundException exception) {
        // TODO this message is not generic enough!

        Status status;
        if (exception.isOccuredDuringChangingBlogRights()) {
            status = new Status("error.blog.change.rights.failed.noEntity", null, NOT_FOUND);
        } else {
            status = new Status("error.user.group.not.found", null, NOT_FOUND);
        }

        return status;
    }

}
