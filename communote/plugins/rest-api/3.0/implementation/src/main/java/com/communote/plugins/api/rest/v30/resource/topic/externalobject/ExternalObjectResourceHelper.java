package com.communote.plugins.api.rest.v30.resource.topic.externalobject;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.external.ExternalObjectAlreadyAssignedException;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.core.external.ExternalSystemNotConfiguredException;
import com.communote.server.core.external.TooManyExternalObjectsPerTopicException;
import com.communote.server.model.external.ExternalObject;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class ExternalObjectResourceHelper {
    /**
     * Add or update external object resources.
     * 
     * @param externalObjectResources
     *            the external object resources.
     * @param topicId
     *            the identifier of the topic.
     * @param onlyAdd
     *            only add the external object and not checks for existing external objects.
     * @throws BlogAccessException
     *             the topic can not accessed.
     * @throws NotFoundException
     *             external object was not found.
     * @throws AuthorizationException
     *             user is not manager to set external object.
     * @throws ExternalObjectAlreadyAssignedException
     *             in case the topic is already assigned
     * @throws ExternalSystemNotConfiguredException
     * @throws TooManyExternalObjectsPerTopicException
     */
    public static void addOrUpdateExternalObjectResources(
            ExternalObjectResource[] externalObjectResources,
            Long topicId, boolean onlyAdd) throws BlogAccessException, NotFoundException,
            AuthorizationException, ExternalObjectAlreadyAssignedException,
            TooManyExternalObjectsPerTopicException, ExternalSystemNotConfiguredException {
        if (externalObjectResources != null) {
            ExternalObjectManagement extObjectManagement = ServiceLocator.instance().getService(
                    ExternalObjectManagement.class);
            for (ExternalObjectResource externalObjectResource : externalObjectResources) {
                ExternalObject externalObject = ExternalObject.Factory.newInstance();
                externalObject.setExternalId(externalObjectResource.getExternalId());
                externalObject.setExternalName(externalObjectResource.getName());
                externalObject.setExternalSystemId(externalObjectResource.getExternalSystemId());
                if (!onlyAdd
                        && extObjectManagement.isExternalObjectAssigned(topicId,
                                externalObjectResource.getExternalSystemId(),
                                externalObjectResource.getExternalId())) {
                    externalObject.setId(externalObjectResource.getExternalObjectId());
                    extObjectManagement.updateExternalObject(topicId, externalObject);
                } else {
                    extObjectManagement.assignExternalObject(topicId, externalObject);
                }
            }
        }
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private ExternalObjectResourceHelper() {
        // Do nothing
    }

}
