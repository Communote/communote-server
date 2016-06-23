package com.communote.plugins.mq.message.core.handler.converter;

import com.communote.common.converter.Converter;
import com.communote.plugins.mq.message.core.data.topic.ExternalObject;

/**
 * Converter that convertes an MQ external object into the Communote counterpart.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MqToCoreExternalObjectConverter implements
        Converter<ExternalObject, com.communote.server.model.external.ExternalObject> {
    private final String externalSystemId;

    /**
     * Create a new converter
     * 
     * @param externalSystemId
     *            the identifier of the external system for which the external objects are converted
     */
    public MqToCoreExternalObjectConverter(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    @Override
    public com.communote.server.model.external.ExternalObject convert(ExternalObject source) {
        com.communote.server.model.external.ExternalObject target;
        target = com.communote.server.model.external.ExternalObject.Factory
                .newInstance();
        target.setExternalSystemId(externalSystemId);
        target.setExternalId(source.getExternalObjectId());
        target.setExternalName(source.getExternalObjectName());
        return target;
    }
}
