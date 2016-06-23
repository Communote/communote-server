package com.communote.plugins.api.rest.v22.resource.tag;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.communote.plugins.api.rest.v22.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v22.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v22.resource.DefaultParameter;
import com.communote.plugins.api.rest.v22.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v22.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v22.response.ResponseHelper;
import com.communote.plugins.api.rest.v22.service.IllegalRequestParameterException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagNotFoundException;
import com.communote.server.core.tag.TagStoreNotFoundException;
import com.communote.server.core.vo.query.converters.TagToTagDataQueryResultConverter;
import com.communote.server.model.tag.Tag;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagResourceHandler
        extends
        DefaultResourceHandler<DefaultParameter, EditTagParameter, DefaultParameter, GetTagParameter,
        DefaultParameter> {

    /**
     * Get the {@link TagManagement}
     * 
     * @return {@link TagManagement}
     */
    private TagManagement getTagManagement() {
        return ServiceLocator.findService(TagManagement.class);
    }

    /**
     * Edit a tag
     * 
     * @param editTagParameter
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
     * @throws IllegalRequestParameterException
     *             Request parameter is wrong.
     * @throws TagStoreNotFoundException
     *             Specified tagStore was not found.
     * @throws TagNotFoundException
     *             Tag was not found.
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleEditInternally(EditTagParameter editTagParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws IllegalRequestParameterException, TagNotFoundException,
            TagStoreNotFoundException, ResponseBuildException, ExtensionNotSupportedException {
        Tag tag = getTagManagement().findTag(editTagParameter.getTagId());

        TagResource tagResource = new TagResource();
        tagResource.setDefaultName(editTagParameter.getDefaultName());
        tagResource.setDescription(editTagParameter.getDescription());
        tagResource.setLanguageCode(editTagParameter.getLanguageCode());
        tagResource.setName(editTagParameter.getName());
        tagResource.setTagStoreAlias(editTagParameter.getTagStoreAlias());
        tagResource.setTagStoreTagId(editTagParameter.getTagStoreTagId());

        TagTO tagTO = TagHelper.buildTagTO(tagResource,
                TagHelper.getTagStoreType(editTagParameter.getTagStoreType()));

        getTagManagement().storeTag(tagTO);

        return ResponseHelper.buildSuccessResponse(tag.getId(), "Tag was successful updated!",
                request);

    }

    /**
     * Get a tag
     * 
     * @param getTagParameter
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
     * @throws IllegalRequestParameterException
     *             Thrown when tagStoreTagId or tagStoreAlias was not set when tag identifier is 0.
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleGetInternally(GetTagParameter getTagParameter, String requestedMimeType,
            UriInfo uriInfo, String requestSessionId, Request request)
            throws IllegalRequestParameterException, ResponseBuildException,
            ExtensionNotSupportedException {
        TagToTagDataQueryResultConverter converter = new TagToTagDataQueryResultConverter(
                ResourceHandlerHelper.getCurrentUserLocale(request));
        TagData tag;
        if (getTagParameter.getTagId() != 0) {
            tag = getTagManagement().findTag(getTagParameter.getTagId(), converter);
        } else if (StringUtils.isNotBlank(getTagParameter.getTagStoreTagId())
                && StringUtils.isNotBlank(getTagParameter.getTagStoreAlias())) {
            tag = getTagManagement().findTagByTagStoreNewTx(getTagParameter.getTagStoreTagId(),
                    getTagParameter.getTagStoreAlias(), converter);
        } else {
            if (StringUtils.isNotBlank(getTagParameter.getTagStoreTagId())) {
                throw new IllegalRequestParameterException("tagStoreTagId",
                        getTagParameter.getTagStoreTagId(), "TagStoreTagId can not be empty!");
            } else {
                throw new IllegalRequestParameterException("tagStoreAlias",
                        getTagParameter.getTagStoreTagId(), "TagStoreAlias can not be empty!");
            }
        }

        return ResponseHelper
                .buildSuccessResponse(TagHelper.buildTagResource(tag), request);
    }
}
