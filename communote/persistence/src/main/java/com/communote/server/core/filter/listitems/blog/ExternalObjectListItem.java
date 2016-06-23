package com.communote.server.core.filter.listitems.blog;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.property.StringPropertyableTO;

/**
 * List item for {@see com.communote.server.persistence.external.ExternalObject}
 *
 * If the user has no access to the topic the topicNameIdentifier and the externalName field will be
 * null.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ExternalObjectListItem extends IdentifiableEntityData implements Serializable, StringPropertyableTO {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String externalId;
    private String externalSystemId;
    private String externalName;

    private Long topicId;
    private String topicNameIdentifier;
    private Set<StringPropertyTO> objectProperties;

    private boolean hasAccessToTopic;

    /**
     * constructor
     */
    public ExternalObjectListItem() {
    }

    /**
     * constructor
     *
     * @param id
     *            the ID of the topic
     */
    public ExternalObjectListItem(Long id) {
        setId(id);
    }

    public ExternalObjectListItem(
            Long id,
            String externalId,
            String externalSystemId,
            String externalName,
            Long topicId,
            String topicNameIdentifier) {
        setId(id);
        this.externalId = externalId;
        this.externalSystemId = externalSystemId;
        this.externalName = externalName;
        this.topicId = topicId;
        this.topicNameIdentifier = topicNameIdentifier;
    }

    /**
     * Add an object property.
     *
     * @param property
     *            the property to add
     */
    public void addObjectProperty(StringPropertyTO property) {
        if (objectProperties != null) {
            if (this.objectProperties == null) {
                this.objectProperties = new HashSet<StringPropertyTO>();
            }
            this.objectProperties.add(property);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExternalObjectListItem other = (ExternalObjectListItem) obj;
        if (externalId == null) {
            if (other.externalId != null) {
                return false;
            }
        } else if (!externalId.equals(other.externalId)) {
            return false;
        }
        if (externalName == null) {
            if (other.externalName != null) {
                return false;
            }
        } else if (!externalName.equals(other.externalName)) {
            return false;
        }
        if (externalSystemId == null) {
            if (other.externalSystemId != null) {
                return false;
            }
        } else if (!externalSystemId.equals(other.externalSystemId)) {
            return false;
        }
        if (hasAccessToTopic != other.hasAccessToTopic) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (topicId == null) {
            if (other.topicId != null) {
                return false;
            }
        } else if (!topicId.equals(other.topicId)) {
            return false;
        }
        if (topicNameIdentifier == null) {
            if (other.topicNameIdentifier != null) {
                return false;
            }
        } else if (!topicNameIdentifier.equals(other.topicNameIdentifier)) {
            return false;
        }
        return true;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getExternalName() {
        return externalName;
    }

    public String getExternalSystemId() {
        return externalSystemId;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Set<StringPropertyTO> getObjectProperties() {
        return objectProperties;
    }

    public Long getTopicId() {
        return topicId;
    }

    public String getTopicNameIdentifier() {
        return topicNameIdentifier;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((externalId == null) ? 0 : externalId.hashCode());
        result = prime * result + ((externalName == null) ? 0 : externalName.hashCode());
        result = prime * result + ((externalSystemId == null) ? 0 : externalSystemId.hashCode());
        result = prime * result + (hasAccessToTopic ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((topicId == null) ? 0 : topicId.hashCode());
        result = prime * result
                + ((topicNameIdentifier == null) ? 0 : topicNameIdentifier.hashCode());
        return result;
    }

    /**
     *
     * @return true if the user has read access to the topic, false otherwise
     */
    public boolean isHasAccessToTopic() {
        return hasAccessToTopic;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    public void setHasAccessToTopic(boolean hasAccessToTopic) {
        this.hasAccessToTopic = hasAccessToTopic;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Set the properties of the external object.
     *
     * @param properties
     *            the properties
     */
    public void setObjectProperties(Set<StringPropertyTO> properties) {
        this.objectProperties = properties;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public void setTopicNameIdentifier(String topicNameIdentifier) {
        this.topicNameIdentifier = topicNameIdentifier;
    }

    @Override
    public String toString() {
        return "ExternalObjectListItem [id=" + id + ", externalId=" + externalId
                + ", externalSystemId=" + externalSystemId + ", externalName=" + externalName
                + ", topicId=" + topicId + ", topicNameIdentifier=" + topicNameIdentifier
                + ", hasAccessToTopic=" + hasAccessToTopic + "]";
    }

}