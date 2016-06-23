package com.communote.server.persistence.lastmodifieddate;

import java.util.Date;

/**
 * Last modification dates of a specific type of entities, e.g. notes, topics.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class LastModificationDate {

    private Long entityId;
    private Date lastModificationDate;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LastModificationDate other = (LastModificationDate) obj;
        if (entityId == null) {
            if (other.entityId != null) {
                return false;
            }
        } else if (!entityId.equals(other.entityId)) {
            return false;
        }
        if (lastModificationDate == null) {
            if (other.lastModificationDate != null) {
                return false;
            }
        } else if (!lastModificationDate.equals(other.lastModificationDate)) {
            return false;
        }
        return true;
    }

    public Long getEntityId() {
        return entityId;
    }

    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
        result = prime * result
                + ((lastModificationDate == null) ? 0 : lastModificationDate.hashCode());
        return result;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

}