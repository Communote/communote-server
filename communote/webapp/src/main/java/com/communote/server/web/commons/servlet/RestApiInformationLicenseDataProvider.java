package com.communote.server.web.commons.servlet;

import java.util.Map;

/**
 * Provider for the <code>license</code> member of the REST API information resource.
 *
 * <p>
 * Note: since the information resource is not versioned like the other REST API resources this must
 * be kept for backwards compatibility
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface RestApiInformationLicenseDataProvider {

    /**
     * Field for license data holding the owner of the license
     */
    public static final String LICENSE_FIELD_FOR = "for";
    /**
     * Field for license data denoting whether the license is valid
     */
    public static final String LICENSE_FIELD_IS_VALID = "isValid";

    /**
     * Field for license data denoting the type of the application
     */
    public static final String LICENSE_FIELD_TYPE = "type";

    public static final String TYPE_STANDALONE = "ST";

    /**
     * @return the license information as key value map. The content of the map will be added to the
     *         <code>license</code> member of the information resource.
     */
    public Map<String, Object> getLicenseInformation();
}
