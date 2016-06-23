package com.communote.server.web.commons.servlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.ApplicationInformation;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.rest.RestletApplicationManager;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserProfileDetails;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.user.UserProfileManagement;

/**
 * This class provides the supported REST API versions plus information about the current user in
 * JSON format.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class RestApiInformationController implements Controller {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RestApiInformationController.class);

    private RestApiInformationLicenseDataProvider licenseDataProvider;

    /**
     * This method adds information about the current license to the rest information.
     *
     * <p>
     * Note: since the information resource is not versioned like the other REST API resources this
     * must be kept for backwards compatibility
     * </p>
     *
     * @return A map including license information.
     */
    private Map<String, Object> getLicenseInformation() {
        if (this.licenseDataProvider != null) {
            return licenseDataProvider.getLicenseInformation();
        }
        // return default values of a valid standalone license
        Map<String, Object> licenseMap = new HashMap<String, Object>();
        licenseMap.put(RestApiInformationLicenseDataProvider.LICENSE_FIELD_FOR, "");
        licenseMap.put(RestApiInformationLicenseDataProvider.LICENSE_FIELD_TYPE,
                RestApiInformationLicenseDataProvider.TYPE_STANDALONE);
        licenseMap.put(RestApiInformationLicenseDataProvider.LICENSE_FIELD_IS_VALID, Boolean.TRUE);
        return licenseMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        RestletApplicationManager<?> restletApplicationManager = ServiceLocator.instance()
                .getService(RestletApplicationManager.class);
        List<String> versions = restletApplicationManager.getVersions();
        Map<String, Object> map = new HashMap<String, Object>();
        Long userId = SecurityHelper.getCurrentUserId();
        if (SecurityHelper.isInternalSystem()) {
            LOGGER.warn("The internal system user is set, but may not.", new Exception());
        }
        if (userId != null && !SecurityHelper.isInternalSystem()) {
            UserProfileDetails user = ServiceLocator.instance()
                    .getService(UserProfileManagement.class)
                    .getUserProfileDetailsById(userId, false);
            if (user != null) {
                map.put("userId", userId);
                map.put("userAlias", user.getUserAlias());
                map.put("userFirstName", user.getFirstName());
                map.put("userLastName", user.getLastName());
                map.put("utcTimeZoneOffset",
                        UserManagementHelper.getCurrentUtcOffsetOfEffectiveUserTimeZone());
                map.put("languageLocale", SessionHandler.instance().getCurrentLocale(request)
                        .getLanguage());
            }
            map.put("license", getLicenseInformation());
        }
        if (!versions.isEmpty()) {
            map.put("preferredVersion", versions.get(0));
            map.put("supportedVersions", versions);
            response.setHeader("X-COMMUNOTE-REST-API", versions.get(0));
        }

        ApplicationInformation applicationInfo = CommunoteRuntime.getInstance()
                .getApplicationInformation();
        map.put("buildNumber", applicationInfo.getBuildNumberWithType());
        map.put("buildTime", applicationInfo.getBuildTime());
        response.setContentType("application/json");
        JsonHelper.getSharedObjectMapper().writeValue(response.getWriter(), map);
        return null;
    }

    /**
     * Set the provider for license member of the information resource
     *
     * @param licenseDataProvider
     *            the provider
     */
    public void setLicenseDataProvider(RestApiInformationLicenseDataProvider licenseDataProvider) {
        this.licenseDataProvider = licenseDataProvider;
    }

}
