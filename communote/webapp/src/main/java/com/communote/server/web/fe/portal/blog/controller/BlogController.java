package com.communote.server.web.fe.portal.blog.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Ajax function for the blog member management and other blog functionality as delete
 * <p>
 * TODO rename me to BlogRightsController and document me
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogController extends MultiActionController {

    private static final String PARAM_BLOG_ID = "blogId";
    private static final String PARAM_NEW_BLOG_ALIAS = "newBlogAlias";

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(BlogController.class);

    /**
     * Sets the blog user role.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             exception.
     */
    public void deleteBlog(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String errorMessage = null;
        try {
            BlogManagement blogManagement = ServiceLocator.findService(BlogManagement.class);
            Long blogId = ServletRequestUtils.getLongParameter(request, PARAM_BLOG_ID);
            Long newBlogId = null;
            String newBlogAlias = request.getParameter(PARAM_NEW_BLOG_ALIAS);
            if (newBlogAlias != null && newBlogAlias.trim().length() > 0) {
                Blog newBlog = blogManagement.findBlogByIdentifier(newBlogAlias);
                if (newBlog != null) {
                    newBlogId = newBlog.getId();
                }
            }
            if (blogId != null) {
                blogManagement.deleteBlog(blogId, newBlogId);
                ControllerHelper.setApplicationSuccess(response);
            } else {
                errorMessage = MessageHelper.getText(request, "blog.delete.failed.no.blog");
            }
        } catch (BlogNotFoundException e) {
            String[] failedBlog = new String[1];
            failedBlog[0] = e.getBlogNameId() != null ? e.getBlogNameId() : e.getBlogId()
                    .toString();
            errorMessage = MessageHelper.getText(request, "error.blogpost.blog.not.found",
                    failedBlog);
        } catch (NoteManagementAuthorizationException e) {
            errorMessage = MessageHelper.getText(request, "error.blogpost.blog.no.write.access",
                    new String[] { e.getBlogTitle() });
        } catch (Exception e) {
            LOGGER.error("Error deleting blog '" + request.getParameter(PARAM_BLOG_ID) + "'", e);
            errorMessage = MessageHelper.getText(request, "blog.delete.failed.unspecific");
        }
        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        if (errorMessage != null) {
            ControllerHelper.setApplicationFailure(response);
            jsonResponse.put("errorMessage", errorMessage);
        }
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }

}
