package com.communote.server.web.fe.widgets.blog;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.codehaus.jackson.node.ArrayNode;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.blog.TopicHierarchyManagement;
import com.communote.server.core.blog.ToplevelTopicCannotBeChildException;
import com.communote.server.core.blog.ToplevelTopicIsAlreadyChildBlogManagementException;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.converter.blog.BlogToDetailBlogListItemConverter;
import com.communote.server.core.exception.ExceptionMapperManagement;
import com.communote.server.core.exception.Status;
import com.communote.server.core.filter.listitems.blog.DetailBlogListItem;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.blog.TopicStructureTO;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.persistence.blog.ParentIsAlreadyChildDataIntegrityViolationException;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.fe.portal.blog.helper.BlogSearchHelper;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget for editing the topic structure.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EditTopicStructureWidget extends AbstractWidget {

    /**
     * Export the children of the topic to the response metadata as a JSON array
     * 
     * @param children
     *            the children to export
     */
    private void exportChildrenToMetadata(List<DetailBlogListItem> children) {
        children = BlogManagementHelper.sortedBlogList(children);
        ArrayNode result = JsonHelper.getSharedObjectMapper().createArrayNode();
        for (DetailBlogListItem child : children) {
            result.add(BlogSearchHelper.createBlogSearchJSONResult(child.getId(),
                    child.getNameIdentifier(), child.getTitle(), false));
        }
        this.setResponseMetadata("childTopics", result);
    }

    /**
     * @return the locale of the current user. will never be null
     */
    protected Locale getLocale() {
        return SessionHandler.instance().getCurrentLocale(getRequest());
    }

    @Override
    @Deprecated
    public String getTile(String outputType) {
        return "core.widget.topic.editStructure";
    }

    @Override
    public Object handleRequest() {
        DetailBlogListItem item = null;
        Status errorStatus = null;
        Long blogId = getLongParameter("blogId", -1L);
        BlogRightsManagement topicRightsManagement = ServiceLocator
                .findService(BlogRightsManagement.class);
        Locale locale = getLocale();
        boolean isSubmit = getBooleanParameter("isSubmit", false);
        if (blogId > -1 && topicRightsManagement.currentUserHasManagementAccess(blogId)) {
            if (isSubmit) {
                errorStatus = handleSubmit(blogId);
            }
            BlogToDetailBlogListItemConverter<DetailBlogListItem> converter =
                    new BlogToDetailBlogListItemConverter<DetailBlogListItem>(
                            DetailBlogListItem.class, true, true, false, true, false,
                            topicRightsManagement, locale);

            try {
                item = ServiceLocator.instance().getService(BlogManagement.class)
                        .getBlogById(blogId, converter);
            } catch (BlogAccessException e) {
                // should not occur
                errorStatus = ServiceLocator.findService(ExceptionMapperManagement.class)
                        .mapException(e);
            }
            if (item != null && item.getChildren() != null) {
                exportChildrenToMetadata(item.getChildren());
            }
        } else {
            errorStatus = ServiceLocator.findService(ExceptionMapperManagement.class).mapException(
                    new BlogAccessException("No access to topic", blogId, BlogRole.MANAGER, null));
        }
        if (errorStatus != null) {
            MessageHelper.saveErrorMessage(getRequest(), errorStatus.getMessage().toString(locale));
            if (isSubmit) {
                ControllerHelper.setApplicationFailure(getResponse());
                setSuccess(false);
            }
        } else if (isSubmit) {
            MessageHelper.saveMessageFromKey(getRequest(),
                    "widget.editTopicStructure.update.success");
        }
        return item;
    }

    /**
     * Update the topic structure for the given topic
     * 
     * @param blogId
     *            the ID of the topic
     * @return error status object or null if there were no errors
     */
    private Status handleSubmit(Long blogId) {
        TopicStructureTO structureTO = new TopicStructureTO();
        String toplevelTopic = getParameter("toplevelTopic", null);
        if (toplevelTopic != null) {
            structureTO.setToplevel(Boolean.valueOf(toplevelTopic));
        }
        Long[] topicIds = getLongArrayParameter("childTopicIdsToRemove");
        if (topicIds != null) {
            structureTO.setChildTopicsToRemove(Arrays.asList(topicIds));
        }
        topicIds = getLongArrayParameter("childTopicIdsToAdd");
        if (topicIds != null) {
            structureTO.setChildTopicsToAdd(Arrays.asList(topicIds));
        }
        ExceptionMapperManagement exceptionMapper = ServiceLocator
                .findService(ExceptionMapperManagement.class);
        Status errorStatus = null;
        try {
            ServiceLocator.findService(TopicHierarchyManagement.class).updateTopicStructure(blogId,
                    structureTO);
        } catch (BlogNotFoundException e) {
            errorStatus = exceptionMapper.mapException(e);
        } catch (BlogAccessException e) {
            errorStatus = exceptionMapper.mapException(e);
        } catch (AuthorizationException e) {
            errorStatus = exceptionMapper.mapException(e);
        } catch (ToplevelTopicCannotBeChildException e) {
            errorStatus = exceptionMapper.mapException(e);
        } catch (ToplevelTopicIsAlreadyChildBlogManagementException e) {
            errorStatus = exceptionMapper.mapException(e);
        } catch (ParentIsAlreadyChildDataIntegrityViolationException e) {
            errorStatus = exceptionMapper.mapException(e);
        }
        return errorStatus;
    }

    @Override
    protected void initParameters() {
        // nothing to do here
    }

    /**
     * @param listItem
     *            the list item holding the topic details
     * @return whether it is possible to set the top-level flag of a topic
     */
    public boolean isSettingToplevelTopicEnabled(DetailBlogListItem listItem) {
        // Editing is also enabled if the topic is already a top level topic. This way
        // the user is able to remove the flag, even if top level hierarchies are disabled.
        // We assume her, that the is is topic manager.
        return SecurityHelper.isClientManager()
                && (listItem.isToplevelTopic() || (ClientProperty.TOP_LEVEL_TOPICS_ENABLED
                        .getValue(ClientProperty.DEFAULT_TOP_LEVEL_TOPICS_ENABLED)));
    }
}
