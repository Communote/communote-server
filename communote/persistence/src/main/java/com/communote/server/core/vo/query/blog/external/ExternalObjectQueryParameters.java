package com.communote.server.core.vo.query.blog.external;

import java.util.Map;

import com.communote.server.core.vo.query.PropertyQueryParameters;

/**
 * Parameters for filtering for external objects
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ExternalObjectQueryParameters extends PropertyQueryParameters {
    /**
     * Paramater for external system id
     */
    public final static String PARAM_EXTERNAL_SYSTEM_ID = "externalSystemId";

    /**
     * Paramater for external object id
     */
    public final static String PARAM_EXTERNAL_ID = "externalId";

    private String externalId;
    private String externalSystemId;

    public String getExternalId() {
        return externalId;
    }

    public String getExternalSystemId() {
        return externalSystemId;
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = super.getParameters();

        if (externalId != null) {
            params.put(PARAM_EXTERNAL_ID, externalId);
        }
        if (externalSystemId != null) {
            params.put(PARAM_EXTERNAL_SYSTEM_ID, externalSystemId);
        }

        return params;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    @Override
    public String toString() {
        return "ExternalObjectQueryParameters [externalId=" + externalId + ", externalSystemId="
                + externalSystemId + "]";
    }
}
