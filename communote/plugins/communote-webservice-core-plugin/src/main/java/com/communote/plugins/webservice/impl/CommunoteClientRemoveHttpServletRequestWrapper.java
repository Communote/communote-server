package com.communote.plugins.webservice.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.communote.server.persistence.user.client.ClientUrlHelper;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteClientRemoveHttpServletRequestWrapper extends
        HttpServletRequestWrapper {

    public CommunoteClientRemoveHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getRequestURI() {
        String requestUri = super.getRequestURI();
        requestUri = ClientUrlHelper.removeIds(requestUri);
        return requestUri;
    }

}