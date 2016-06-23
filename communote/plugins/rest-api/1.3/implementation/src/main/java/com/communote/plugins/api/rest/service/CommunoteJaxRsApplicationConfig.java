package com.communote.plugins.api.rest.service;

import java.util.HashSet;
import java.util.Set;

import com.communote.plugins.api.rest.resource.attachment.AttachmentResource;
import com.communote.plugins.api.rest.resource.attachment.AttachmentResourceHandler;
import com.communote.plugins.api.rest.resource.note.NoteResource;
import com.communote.plugins.api.rest.resource.note.NoteResourceHandler;
import com.communote.plugins.api.rest.resource.note.favorite.FavoriteResource;
import com.communote.plugins.api.rest.resource.note.favorite.FavoriteResourceHandler;
import com.communote.plugins.api.rest.resource.note.like.LikeResource;
import com.communote.plugins.api.rest.resource.note.like.LikeResourceHandler;
import com.communote.plugins.api.rest.resource.tag.TagResource;
import com.communote.plugins.api.rest.resource.tag.TagResourceHandler;
import com.communote.plugins.api.rest.resource.tag.TagSuggestionListResourceHandler;
import com.communote.plugins.api.rest.resource.tagsuggestionlist.TagSuggestionListResource;
import com.communote.plugins.api.rest.resource.timelinenote.TimelineNoteResource;
import com.communote.plugins.api.rest.resource.timelinenote.TimelineNoteResourceHandler;
import com.communote.plugins.api.rest.resource.timelinetag.TimelineTagResource;
import com.communote.plugins.api.rest.resource.timelinetag.TimelineTagResourceHandler;
import com.communote.plugins.api.rest.resource.timelinetopic.TimelineTopicResource;
import com.communote.plugins.api.rest.resource.timelinetopic.TimelineTopicResourceHandler;
import com.communote.plugins.api.rest.resource.timelineuser.TimelineUserResource;
import com.communote.plugins.api.rest.resource.timelineuser.TimelineUserResourceHandler;
import com.communote.plugins.api.rest.resource.topic.TopicResource;
import com.communote.plugins.api.rest.resource.topic.TopicResourceHandler;
import com.communote.plugins.api.rest.resource.topic.follow.FollowResource;
import com.communote.plugins.api.rest.resource.topic.follow.FollowResourceHandler;
import com.communote.plugins.api.rest.resource.topic.property.PropertyResource;
import com.communote.plugins.api.rest.resource.topic.property.PropertyResourceHandler;
import com.communote.plugins.api.rest.resource.topic.right.RightResource;
import com.communote.plugins.api.rest.resource.topic.right.RightResourceHandler;
import com.communote.plugins.api.rest.resource.topic.role.RoleResource;
import com.communote.plugins.api.rest.resource.topic.role.RoleResourceHandler;
import com.communote.plugins.api.rest.resource.topic.rolebulkexternal.RoleBulkExternalResource;
import com.communote.plugins.api.rest.resource.topic.rolebulkexternal.RoleBulkExternalResourceHandler;
import com.communote.plugins.api.rest.resource.topic.roleexternal.RoleExternalResource;
import com.communote.plugins.api.rest.resource.topic.roleexternal.RoleExternalResourceHandler;
import com.communote.plugins.api.rest.resource.user.UserResource;
import com.communote.plugins.api.rest.resource.user.UserResourceHandler;
import com.communote.plugins.api.rest.resource.user.image.ImageResource;
import com.communote.plugins.api.rest.resource.user.image.ImageResourceHandler;
import com.communote.plugins.api.rest.service.common.ApplicationConfigUtils;
import com.communote.plugins.api.rest.servlet.AbstractCommunoteApplicationConfig;
import com.communote.plugins.api.rest.servlet.ResourceHandlerLocator;

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
        ResourceHandlerLocator.getInstance().addAssoziation(NoteResource.class,
                new NoteResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(FavoriteResource.class,
                new FavoriteResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(LikeResource.class,
                new LikeResourceHandler());
        ResourceHandlerLocator
                .getInstance()
                .addAssoziation(
                        com.communote.plugins.api.rest.resource.note.property.PropertyResource.class,
                        new com.communote.plugins.api.rest.resource.note.property.PropertyResourceHandler());

        // user resource
        ResourceHandlerLocator.getInstance().addAssoziation(UserResource.class,
                new UserResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(ImageResource.class,
                new ImageResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(
                com.communote.plugins.api.rest.resource.user.follow.FollowResource.class,
                new com.communote.plugins.api.rest.resource.user.follow.FollowResourceHandler());
        ResourceHandlerLocator
                .getInstance()
                .addAssoziation(
                        com.communote.plugins.api.rest.resource.user.property.PropertyResource.class,
                        new com.communote.plugins.api.rest.resource.user.property.PropertyResourceHandler());

        // attachment resource
        ResourceHandlerLocator.getInstance().addAssoziation(AttachmentResource.class,
                new AttachmentResourceHandler());

        // blog resource
        ResourceHandlerLocator.getInstance().addAssoziation(TopicResource.class,
                new TopicResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(FollowResource.class,
                new FollowResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(PropertyResource.class,
                new PropertyResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(RightResource.class,
                new RightResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(RoleResource.class,
                new RoleResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(RoleExternalResource.class,
                new RoleExternalResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(RoleBulkExternalResource.class,
                new RoleBulkExternalResourceHandler());

        // timeline resources
        ResourceHandlerLocator.getInstance().addAssoziation(TimelineNoteResource.class,
                new TimelineNoteResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(TimelineTopicResource.class,
                new TimelineTopicResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(TimelineUserResource.class,
                new TimelineUserResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(TimelineTagResource.class,
                new TimelineTagResourceHandler());

        // tagging resources
        ResourceHandlerLocator.getInstance().addAssoziation(TagResource.class,
                new TagResourceHandler());
        ResourceHandlerLocator.getInstance().addAssoziation(TagSuggestionListResource.class,
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
