package com.communote.server.persistence.tag;

import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.model.tag.Tag;

/**
 * Base class for TagStore.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DefaultTagStore implements TagStore {

    /**
     * The default priority
     */
    public static final int DEFAULT_PRIORITY = 1000;

    private final int priority;
    private final String tagStoreId;
    private final TagStoreType[] types;
    private final boolean multilingual;

    /**
     * Constructor.
     * 
     * @param tagStoreId
     *            Alias of this TagStore.
     * @param order
     *            order/priority of this tag store
     * @param multilingual
     *            If set, this TagStore supports multiple languages.
     * @param types
     *            Types, this TagStore can handle.
     */
    public DefaultTagStore(String tagStoreId, int order, boolean multilingual,
            TagStoreType... types) {
        this.tagStoreId = tagStoreId;
        this.priority = order;
        this.multilingual = multilingual;
        if (types == null) {
            this.types = new TagStoreType[0];
        } else {
            this.types = types;
        }
    }

    /**
     * Constructor with a priority of 1000 and multilingual support.
     * 
     * @param alias
     *            Alias of this TagStore.
     * @param types
     *            Types, this TagStore can handle.
     */
    public DefaultTagStore(String alias, TagStoreType... types) {
        this(alias, DefaultTagStore.DEFAULT_PRIORITY, true, types);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(TagStoreType type) {
        for (TagStoreType innerType : types) {
            if (innerType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return priority;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTagStoreId() {
        return tagStoreId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTagStoreTagId(Tag tag) {
        return tag.getDefaultName().toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagStoreType[] getTypes() {
        return types;
    }

    /**
     * @return True, if this TagStore supports multiple languages, else false.
     */
    @Override
    public boolean isMultilingual() {
        return multilingual;
    }
}
