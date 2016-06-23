package com.communote.plugins.api.rest.v24.resource.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.communote.plugins.api.rest.v24.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v24.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v24.resource.DefaultParameter;
import com.communote.plugins.api.rest.v24.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v24.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v24.resource.tagsuggestionlist.GetCollectionTagSuggestionListParameter;
import com.communote.plugins.api.rest.v24.resource.tagsuggestionlist.TagSuggestionListResource;
import com.communote.plugins.api.rest.v24.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.core.tag.TagSuggestion;
import com.communote.server.core.tag.TagSuggestionManagement;

/**
 * Handler for TagSuggestionListResource
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TagSuggestionListResourceHandler
        extends
        DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter, DefaultParameter,
        GetCollectionTagSuggestionListParameter> {

    /**
     * Getter for the {@link TagSuggestionManagement}
     * 
     * @return {@link TagSuggestionManagement}
     */
    private TagSuggestionManagement getTagSuggestionManagement() {
        return ServiceLocator.instance().getService(TagSuggestionManagement.class);
    }

    /**
     * List for tag suggestions
     * 
     * @param getCollectionTagSuggestionListParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            - The sessionId for the session
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleListInternally(
            GetCollectionTagSuggestionListParameter getCollectionTagSuggestionListParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException {
        Map<String, Object> parameters = TagHelper.toMap(uriInfo.getQueryParameters());

        TagStoreType type = TagHelper.getTagStoreType(getCollectionTagSuggestionListParameter
                .getTagStoreType());

        Collection<String> suggestionAliases = null;
        if (getCollectionTagSuggestionListParameter.getF_suggestionAliases() != null) {
            suggestionAliases = Arrays.asList(getCollectionTagSuggestionListParameter
                    .getF_suggestionAliases());
        }

        Collection<String> suggestionProviderAliases = null;
        if (getCollectionTagSuggestionListParameter.getF_suggestionProviderAliases() != null) {
            suggestionProviderAliases = Arrays.asList(getCollectionTagSuggestionListParameter
                    .getF_suggestionProviderAliases());
        }
        Boolean assignedTagsOnly = getCollectionTagSuggestionListParameter.getAssignedTagsOnly();
        if (assignedTagsOnly == null) {
            assignedTagsOnly = Boolean.FALSE;
        }

        Collection<TagSuggestion> suggestions = getTagSuggestionManagement().findTagSuggestions(
                type, suggestionProviderAliases, suggestionAliases,
                assignedTagsOnly.booleanValue(), parameters,
                ResourceHandlerHelper.getNameProvider(request),
                ResourceHandlerHelper.getCurrentUserLocale(request));

        List<TagSuggestionListResource> tagSuggestionListResources = new ArrayList<TagSuggestionListResource>();
        for (TagSuggestion suggestion : suggestions) {
            TagSuggestionListResource tagSuggestionListResource = new TagSuggestionListResource();
            tagSuggestionListResource.setName(suggestion.getName(ResourceHandlerHelper
                    .getCurrentUserLocale(
                    request)));
            // if no I18N is available or empty string, then use the alias of the TagSuggestion
            if (StringUtils.isEmpty(tagSuggestionListResource.getName())) {
                tagSuggestionListResource.setName(suggestion.getAlias());
            }
            tagSuggestionListResource.setAlias(suggestion.getAlias());
            tagSuggestionListResource.setProviderAlias(suggestion.getProviderAlias());

            // copy the tags
            Collection<TagData> tagListItems = suggestion.getTags();
            if (tagListItems.size() > 0) {
                TagResource[] tagResources = new TagResource[tagListItems.size()];
                int i = 0;
                for (TagData tagListItem : tagListItems) {
                    tagResources[i] = TagHelper.buildTagResource(tagListItem);
                    i++;
                }
                tagSuggestionListResource.setTags(tagResources);
            }
            tagSuggestionListResources.add(tagSuggestionListResource);
        }
        return ResponseHelper.buildSuccessResponse(tagSuggestionListResources, request);
    }
}
