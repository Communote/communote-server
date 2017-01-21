package com.communote.server.web.fe.widgets.blog;

import static com.communote.server.core.vo.query.TimelineFilterViewType.CLASSIC;
import static com.communote.server.core.vo.query.TimelineFilterViewType.COMMENT;
import static com.communote.server.core.vo.query.TimelineFilterViewType.THREAD;
import static com.communote.server.web.fe.widgets.WidgetConstants.PARAM_BLOG_POST_ID;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.matcher.Matcher;
import com.communote.common.util.PageableList;
import com.communote.common.util.ParameterHelper;
import com.communote.common.util.UriUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.common.util.LogHelper;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.note.processor.LikeNoteRenderingPreProcessor;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.NoteQueryParametersConfigurator;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.java.note.MatcherFactory;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToDiscussionNoteDataConverter;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.BlogProperty;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.service.NoteService;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;
import com.communote.server.web.fe.widgets.extension.note.CPLNoteActionsProviderManagement;
import com.communote.server.web.fe.widgets.extension.note.CPLNoteItemTemplateProviderManagement;
import com.communote.server.web.fe.widgets.extension.note.CPLNoteMetaDataProviderManagement;
import com.communote.server.web.fe.widgets.user.news.RssSupportWidget;

/**
 * Widget for displaying a list of blog posts.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ChronologicalPostListWidget extends AbstractPagedListWidget<NoteData> implements
        RssSupportWidget {

    private static final String RESPONSE_METADATA_LAST_NOTE_ID = "lastNoteId";

    private static final String RESPONSE_METADATA_LAST_NOTE_DATE = "lastNoteCreationTimestamp";
    private static final String RESPONSE_METADATA_FIRST_NOTE_CREATION_TIMESTAMP = "firstNoteCreationTimestamp";
    private static final String ATTRIBUTE_VIEW_TYPE_ARRAY = "viewTypeArray";
    private static final String PARAMETER_VIEW_TYPES = "viewTypes";
    // parameter defining the mode for rendering the response
    private static final String PARAM_VIEW_MODE = "viewMode";
    // render a list of top level notes including header and footer if requested
    private static final String VIEW_MODE_LIST = "LIST";
    // render the notes of a discussion
    private static final String VIEW_MODE_DISCUSSION = "DISCUSSION";
    // render a single note of a discussion
    private static final String VIEW_MODE_DISCUSSION_NOTE = "DISCUSSION_NOTE";
    // render a single top-level note within the list
    private static final String VIEW_MODE_LIST_NOTE = "LIST_NOTE";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronologicalPostListWidget.class);

    private final static FilterWidgetParameterNameProvider NAME_PROVIDER = FilterWidgetParameterNameProvider.INSTANCE;

    private static final String[] SUPPLIED_PARAMETERS = new String[] {
        NAME_PROVIDER.getNameForTags(), NAME_PROVIDER.getNameForTagPrefix(),
        NAME_PROVIDER.getNameForUserIds(), NAME_PROVIDER.getNameForTargetBlogId(),
        NAME_PROVIDER.getNameForParentPostId(), PARAM_BLOG_POST_ID,
        NAME_PROVIDER.getNameForUserSearchString(), NAME_PROVIDER.getNameForBlogIds(),
        NAME_PROVIDER.getNameForShowPostsForMe(), NAME_PROVIDER.getNameForFavorite(),
        NAME_PROVIDER.getNameForStartDate(), NAME_PROVIDER.getNameForEndDate(),
        NAME_PROVIDER.getNameForPropertyFilter(), NAME_PROVIDER.getNameForFollowedNotes(),
        NAME_PROVIDER.getNameForFullTextSearchString(),
        NAME_PROVIDER.getNameForDirectMessages() };

    private static NoteQuery NOTE_QUERY_DEFINITION = QueryDefinitionRepository.instance()
            .getQueryDefinition(NoteQuery.class);

    private static PropertyManagement PROPERTY_MANAGMENT = ServiceLocator.instance().getService(
            PropertyManagement.class);

    /**
     * Get the parameter names, which are necessary to save or retrieve the corresponding newsfeeds.
     *
     * @return Array with all required parameter names
     */
    public static String[] getSuppliedParameters() {
        return SUPPLIED_PARAMETERS;
    }

    private final NoteQueryParametersConfigurator queryParametersConfigurator = new NoteQueryParametersConfigurator(
            NAME_PROVIDER);

    private NoteData previousNote;

    // the query instance used for querying the results
    private NoteQueryParameters queryParameters;

    private final StopWatch stopWatch = new StopWatch();

    private final CPLNoteItemTemplateProviderManagement noteItemTemplateProvider = WebServiceLocator
            .instance().getWidgetExtensionManagementRepository()
            .getExtensionManagement(CPLNoteItemTemplateProviderManagement.class);
    private final CPLNoteActionsProviderManagement noteActionsProvider = WebServiceLocator
            .instance().getWidgetExtensionManagementRepository()
            .getExtensionManagement(CPLNoteActionsProviderManagement.class);
    private final CPLNoteMetaDataProviderManagement noteMetaDataProvider = WebServiceLocator
            .instance().getWidgetExtensionManagementRepository()
            .getExtensionManagement(CPLNoteMetaDataProviderManagement.class);

    private TimelineFilterViewType selectedViewType;

    /**
     * Appends all supplied parameters to the StringBuilder if the params are not in the
     * paramsToIgnore array. The parameter values will be URI encoded.
     *
     * @param rssParams
     *            the StringBuilder used for appending
     * @param paramsToIgnore
     *            array of params to ignore, can be null
     */
    private void appendSuppliedParamsToRssParams(StringBuilder rssParams, String[] paramsToIgnore) {
        String[] params = getSuppliedParameters();

        for (int z = 0; z < params.length; z++) {
            if (!ArrayUtils.contains(paramsToIgnore, params[z])
                    && StringUtils.isNotEmpty(getParameters().get(params[z]))) {
                rssParams.append(params[z]);
                rssParams.append("=");
                rssParams.append(UriUtils.encodeUriComponent(getParameters().get(params[z])));
                rssParams.append("&");
            }
        }
    }

    /**
     * @param definition
     *            the definition to use
     * @return the configured tagging instance
     */
    protected NoteQueryParameters configureQueryInstance(NoteQuery definition) {
        NoteQueryParameters noteQueryParameters = definition.createInstance();
        queryParametersConfigurator.configure(getParameters(), noteQueryParameters);
        return noteQueryParameters;
    }

    /**
     * @param locale
     *            the locale of the current user
     * @return the render context suitable for the widget
     */
    private NoteRenderContext createRenderContext(Locale locale) {
        NoteRenderContext renderContext = new NoteRenderContext(NoteRenderMode.PORTAL, locale);
        renderContext.setRequest(getRequest());
        return renderContext;
    }

    /**
     * Get an boolean blog property.
     *
     * @param blogId
     *            the id of the blog to get the property of
     * @param keyGroup
     *            the key group of the property to get
     * @param key
     *            the key of the property
     * @return the boolean property or null if the property does not exists
     */
    public Boolean getBlogPropertyAsBoolean(Long blogId, String keyGroup, String key) {
        Boolean value = null;
        try {
            BlogProperty property = (BlogProperty) PROPERTY_MANAGMENT.getObjectProperty(
                    PropertyType.BlogProperty, blogId, keyGroup, key);
            if (property != null) {
                value = BooleanUtils.toBooleanObject(property.getPropertyValue());
            }
        } catch (NotFoundException e) {
            LOGGER.debug(e.getMessage());
        } catch (AuthorizationException e) {
            LOGGER.debug(e.getMessage());
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    /**
     * @return the userLocale
     */
    public Locale getCurrentUserLocale() {
        return SessionHandler.instance().getCurrentLocale(getRequest());
    }

    /**
     * Helper which fetches the notes of a discussion and returns them a style dependent of the
     * selected view.
     *
     * @param discussionId
     *            If of the discussion.
     * @param locale
     *            of the current user
     * @return the found notes
     */
    public PageableList<NoteData> getDiscussionNotes(long discussionId, boolean listViewMode,
            Locale locale) {
        // in listViewMode always use COMMENT instead of selectedViewType to get all comments
        QueryResultConverter<SimpleNoteListItem, DiscussionNoteData> discussionConverter = new SimpleNoteListItemToDiscussionNoteDataConverter(
                createRenderContext(locale), listViewMode ? COMMENT : selectedViewType, null);
        NoteService noteService = ServiceLocator.instance().getService(NoteService.class);
        try {
            DiscussionNoteData discussionData = noteService.getNoteWithComments(discussionId,
                    discussionConverter);
            // convert discussion object into flat list of note list data items
            List<NoteData> discussionNotes = new ArrayList<NoteData>();
            if (listViewMode) {
                // always add root note
                discussionNotes.add(discussionData);
                Matcher<NoteData> matcher = MatcherFactory.createMatcher(queryParameters);
                if (selectedViewType.equals(COMMENT)) {
                    Collection<NoteData> filteredCommets = matcher.filter(discussionData
                            .getComments());
                    discussionData.getComments().clear();
                    discussionData.getComments().addAll(filteredCommets);
                } else {
                    discussionNotes.addAll(discussionData.getComments());
                    discussionData.getComments().clear();
                    Collection<NoteData> filteredNotes = matcher.filter(discussionNotes);
                    discussionNotes.clear();
                    discussionNotes.addAll(filteredNotes);
                    // reverse as CLASSIC shows oldest at end
                    Collections.reverse(discussionNotes);
                }
            } else {
                // add discussion root to top
                if (selectedViewType == THREAD) {
                    discussionNotes.add(discussionData);
                }

                discussionNotes.addAll(discussionData.getComments());
            }
            PageableList<NoteData> result = new PageableList<NoteData>(discussionNotes);
            result.setMinNumberOfElements(discussionNotes.size());
            return result;
        } catch (NoteNotFoundException e) {
            LOGGER.debug(e.getMessage()); // Ignore, but log on debug
        } catch (AuthorizationException e) {
            LOGGER.debug(e.getMessage()); // Ignore, but log on debug
        }
        return PageableList.emptyList();
    }

    /**
     * Get the discussion title for the note (e.g. "Max Mustermann (max) um 18:08). If no parent
     * note is set then empty string is returned.
     *
     * @param note
     *            the note
     * @param locale
     *            the locale
     * @return the discussion title
     */
    public String getDiscussionTitle(NoteData note, Locale locale) {
        String discussionTitle;
        DateFormat discussionDate = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT, locale);
        TimeZone effectiveUserTimeZone = UserManagementHelper.getEffectiveUserTimeZone();
        discussionDate.setTimeZone(effectiveUserTimeZone);

        if (note.getParent() == null) {
            discussionTitle = ResourceBundleManager.instance().getText(
                    "blog.post.list.reply.link",
                    locale,
                    new Object[] {
                            UserNameHelper.getDetailedUserSignature(note.getUser().getFirstName(),
                                    note.getUser().getLastName(), note.getUser().getAlias()),
                                    discussionDate.format(note.getCreationDate()) });
        } else {
            discussionTitle = ResourceBundleManager.instance().getText(
                    "blog.post.list.reply.link",
                    locale,
                    new Object[] {
                            UserNameHelper.getDetailedUserSignature(note.getParent().getUser()
                                    .getFirstName(), note.getParent().getUser().getLastName(), note
                                    .getParent().getUser().getAlias()),
                                    discussionDate.format(note.getParent().getCreationDate()) });
        }
        return discussionTitle;
    }

    /**
     * Return a CSV listing the actions that can be executed on a note.
     *
     * @param note
     *            the note
     * @return a string containing the supported actions of a note separated by comma
     */
    public String getNoteActions(NoteData note) {
        List<String> actions = new ArrayList<>();
        // add built-in actions
        if (note.isCommentable()) {
            actions.add("comment");
        }
        if (note.hasPermission(NotePermissionManagement.PERMISSION_LIKE)) {
            actions.add("like");
        }
        if (note.hasPermission(NotePermissionManagement.PERMISSION_FAVOR)) {
            actions.add("favor");
        }
        if (note.isEditable()) {
            actions.add("edit");
        }
        if (note.isDeletable()) {
            actions.add("delete");
        }
        if (note.getParent() == null
                && note.hasPermission(NotePermissionManagement.PERMISSION_MOVE)) {
            actions.add("move");
        }
        if (note.isRepostable()) {
            actions.add("repost");
        }
        actions.add("export");
        actions.add("permalink");
        // call plugin providers
        noteActionsProvider.addActions(getParameters(), note, actions);
        return StringUtils.join(actions, ',');
    }

    /**
     * Return the location of a template to use for rendering the HTML of the provided note
     *
     * @param data
     *            the note details
     * @return the template location
     */
    public String getNoteItemTemplate(NoteData data) {
        String template = noteItemTemplateProvider.getNoteItemTemplate(data);
        if (template == null) {
            template = "/WEB-INF/vm/widget/blog/ChronologicalPostList-Single-Note.Widget.html.vm";
        }
        return template;
    }

    /**
     * Create a JSON object containing some meta data about a note.
     *
     * @param note
     *            the note
     * @return the serialized JSON
     */
    public String getNoteMetaData(NoteData note) {
        Map<String, Object> metaData = new HashMap<String, Object>();
        metaData.put("topicId", note.getBlog().getId());
        metaData.put("topicAlias", note.getBlog().getAlias());
        metaData.put("favorite", note.isFavorite());
        Boolean liked = note.getProperty(LikeNoteRenderingPreProcessor.PROPERTY_LIKED);
        metaData.put("liked", Boolean.TRUE.equals(liked));
        Collection<?> likers = note.getProperty(LikeNoteRenderingPreProcessor.PROPERTY_LIKERS);
        int likeCount = likers != null ? likers.size() : 0;
        metaData.put("likeCount", likeCount);
        metaData.put("discussionId", note.getDiscussionId());
        metaData.put("discussionCommentCount", note.getNumberOfDiscussionNotes() - 1);
        metaData.put("discussionDepth", note.getDiscussionDepth());
        metaData.put("discussionPath", note.getDiscussionPath());
        noteMetaDataProvider.addMetaData(getParameters(), note, metaData);
        try {
            return JsonHelper.getSharedObjectMapper().writeValueAsString(metaData);
        } catch (IOException e) {
            LOGGER.error("Converting note metadata to JSON failed", e);
            return "{}";
        }
    }

    /**
     * Get the note title for the note (e.g. "Max Mustermann (max) um 18:08)
     *
     * @param note
     *            the note
     * @param locale
     *            the locale
     * @return title for the note
     */
    public String getNoteTitle(NoteData note, Locale locale) {
        DateFormat noteDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
                locale);
        TimeZone effectiveUserTimeZone = UserManagementHelper.getEffectiveUserTimeZone();
        noteDate.setTimeZone(effectiveUserTimeZone);
        String noteTitle = ResourceBundleManager.instance().getText(
                "blog.post.single.item.title",
                locale,
                UserNameHelper.getDetailedUserSignature(note.getUser().getFirstName(), note
                        .getUser().getLastName(), note.getUser().getAlias()),
                        noteDate.format(note.getCreationDate()));
        return noteTitle;
    }

    /**
     * @return The parent post id as long.
     */
    public long getParentPostFilter() {
        return getLongParameter(NAME_PROVIDER.getNameForParentPostId(), 0);
    }

    /**
     *
     * @return the query parameters used for this widget
     */
    public NoteQueryParameters getQueryParameters() {
        return queryParameters;
    }

    /**
     * {@inheritDoc}
     */
    public String getRssFeedLink() {
        return ControllerHelper.getRssFeedLink(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRssParameters() {
        StringBuilder rssParams = new StringBuilder();
        // ignore start and enddate for URL rendering
        appendSuppliedParamsToRssParams(
                rssParams,
                new String[] { NAME_PROVIDER.getNameForStartDate(),
                        NAME_PROVIDER.getNameForEndDate() });
        return rssParams.toString();
    }

    /**
     * Get the details of a single note.
     *
     * @param noteId
     *            the ID of the note
     * @param locale
     *            the locale of the current user
     * @return a pageable list containing the details of the note. If the note does not exist an
     *         empty list is returned.
     */
    private PageableList<NoteData> getSingleNote(Long noteId, Locale locale) {
        NoteService noteService = ServiceLocator.findService(NoteService.class);

        NoteData noteListData = null;
        try {
            noteListData = noteService.getNote(noteId, createRenderContext(locale));
        } catch (AuthorizationException e) {
            LOGGER.debug("Current user is not authorized to access note {}", noteId);
        } catch (NoteNotFoundException e) {
            LOGGER.debug("Note {} does not exist", noteId);
        }
        if (noteListData == null) {
            return PageableList.emptyList();
        }
        PageableList<NoteData> result = new PageableList<NoteData>(new ArrayList<NoteData>(1));
        result.add(noteListData);
        result.setMinNumberOfElements(1);
        return result;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.blog.post.list";
    }

    /**
     * Gets the effective time zone for the user
     *
     * @return the time zone of the current user
     */
    public TimeZone getTimeZone() {
        return UserManagementHelper.getEffectiveUserTimeZone();
    }

    /**
     * Gets the effective time zone id for the user
     *
     * @return effective time zone id of current user
     */

    public String getTimeZoneId() {
        TimeZone timeZone = UserManagementHelper.getEffectiveUserTimeZone();
        return timeZone.getID();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PageableList<NoteData> handleQueryList() {
        selectedViewType = TimelineQueryParametersConfigurator.determineViewType(getParameters(),
                NAME_PROVIDER);

        if (LogHelper.PERF_LOG.isDebugEnabled()) {
            stopWatch.start();
        }
        Locale locale = getCurrentUserLocale();
        queryParameters = configureQueryInstance(NOTE_QUERY_DEFINITION);

        previousNote = null;
        PageableList<NoteData> result;
        String viewMode = getParameter(PARAM_VIEW_MODE, VIEW_MODE_LIST);
        // optimization: bypass query stuff fetch data directly from cache where applicable
        if (queryParameters.getNoteId() != null && VIEW_MODE_DISCUSSION_NOTE.equals(viewMode)) {
            result = getSingleNote(queryParameters.getNoteId(), locale);
        } else if (queryParameters.getDiscussionId() != null) {
            result = getDiscussionNotes(queryParameters.getDiscussionId(),
                    VIEW_MODE_LIST.equals(viewMode), locale);
        } else if (queryParameters.getNoteId() != null
                && VIEW_MODE_DISCUSSION_NOTE.equals(viewMode)) {
            result = getSingleNote(queryParameters.getNoteId(), locale);
        } else {
            SimpleNoteListItemToDiscussionNoteDataConverter converter = new SimpleNoteListItemToDiscussionNoteDataConverter(
                    createRenderContext(locale), selectedViewType, queryParameters);
            Matcher<NoteData> matcher = MatcherFactory.createMatcher(queryParameters);
            converter.setCommentMatcher(matcher, true);
            PageableList<DiscussionNoteData> interimResult = ServiceLocator.findService(
                    QueryManagement.class).query(NOTE_QUERY_DEFINITION, queryParameters, converter);
            result = new PageableList<NoteData>(new ArrayList<NoteData>(interimResult));
            result.setOffset(interimResult.getOffset());
            result.setMinNumberOfElements(interimResult.getMinNumberOfElements());
        }

        setPageInformation(queryParameters, result);
        if (LogHelper.PERF_LOG.isDebugEnabled()) {
            stopWatch.stop();
            LogHelper.logPerformance("ChronologicalPostList#queryList", stopWatch);
        }
        setAttributesAndResponseMetadata(result);
        return result;
    }

    /**
     * init the widget parameters to these values: filter = '' offset = 0 maxCount = 30
     */
    @Override
    protected void initParameters() {
        super.initParameters();
        setParameter(NAME_PROVIDER.getNameForUserIds(), StringUtils.EMPTY);
        setParameter(NAME_PROVIDER.getNameForBlogIds(), StringUtils.EMPTY);
    }

    /**
     * Called after rendering the list
     */
    public void postRenderList() {
        if (selectedViewType == THREAD) {
            int lastLevel = previousNote == null ? 0 : previousNote.getDiscussionDepth();
            getRequest().setAttribute("levelDivAfter", StringUtils.repeat("</div>", lastLevel));
        }
    }

    /**
     * This method sets necessary query attributes in the request and the response metadata to be
     * transfered to the client-side widget.
     *
     * @param result
     *            The result items.
     */
    private void setAttributesAndResponseMetadata(PageableList<? extends NoteData> result) {
        Long targetTopicId = ParameterHelper.getParameterAsLong(getParameters(),
                NAME_PROVIDER.getNameForTargetBlogId());
        getRequest().setAttribute("targetBlogIdSet", targetTopicId != null);
        getRequest().setAttribute("singleBlogSelected",
                queryParameters.getTypeSpecificExtension().isFilteredForSingleBlog());
        String viewMode = setViewModeAndRenderStyleAttribute();
        if (result.size() > 0 && viewMode.equals(VIEW_MODE_LIST)) {
            Date lastNoteDate;
            Long lastNoteId;
            Long firstNoteCreationTimestamp;
            if (selectedViewType == COMMENT) {
                if (result.get(result.size() - 1) instanceof DiscussionNoteData) {
                    DiscussionNoteData lastDiscussion = (DiscussionNoteData) result.get(result
                            .size() - 1);
                    if (lastDiscussion.getComments() == null
                            || lastDiscussion.getComments().size() == 0) {
                        lastNoteDate = lastDiscussion.getLastDiscussionCreationDate();
                        lastNoteId = lastDiscussion.getId();
                    } else {
                        NoteData lastNote = lastDiscussion.getComments().get(
                                lastDiscussion.getComments().size() - 1);
                        lastNoteDate = lastNote.getCreationDate();
                        lastNoteId = lastNote.getDiscussionId();
                    }
                } else {
                    lastNoteDate = result.get(result.size() - 1).getCreationDate();
                    lastNoteId = result.get(result.size() - 1).getId();
                }
                firstNoteCreationTimestamp = result.get(0).getLastDiscussionCreationDate()
                        .getTime();
            } else {
                // CLASSIC style
                firstNoteCreationTimestamp = result.get(0).getCreationDate().getTime();
                lastNoteDate = result.get(result.size() - 1).getCreationDate();
                lastNoteId = result.get(result.size() - 1).getId();
            }
            setResponseMetadata(RESPONSE_METADATA_LAST_NOTE_DATE, lastNoteDate.getTime());
            setResponseMetadata(RESPONSE_METADATA_FIRST_NOTE_CREATION_TIMESTAMP,
                    firstNoteCreationTimestamp);
            setResponseMetadata(RESPONSE_METADATA_LAST_NOTE_ID, lastNoteId);
        }
        getRequest().setAttribute("selectedViewType", selectedViewType);
        getRequest().setAttribute(
                ATTRIBUTE_VIEW_TYPE_ARRAY,
                TimelineFilterViewType.valuesOf(getParameter(PARAMETER_VIEW_TYPES,
                        CLASSIC.toString())));
    }

    /**
     * Check request for viewMode parameter and set it as a request attribute. Additionally derive
     * the renderStyle from selectedViewType that is suitable for the found viewMode.
     *
     * @return the found view mode
     */
    private String setViewModeAndRenderStyleAttribute() {
        String mode = getParameter(PARAM_VIEW_MODE, VIEW_MODE_LIST);
        String renderStyle;
        if (VIEW_MODE_LIST.equals(mode) || VIEW_MODE_LIST_NOTE.equals(mode)) {
            // list only supports classic or comment
            renderStyle = selectedViewType == COMMENT ? COMMENT.name() : CLASSIC.name();
        } else if (VIEW_MODE_DISCUSSION.equals(mode) || VIEW_MODE_DISCUSSION_NOTE.equals(mode)) {
            renderStyle = selectedViewType == COMMENT ? COMMENT.name() : THREAD.name();
        } else {
            LOGGER.debug("Unsupported viewMode {}, falling back to LIST", mode);
            mode = VIEW_MODE_LIST;
            renderStyle = selectedViewType == COMMENT ? COMMENT.name() : CLASSIC.name();
        }
        getRequest().setAttribute("renderStyle", renderStyle);
        getRequest().setAttribute(PARAM_VIEW_MODE, mode);
        return mode;
    }
}
