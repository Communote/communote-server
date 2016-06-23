package com.communote.server.core.vo.query.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.ParameterHelper;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.property.BinaryPropertyAccessor.DummyPropertyable;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.DiscussionFilterMode;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.TimelineQueryParameters;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.filter.PropertyFilter;
import com.communote.server.core.vo.query.logical.AtomicTagFormula;
import com.communote.server.core.vo.query.logical.CompoundTagFormula;
import com.communote.server.core.vo.query.logical.CompoundTagFormula.CompoundFormulaType;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.property.Propertyable;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.note.UserNoteEntityImpl;

/**
 * This {@link QueryParametersConfigurator} can be used to configure a {@link CoreItemQueryInstance}
 * .
 *
 * @param <T>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TimelineQueryParametersConfigurator<T extends TimelineQueryParameters> extends
        QueryParametersConfigurator {

    private static Logger LOGGER = LoggerFactory
            .getLogger(TimelineQueryParametersConfigurator.class);

    private final static Map<String, Class<? extends Propertyable>> NAMES_TO_PROPERTYABLES;
    static {
        NAMES_TO_PROPERTYABLES = new HashMap<String, Class<? extends Propertyable>>();
        NAMES_TO_PROPERTYABLES.put("UserNoteEntity", UserNoteEntityImpl.class);
        NAMES_TO_PROPERTYABLES.put("Note", Note.class);
        NAMES_TO_PROPERTYABLES.put("Topic", Blog.class);
        NAMES_TO_PROPERTYABLES.put("Entity", CommunoteEntity.class);
        NAMES_TO_PROPERTYABLES.put("Dummy", DummyPropertyable.class);
    }

    /**
     * Helper to determine the {@link TimelineFilterViewType} based on the parameters. In case no
     * parameter is set or invalid, {@link TimelineFilterViewType#CLASSIC} is returned.
     *
     * @param parameters
     *            the parameters to use
     * @param parameterNameProvider
     *            the parameter name provider to use
     * @return the filter type
     */
    public static TimelineFilterViewType determineViewType(
            Map<String, ? extends Object> parameters,
            QueryParametersParameterNameProvider parameterNameProvider) {
        String selectViewType = ParameterHelper.getParameterAsString(parameters,
                parameterNameProvider.getNameForSelectedViewType());
        if (StringUtils.isBlank(selectViewType)) {
            return TimelineFilterViewType.CLASSIC;
        }
        try {
            return TimelineFilterViewType.valueOf(selectViewType);
        } catch (IllegalArgumentException iae) {
            LOGGER.debug("viewType could not be parsed. Assuming default. viewType= {}",
                    selectViewType);
            return TimelineFilterViewType.CLASSIC;
        }
    }

    /**
     * Constructor for this {@link QueryParametersConfigurator}.
     *
     * @param nameProvider
     *            To fetch parameters for the query instance.
     */
    public TimelineQueryParametersConfigurator(QueryParametersParameterNameProvider nameProvider) {
        super(nameProvider);
        if (nameProvider == null) {
            throw new IllegalArgumentException("nameProvider cannot be null!");
        }
    }

    /**
     * Constructor for this {@link QueryParametersConfigurator}.
     *
     * @param nameProvider
     *            To fetch parameters for the query instance.
     * @param defaultMaxCount
     *            the default value to use if maxCount parameter is not set
     */
    public TimelineQueryParametersConfigurator(QueryParametersParameterNameProvider nameProvider,
            int defaultMaxCount) {
        super(nameProvider, defaultMaxCount);
        if (nameProvider == null) {
            throw new IllegalArgumentException("nameProvider cannot be null!");
        }
    }

    /**
     * Method to start the configuration process.
     *
     * @param parameters
     *            Map which holds the parameters to be used to configure the query instance.
     * @param queryParameters
     *            Class of type QueryInstance.
     */
    public void configure(Map<String, ? extends Object> parameters, T queryParameters) {
        queryParameters.setResultSpecification(getResultSpecification(parameters));
        configureQueryParameters(parameters, queryParameters);
        configureRank(parameters, queryParameters);
        configurePropertyFilter(parameters, queryParameters);
        configureBlogSpecificQueryInstance(parameters, queryParameters);
        queryParameters.setUserAliases(ParameterHelper.getParameterAsStringSet(parameters,
                getParameterNameProvider().getNameForUserAliases(), ","));
    }

    private void configureAttachments(T queryParameters,
            QueryParametersParameterNameProvider nameProvider,
            Map<String, ? extends Object> parameters) {

        String[] contentIds = ParameterHelper.getParameterAsStringArray(parameters,
                nameProvider.getNameForAttachmentContentIds(), ",");
        String[] repoConnectorIds = ParameterHelper.getParameterAsStringArray(parameters,
                nameProvider.getNameForAttachmentRepositoryConnectorIds(), ",");

        String singleRepoId = null;
        if (repoConnectorIds != null && repoConnectorIds.length > 0) {
            singleRepoId = repoConnectorIds[0];
        }

        if (contentIds != null && contentIds.length > 0) {

            if (singleRepoId != null) {
                // if there is not exactly on repo connector id, and more content ids, do not use it
                // as a single one
                if (repoConnectorIds.length != 1 || contentIds.length > repoConnectorIds.length) {
                    singleRepoId = null;
                }
            }
            List<String> contentIdsList = new ArrayList<>();
            List<String> repoConnectorIdsList = new ArrayList<>();

            boolean repoConnectorMoreThanNull = false;
            for (int i = 0; i < contentIds.length; i++) {
                if (contentIds[i] != null) {

                    String repoId = null;
                    if (repoConnectorIds != null && repoConnectorIds.length > i) {
                        repoId = repoConnectorIds[i];
                        repoConnectorMoreThanNull = true;
                    }

                    contentIdsList.add(contentIds[i]);
                    repoConnectorIdsList.add(repoId);
                }
            }

            queryParameters.setAttachmentContentIds(contentIdsList.toArray(new String[] { }));
            if (repoConnectorMoreThanNull) {
                queryParameters.setAttachmentRepositoryConnectorIds(repoConnectorIdsList
                        .toArray(new String[] { }));
            }
        }

        if (singleRepoId != null) {
            queryParameters.setAttachmentRepositoryConnectorId(singleRepoId);
        }

    }

    /**
     * Configure the blog filter
     *
     * @param extension
     *            the extension
     * @param nameProvider
     *            the name provider used
     * @param parameters
     *            the parameters to be used
     */
    private void configureBlogFilter(TaggingCoreItemUTPExtension extension,
            QueryParametersParameterNameProvider nameProvider,
            Map<String, ? extends Object> parameters) {
        Long targetBlogId = ParameterHelper.getParameterAsLong(parameters,
                nameProvider.getNameForTargetBlogId());
        boolean aliasFilter = false;
        boolean idFilter = false;
        boolean includeChildTopics = ParameterHelper.getParameterAsBoolean(parameters,
                getParameterNameProvider().getNameForIncludeChildTopics(), false);
        // if targetBlogId is defined all the other blog filter parameters are ignored to give this
        // filter precedence. But if the child topics should be included we ignore the
        // targetBlogId if blogIds are defined to cover the use-case that certain subtopics should
        // be fetched. For that to work we also disable the includeChildTopics
        if (targetBlogId != null && !includeChildTopics) {
            extension.setBlogFilter(new Long[] { targetBlogId });
        } else {
            Long[] blogFilter = ParameterHelper.getParameterAsLongArray(parameters,
                    nameProvider.getNameForBlogIds());
            if (blogFilter != null && blogFilter.length > 0) {
                extension.setBlogFilter(blogFilter);
                idFilter = true;
            }
            String[] blogAliasFilter = ParameterHelper.getParameterAsStringArray(parameters,
                    nameProvider.getNameForBlogAliases(), ",");
            if (blogAliasFilter != null && blogAliasFilter.length > 0) {
                extension.setBlogAliasFilter(blogAliasFilter);
                aliasFilter = true;
            }
        }
        if (includeChildTopics && targetBlogId != null && !aliasFilter && !idFilter) {
            extension.setIncludeChildTopics(true);
            extension.setBlogFilter(new Long[] { targetBlogId });
        }
    }

    /**
     * This method configures the given QueryInstance with blog specific settings from the parameter
     * map.
     *
     * @param parameters
     *            Map with parameters.
     * @param queryParameters
     *            Class of type QueryInstance.
     */
    protected void configureBlogSpecificQueryInstance(Map<String, ? extends Object> parameters,
            T queryParameters) {
        QueryParametersParameterNameProvider nameProvider = getParameterNameProvider();

        TaggingCoreItemUTPExtension extension = new TaggingCoreItemUTPExtension();
        queryParameters.setTypeSpecificExtension(extension);

        if (SecurityHelper.isPublicUser()) {
            extension.setPublicAccess(true);
        }

        Long currentUserId = SecurityHelper.assertCurrentUser().getUserId();
        extension.setTopicAccessLevel(TopicAccessLevel.READ);
        extension.setUserId(currentUserId);

        boolean showPostsForMe = ParameterHelper.getParameterAsBoolean(parameters,
                nameProvider.getNameForShowPostsForMe(), Boolean.FALSE);
        if (showPostsForMe) {
            queryParameters.setUserToBeNotified(new Long[] { currentUserId });
            queryParameters.setMentionDiscussionAuthors(true);
            queryParameters.setMentionTopicAuthors(true);
            queryParameters.setMentionTopicReaders(true);
            queryParameters.setMentionTopicManagers(true);
        }

        Boolean filterFavorites = ParameterHelper.getParameterAsBoolean(parameters,
                nameProvider.getNameForFavorite());
        queryParameters.setFavorites(filterFavorites);

        boolean filterDirectMessages = ParameterHelper.getParameterAsBoolean(parameters,
                nameProvider.getNameForDirectMessages(), false);
        queryParameters.setDirectMessage(filterDirectMessages);

        boolean filterFollowedItems = ParameterHelper.getParameterAsBoolean(parameters,
                nameProvider.getNameForFollowedNotes(), false);
        queryParameters.setRetrieveOnlyFollowedItems(filterFollowedItems);

        configureBlogFilter(extension, nameProvider, parameters);

        configureDiscussion(queryParameters, extension, nameProvider, parameters);
        extension.setShowDiscussionParticipation(ParameterHelper.getParameterAsBoolean(parameters,
                nameProvider.getNameForShowDiscussionParticipation(), false));
    }

    /**
     * Configure the discussion parameters
     *
     * @param instance
     *            the instance
     * @param extension
     *            the extension
     * @param nameProvider
     *            the name provider used
     * @param parameters
     *            the parameters to be used
     */
    private void configureDiscussion(T instance, TaggingCoreItemUTPExtension extension,
            QueryParametersParameterNameProvider nameProvider,
            Map<String, ? extends Object> parameters) {
        Long discussionId = ParameterHelper.getParameterAsLong(parameters,
                nameProvider.getNameForDiscussionId());
        long parentPostId = ParameterHelper.getParameterAsLong(parameters,
                nameProvider.getNameForParentPostId(), 0l);
        if (discussionId != null) {
            // if discussion is set do not filter for a single not and retrieve all items
            instance.setNoteId(null);
            instance.setDiscussionId(discussionId);
            instance.setResultSpecification(new ResultSpecification());
        } else if (parentPostId > 0) {
            extension.setParentPostId(parentPostId);
        }

        String discussionFilterModeAsString = ParameterHelper.getParameterAsString(parameters,
                nameProvider.getNameForDiscussionFilterMode(), DiscussionFilterMode.ALL.name());
        DiscussionFilterMode discussionFilterMode = DiscussionFilterMode.ALL;
        try {
            discussionFilterMode = DiscussionFilterMode.valueOf(discussionFilterModeAsString);
        } catch (Exception e) {
            LOGGER.warn("Non existing discussion filter mode set: {}", discussionFilterModeAsString);
        }
        instance.setDiscussionFilterMode(discussionFilterMode);
    }

    /**
     * This method configures any property filters, if given.
     *
     * @param parameters
     *            The parameters.
     * @param instance
     *            The query instance.
     */
    private void configurePropertyFilter(Map<String, ? extends Object> parameters, T instance) {
        String propertyFilterObject = ParameterHelper.getParameterAsString(parameters,
                getParameterNameProvider().getNameForPropertyFilter());
        if (propertyFilterObject == null) {
            return;
        }
        String[] propertyFilters = propertyFilterObject.split("\\},\\{");
        Map<String, PropertyFilter> filterGroups = new HashMap<String, PropertyFilter>();
        loop: for (String propertyFilter : propertyFilters) {
            // TODO why not parsing as JSON?
            String filterArray = StringUtils.substringBetween(propertyFilter, "[\"", "\"]");
            if (filterArray == null
                    && (filterArray = StringUtils.substringBetween(propertyFilter, "['", "']")) == null) {
                continue loop;
            }
            // [0:Property, 1:Group, 2:Key, 3:Value, 4:MatchMode, 5:Negate (Optional)]
            String[] filterDefinition = filterArray.split("[\"'],[\"']");
            if ("contentTypes.discussion".equals(filterDefinition[2])) {
                instance.setDiscussionFilterMode(DiscussionFilterMode.IS_DISCUSSION);
                continue loop;
            }
            PropertyFilter filter = filterGroups.get(filterDefinition[1]);

            if (NAMES_TO_PROPERTYABLES.get(filterDefinition[0]) == null) {
                throw new IllegalArgumentException("The given property is invalid: "
                        + filterDefinition[0]);
            }
            boolean negate = filterDefinition.length >= 6
                    && (Boolean.parseBoolean(filterDefinition[5]) || "negate"
                            .equals(filterDefinition[5]));
            filter = new PropertyFilter(filterDefinition[1],
                    NAMES_TO_PROPERTYABLES.get(filterDefinition[0]), negate);
            filterGroups.put(filterDefinition[1], filter);
            instance.addPropertyFilter(filter);

            filter.addProperty(filterDefinition[2], filterDefinition[3],
                    PropertyFilter.MatchMode.valueOf(filterDefinition[4]));
        }
    }

    /**
     * This method configures a given query instance with the provided parameters map.
     *
     * @param parameters
     *            Map with parameters.
     * @param queryParameters
     *            Class of type QueryInstance.
     */
    protected void configureQueryParameters(Map<String, ? extends Object> parameters,
            T queryParameters) {
        SecurityHelper.assertCurrentUser();

        QueryParametersParameterNameProvider nameProvider = getParameterNameProvider();
        for (String nameForNoteId : nameProvider.getNamesForNoteId()) {
            Long noteId = ParameterHelper.getParameterAsLong(parameters, nameForNoteId);
            if (noteId != null) {
                queryParameters.setNoteId(noteId);
                break;
            }
        }

        Long[] userIds = ParameterHelper.getParameterAsLongArray(parameters,
                nameProvider.getNameForUserIds());
        queryParameters.setUserIds(userIds);
        queryParameters.setUserIdsToIgnore(ParameterHelper.getParameterAsLongArray(parameters,
                nameProvider.getNameForUserIdsToIgnore()));

        configureTags(queryParameters, nameProvider, parameters);
        configureAttachments(queryParameters, nameProvider, parameters);

        String[] userSearchString = ParameterHelper.getParameterAsStringArray(parameters,
                nameProvider.getNameForUserSearchString(), " ");
        queryParameters.setUserSearchFilters(userSearchString);
        if (userSearchString != null && userSearchString.length > 0) {
            queryParameters.setUserSearchFilters(userSearchString);
        }

        String[] fullTextSearch = ParameterHelper.getParameterAsStringArray(parameters,
                nameProvider.getNameForFullTextSearchString(), " ");
        String[] postTextSearch = ParameterHelper.getParameterAsStringArray(parameters,
                nameProvider.getNameForPostSearchString(), " ");
        fullTextSearch = (String[]) ArrayUtils.addAll(fullTextSearch, postTextSearch);
        if (fullTextSearch != null && fullTextSearch.length > 0) {
            queryParameters.setFullTextSearchFilters(fullTextSearch);
        }
        String[] topicSearch = ParameterHelper.getParameterAsStringArray(parameters,
                nameProvider.getNameForBlogSearchString(), " ");
        queryParameters.setTopicSearchFilters(topicSearch);
        Long start = ParameterHelper.getParameterAsLong(parameters,
                nameProvider.getNameForStartDate(), null);
        Long end = ParameterHelper.getParameterAsLong(parameters, nameProvider.getNameForEndDate(),
                null);
        if (start != null) {
            queryParameters.setLowerTagDate(new Date(start));
        }
        if (end != null) {
            queryParameters.setUpperTagDate(new Date(end));
        }
    }

    /**
     * Configure the minimum and maximum Ranks
     *
     * @param parameters
     *            the parameters to use
     * @param queryParameters
     *            the query parameters to configure
     */
    private void configureRank(Map<String, ? extends Object> parameters, T queryParameters) {
        Double minimumRank = ParameterHelper.getParameterAsDouble(parameters,
                getParameterNameProvider().getNameForMinimumRank());
        Double maximumRank = ParameterHelper.getParameterAsDouble(parameters,
                getParameterNameProvider().getNameForMaximumRank());

        if (minimumRank != null && minimumRank > 0) {
            queryParameters.setMinimumRank(minimumRank);
        }

        if (maximumRank != null && maximumRank < 1) {
            queryParameters.setMaximumRank(maximumRank);
        }

        Boolean sortByDayDateAndRank = ParameterHelper.getParameterAsBoolean(parameters,
                getParameterNameProvider().getNameForSortByDayDateAndRank());

        if (sortByDayDateAndRank != null) {
            queryParameters.setSortByDayDateAndRank(sortByDayDateAndRank);
            // if sort by rank we must force the minimum rank filter, otherwise the sort will fail.
            if (queryParameters.getMinimumRank() == null) {
                queryParameters.setMinimumRank(0d);
            }
        }

    }

    /**
     * Configures the tag parameters.
     *
     * @param instance
     *            the query instance
     * @param nameProvider
     *            the name provider to use
     * @param parameters
     *            the parameters to use
     */
    private void configureTags(T instance, QueryParametersParameterNameProvider nameProvider,
            Map<String, ? extends Object> parameters) {
        String[] tags = ParameterHelper.getParameterAsStringArray(parameters,
                nameProvider.getNameForTags(), ",");
        if (tags != null && tags.length > 0) {
            if (tags.length == 1) {
                instance.setLogicalTags(new AtomicTagFormula(tags[0], false));
            } else {
                CompoundTagFormula compuntTagFormula = new CompoundTagFormula(
                        CompoundFormulaType.CONJUNCTION, false);
                for (String tag : tags) {
                    compuntTagFormula.addAtomicFormula(new AtomicTagFormula(tag, false));
                }
                instance.setLogicalTags(compuntTagFormula);
            }
        }

        String tagPrefix = ParameterHelper.getParameterAsString(parameters,
                nameProvider.getNameForTagPrefix());
        instance.setTagPrefix(tagPrefix);
        List<Long> tagIds = ParameterHelper.getParameterAsLongList(parameters,
                nameProvider.getNameForTagIds());
        if (tagIds != null) {
            instance.setTagIds(new HashSet<Long>(tagIds));
        }
    }

    /**
     * Helper to determine the {@link TimelineFilterViewType} based on the parameters and the
     * defined name provider. In case no parameter is set or invalid,
     * {@link TimelineFilterViewType#CLASSIC} is returned.
     *
     * Is using
     * {@link TimelineQueryParametersConfigurator#determineViewType(Map, QueryParametersParameterNameProvider)}
     *
     * @param parameters
     *            the parameters to take it from
     * @return the view type extracted from the parameters
     */
    public TimelineFilterViewType determineViewType(Map<String, ? extends Object> parameters) {
        return TimelineQueryParametersConfigurator.determineViewType(parameters,
                getParameterNameProvider());
    }
}
