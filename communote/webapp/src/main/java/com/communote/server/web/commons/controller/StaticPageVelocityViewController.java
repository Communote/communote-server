package com.communote.server.web.commons.controller;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Special velocity view manager which shows static content which can be configured with
 * {@link StaticPageContentManager}. The list of static page sections will be exposed under the name
 * <code>pageSections</code>.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class StaticPageVelocityViewController extends VelocityViewController {

    private StaticPageContentManager contentManager;

    /**
     * @return the content manager of this controller
     */
    public StaticPageContentManager getContentManager() {
        return contentManager;
    }

    @Override
    protected boolean prepareModel(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws Exception {

        if (contentManager != null && contentManager.getSections() != null) {
            model.put("pageSections", contentManager.getSections());
        } else {
            model.put("pageSections", Collections.EMPTY_LIST);
        }
        return super.prepareModel(request, response, model);
    }

    /**
     * Set the content manager
     *
     * @param contentManager
     *            the manager of the static content
     */
    public void setContentManager(StaticPageContentManager contentManager) {
        this.contentManager = contentManager;
    }

}
