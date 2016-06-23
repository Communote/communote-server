package com.communote.server.core.converter.user;

import com.communote.common.converter.PojoTargetConverter;
import com.communote.server.core.filter.listitems.blog.member.EntityGroupListItem;
import com.communote.server.model.user.group.Group;


/**
 * Converter to convert a group entity into a EntityGroupListItem
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GroupToEntityGroupListItemConverter extends
        PojoTargetConverter<Group, EntityGroupListItem> {
    /**
     * Create a new instance
     * 
     * @param clazz
     *            the class of the target
     */
    public GroupToEntityGroupListItemConverter(Class<EntityGroupListItem> clazz) {
        super(clazz);
    }

    @Override
    public void convert(Group source, EntityGroupListItem target) {
        target.setId(source.getId());
        target.setAlias(source.getAlias());
        target.setName(source.getName());
    }
}
