/**
 *
 */
package com.communote.server.web.fe.portal.user.client.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientPropertySecurity;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.user.UserProfileDetails;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.web.fe.portal.blog.controller.InitialFiltersViewController;

/**
 * Controller for the user management view section.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientUserManagementViewController extends InviteUserToClientController {
    private static final String JSON_KEY_USER_SEARCH_STRING = "userSearchString";
    private static final String JSON_KEY_USER_ID = "userId";

    /**
     * Builds a JSON object containing the initial filter parameters.
     *
     * @param details
     *            details about the user to set
     * @return the json object
     */
    private JsonNode buildInitialFiltersJson(UserProfileDetails details) {
        if (details != null) {
            ObjectNode result = JsonHelper.getSharedObjectMapper().createObjectNode();
            result.put(JSON_KEY_USER_ID, details.getUserId());
            result.put(JSON_KEY_USER_SEARCH_STRING, details.getUserAlias());
            return result;
        }
        return JsonHelper.getSharedObjectMapper().getNodeFactory().nullNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        setInitialFilters(request);
        return super.handleRequestInternal(request, response);
    }

    /**
     * Extracts some initial filters from the request and stores them in a JSON object for use in
     * the user management view.
     *
     * @param request
     *            the request
     */
    private void setInitialFilters(HttpServletRequest request) {
        Long userId = ServletRequestUtils.getLongParameter(request,
                ClientUserManagementController.PARAM_USER_ID, -1L);
        UserProfileDetails details = null;
        if (userId != -1) {
            details = ServiceLocator.findService(UserProfileManagement.class)
                    .getUserProfileDetailsById(userId, false);
        }
        JsonNode filters = buildInitialFiltersJson(details);
        request.setAttribute(InitialFiltersViewController.KEY_INITIAL_FILTERS_JSON,
                JsonHelper.writeJsonTreeAsString(filters));

        ClientConfigurationProperties properties = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();
        String primaryExternalAuthentication = properties.getPrimaryExternalAuthentication();
        boolean isDBAuthenticationAllowed = primaryExternalAuthentication == null
                || properties.getProperty(ClientPropertySecurity.ALLOW_DB_AUTH_ON_EXTERNAL,
                        ClientPropertySecurity.DEFAULT_ALLOW_DB_AUTH_ON_EXTERNAL);
        List<String> invitationProviders = new ArrayList<String>();
        if (primaryExternalAuthentication != null) {
            invitationProviders.add(primaryExternalAuthentication);
        }
        if (isDBAuthenticationAllowed) {
            invitationProviders.add(ConfigurationManagement.DEFAULT_DATABASE_ID);
        }
        request.setAttribute("invitationProviders", invitationProviders);

    }
}
