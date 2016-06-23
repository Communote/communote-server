package com.communote.plugins.api.rest.v22.converter;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.v22.resource.tag.TagHelper;
import com.communote.plugins.api.rest.v22.resource.tag.TagResource;
import com.communote.plugins.api.rest.v22.resource.topic.TopicResource;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.converter.blog.BlogToUserDetailBlogListItemConverter;
import com.communote.server.core.filter.listitems.blog.BlogTagListItem;
import com.communote.server.core.filter.listitems.blog.UserDetailBlogListItem;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.vo.query.blog.DataAccessBlogConverter;

/**
 * This converter convert a {@link BlogTagListItem} to a {@link TopicResource}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            The BlogTagListItem which is the incoming list
 * @param <O>
 *            The BlogResource which is the final list
 */
public class TopicTagListItemToTopicResourceConverter<T extends BlogTagListItem, O extends TopicResource>
        extends DataAccessBlogConverter<T, O> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TopicTagListItemToTopicResourceConverter.class);

    private final BlogToUserDetailBlogListItemConverter converter = new BlogToUserDetailBlogListItemConverter(
            UserDetailBlogListItem.class, false, false, false, false, false, null);

    @Override
    public boolean convert(T queryResult, O finalResult) {
        try {
            fillingResultItem(queryResult, finalResult);
            return true;
        } catch (BlogAccessException e) {
            LOGGER.error("Unexpected error converting a topic", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public O create() {
        return (O) new TopicResource();
    }

    /**
     * Filling the PostListItem
     * 
     * @param topicTagListItem
     *            The blogListItem with tags.
     * @param topicResource
     *            The resource of an blog.
     * @throws BlogAccessException
     *             in case the current user has no access to the topic, should not occur
     */
    private void fillingResultItem(T topicTagListItem, O topicResource) throws BlogAccessException {
        topicResource.setTopicId(String.valueOf(topicTagListItem.getId()));
        topicResource.setTitle(topicTagListItem.getTitle());
        topicResource.setAlias(topicTagListItem.getNameIdentifier());
        topicResource.setDescription(topicTagListItem.getDescription());
        topicResource.setLastModificationDate(topicTagListItem.getLastModificationDate());
        topicResource.setIsFollow(ServiceLocator.instance().getService(FollowManagement.class)
                .followsBlog(topicTagListItem.getId()));
        UserDetailBlogListItem detailBlog = ServiceLocator.instance()
                .getService(BlogManagement.class).getBlogById(
                        topicTagListItem.getId(), converter);
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

        topicResource.setTags(tagResources);
    }
}
