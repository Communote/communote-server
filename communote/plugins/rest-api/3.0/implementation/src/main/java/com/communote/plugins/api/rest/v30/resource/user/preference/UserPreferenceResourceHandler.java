package com.communote.plugins.api.rest.v30.resource.user.preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v30.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v30.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.resource.user.userpreference.EditUserPreferenceParameter;
import com.communote.plugins.api.rest.v30.resource.user.userpreference.GetUserPreferenceParameter;
import com.communote.plugins.api.rest.v30.resource.user.userpreference.UserPreferenceResource;
import com.communote.plugins.api.rest.v30.resource.user.userpreference.preference.PreferenceResource;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.vo.user.preferences.UserPreference;
import com.communote.server.service.UserPreferenceService;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserPreferenceResourceHandler
        extends
        DefaultResourceHandler<DefaultParameter,
        EditUserPreferenceParameter, DefaultParameter, GetUserPreferenceParameter,
        DefaultParameter> {

    /**
     * {@inheritDoc}
     * 
     * @throws ExtensionNotSupportedException
     *             Thrown, when building the response failed.
     * @throws ResponseBuildException
     *             Thrown, when building the response failed.
     * @throws AuthorizationException
     *             Thrown, when the current user is not allowed to access the given preferences.
     * @throws NotFoundException
     *             Thrown, when there are not preferences registered for the given type.
     */
    @Override
    protected Response handleEditInternally(EditUserPreferenceParameter editParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException, NotFoundException,
            AuthorizationException {
        PreferenceResource[] preferences = editParameter.getPreferences();
        if (preferences != null) {
            Map<String, String> preferencesAsMap = new HashMap<String, String>();
            for (PreferenceResource preference : preferences) {
                preferencesAsMap.put(preference.getKey(), preference.getValue());
            }
            ServiceLocator.findService(UserPreferenceService.class).mergePreferences(
                    editParameter.getClassName(), preferencesAsMap);
        }
        return ResponseHelper.buildSuccessResponse(0L, request);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws AuthorizationException
     *             Thrown, when the current user is not allowed to access the given preferences.
     * @throws ExtensionNotSupportedException
     *             Thrown, when building the response failed.
     * @throws ResponseBuildException
     *             Thrown, when building the response failed.
     */
    @Override
    protected Response handleGetInternally(GetUserPreferenceParameter parameters,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws AuthorizationException, ResponseBuildException, ExtensionNotSupportedException {
        UserPreferenceService userPreferenceService = ServiceLocator
                .findService(UserPreferenceService.class);
        UserPreference preferences = userPreferenceService.getPreferences(parameters
                .getF_className());
        UserPreferenceResource preferenceResource = new UserPreferenceResource();
        preferenceResource.setClassName(parameters.getF_className());
        List<PreferenceResource> entries = new ArrayList<PreferenceResource>();
        for (Entry<String, String> preference : preferences.getPreferences().entrySet()) {
            PreferenceResource entry = new PreferenceResource();
            entry.setKey(preference.getKey());
            entry.setValue(preference.getValue());
            entries.add(entry);
        }
        preferenceResource.setPreferences(entries.toArray(new PreferenceResource[entries
                .size()]));
        return ResponseHelper.buildSuccessResponse(preferenceResource, request);
    }
}
