package com.communote.server.web.api.service.post;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.service.NoteService;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.to.ApiResult;


/**
 * Api controller to delete a post
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class DeletePostApiController extends BaseRestApiController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws ApiException {
        Long postId = getResourceId(request, true);
        try {
            ServiceLocator.instance().getService(NoteService.class)
                    .deleteNote(postId, false, false);
        } catch (NoteManagementAuthorizationException e) {
            throw new ApiException(e.getMessage());
        }
        // no exception => success but no result
        return null;
    }

}
