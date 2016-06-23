package com.communote.plugins.api.rest.v30.resource.lastmodificationdate;

import java.util.Date;

import com.communote.server.persistence.lastmodifieddate.LastModificationDateFactory;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LastModificationDateResourceFactory implements
        LastModificationDateFactory<LastModificationDateResource> {

    @Override
    public LastModificationDateResource createLastModificationDate(Long entityId, Date date) {
        LastModificationDateResource res = new LastModificationDateResource();
        res.setEntityId(entityId);
        res.setLastModificationDate(date);
        return res;
    }

}