package com.communote.plugins.api.rest.v30.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.v30.resource.tag.TagHelper;
import com.communote.plugins.api.rest.v30.resource.tag.TagResource;
import com.communote.plugins.api.rest.v30.resource.topic.TopicResource;
import com.communote.plugins.api.rest.v30.resource.topic.childtopic.ChildTopicResource;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.type.EntityBannerImageDescriptor;
import com.communote.server.api.core.image.type.EntityProfileImageDescriptor;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.converter.blog.BlogToUserDetailBlogListItemConverter;
import com.communote.server.core.filter.listitems.blog.BlogTagListItem;
import com.communote.server.core.filter.listitems.blog.DetailBlogListItem;
import com.communote.server.core.filter.listitems.blog.UserDetailBlogListItem;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.image.type.EntityImageManagement;
import com.communote.server.core.image.type.EntityImageManagement.ImageType;
import com.communote.server.core.lastmodifieddate.LastModificationDateManagement;
import com.communote.server.core.vo.query.blog.DataAccessBlogConverter;
import com.communote.server.model.blog.BlogRole;

/**
 * This converter convert a {@link BlogTagListItem} to a {@link TopicResource}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            The BlogTagListItem which is the incoming list
 */
public class TopicTagListItemToTopicResourceConverter<T extends BlogTagListItem> extends
        DataAccessBlogConverter<T, TopicResource> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TopicTagListItemToTopicResourceConverter.class);
    private final BlogToUserDetailBlogListItemConverter converter = new BlogToUserDetailBlogListItemConverter(
            UserDetailBlogListItem.class, false, false, false, true, false,
            ServiceLocator.findService(BlogRightsManagement.class));

    private ImageManager imageManagement;

    @Override
    public boolean convert(T queryResult, TopicResource finalResult) {
        try {
            fillingResultItem(queryResult, finalResult);
            return true;
        } catch (BlogAccessException | BlogNotFoundException e) {
            LOGGER.error("Unexpected error converting a topic", e);
            return false;
        }
    }

    @Override
    public TopicResource create() {
        return new TopicResource();
    }

    /**
     * Add the profile and banner image details to the resource
     *
     * @param topicResource
     *            the resource to modify
     */
    private void fillImageData(TopicResource topicResource) {
        EntityImageManagement globalIdImageMangement = ServiceLocator
                .findService(EntityImageManagement.class);
        String bannerImageId = "topic." + topicResource.getTopicId();
        if (!globalIdImageMangement.hasCustomImage(bannerImageId, ImageType.BANNER)) {
            bannerImageId = EntityImageManagement.DEFAULT_IMAGE_ENTITY_ID;
        }
        topicResource.setBannerImageId(bannerImageId);
        String profileImageId = "topic." + topicResource.getTopicId();
        if (!globalIdImageMangement.hasCustomImage(profileImageId, ImageType.PROFILE)) {
            profileImageId = EntityImageManagement.DEFAULT_IMAGE_ENTITY_ID;
        }
        topicResource.setProfileImageId(profileImageId);
        // TODO date attribute is deprecated and should be removed in next version
        Date imageDate;
        try {
            // TODO should be using the imageManagement.getVersionString
            imageDate = globalIdImageMangement
                    .getImageLastModified(bannerImageId, ImageType.BANNER);
            if (imageDate == null) {
                // TODO better set build timestamp?
                imageDate = new Date(0);
            }
        } catch (AuthorizationException e) {
            imageDate = new Date(0);
        }
        topicResource.setLastBannerImageModificationDate(imageDate);
        try {
            imageDate = globalIdImageMangement.getImageLastModified(profileImageId,
                    ImageType.PROFILE);
            if (imageDate == null) {
                // TODO better set build timestamp?
                imageDate = new Date(0);
            }
        } catch (AuthorizationException e) {
            imageDate = new Date(0);
        }
        topicResource.setLastProfileImageModificationDate(imageDate);
        ImageManager imgManagement = getImageManagement();
        try {
            topicResource.setBannerImageVersion(imgManagement.getImageVersionString(
                    EntityBannerImageDescriptor.IMAGE_TYPE_NAME, bannerImageId));
        } catch (ImageNotFoundException | AuthorizationException e) {
            // should not occur since we have access and there is always a default image
            LOGGER.error("Unexpected exception while getting profile image version", e);
            topicResource.setBannerImageVersion("0");
        }
        try {
            topicResource.setProfileImageVersion(imgManagement.getImageVersionString(
                    EntityProfileImageDescriptor.IMAGE_TYPE_NAME, profileImageId));
        } catch (ImageNotFoundException | AuthorizationException e) {
            // should not occur since we have access and there is always a default image
            LOGGER.error("Unexpected exception while getting profile image version", e);
            topicResource.setProfileImageVersion("0");
        }
    }

    /**
     * Filling the PostListItem
     *
     * @param topicTagListItem
     *            The blogListItem with tags.
     * @param topicResource
     *            The resource of an blog.
     * @throws BlogAccessException
     *             in case the access rights were changed after getting the topicTagListItem and the
     *             current user has no access anymore
     * @throws BlogNotFoundException
     *             in case the topic was removed after getting the topicTagListItem
     */
    public void fillingResultItem(T topicTagListItem, TopicResource topicResource)
            throws BlogAccessException, BlogNotFoundException {
        topicResource.setTopicId(String.valueOf(topicTagListItem.getId()));
        topicResource.setTitle(topicTagListItem.getTitle());
        topicResource.setAlias(topicTagListItem.getNameIdentifier());
        topicResource.setDescription(topicTagListItem.getDescription());
        topicResource.setLastModificationDate(topicTagListItem.getLastModificationDate());
        topicResource.setIsFollow(ServiceLocator.findService(FollowManagement.class).followsBlog(
                topicTagListItem.getId()));
        UserDetailBlogListItem detailBlog = ServiceLocator.instance()
                .getService(BlogManagement.class).getBlogById(topicTagListItem.getId(), converter);
        topicResource.setAllCanRead(detailBlog.isAllCanRead());
        topicResource.setAllCanWrite(detailBlog.isAllCanWrite());
        topicResource.setTopicEmail(detailBlog.getBlogEmail());
        topicResource.setUserRole(detailBlog.getUserRole().getValue());
        topicResource.setCreateSystemNotes(detailBlog.isCreateSystemNotes());
        Collection<TagData> tagListItems = topicTagListItem.getTags();
        TagResource[] tagResources = new TagResource[tagListItems.size()];
        int i = 0;
        for (TagData tagListItem : tagListItems) {
            tagResources[i] = TagHelper.buildTagResource(tagListItem);
            i++;
        }
        List<ChildTopicResource> children = new ArrayList<ChildTopicResource>();
        for (DetailBlogListItem childTopic : detailBlog.getChildren()) {
            ChildTopicResource child = new ChildTopicResource();
            child.setChildTopicId(childTopic.getId());
            children.add(child);
        }
        topicResource.setChildren(children.toArray(new ChildTopicResource[children.size()]));
        topicResource.setTags(tagResources);
        fillImageData(topicResource);
        try {
            topicResource.setCrawlLastModificationDate(ServiceLocator.findService(
                    LastModificationDateManagement.class).getTopicCrawlLastModificationDate(
                    topicTagListItem.getId()));
        } catch (AuthorizationException e) {
            throw new BlogAccessException(e.getMessage(), e.getCause(), topicTagListItem.getId(),
                    BlogRole.VIEWER, null);
        }
    }

    /**
     * @return lazily initialized ImageManager
     */
    private ImageManager getImageManagement() {
        if (imageManagement == null) {
            imageManagement = ServiceLocator.findService(ImageManager.class);
        }
        return imageManagement;
    }
}
