package com.communote.server.core.exception;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            Type of the exception.
 */
public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

    private final Class<T> clazz;

    /**
     * Constructor.
     * 
     * @param clazz
     *            The class this mapper works for.
     */
    public AbstractExceptionMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * @return The class this mapper works for.
     */
    @Override
    public Class<T> getExceptionClass() {
        return clazz;
    }

}
