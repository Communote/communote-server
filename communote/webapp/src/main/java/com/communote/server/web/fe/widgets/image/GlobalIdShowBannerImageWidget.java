package com.communote.server.web.fe.widgets.image;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.core.user.helper.UserNameFormat;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserProfile;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.widgets.annotations.AnnotatedSingleResultWidget;
import com.communote.server.widgets.annotations.ViewIdentifier;

/**
 * This widget is used to upload images for entities with Global Ids.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@ViewIdentifier("widget.entity.global-id.show-banner.image")
public class GlobalIdShowBannerImageWidget extends AnnotatedSingleResultWidget {
    private final static String PARAMETER_TITLE = "title";
    private final static String PARAMETER_DESCRIPTION = "description";
    private final static String PARAMETER_ENTITY_ID_FOR_IMAGE = "entityIdForImage";
    private final static String PARAMETER_ENTITY_ID = "entityId";
    private final static String PARAMETER_FOLLOWS_ENTITY = "followsEntity";
    private final static String PARAMETER_FOLLOW_TYPE = "followType";

    /**
     * Add a useful title and the default banner image when an entity was not found. Also sets some
     * response metadata to inform the client-side widget that the entity was not found.
     * 
     * @param entityType
     *            the type of the entity which can be 'topic', 'user' or 'tag'
     * @param entityId
     *            the ID of the entity
     * @param result
     *            the singleResult container
     * @param locale
     *            the current user's locale
     */
    private void handleEntityNotFound(String entityType, Long entityId, Map<String, Object> result,
            Locale locale) {
        result.put(PARAMETER_ENTITY_ID_FOR_IMAGE, "default");
        result.put(
                PARAMETER_TITLE,
                ResourceBundleManager.instance().getText(
                        "common.error." + entityType + ".not.found", locale));
        HashMap<String, Object> details = new HashMap<>();
        details.put("id", entityId);
        details.put("type", entityType);
        setResponseMetadata("entityNotFound", details);
    }

    /**
     * abstract method hook that initializes the widget's parameters
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }

    /**
     * @return the result
     */
    @Override
    protected Object processSingleResult() {
        String contextId = getParameter("contextId");
        Locale locale = SessionHandler.instance().getCurrentLocale(getRequest());
        Map<String, Object> result = new HashMap<String, Object>();
        if ("topicSelected".equals(contextId) || "topicEdit".equals(contextId)) {
            setTopicData(getParameter("targetBlogId"), result, "topicEdit".equals(contextId),
                    locale);
        } else if ("userSelected".equals(contextId) || "userEdit".equals(contextId)) {
            setUserData(getParameter("userId"), result, "userEdit".equals(contextId), locale);
        } else if ("tagSelected".equals(contextId) || "tagEdit".equals(contextId)) {
            setTagData(getParameter("tagIds"), result, locale);
        } else {
            result.put(PARAMETER_TITLE,
                    getParameter("title", ClientHelper.getCurrentClient().getName()));
            result.put(PARAMETER_ENTITY_ID_FOR_IMAGE, "default");
        }
        return result;
    }

    /**
     * Add description to the result if it is not blank or null
     * 
     * @param description
     *            the description to add
     * @param result
     *            the map to add the description to
     */
    private void setDescription(String description, Map<String, Object> result) {
        if (description != null) {
            description = description.trim();
            if (description.length() > 0) {
                result.put(PARAMETER_DESCRIPTION, description);
            }
        }
    }

    /**
     * Method to set the tag data.
     * 
     * @param tagIds
     *            The tag to load (we only allow one).
     * @param result
     *            Map to add the data to.
     * @param locale
     *            current user's locale
     */
    private void setTagData(String tagIds, Map<String, Object> result, Locale locale) {
        Long tagId = Long.parseLong(tagIds);
        TagManagement tagManagement = ServiceLocator.findService(TagManagement.class);
        TagData tag = tagManagement.findTag(tagId, locale);
        if (tag != null) {
            result.put(PARAMETER_TITLE, tag.getName());
            setDescription(
                    ResourceBundleManager.instance().getText("tag.view.used.count.explodes.false",
                            locale, tagManagement.getCount(tagId)), result);
            result.put(PARAMETER_ENTITY_ID_FOR_IMAGE, "tag." + tagIds);
            result.put(PARAMETER_ENTITY_ID, tagId);
            result.put(PARAMETER_FOLLOW_TYPE, "Tag");
            result.put(PARAMETER_FOLLOWS_ENTITY, ServiceLocator.findService(FollowManagement.class)
                    .followsTag(tagId));
        } else {
            handleEntityNotFound("tag", tagId, result, locale);
        }
    }

    /**
     * Method to set the topic data.
     * 
     * @param targetBlogId
     *            Id of the topic to load.
     * @param result
     *            Map to add the data to.
     * @param editMode
     *            true if the context is the edit mode
     * @param locale
     *            current user's locale
     */
    private void setTopicData(String targetBlogId, Map<String, Object> result, boolean editMode,
            Locale locale) {
        Long topicId = Long.parseLong(targetBlogId);
        try {
            Blog topic = ServiceLocator.findService(BlogManagement.class).getBlogById(topicId,
                    false);
            if (editMode) {
                result.put(PARAMETER_TITLE,
                        MessageHelper.getText(getRequest(), "blog.edit.heading", new Object[] {
                                topic.getTitle() }));
            } else {
                result.put(PARAMETER_TITLE, topic.getTitle());
                result.put(PARAMETER_FOLLOW_TYPE, "Blog");
                result.put(PARAMETER_FOLLOWS_ENTITY,
                        ServiceLocator.findService(FollowManagement.class)
                                .followsBlog(topicId));
                setDescription(topic.getDescription(), result);
            }
            result.put(PARAMETER_ENTITY_ID_FOR_IMAGE, "topic." + targetBlogId);
            result.put(PARAMETER_ENTITY_ID, topicId);
        } catch (BlogNotFoundException e) {
            handleEntityNotFound("topic", topicId, result, locale);
        } catch (BlogAccessException e) {
            result.put(PARAMETER_ENTITY_ID_FOR_IMAGE, "default");
            result.put(PARAMETER_TITLE,
                    ResourceBundleManager.instance().getText(
                            "common.error.topic.no.access", locale));
        }
    }

    /**
     * Method to set the user data.
     * 
     * @param userIdAsString
     *            The users id as String.
     * @param result
     *            Map to add the data to.
     * @param editMode
     *            true if the context is the edit mode
     * @param locale
     *            current user's locale
     */
    private void setUserData(String userIdAsString, Map<String, Object> result, boolean editMode,
            Locale locale) {
        Long userId = Long.parseLong(userIdAsString);
        User user = ServiceLocator.findService(UserManagement.class).findUserByUserId(
                userId, false);
        if (user != null) {
            String description = "";
            description = description.trim();
            if (editMode) {
                result.put(PARAMETER_TITLE,
                        MessageHelper.getText(getRequest(), "user.edit.heading"));
            } else {
                result.put(PARAMETER_TITLE,
                        UserNameHelper.getUserSignature(user, UserNameFormat.MEDIUM));
                UserProfile profile = ServiceLocator.findService(UserProfileManagement.class)
                        .findUserProfileByUserId(userId);
                if (profile != null) {
                    if (profile.getPosition() != null && profile.getPosition().length() > 0) {
                        description += profile.getPosition();
                    }
                    if (profile.getCompany() != null && profile.getCompany().length() > 0) {
                        description += " (" + profile.getCompany() + ")";
                    }
                }
                setDescription(description, result);
                result.put(PARAMETER_FOLLOW_TYPE, "User");
                result.put(PARAMETER_FOLLOWS_ENTITY,
                        ServiceLocator.findService(FollowManagement.class)
                                .followsUser(userId));
            }
            result.put(PARAMETER_ENTITY_ID_FOR_IMAGE, "user." + userId);
            result.put(PARAMETER_ENTITY_ID, userId);
        } else {
            handleEntityNotFound("user", userId, result, locale);
        }
    }

}
