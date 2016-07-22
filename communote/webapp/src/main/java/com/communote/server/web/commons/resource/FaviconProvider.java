package com.communote.server.web.commons.resource;

import javax.servlet.http.HttpServletRequest;

import com.communote.common.util.Orderable;

/**
 * Provider which defines the URL from which a favicon icon can be downloaded.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface FaviconProvider extends Orderable {

    /**
     * Return the absolute or relative URL to the favicon icon exposed by this provider. The URL
     * should contain a timestamp parameter or something comparable which reflects the time of the
     * last modification of the icon to ensure that the URL changes when the icon is modified and
     * thus, avoid unwanted browser caching effects.
     *
     * @param request
     *            the current request
     * @return the URL to the favicon icon
     */
    String getUrl(HttpServletRequest request);
}
