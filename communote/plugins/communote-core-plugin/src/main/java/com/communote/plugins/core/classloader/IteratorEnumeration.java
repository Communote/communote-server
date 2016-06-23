package com.communote.plugins.core.classloader;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Enumerator based on an iterator
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            The type the enumerator will return
 */
public class IteratorEnumeration<T> implements Enumeration<T> {
    private final Iterator<T> iterator;

    /**
     * 
     * @param it
     *            the iterator to be used
     */
    public IteratorEnumeration(Iterator<T> it) {
        iterator = it;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMoreElements() {
        return iterator == null ? false : iterator.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T nextElement() {
        return iterator == null ? null : iterator.next();
    }

}