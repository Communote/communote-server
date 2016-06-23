package com.communote.server.web.fe.portal.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.support.WebContentGenerator;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.bootstrap.InitializationStatus;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.web.commons.MessageHelper;

/**
 * Controller which answers POST requests with a JSON object containing the current status of the
 * application initialization.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ApplicationInitializationStatusController implements Controller {

    private static final Logger LOG = LoggerFactory
            .getLogger(ApplicationInitializationStatusController.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // only support POST
        if (!request.getMethod().equals(WebContentGenerator.METHOD_POST)) {
            LOG.debug("Ignoring request because it is not a POST");
            return null;
        }

        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        StringBuilder message = new StringBuilder();
        InitializationStatus status = CommunoteRuntime.getInstance().getInitializationStatus();
        if (InitializationStatus.Type.SUCCESS.equals(status.getStatus())) {
            jsonResponse.put("status", "OK");
            String url = StringUtils.removeEnd(request.getRequestURL().toString(),
                    request.getServletPath());
            message.append(MessageHelper.getText(request, "initialization.status.success",
                    new Object[] { url }));
        } else if (InitializationStatus.Type.FAILURE.equals(status.getStatus())) {
            jsonResponse.put("status", "ERROR");
            message.append(MessageHelper.getText(request, "initialization.status.failed"));
        } else {
            jsonResponse.put("status", "PROCESSING");
        }

        jsonResponse.put("message", message.toString());

        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
        return null;
    }
}
