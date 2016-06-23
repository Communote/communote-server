package com.communote.plugins.bookmarklet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.osgi.framework.BundleContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.plugins.core.views.ViewController;
import com.communote.plugins.core.views.ViewControllerException;
import com.communote.plugins.core.views.annotations.Page;
import com.communote.plugins.core.views.annotations.UrlMapping;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;

/**
 * Controller for the the Widget to list the top n workspaces.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */

@Component
@Provides
@Instantiate(name = "BookmarkletController")
@UrlMapping("/*/bookmarklet")
@Page(menu = "bookmarklet", titleKey = "bookmarklet.pageTitle", jsMessagesCategory = "portal", jsCategories = {
        "tinyMCE", "communote-core", "portal" }, cssCategories = { "bookmarklet" })
public class BookmarkletController extends ViewController implements Controller {

    /**
     * @param bundleContext
     *            The current bundle context.
     */
    public BookmarkletController(BundleContext bundleContext) {
        super(bundleContext.getBundle().getSymbolicName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentTemplate() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = super.handleRequest(request, response);
        modelAndView.setViewName("special.page.bookmarklet");
        return modelAndView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ? extends Object> processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ViewControllerException {
        String blogAlias = request.getParameter("a");
        Map<String, Object> model = new HashMap<String, Object>();
        if (blogAlias != null) {
            Blog blog;
            try {
                blog = ServiceLocator.instance().getService(BlogManagement.class)
                        .findBlogByIdentifier(blogAlias);
            } catch (BlogAccessException e) {
                throw new ViewControllerException(HttpServletResponse.SC_FORBIDDEN,
                        "Current user has no access to the topic " + blogAlias, e);
            }
            if (blog != null) {
                model.put("blog", blog);
                BlogRole role = ServiceLocator.instance().getService(BlogRightsManagement.class)
                        .getRoleOfCurrentUser(blog.getId(), false);
                model.put("blogRole", role.toString());
            }
        }
        return model;
    }
}
