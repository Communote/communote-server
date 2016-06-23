package com.communote.server.web.api.service.blog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.to.ApiResult;


/**
 * Api controller to delete a blog
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class DeleteBlogApiController extends BaseRestApiController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws HttpRequestMethodNotSupportedException,
            ApiException {
        Long blogId = getResourceId(request, true);
        try {
            ServiceLocator.instance().getService(BlogManagement.class).deleteBlog(blogId, null);
        } catch (NoteManagementAuthorizationException e) {
            throw new ApiException(e.getMessage());
        } catch (BlogNotFoundException e) {
            throw new ApiException(e.getMessage());
        }
        // no exception => success but no result
        return null;
    }

}
