package com.communote.server.core.filter.listitems.blog.member;

import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.persistence.DisplayName;

/**
 * Base value object for users and groups
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class CommunoteEntityData extends IdentifiableEntityData implements DisplayName {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Long entityId;

    /**
     * @return get alias of the item
     */
    public abstract String getAlias();

    /**
     * @return the entityId
     */
    public Long getEntityId() {
        return entityId;
    }

    @Override
    public Long getId() {
        return getEntityId();
    }

    /**
     *
     * @return the type of this list item as string value.
     */
    public abstract String getType();

    /**
     * @return whether the entity is a group or a user
     */
    public abstract boolean isGroup();

    /**
     * @param entityId
     *            the entityId to set
     */
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    @Override
    public void setId(Long id) {
        setEntityId(id);
    }

}
