package com.communote.plugins.api.rest.v30.resource.group.member;

import java.util.ArrayList;
import java.util.List;

import com.communote.common.converter.Converter;
import com.communote.plugins.api.rest.v30.resource.group.GroupResource;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.Group;

/**
 * Converts a {@link Group} to a {@link GroupResource}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MemberResourceConverter implements
        Converter<Group, List<MemberResource>> {

    @Override
    public List<MemberResource> convert(Group source) {
        List<MemberResource> members = new ArrayList<>();

        if (source instanceof ExternalUserGroup) {
            throw new IllegalArgumentException("Only local groups will be resolved.");
        }

        for (CommunoteEntity entity : source.getGroupMembers()) {

            MemberResource memberResource = new MemberResource();
            memberResource.setEntityId(entity.getId());
            memberResource.setIsGroup(entity instanceof Group);
            members.add(memberResource);

        }

        return members;
    }
}