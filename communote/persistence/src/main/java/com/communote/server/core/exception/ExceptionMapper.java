package com.communote.server.core.exception;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <T>
 *            Type of the exception.
 */
public interface ExceptionMapper<T extends Throwable> extends ErrorCodes {
    /**
     * @return The class this mapper is for.
     */
    Class<T> getExceptionClass();

    /**
     * Maps the given exception to a status.
     * 
     * @param exception
     *            The exception to map.
     * @return The status for the exception.
     */
    Status mapException(T exception);

}
