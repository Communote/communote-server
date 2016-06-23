package com.communote.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A list wrapper with additional attributes which state whether there are more elements if the list
 * represents subset of a larger data-set. This is for instance useful if the list holds only a
 * range of the results of a database query.
 *
 * @param <E>
 *            The type of the list
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PageableList<E> implements List<E> {

    private static final PageableList EMPTY_LIST = new PageableList(Collections.emptyList());

    /**
     * Get an empty, unmodifiable empty list
     *
     * @param <T>
     *            the type (does not matter since list is empty)
     * @return the empty list
     */
    public static <T> PageableList<T> emptyList() {
        return EMPTY_LIST;
    }

    private int offset;
    private int minNumberOfElements;

    private final List<E> list;

    /**
     * Create a list wrapping the given list
     *
     * @param myCollection
     *            the actual list to wrap
     */
    public PageableList(Collection<E> myCollection) {
        this.list = new ArrayList<E>(myCollection);
    }

    /**
     * Create a list wrapping the given list
     *
     * @param myList
     *            the actual list to wrap
     */
    public PageableList(List<E> myList) {
        this.list = myList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(E e) {
        return list.add(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int index, E element) {
        list.add(index, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return list.addAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return list.addAll(index, c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        list.clear();
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    /**
     * create a new empty list of the same type
     *
     * @param typeClass
     *            the class of the resulting class
     * @param <T>
     *            type of the resulting class
     * @return an empty list of the same type
     */
    protected <T extends Object> PageableList<T> createEmptyList(Class<T> typeClass) {
        return new PageableList<T>(new ArrayList<T>());
    }

    /**
     * @param typeClass
     *            the class of the resulting class
     * @param <T>
     *            type of the resulting class
     * @return list with the meta data set as this
     */
    public <T extends Object> PageableList<T> createEmptyListWithMetaData(Class<T> typeClass) {
        PageableList<T> copy = createEmptyList(typeClass);
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(int index) {
        return list.get(index);
    }

    /**
     * @return the number of additional elements which are at least contained in the full result.
     *         This number is the difference of {@link #getMinNumberOfElements()}, the actual size
     *         of this result range ({@link #size()}) and of course the offset of the range (
     *         {@link #getOffset()}).
     */
    public int getMinNumberOfAdditionalElements() {
        return minNumberOfElements - offset - list.size();
    }

    /**
     * @return the number of elements which are at least contained in the full result.
     * @see PageableList#setMinNumberOfElements(int)
     */
    public int getMinNumberOfElements() {
        return minNumberOfElements;
    }

    /**
     * @return the offset of this result set range within all results
     */
    public int getOffset() {
        return offset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E remove(int index) {
        return list.remove(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E set(int index, E element) {
        return list.set(index, element);
    }

    /**
     * @param minNumberOfElements
     *            the number of elements which are at least contained in the full result. This does
     *            not have to be the overall size. So for instance when doing a query one can define
     *            to retrieve only the first n matches but also to check if there are m additional
     *            hits. When the query completes you would add the n matches to the list and set
     *            this value to n + the actual number of additional results found. This number could
     *            be m or a smaller value. In the latter case minNumberOfElements would hold the
     *            overall size.
     */
    public void setMinNumberOfElements(int minNumberOfElements) {
        this.minNumberOfElements = minNumberOfElements;
    }

    /**
     * @param offset
     *            the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return list.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }
}
