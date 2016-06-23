package com.communote.server.persistence.lastmodifieddate;

import java.util.Date;

/**
 * Standard factory that creates {@link LastModificationDate}'s
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StandardLastModificationDateFactory implements
        LastModificationDateFactory<LastModificationDate> {

    @Override
    public LastModificationDate createLastModificationDate(Long entityId, Date date) {
        LastModificationDate lastDate = new LastModificationDate();
        lastDate.setEntityId(entityId);
        lastDate.setLastModificationDate(date);
        return lastDate;
    }

}