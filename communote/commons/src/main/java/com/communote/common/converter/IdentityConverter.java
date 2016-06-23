package com.communote.common.converter;

/**
 * Simple converter which returns the source object without conversion.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            the type of source and target
 */
public class IdentityConverter<T> implements Converter<T, T> {
    @Override
    public T convert(T source) {
        return source;
    }
}
