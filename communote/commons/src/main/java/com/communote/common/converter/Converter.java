package com.communote.common.converter;

/**
 * Generic converter interface to convert one type into another.
 * 
 * @param <S>
 *            the source type of the conversion
 * @param <T>
 *            the target type of the conversion
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface Converter<S, T> {

    /**
     * Converts the source object which is of type S into an object of type T
     * 
     * @param source
     *            the object to convert
     * @return the converted object
     */
    T convert(S source);
}
