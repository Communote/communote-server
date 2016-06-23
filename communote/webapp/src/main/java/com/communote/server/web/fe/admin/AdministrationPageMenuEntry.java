package com.communote.server.web.fe.admin;

import javax.servlet.http.HttpServletRequest;

import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Menu entry of the administration menu with a URL pointing to the administration page that will be
 * opened when selecting this entry.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AdministrationPageMenuEntry extends BasicMenuEntry {

    private final String pageUrl;

    /**
     * Create a new admin menu entry
     *
     * @param id
     *            the ID of the entry has to be unique in the whole administration menu
     * @param labelMessageKey
     *            message key of the label
     * @param pageUrl
     *            relative URL of the administration page
     */
    public AdministrationPageMenuEntry(String id, String labelMessageKey, String pageUrl) {
        super(id, labelMessageKey);
        this.pageUrl = pageUrl;
    }

    /**
     * @return relative URL of the administration page
     */
    public String getPageUrl() {
        return pageUrl;
    }

    /**
     * Render the URL of the administration page
     *
     * @param request
     *            the current request
     * @return the rendered URL
     */
    public String renderPageUrl(HttpServletRequest request) {
        return ControllerHelper.renderUrl(request, pageUrl, null, false, false, null, null, false,
                false);
    }

}
