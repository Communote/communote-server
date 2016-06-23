package com.communote.plugins.mq.message.core.handler.converter;

import com.communote.common.converter.Converter;
import com.communote.plugins.mq.message.core.data.role.ExternalTopicRole;
import com.communote.server.core.vo.external.ExternalTopicRoleTO;
import com.communote.server.model.blog.BlogRole;

/**
 * Convert external topic role to external topic role transfer object
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalTopicRoleToExternalTopicRoleTOConverter implements
        Converter<ExternalTopicRole, ExternalTopicRoleTO> {

    /**
     * will be converted to a null role value
     */
    private static final String BLOG_ROLE_NONE = "NONE";

    /**
     * Convert to a TO. Will throw an IllegalArgumentException if the role cannot be converted or
     * the entity is missing.
     * 
     * @param source
     *            the source object
     * @return the converted result
     */
    @Override
    public ExternalTopicRoleTO convert(ExternalTopicRole source) {
        ExternalTopicRoleTO externalTopicRoleTO = new ExternalTopicRoleTO();
        if (source.getEntity() == null) {
            throw new IllegalArgumentException("The entity member is not set");
        }
        externalTopicRoleTO.setEntityId(source.getEntity().getEntityId());
        externalTopicRoleTO.setEntityAlias(source.getEntity()
                    .getEntityAlias());
        externalTopicRoleTO.setIsGroup(source.getEntity().getIsGroup());
        if (source.getTopicRole() != null && !source.getTopicRole().equals(BLOG_ROLE_NONE)) {
            externalTopicRoleTO.setRole(BlogRole.fromString(source.getTopicRole()));
        }
        externalTopicRoleTO.setExternalEntityId(source.getEntity().getExternalId());
        externalTopicRoleTO.setExternalObjectId(source.getExternalObjectId());
        return externalTopicRoleTO;
    }

}