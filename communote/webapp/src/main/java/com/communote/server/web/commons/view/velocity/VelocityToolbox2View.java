package com.communote.server.web.commons.view.velocity;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.ViewToolContext;
import org.apache.velocity.tools.view.ViewToolManager;
import org.springframework.web.servlet.view.velocity.VelocityToolboxView;

import com.communote.server.web.WebServiceLocator;

/**
 * A velocity toolbox view that supports the velocity tools of version 2.0
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class VelocityToolbox2View extends VelocityToolboxView {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Context createVelocityContext(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // create view context that contains tools, request, response and servlet context
        ViewToolContext context = getToolManager().createContext(request, response);
        context.putAll(model);
        return context;
    }

    /**
     * Returns the shared tool manger
     *
     * @return the manager
     */
    private ViewToolManager getToolManager() {
        return WebServiceLocator.instance().getService(VelocityTemplateManager.class)
                .getToolManager();
    }
}
