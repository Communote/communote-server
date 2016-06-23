package com.communote.server.core.common.time;

import java.util.Date;

/**
 * Interface to mark an object as being aware of the date of the last modification.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface LastModificationAware {

    /**
     * @return the date of the last modification of the object
     */
    Date getLastModificationDate();

    /**
     * Set the date of the last modification
     * 
     * @param lastModificationDate
     *            the modification date
     */
    void setLastModificationDate(Date lastModificationDate);
}
