package com.communote.plugins.api.rest.v22.service;

import java.util.HashSet;
import java.util.Set;

import com.communote.plugins.api.rest.v22.resource.attachment.AttachmentResource;
import com.communote.plugins.api.rest.v22.resource.attachment.AttachmentResourceHandler;
import com.communote.plugins.api.rest.v22.resource.note.NoteResource;
import com.communote.plugins.api.rest.v22.resource.note.NoteResourceHandler;
import com.communote.plugins.api.rest.v22.resource.note.favorite.FavoriteResource;
import com.communote.plugins.api.rest.v22.resource.note.favorite.FavoriteResourceHandler;
import com.communote.plugins.api.rest.v22.resource.note.like.LikeResource;
import com.communote.plugins.api.rest.v22.resource.note.like.LikeResourceHandler;
import com.communote.plugins.api.rest.v22.resource.tag.TagResource;
import com.communote.plugins.api.rest.v22.resource.tag.TagResourceHandler;
import com.communote.plugins.api.rest.v22.resource.tag.TagSuggestionListResourceHandler;
import com.communote.plugins.api.rest.v22.resource.tagsuggestionlist.TagSuggestionListResource;
import com.communote.plugins.api.rest.v22.resource.timelinenote.TimelineNoteResource;
import com.communote.plugins.api.rest.v22.resource.timelinenote.TimelineNoteResourceHandler;
import com.communote.plugins.api.rest.v22.resource.timelinetag.TimelineTagResource;
import com.communote.plugins.api.rest.v22.resource.timelinetag.TimelineTagResourceHandler;
import com.communote.plugins.api.rest.v22.resource.timelinetopic.TimelineTopicResource;
import com.communote.plugins.api.rest.v22.resource.timelinetopic.TimelineTopicResourceHandler;
import com.communote.plugins.api.rest.v22.resource.timelineuser.TimelineUserResource;
import com.communote.plugins.api.rest.v22.resource.timelineuser.TimelineUserResourceHandler;
import com.communote.plugins.api.rest.v22.resource.topic.TopicResource;
import com.communote.plugins.api.rest.v22.resource.topic.TopicResourceHandler;
import com.communote.plugins.api.rest.v22.resource.topic.externalobject.ExternalObjectResource;
import com.communote.plugins.api.rest.v22.resource.topic.externalobject.ExternalObjectResourceHandler;
import com.communote.plugins.api.rest.v22.resource.topic.follow.FollowResource;
import com.communote.plugins.api.rest.v22.resource.topic.follow.FollowResourceHandler;
import com.communote.plugins.api.rest.v22.resource.topic.property.PropertyResource;
import com.communote.plugins.api.rest.v22.resource.topic.property.PropertyResourceHandler;
import com.communote.plugins.api.rest.v22.resource.topic.right.RightResource;
import com.communote.plugins.api.rest.v22.resource.topic.right.RightResourceHandler;
import com.communote.plugins.api.rest.v22.resource.topic.role.RoleResource;
import com.communote.plugins.api.rest.v22.resource.topic.role.RoleResourceHandler;
import com.communote.plugins.api.rest.v22.resource.topic.rolebulkexternal.RoleBulkExternalResource;
import com.communote.plugins.api.rest.v22.resource.topic.rolebulkexternal.RoleBulkExternalResourceHandler;
import com.communote.plugins.api.rest.v22.resource.topic.roleexternal.RoleExternalResource;
import com.communote.plugins.api.rest.v22.resource.topic.roleexternal.RoleExternalResourceHandler;
import com.communote.plugins.api.rest.v22.resource.user.UserResource;
import com.communote.plugins.api.rest.v22.resource.user.UserResourceHandler;
import com.communote.plugins.api.rest.v22.resource.user.image.ImageResource;
import com.communote.plugins.api.rest.v22.resource.user.image.ImageResourceHandler;
import com.communote.plugins.api.rest.v22.service.common.ApplicationConfigUtils;
import com.communote.plugins.api.rest.v22.servlet.AbstractCommunoteApplicationConfig;
import com.communote.plugins.api.rest.v22.servlet.ResourceHandlerLocator;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteJaxRsApplicationConfig extends AbstractCommunoteApplicationConfig {

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureResourceHandler() {

        // note resource
        ResourceHandlerLocator locator = ResourceHandlerLocator.getInstance();
        locator.addAssoziation(NoteResource.class,
                new NoteResourceHandler());
        locator.addAssoziation(FavoriteResource.class,
                new FavoriteResourceHandler());
        locator.addAssoziation(LikeResource.class,
                new LikeResourceHandler());
        locator.addAssoziation(
                com.communote.plugins.api.rest.v22.resource.note.property.PropertyResource.class,
                new com.communote.plugins.api.rest.v22.resource.note.property.PropertyResourceHandler());

        // user resource
        locator.addAssoziation(UserResource.class, new UserResourceHandler());
        locator.addAssoziation(ImageResource.class, new ImageResourceHandler());
        locator.addAssoziation(
                com.communote.plugins.api.rest.v22.resource.user.follow.FollowResource.class,
                new com.communote.plugins.api.rest.v22.resource.user.follow.FollowResourceHandler());
        locator.addAssoziation(
                com.communote.plugins.api.rest.v22.resource.user.property.PropertyResource.class,
                new com.communote.plugins.api.rest.v22.resource.user.property.PropertyResourceHandler());

        // attachment resource
        locator.addAssoziation(AttachmentResource.class, new AttachmentResourceHandler());

        // blog resource
        locator.addAssoziation(TopicResource.class, new TopicResourceHandler());
        locator.addAssoziation(ExternalObjectResource.class, new ExternalObjectResourceHandler());
        locator.addAssoziation(FollowResource.class, new FollowResourceHandler());
        locator.addAssoziation(PropertyResource.class, new PropertyResourceHandler());
        locator.addAssoziation(RightResource.class, new RightResourceHandler());
        locator.addAssoziation(RoleResource.class, new RoleResourceHandler());
        locator.addAssoziation(RoleExternalResource.class, new RoleExternalResourceHandler());
        locator.addAssoziation(RoleBulkExternalResource.class,
                new RoleBulkExternalResourceHandler());

        // timeline resources
        locator.addAssoziation(TimelineNoteResource.class, new TimelineNoteResourceHandler());
        locator.addAssoziation(TimelineTopicResource.class, new TimelineTopicResourceHandler());
        locator.addAssoziation(TimelineUserResource.class, new TimelineUserResourceHandler());
        locator.addAssoziation(TimelineTagResource.class, new TimelineTagResourceHandler());

        // tagging resources
        locator.addAssoziation(TagResource.class, new TagResourceHandler());
        locator.addAssoziation(TagSuggestionListResource.class,
                new TagSuggestionListResourceHandler());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<Object>(super.getSingletons());
        return ApplicationConfigUtils.extentSingletonsWithExceptionMappers(singletons);
    }
}
