package com.communote.server.persistence.tag;

import com.communote.common.util.Orderable;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.model.tag.Tag;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface TagStore extends Orderable {

    /**
     * @param type
     *            The class to check.
     * @return True, if this TagStore can handle the given class.
     */
    public abstract boolean canHandle(TagStoreType type);

    /**
     * @return the order value which is interpreted as the priority of the tag store. Tag stores
     *         with a higher priority will take precedence.
     */
    @Override
    public int getOrder();

    /**
     * @return The alias of this tag store.
     */
    public abstract String getTagStoreId();

    /**
     * @param tag
     *            The tag.
     * @return The id within this TagStore of this tag. This defaults to the lower case version of
     *         the default name of the tag.
     */
    public abstract String getTagStoreTagId(Tag tag);

    /**
     * @return the classes this tag store handles.
     */
    public abstract TagStoreType[] getTypes();

    /**
     * @return True, if this TagStore supports multiple languages.
     */
    public boolean isMultilingual();

}