package com.communote.server.web.api.service.blog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.core.blog.BlogRightsManagementException;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.service.IllegalRequestParameterException;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.api.to.ApiResult.ResultStatus;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class BlogPublicAccessApiController extends BaseRestApiController {

    /** The Constant PARAM_BLOG. */
    public final static String PARAM_BLOG = "blogId";

    /** The Constant PARAM_PUBLIC_ACCESS. */
    public final static String PARAM_PUBLIC_ACCESS = "publicAccess";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws IllegalRequestParameterException {

        Long blogId = getLongParameter(request, PARAM_BLOG);
        boolean allowPublicAccess = getBooleanParameter(request, PARAM_PUBLIC_ACCESS);

        try {
            getBlogRightsManagement().changePublicAccess(blogId, allowPublicAccess);
        } catch (BlogNotFoundException e) {
            apiResult.setStatus(ResultStatus.ERROR.name());
            apiResult.setMessage(ResourceBundleManager.instance().getText(
                    "blog.management.error.blog.not.found",
                    SessionHandler.instance().getCurrentLocale(request)));
        } catch (BlogAccessException e) {
            apiResult.setStatus(ResultStatus.ERROR.name());
            apiResult.setMessage(ResourceBundleManager.instance().getText(
                    "error.blog.change.rights.public.access.failed",
                    SessionHandler.instance().getCurrentLocale(request)));
        } catch (BlogRightsManagementException e) {
            apiResult.setStatus(ResultStatus.ERROR.name());
            apiResult.setMessage(ResourceBundleManager.instance().getText(
                    "error.blog.change.rights.public.access.failed",
                    SessionHandler.instance().getCurrentLocale(request)));
        }
        // no exception => success but no result
        return null;
    }

    /**
     * @return the blog management
     */
    private BlogRightsManagement getBlogRightsManagement() {
        return ServiceLocator.findService(BlogRightsManagement.class);
    }

}
