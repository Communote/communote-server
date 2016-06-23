package com.communote.server.web.api.service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.io.BinaryData;


/**
 * BinaryApiController is the base class for all API calls returning binary data.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead
 */
@Deprecated
public abstract class BinaryApiController extends BaseApiController {

    private final static Logger LOG = Logger.getLogger(BinaryApiController.class);

    /**
     * This methods executes the main API action and returns the binary data to be send back.
     * 
     * @param request
     *            the http request
     * @param response
     *            the http resptonse
     * @return the BinaryData
     * @throws ApiException
     *             in case of an illegal use of the api (wrong parameters, invalid resources)
     * @throws ServletException
     *             in case of an request error
     */
    protected abstract BinaryData execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, ApiException;

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        try {
            BinaryData data = execute(request, response);

            response.setContentType(data.getContentType());
            response.setContentLength(data.getData().length);

            response.getOutputStream().write(data.getData());

        } catch (AuthenticationException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error executing API request for " + request.getRequestURI() + "! "
                        + e.getMessage(), e);
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        } catch (Throwable e) {
            LOG.fatal("Error executing API request for " + request.getRequestURI() + "! "
                    + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return null;
    }
}
