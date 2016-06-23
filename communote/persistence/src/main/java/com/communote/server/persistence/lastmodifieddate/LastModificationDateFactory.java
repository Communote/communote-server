package com.communote.server.persistence.lastmodifieddate;

import java.util.Date;

/**
 * Simple factory to create object for last modification dates to avoid copying of pojos
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface LastModificationDateFactory<T> {

    /**
     * Create some form of object that holds the entity id and date
     * 
     * @param entityId
     *            the id of the entity
     * @param lastModificationDate
     *            the last modification date of the entity
     * @return an object storing both information
     */
    public T createLastModificationDate(Long entityId, Date lastModificationDate);

}