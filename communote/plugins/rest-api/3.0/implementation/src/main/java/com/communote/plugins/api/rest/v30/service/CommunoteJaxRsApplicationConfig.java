package com.communote.plugins.api.rest.v30.service;

import java.util.HashSet;
import java.util.Set;

import com.communote.plugins.api.rest.v30.resource.attachment.AttachmentResource;
import com.communote.plugins.api.rest.v30.resource.attachment.AttachmentResourceHandler;
import com.communote.plugins.api.rest.v30.resource.attachment.preview.PreviewResource;
import com.communote.plugins.api.rest.v30.resource.attachment.preview.PreviewResourceHandler;
import com.communote.plugins.api.rest.v30.resource.group.GroupResource;
import com.communote.plugins.api.rest.v30.resource.group.GroupResourceHandler;
import com.communote.plugins.api.rest.v30.resource.group.member.MemberResource;
import com.communote.plugins.api.rest.v30.resource.group.member.MemberResourceHandler;
import com.communote.plugins.api.rest.v30.resource.lastmodificationdate.LastModificationDateResource;
import com.communote.plugins.api.rest.v30.resource.lastmodificationdate.LastModificationDateResourceHandler;
import com.communote.plugins.api.rest.v30.resource.note.NoteResource;
import com.communote.plugins.api.rest.v30.resource.note.NoteResourceHandler;
import com.communote.plugins.api.rest.v30.resource.note.favorite.FavoriteResource;
import com.communote.plugins.api.rest.v30.resource.note.favorite.FavoriteResourceHandler;
import com.communote.plugins.api.rest.v30.resource.note.like.LikeResource;
import com.communote.plugins.api.rest.v30.resource.note.like.LikeResourceHandler;
import com.communote.plugins.api.rest.v30.resource.tag.TagResource;
import com.communote.plugins.api.rest.v30.resource.tag.TagResourceHandler;
import com.communote.plugins.api.rest.v30.resource.tag.TagSuggestionListResourceHandler;
import com.communote.plugins.api.rest.v30.resource.tagsuggestionlist.TagSuggestionListResource;
import com.communote.plugins.api.rest.v30.resource.timelinenote.TimelineNoteResource;
import com.communote.plugins.api.rest.v30.resource.timelinenote.TimelineNoteResourceHandler;
import com.communote.plugins.api.rest.v30.resource.timelinetag.TimelineTagResource;
import com.communote.plugins.api.rest.v30.resource.timelinetag.TimelineTagResourceHandler;
import com.communote.plugins.api.rest.v30.resource.timelinetopic.TimelineTopicResource;
import com.communote.plugins.api.rest.v30.resource.timelinetopic.TimelineTopicResourceHandler;
import com.communote.plugins.api.rest.v30.resource.timelineuser.TimelineUserResource;
import com.communote.plugins.api.rest.v30.resource.timelineuser.TimelineUserResourceHandler;
import com.communote.plugins.api.rest.v30.resource.topic.TopicResource;
import com.communote.plugins.api.rest.v30.resource.topic.TopicResourceHandler;
import com.communote.plugins.api.rest.v30.resource.topic.childtopic.ChildTopicResource;
import com.communote.plugins.api.rest.v30.resource.topic.childtopic.ChildTopicResourceHandler;
import com.communote.plugins.api.rest.v30.resource.topic.externalobject.ExternalObjectResource;
import com.communote.plugins.api.rest.v30.resource.topic.externalobject.ExternalObjectResourceHandler;
import com.communote.plugins.api.rest.v30.resource.topic.follow.FollowResource;
import com.communote.plugins.api.rest.v30.resource.topic.follow.FollowResourceHandler;
import com.communote.plugins.api.rest.v30.resource.topic.property.PropertyResource;
import com.communote.plugins.api.rest.v30.resource.topic.property.PropertyResourceHandler;
import com.communote.plugins.api.rest.v30.resource.topic.right.RightResource;
import com.communote.plugins.api.rest.v30.resource.topic.right.RightResourceHandler;
import com.communote.plugins.api.rest.v30.resource.topic.role.RoleResource;
import com.communote.plugins.api.rest.v30.resource.topic.role.RoleResourceHandler;
import com.communote.plugins.api.rest.v30.resource.topic.rolebulkexternal.RoleBulkExternalResource;
import com.communote.plugins.api.rest.v30.resource.topic.rolebulkexternal.RoleBulkExternalResourceHandler;
import com.communote.plugins.api.rest.v30.resource.topic.roleexternal.RoleExternalResource;
import com.communote.plugins.api.rest.v30.resource.topic.roleexternal.RoleExternalResourceHandler;
import com.communote.plugins.api.rest.v30.resource.user.UserResource;
import com.communote.plugins.api.rest.v30.resource.user.UserResourceHandler;
import com.communote.plugins.api.rest.v30.resource.user.externallogin.ExternalLoginResource;
import com.communote.plugins.api.rest.v30.resource.user.externallogin.ExternalLoginResourceHandler;
import com.communote.plugins.api.rest.v30.resource.user.image.ImageResource;
import com.communote.plugins.api.rest.v30.resource.user.image.ImageResourceHandler;
import com.communote.plugins.api.rest.v30.resource.user.navigationitem.NavigationItemResource;
import com.communote.plugins.api.rest.v30.resource.user.navigationitem.NavigationItemResourceHandler;
import com.communote.plugins.api.rest.v30.resource.user.observation.ObservationResource;
import com.communote.plugins.api.rest.v30.resource.user.observation.ObservationResourceHandler;
import com.communote.plugins.api.rest.v30.resource.user.preference.UserPreferenceResourceHandler;
import com.communote.plugins.api.rest.v30.resource.user.userpreference.UserPreferenceResource;
import com.communote.plugins.api.rest.v30.service.common.ApplicationConfigUtils;
import com.communote.plugins.api.rest.v30.servlet.AbstractCommunoteApplicationConfig;
import com.communote.plugins.api.rest.v30.servlet.ResourceHandlerLocator;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.service.NavigationItemService;

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
        locator.addAssoziation(NoteResource.class, new NoteResourceHandler());
        locator.addAssoziation(FavoriteResource.class, new FavoriteResourceHandler());
        locator.addAssoziation(LikeResource.class, new LikeResourceHandler());
        locator
                .addAssoziation(
                        com.communote.plugins.api.rest.v30.resource.note.property.PropertyResource.class,
                        new com.communote.plugins.api.rest.v30.resource.note.property.PropertyResourceHandler());

        // user resource
        locator.addAssoziation(UserResource.class, new UserResourceHandler());
        locator.addAssoziation(ImageResource.class, new ImageResourceHandler());
        locator.addAssoziation(UserPreferenceResource.class, new UserPreferenceResourceHandler());
        locator.addAssoziation(
                com.communote.plugins.api.rest.v30.resource.user.follow.FollowResource.class,
                new com.communote.plugins.api.rest.v30.resource.user.follow.FollowResourceHandler());
        locator.addAssoziation(
                com.communote.plugins.api.rest.v30.resource.user.property.PropertyResource.class,
                new com.communote.plugins.api.rest.v30.resource.user.property.PropertyResourceHandler());
        locator.addAssoziation(
                ObservationResource.class, new ObservationResourceHandler(
                        ServiceLocator.findService(NavigationItemService.class), ServiceLocator
                                .findService(QueryManagement.class)));
        locator.addAssoziation(NavigationItemResource.class, new NavigationItemResourceHandler(
                ServiceLocator.findService(NavigationItemService.class)));
        locator.addAssoziation(ExternalLoginResource.class, new ExternalLoginResourceHandler());

        // group resource
        locator.addAssoziation(GroupResource.class, new GroupResourceHandler());
        locator.addAssoziation(MemberResource.class, new MemberResourceHandler());

        // attachment resources
        locator.addAssoziation(AttachmentResource.class, new AttachmentResourceHandler());
        locator.addAssoziation(PreviewResource.class, new PreviewResourceHandler());

        // topic resource
        locator.addAssoziation(TopicResource.class, new TopicResourceHandler());
        locator.addAssoziation(ChildTopicResource.class, new ChildTopicResourceHandler());
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

        // last modifcation dates
        locator.addAssoziation(LastModificationDateResource.class,
                new LastModificationDateResourceHandler());
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
