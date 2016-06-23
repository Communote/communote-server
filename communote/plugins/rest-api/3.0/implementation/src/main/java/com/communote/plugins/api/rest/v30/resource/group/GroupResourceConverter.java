package com.communote.plugins.api.rest.v30.resource.group;

import com.communote.common.converter.Converter;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.Group;

/**
 * Converts a {@link Group} to a {@link GroupResource}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class GroupResourceConverter implements
        Converter<Group, GroupResource> {

    @Override
    public GroupResource convert(Group source) {
        GroupResource target = new GroupResource();
        target.setAlias(source.getAlias());
        target.setDescription(source.getDescription());
        target.setName(source.getName());
        target.setGroupId(String.valueOf(source.getId()));

        if (source instanceof ExternalUserGroup) {

            ExternalUserGroup externalSouce = (ExternalUserGroup) source;
            target.setExternalId(externalSouce.getExternalId());
            target.setExternalSystemId(externalSouce.getExternalSystemId());

        }

        return target;
    }
}