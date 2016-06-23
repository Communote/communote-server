package com.communote.plugins.api.rest.v24.converter;

import com.communote.plugins.api.rest.v24.resource.topic.externalobject.ExternalObjectResource;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.model.external.ExternalObject;

/**
 * This converter convert a {@link ExternalObject} to a {@link ExternalObjectResource}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            The BlogTagListItem which is the incoming list
 * @param <O>
 *            The BlogResource which is the final list
 */
public class ExternalObjectToExternalObjectResourceConverter<T extends ExternalObject, O extends ExternalObjectResource>
        extends QueryResultConverter<T, O> {

    @Override
    public boolean convert(T queryResult, O finalResult) {
        fillingResultItem(queryResult, finalResult);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public O create() {
        return (O) new ExternalObjectResource();
    }

    /**
     * Filling the {@link ExternalObjectResource}
     * 
     * @param externalObject
     *            The external object.
     * @param externalObjectResource
     *            The external object resource.
     */
    private void fillingResultItem(T externalObject, O externalObjectResource) {
        externalObjectResource.setExternalObjectId(externalObject.getId());
        externalObjectResource.setExternalId(externalObject.getExternalId());
        externalObjectResource.setExternalSystemId(externalObject.getExternalSystemId());
        externalObjectResource.setName(externalObject.getExternalName());
    }
}