package com.communote.server.web.api.service.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.communote.server.core.security.SecurityHelper;
import com.communote.server.web.api.service.ApiResultApiController;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.api.to.user.LoginInfo;


/**
 * Controller to login in the user by username and password
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead
 */
@Deprecated
public class LoginApiController extends ApiResultApiController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object execute(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) {
        return new LoginInfo(request.getSession().getId(), SecurityHelper.assertCurrentUserId(),
                "-1");
    }
}
