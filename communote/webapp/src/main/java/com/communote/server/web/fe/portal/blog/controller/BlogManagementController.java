package com.communote.server.web.fe.portal.blog.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.BlogTO;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.blog.BlogManagementException;
import com.communote.server.core.blog.helper.MailBasedPostingHelper;
import com.communote.server.core.converter.blog.BlogToDetailBlogListItemConverter;
import com.communote.server.core.filter.listitems.blog.DetailBlogListItem;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.tag.TagParserFactory;
import com.communote.server.core.tag.impl.CommaTagParser;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.web.commons.FormAction;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.fe.portal.blog.forms.BlogManagementForm;
import com.communote.server.web.fe.widgets.WidgetConstants;

/**
 * Controller for the Blog Management Widget, where a new Blog can be created.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogManagementController extends BaseFormController {

    /**
     * @param blogProperties
     *            Properties.
     * @return List of properties.
     */
    private List<StringPropertyTO> createPropertyToList(Map<String, Object> blogProperties) {
        List<StringPropertyTO> list = new ArrayList<StringPropertyTO>();

        for (Map.Entry<String, Object> entry : blogProperties.entrySet()) {
            StringPropertyTO propertyTo = new StringPropertyTO();
            String[] segments = entry.getKey().split("\\.");
            if (segments.length > 1) {
                String propertyKey = segments[segments.length - 1];
                String keyGroup = StringUtils.chomp(entry.getKey(), "." + propertyKey);

                propertyTo.setKeyGroup(keyGroup);
                propertyTo.setPropertyKey(propertyKey);
                propertyTo.setPropertyValue((String) entry.getValue());
                propertyTo.setLastModificationDate(new Date());

                list.add(propertyTo);
            }
        }
        return list;
    }

    /**
     * This method handles the new blog creation.
     * 
     * @param blogTo
     *            the transfer object for blog creation
     * @param request
     *            the request
     * @param errors
     *            the errors
     * @return The blog.
     */
    private Blog createTopic(CreationBlogTO blogTo, HttpServletRequest request, BindException errors) {
        Blog blog = null;
        try {
            blog = ServiceLocator.instance().getService(BlogManagement.class).createBlog(blogTo);
            MessageHelper.saveMessage(request,
                    MessageHelper.getText(request, "blog.create.success"));
            // TODO if blog == null -> blog creation failed
        } catch (NonUniqueBlogIdentifierException e) {
            errors.rejectValue("nameIdentifier", "error.blog.identifier.noneunique",
                    "The Name Identifier is not unique!");
        } catch (BlogIdentifierValidationException e) {
            errors.rejectValue("nameIdentifier", "error.blog.identifier.notvalid",
                    "An empty Name Identifier is not allowed.");
        } catch (BlogNotFoundException e) {
            MessageHelper.saveErrorMessageFromKey(request, "blog.create.error.parent-not-found");
        } catch (BlogAccessException e) {
            if (TopicPermissionManagement.PERMISSION_CREATE_TOPIC.equals(e.getPermission())) {
                MessageHelper.saveErrorMessageFromKey(request, "blog.create.error.not.allowed");
            } else {
                MessageHelper
                        .saveErrorMessageFromKey(request, "blog.create.error.parent-no-access");
            }
        }
        return blog;
    }

    /**
     * Creates a blog TO.
     * 
     * @param currentUserId
     *            ID of the current user
     * @param form
     *            the backing form object
     * @return the transfer object
     */
    private CreationBlogTO createTopicTO(Long currentUserId, BlogManagementForm form) {
        CreationBlogTO topicTO = new CreationBlogTO();
        topicTO.setCreatorUserId(currentUserId);
        String[] tags = TagParserFactory.instance().getDefaultTagParser().parseTags(form.getTags());
        topicTO.setUnparsedTags(tags);
        topicTO.setTitle(form.getTitle());
        topicTO.setDescription(form.getDescription());
        topicTO.setNameIdentifier(form.getNameIdentifier());
        topicTO.setCreateSystemNotes(form.isCreateSystemNotes());
        topicTO.setProperties(createPropertyToList(form.getBlogProperties()));
        topicTO.setParentTopicId(form.getParentTopicId());
        topicTO.setToplevelTopic(form.isToplevelTopic());
        return topicTO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        Long blogId;
        boolean currentUserFollowsBlog;

        try {
            blogId = Long.parseLong(request.getParameter(WidgetConstants.PARAM_BLOG_ID));
        } catch (Exception e) {
            blogId = null;
        }

        DetailBlogListItem item = null;
        if (blogId != null) {
            BlogRightsManagement topicRightsManagement = ServiceLocator
                    .findService(BlogRightsManagement.class);
            BlogToDetailBlogListItemConverter<DetailBlogListItem> converter =
                    new BlogToDetailBlogListItemConverter<DetailBlogListItem>(
                            DetailBlogListItem.class, true, true, false, true, false,
                            topicRightsManagement, getLocale(request));
            item = ServiceLocator.instance().getService(BlogManagement.class)
                    .getBlogById(blogId, converter);
            BlogRole usersRole = topicRightsManagement.getRoleOfCurrentUser(blogId, false);
            request.setAttribute("isBlogManager", BlogRole.MANAGER.equals(usersRole));
        }

        BlogManagementForm form = new BlogManagementForm(item);
        if (item != null) {
            form.setAction(FormAction.EDIT);
            currentUserFollowsBlog = ServiceLocator.instance().getService(FollowManagement.class)
                    .followsBlog(blogId);
            form.setCurrentUserFollowsBlog(currentUserFollowsBlog);
            form.setTags(new CommaTagParser().buildTagString(item.getTagItems()));
        } else {
            // no blogId provided or blog not found, deletion in progress ?
            form.setAction(FormAction.CREATE);
            form.setParentTopicId(ParameterHelper.getParameterAsLong(request.getParameterMap(),
                    "parentTopicId"));
            form.setToplevelTopic(ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                    "toplevelTopic", false));
        }
        request.setAttribute("emailSuffix",
                MailBasedPostingHelper.getClientWideBlogEmailAddressSuffix());
        return form;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        ModelAndView mav;
        Long currentUserId = SecurityHelper.assertCurrentUserId();
        Blog blog;
        BlogManagementForm form = (BlogManagementForm) command;
        if (StringUtils.equals(form.getAction(), FormAction.CREATE) && form.getBlogId() == null) {
            blog = createTopic(createTopicTO(currentUserId, form), request, errors);
        } else if (StringUtils.equals(form.getAction(), FormAction.EDIT)
                && form.getBlogId() != null) {
            blog = updateTopic(form.getBlogId(), createTopicTO(currentUserId, form), request,
                    errors);
        } else {
            throw new RuntimeException("invalid formular state");
        }
        if (blog != null) {
            updateForm(form, blog);
        }
        if (errors.getErrorCount() > 0 || blog == null) {
            ControllerHelper.setApplicationFailure(response);
            mav = showForm(request, errors, getFormView());
        } else {
            ControllerHelper.setApplicationSuccess(response);
            mav = new ModelAndView(this.getFormView(), getCommandName(), form);
        }
        return mav;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView processFormSubmission(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors) throws Exception {
        if (errors.hasErrors()) {
            ControllerHelper.setApplicationFailure(response);
        }
        return super.processFormSubmission(request, response, command, errors);
    }

    /**
     * Update the form
     * 
     * @param form
     *            the form
     * @param blog
     *            the blog
     * @return the form
     */
    private BlogManagementForm updateForm(BlogManagementForm form, Blog blog) {
        form.setNameIdentifier(blog.getNameIdentifier());
        if (FormAction.CREATE.equals(form.getAction())) {
            form.setAction(FormAction.EDIT);
            form.setBlogId(blog.getId());
        }
        return form;
    }

    /**
     * This method is called when the blog should be updated.
     * 
     * @param blogId
     *            the ID of the blog to update
     * @param blogTo
     *            the blog transfer object holding the details for the update
     * @param request
     *            the request.
     * @param errors
     *            the errors.
     * @return the blog
     */
    private Blog updateTopic(Long blogId, BlogTO blogTo, HttpServletRequest request,
            BindException errors) {
        Blog blog = null;
        try {
            blog = ServiceLocator.instance().getService(BlogManagement.class)
                    .updateBlog(blogId, blogTo);
            if (blog == null) {
                errors.reject("blog.management.error.blog.not.found");
            } else {
                MessageHelper.saveMessage(request, MessageHelper.getText(request,
                        "blog.update.success"));
            }
        } catch (NonUniqueBlogIdentifierException e) {
            errors.rejectValue("nameIdentifier", "error.blog.identifier.noneunique",
                    "The alias is not unique!");
        } catch (BlogIdentifierValidationException e) {
            errors.rejectValue("error.blog.identifier.notvalid",
                    "The alias is not valid.");
        } catch (BlogAccessException e) {
            errors.reject("error.blogpost.blog.no.access.no.manager");
        } catch (BlogManagementException e) {
            errors.reject("blog.management.update.error");
        }
        return blog;
    }

}
