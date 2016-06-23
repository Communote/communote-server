package com.communote.server.core.vo.user;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NavigationItemDataTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Clone the provided data TO. The filters will be a shallow clone
     * 
     * @param data
     *            the TO to clone
     * @return the cloned object or null if null was passed
     */
    public static NavigationItemDataTO clone(NavigationItemDataTO data) {
        if (data == null) {
            return null;
        }
        NavigationItemDataTO clone = new NavigationItemDataTO();
        clone.setContextId(data.getContextId());
        clone.setContextType(data.getContextType());
        if (data.getFilters() == null) {
            clone.setFilters(null);
        } else {
            // shallow clone
            for (Map.Entry<String, Object> entry : data.getFilters().entrySet()) {
                clone.getFilters().put(entry.getKey(), entry.getValue());
            }
        }
        return clone;
    }

    private String contextType;

    private String contextId;
    private Map<String, Object> filters = new HashMap<String, Object>();

    /**
     * @return Id for the entity the context refers to.
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * @return Type of the context.
     */
    public String getContextType() {
        return contextType;
    }

    /**
     * @return Filters of this context.
     */
    public Map<String, Object> getFilters() {
        return filters;
    }

    /**
     * @param contextId
     *            Id for the entity the context refers to.
     */
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    /**
     * @param contextType
     *            Type of the context.
     */
    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    /**
     * @param filters
     *            Filters of this context.
     */
    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    @Override
    public String toString() {
        return "NavigationItemDataTO [contextType=" + contextType + ", contextId=" + contextId
                + ", filters=" + filters + "]";
    }
}
