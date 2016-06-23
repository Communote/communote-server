package com.communote.server.web.api.service.blog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.api.to.ApiResult.ResultStatus;
import com.communote.server.web.commons.MessageHelper;

/**
 * Api controller to set the blog rights
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class BlogRightApiController extends BaseRestApiController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws HttpRequestMethodNotSupportedException,
            ApiException {

        String errorMessageKey = null;

        Long blogId = getLongParameter(request, "blogId");
        Boolean allCanRead = getBooleanParameter(request, "allCanRead");
        Boolean allCanWrite = getBooleanParameter(request, "allCanWrite");

        // TODO IMPLEMENT_ME - validate params for not being null
        try {
            getBlogRightsManagement().setAllCanReadAllCanWrite(blogId, allCanRead, allCanWrite);
        } catch (BlogNotFoundException e) {
            errorMessageKey = "error.blog.change.rights.failed.noBlog";
        } catch (BlogAccessException e) {
            errorMessageKey = "error.blog.change.rights.failed.noBlogAccess";
        }

        if (errorMessageKey != null) {
            String message = MessageHelper.getText(request, errorMessageKey);
            apiResult.setMessage(message);
            apiResult.setStatus(ResultStatus.ERROR.name());
        }

        // nothing to return
        return null;
    }

    /**
     * @return the blog management
     */
    private BlogRightsManagement getBlogRightsManagement() {
        return ServiceLocator.findService(BlogRightsManagement.class);
    }

}
