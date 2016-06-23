package com.communote.plugins.api.rest.v30.resource.user.externallogin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.v30.resource.user.externallogin.externalproperty.ExternalPropertyResource;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.service.UserService;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalLoginResourceConverter extends
        QueryResultConverter<ExternalUserAuthentication, ExternalLoginResource> {

    private final Long userId;

    private final static Logger LOGGER = LoggerFactory
            .getLogger(ExternalLoginResourceConverter.class);

    public ExternalLoginResourceConverter(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean convert(ExternalUserAuthentication source, ExternalLoginResource target) {

        try {

            final Collection<StringPropertyTO> sourceProperties = getUserService()
                    .getExternalLoginProperties(userId, source);

            target.setDatabaseId(source.getId());
            target.setExternalLoginId(source.getExternalUserId());
            target.setExternalSystemId(source.getSystemId());
            target.setPermanentId(source.getPermanentId());

            List<ExternalPropertyResource> targetProperties;

            if (sourceProperties != null) {
                targetProperties = new ArrayList<>(sourceProperties.size());
                for (StringPropertyTO sourceProp : sourceProperties) {
                    ExternalPropertyResource targetProp = new ExternalPropertyResource();
                    targetProp.setKey(sourceProp.getPropertyKey());
                    targetProp.setKeyGroup(sourceProp.getKeyGroup());
                    targetProp.setValue(sourceProp.getPropertyValue());

                    targetProperties.add(targetProp);
                }

                target.setProperties(targetProperties
                        .toArray(new ExternalPropertyResource[targetProperties
                                .size()]));
            }

            return true;

        } catch (AuthorizationException e) {
            LOGGER.debug("AuthorizationException getting ExternalLoginResource. Result item for "
                    + source.attributesToString() + " will be ignored. ", e);

        }

        return false;
    }

    @Override
    public ExternalLoginResource create() {
        return new ExternalLoginResource();
    }

    private UserService getUserService() {
        return ServiceLocator.findService(UserService.class);
    }
}