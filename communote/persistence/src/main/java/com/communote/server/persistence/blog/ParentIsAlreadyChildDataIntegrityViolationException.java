package com.communote.server.persistence.blog;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * When this exception is thrown, the given parent is already a child of the given child.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ParentIsAlreadyChildDataIntegrityViolationException extends
        DataIntegrityViolationException {
    /**
     * Default serial version uid
     */
    private static final long serialVersionUID = 1L;
    private final Long violatingParentTopicId;
    private final Long violatingChildTopicId;

    /**
     * @param msg
     *            A message describing the error.
     * @param
     */
    public ParentIsAlreadyChildDataIntegrityViolationException(String msg,
            Long violatingParentTopicId, Long violatingChildTopicId) {
        super(msg);
        this.violatingParentTopicId = violatingParentTopicId;
        this.violatingChildTopicId = violatingChildTopicId;
    }

    /**
     * @return Id of the violating child
     */
    public Long getViolatingChildTopicId() {
        return violatingChildTopicId;
    }

    /**
     * @return Id of the violating parent.
     */
    public Long getViolatingParentTopicId() {
        return violatingParentTopicId;
    }
}
