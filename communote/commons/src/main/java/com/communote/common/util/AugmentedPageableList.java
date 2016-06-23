package com.communote.common.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * A {@link PageableList} that can be augmented with additional data.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <E>
 *            The type of the list
 */
public class AugmentedPageableList<E> extends PageableList<E> {

    /**
     * This enum provides the available augmentation keys which are used to reference augmentation
     * data.
     */
    public enum AugmentationKeys {
        /**
         * Upper creation date-time of all members of the pageable list
         */
        CREATION_TIME_MAX,
        /**
         * Upper creation date-time of all members of the pageable list
         */
        CREATION_TIME_MIN
    }

    private final EnumMap<AugmentationKeys, Object> augmentationData;

    /**
     * Creates the augmented pageable list with the given list as back-end.
     * 
     * @param myList
     *            the list to wrap
     */
    public AugmentedPageableList(List<E> myList) {
        super(myList);
        augmentationData = new EnumMap<AugmentationKeys, Object>(AugmentationKeys.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <T extends Object> PageableList<T> createEmptyList(Class<T> typeClass) {
        return new AugmentedPageableList<T>(new ArrayList<T>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Object> PageableList<T> createEmptyListWithMetaData(Class<T> typeClass) {
        AugmentedPageableList<T> copy = (AugmentedPageableList<T>) super
                .createEmptyListWithMetaData(typeClass);
        for (AugmentationKeys key : AugmentationKeys.values()) {
            Object value = this.getAugmentation(key);
            copy.setAugmentation(key, value);
        }
        return copy;
    }

    /**
     * Returns augmentation data for the given key.
     * 
     * @param key
     *            key identifying the requested data
     * @return augmentation data
     */
    public Object getAugmentation(AugmentationKeys key) {
        return augmentationData.get(key);
    }

    /**
     * Sets the augmentation data for the given key.
     * 
     * @param key
     *            key identifying the augmentation data to be set
     * @param value
     *            the augmentation data
     */
    public void setAugmentation(AugmentationKeys key, Object value) {
        augmentationData.put(key, value);
    }
}
