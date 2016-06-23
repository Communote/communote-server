package com.communote.service.exception;

import com.communote.server.core.exception.AbstractExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <T>
 *            Type of the exception.
 */
public class TestExceptionMapper<T extends Throwable> extends
        AbstractExceptionMapper<T> {

    private final String errorCode;
    private final String messageKey;

    /**
     * Constructor.
     * 
     * @param clazz
     *            The clazz.
     * @param messageKey
     *            The message key.
     * @param errorCode
     *            The error code.
     */
    public TestExceptionMapper(Class<T> clazz, String messageKey, String errorCode) {
        super(clazz);
        this.messageKey = messageKey;
        this.errorCode = errorCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(T exception) {
        return new Status(messageKey, null, errorCode);
    }
}
