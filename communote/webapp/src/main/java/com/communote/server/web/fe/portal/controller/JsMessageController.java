package com.communote.server.web.fe.portal.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.common.util.ParameterHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.i18n.JsMessagesRegistry;

/**
 * Simple Controller for JS Messages
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class JsMessageController implements Controller {

    /** message category */
    private final static String CATEGORY_PARAMETER = "category";

    /**
     * Returns the messages by category in user language
     * 
     * @param request
     *            The HttpServletRequest
     * @param response
     *            The HttpServletResponse
     * @return null
     * @throws Exception
     *             Exception
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

//return common category by default
String category = ParameterHelper.getParameterAsString(request.getParameterMap(),
        CATEGORY_PARAMETER, JsMessagesRegistry.CATEGORY_COMMON);
        JsMessagesRegistry messagesRegistry = WebServiceLocator.instance().getJsMessagesRegistry();
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        String eTagValue = "\"" + locale.getLanguage() + "\"";
        // round timestamp down to nearest second, since the date header is not supporting
        // milliseconds
        long modifiedTimestamp = (messagesRegistry.getLastModificationTimestamp(category) / 1000L) * 1000L;
        // ETag must also be added when sending 304
        response.setHeader("ETag", eTagValue);
        // check conditional Header
        String nonMatch = request.getHeader("If-None-Match");
        if (nonMatch != null && nonMatch.contains(eTagValue)
                && modifiedTimestamp == request.getDateHeader("If-Modified-Since")) {
            response.setStatus(304);
        } else {
            response.addDateHeader("Last-Modified", modifiedTimestamp);
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "max-age=0, public");
            // get messages and write response
            response.getWriter().write(messagesRegistry.getJsMessages(request, category));
        }
        return null;
    }
}