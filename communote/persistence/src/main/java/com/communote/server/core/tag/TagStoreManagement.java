package com.communote.server.core.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.communote.common.util.DescendingOrderComparator;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.persistence.tag.DefaultTagStore;
import com.communote.server.persistence.tag.TagStore;

/**
 * Management for TagStores. This management contains default TagStores for notes, blogs and
 * entities. These all have a priority of 0.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
// TODO not thread-safe. Getting a store can lead to a ConcurrentModificationException.
@Service
public class TagStoreManagement {

    private final DescendingOrderComparator tagStorePriorityComparator = new DescendingOrderComparator();

    private final List<TagStore> tagStores = new ArrayList<TagStore>();
    {
        // create tagstores with default priority and disabled multi-lingual support, because it's
        // faster in retrieval
        tagStores.add(new DefaultTagStore(Types.NOTE.getDefaultTagStoreId(),
                DefaultTagStore.DEFAULT_PRIORITY, false,
                TagStoreType.Types.NOTE));
        tagStores.add(new DefaultTagStore(Types.BLOG.getDefaultTagStoreId(),
                DefaultTagStore.DEFAULT_PRIORITY, false,
                TagStoreType.Types.BLOG));
        tagStores.add(new DefaultTagStore(Types.ENTITY.getDefaultTagStoreId(),
                DefaultTagStore.DEFAULT_PRIORITY, false,
                TagStoreType.Types.ENTITY));
    }

    /**
     * Adds or replaces an existing tag store for the given alias.
     * 
     * @param tagStore
     *            The tag store.
     */
    public synchronized void addTagStore(TagStore tagStore) {
        tagStores.add(tagStore);
        Collections.sort(tagStores, tagStorePriorityComparator);
    }

    /**
     * @param alias
     *            The alias of the tag store to load.
     * @param defaultType
     *            If this is not null, but there is no TagStore with the given id, the next TagStore
     *            for this type will be loaded (if there is one).
     * @return The TagStore.
     */
    public TagStore getTagStore(String alias, TagStoreType defaultType) {
        for (TagStore tagStore : tagStores) {
            if (tagStore.getTagStoreId().equals(alias)) {
                return tagStore;
            }
        }
        if (defaultType != null) {
            return getTagStore(defaultType);
        }
        return null;
    }

    /**
     * @param type
     *            The type we search a tag store for.
     * @return The tag store or null if none.
     */
    public TagStore getTagStore(TagStoreType type) {
        for (TagStore tagStore : tagStores) {
            if (tagStore.canHandle(type)) {
                return tagStore;
            }
        }
        return null;
    }

    /**
     * @param type
     *            The tyoe to check.
     * @return <code>True</code>, if there is more than one tag store for this type.
     */
    public boolean hasMoreThanOneTagStore(TagStoreType type) {
        int counter = 0;
        for (TagStore tagStore : tagStores) {
            if (tagStore.canHandle(type)) {
                if (counter >= 1) {
                    return true;
                }
                counter++;
            }
        }
        return false;
    }

    /**
     * Returns whether there is at least one multilingual tag store for the given type
     * 
     * @param type
     *            the type to check
     * @return true if there is at least one tag store for the given type that is multilingual
     */
    public boolean hasMultilingualTagStore(TagStoreType type) {
        for (TagStore tagStore : tagStores) {
            if (tagStore.canHandle(type)) {
                if (tagStore.isMultilingual()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Test whether the provided TagStore is one of the built-in default TagStores.
     * 
     * @param tagStore
     *            the store to test
     * @return true if it is a default tag store, false otherwise
     */
    public boolean isDefaultTagStore(TagStore tagStore) {
        return tagStore.getTypes().length == 1
                && (Types.NOTE.getDefaultTagStoreId().equals(tagStore.getTagStoreId())
                        || Types.BLOG.getDefaultTagStoreId().equals(tagStore.getTagStoreId()) || Types.ENTITY
                        .getDefaultTagStoreId().equals(tagStore.getTagStoreId()));
    }

    /**
     * @param tagStore
     *            The TagStore to remove.
     */
    // TODO should we allow the removal of the default tag stores?
    public synchronized void removeTagStore(TagStore tagStore) {
        tagStores.remove(tagStore);
    }
}
