package com.communote.plugins.api.rest.v30.resource.topic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.communote.common.string.StringHelper;
import com.communote.plugins.api.rest.v30.converter.TopicTagListItemToTopicResourceConverter;
import com.communote.plugins.api.rest.v30.resource.tag.TagResource;
import com.communote.plugins.api.rest.v30.resource.topic.property.PropertyResource;
import com.communote.plugins.api.rest.v30.service.IllegalRequestParameterException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.BlogTO;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.core.filter.listitems.blog.BlogTagListItem;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.blog.BlogQueryParameters;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.config.BlogQueryParametersConfigurator;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.model.blog.Blog;

/**
 * Helper for {@link TopicResourceHandler}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class TopicResourceHelper {

    final static TopicTagListItemToTopicResourceConverter<BlogTagListItem> CONVERTER = new TopicTagListItemToTopicResourceConverter<BlogTagListItem>();

    /**
     * Get a set of tag transfer objects for storing with blog
     *
     * @param tagResources
     *            list of {@link TagResource}
     * @return set of {@link TagTO}
     */
    private static HashSet<TagTO> buildTags(TagResource[] tagResources) {
        HashSet<TagTO> tags = new HashSet<TagTO>();
        if (tagResources != null) {
            for (TagResource tagResource : tagResources) {
                tags.add(new TagTO(tagResource.getDefaultName(), TagStoreType.Types.BLOG));
            }
        }
        return tags;
    }

    /**
     * Get query instance to filter notes
     *
     * @param topicCollectionParameter
     *            Extract context parameter needed for filter
     * @param nameProvider
     *            The name provider.
     * @return NoteQueryInstance
     * @throws IllegalRequestParameterException
     *             request parameter exception
     */
    public static BlogQueryParameters configureQueryInstance(
            GetCollectionTopicParameter topicCollectionParameter,
            QueryParametersParameterNameProvider nameProvider)
                    throws IllegalRequestParameterException {

        BlogQueryParameters queryParameters = new BlogQueryParameters();
        BlogQueryParametersConfigurator<BlogQueryParameters> queryInstanceConfigurator;
        queryInstanceConfigurator = new BlogQueryParametersConfigurator<BlogQueryParameters>(
                nameProvider, 10, false);
        queryInstanceConfigurator.configure(
                generateParameterMap(topicCollectionParameter, nameProvider), queryParameters);

        switch (topicCollectionParameter.getTopicListType()) {
        case LAST_MODIFIED:
            queryParameters.sortByLastModificationDateAsc();
            break;
        case MANAGER:
            queryParameters.setAccessLevel(TopicAccessLevel.MANAGER);
            break;
        case READ:
            queryParameters.setAccessLevel(TopicAccessLevel.READ);
            break;
        case WRITE:
            queryParameters.setAccessLevel(TopicAccessLevel.WRITE);
            break;
        default:
            throw new IllegalRequestParameterException("topicListType", topicCollectionParameter
                    .getTopicListType().name(), "Invalid value. Allowed values are: "
                            + StringHelper.toString(ETopicListType.values(), "|"));
        }
        queryParameters.setShowOnlyRootTopics(false);
        return queryParameters;
    }

    /**
     * Generate map with all valid parameters
     *
     * @param topicCollectionParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param nameProvider
     *            The name provider.
     * @return map of all valid topic parameters
     */
    private static Map<String, Object> generateParameterMap(
            GetCollectionTopicParameter topicCollectionParameter,
            QueryParametersParameterNameProvider nameProvider) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(nameProvider.getNameForBlogAliases(),
                StringUtils.join(topicCollectionParameter.getF_topicAliases(), ","));
        parameters.put(nameProvider.getNameForBlogIds(),
                StringUtils.join(topicCollectionParameter.getF_topicIds(), ","));
        parameters.put(nameProvider.getNameForBlogIdsToExclude(),
                StringUtils.join(topicCollectionParameter.getF_topicIdsToExclude(), ","));
        parameters.put(nameProvider.getNameForFollowedBlogs(),
                BooleanUtils.toBoolean(topicCollectionParameter.getF_showFollowedItems()));
        parameters.put(nameProvider.getNameForMaxCount(),
                ObjectUtils.toString(topicCollectionParameter.getMaxCount()));
        parameters.put(nameProvider.getNameForOffset(),
                ObjectUtils.toString(topicCollectionParameter.getOffset()));
        parameters.put(nameProvider.getNameForTagPrefix(),
                StringUtils.trimToEmpty(topicCollectionParameter.getF_tagPrefix()));
        parameters.put(nameProvider.getNameForTags(),
                StringUtils.join(topicCollectionParameter.getF_tags(), ","));
        if (StringUtils.isNotBlank(topicCollectionParameter.getF_titleSearchString())) {
            parameters.put(nameProvider.getNameForBlogSearchString(),
                    topicCollectionParameter.getF_titleSearchString());
        }
        parameters.put(nameProvider.getNameForExternalObjectId(),
                topicCollectionParameter.getF_externalObjectId());
        parameters.put(nameProvider.getNameForExternalObjectSystemId(),
                topicCollectionParameter.getF_externalObjectSystemId());
        return parameters;
    }

    /**
     * Get transfer object of topic
     *
     * @param createTopicParameter
     *            {@link CreateTopicParameter}
     * @return {@link BlogTO}
     */
    public static CreationBlogTO getBlogTO(CreateTopicParameter createTopicParameter) {
        CreationBlogTO blogTO = new CreationBlogTO();
        blogTO.setDescription(createTopicParameter.getDescription());
        blogTO.setNameIdentifier(createTopicParameter.getAlias());
        blogTO.setTags(buildTags(createTopicParameter.getTags()));
        blogTO.setCreatorUserId(SecurityHelper.assertCurrentUserId());

        if (createTopicParameter.getAllCanRead() != null) {
            blogTO.setAllCanRead(createTopicParameter.getAllCanRead());
        }

        if (createTopicParameter.getAllCanWrite() != null) {
            blogTO.setAllCanWrite(createTopicParameter.getAllCanWrite());
        }

        if (createTopicParameter.getTitle() == null) {
            throw new NullPointerException(
                    "Attribute 'title' of a POST request should not be null.");
        } else {
            blogTO.setTitle(createTopicParameter.getTitle());
        }
        return blogTO;
    }

    /**
     * Get transfer object of topic
     *
     * @param editTopicParameter
     *            {@link EditTopicParameter}
     * @param topic
     *            {@link Blog}
     * @return {@link BlogTO}
     * @throws BlogNotFoundException
     *             can not found topic
     * @throws BlogAccessException
     *             can not access blog
     */
    public static BlogTO getBlogTO(EditTopicParameter editTopicParameter, Blog topic)
            throws BlogAccessException, BlogNotFoundException {
        BlogTO blogTO = new BlogTO();
        blogTO.setDescription(editTopicParameter.getDescription());
        blogTO.setNameIdentifier(editTopicParameter.getAlias());
        blogTO.setTags(buildTags(editTopicParameter.getTags()));
        blogTO.setTitle(editTopicParameter.getTitle());

        // set the all can read and all can write flag
        Boolean allCanRead = null, allCanWrite = null;
        if (editTopicParameter.getAllCanRead() != null) {
            allCanRead = editTopicParameter.getAllCanRead();
        }
        if (editTopicParameter.getAllCanWrite() != null) {
            allCanWrite = editTopicParameter.getAllCanWrite();
        }

        if (allCanRead != null || allCanWrite != null) {
            if (allCanRead == null) {
                allCanRead = topic.isAllCanRead();
            }
            if (allCanWrite == null) {
                allCanWrite = topic.isAllCanWrite();
            }
            ServiceLocator.findService(BlogRightsManagement.class).setAllCanReadAllCanWrite(
                    topic.getId(), allCanRead, allCanWrite);
        }

        if (editTopicParameter.getProperties() != null) {
            List<StringPropertyTO> stringPropertyTOs = new ArrayList<StringPropertyTO>();
            for (PropertyResource propertyResource : editTopicParameter.getProperties()) {
                StringPropertyTO stringPropertyTO = new StringPropertyTO();
                stringPropertyTO.setKeyGroup(propertyResource.getKeyGroup());
                stringPropertyTO.setPropertyKey(propertyResource.getKey());
                stringPropertyTO.setPropertyValue(propertyResource.getValue());
                stringPropertyTO.setLastModificationDate(new Date());
                stringPropertyTOs.add(stringPropertyTO);
            }
            blogTO.setProperties(stringPropertyTOs);
        }
        return blogTO;
    }

    /**
     * Get the topic identifier of alias or id
     *
     * @param eTopicIdentifierName
     *            enumeration of identifier possibilities
     * @param topicId
     *            identifier alias or topicId
     * @param blogManagement
     *            {@link BlogManagement}
     * @return topic identifier
     * @throws BlogNotFoundException
     *             topic can not been found by alias
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    public static long getTopicIdByIdentifier(String eTopicIdentifierName, String topicId,
            BlogManagement blogManagement) throws BlogNotFoundException, BlogAccessException {
        if (StringUtils.isNotBlank(eTopicIdentifierName)
                && eTopicIdentifierName.equals(ETopicIdentifier.ALIAS.name())) {
            Blog blog = blogManagement.findBlogByIdentifier(topicId);
            if (blog == null) {
                throw new BlogNotFoundException("topic can not found by alias", null, topicId);
            }
            return blog.getId();
        } else {
            return Long.parseLong(topicId);
        }
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private TopicResourceHelper() {
        // Do nothing
    }

}
