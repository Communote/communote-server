package com.communote.server.web.fe.portal.blog.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.string.StringHelper;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.blog.export.PermalinkGenerationManagement;
import com.communote.server.core.blog.export.PermalinkGenerator;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagNotFoundException;
import com.communote.server.core.util.InitalFiltersVO;
import com.communote.server.core.util.InitialFilterDataProvider;
import com.communote.server.model.tag.Tag;
import com.communote.server.web.commons.controller.VelocityViewController;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * This controller can put initial filter parameters in the model which are retrieved by
 * interpreting the request URL as a permanent link.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class InitialFiltersViewController extends VelocityViewController {

    /**
     * name of request parameter that defines what type the filter (aka value, aka identifier) is.
     * Possible values are ID or ALIAS, default is ALIAS
     */
    private static final String REQUEST_PARAM_ENTITY_IDENTIFIER = "entityIdentifier";

    /** value for the entity identifier which represent the database id */
    private static final String REQUEST_PARAM_ENTITY_IDENTIFIER_VALUE_ID = "ID";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(InitialFiltersViewController.class);

    /** Name of the model key with the json string of the initial filters */
    public static final String KEY_INITIAL_FILTERS_JSON = "initialFiltersJson";

    private static final String KEY_PERMALINK_FOUND = "initialFiltersPermalinkFound";

    /** Name for the model key with the java VO of the initial filters */
    public static final String KEY_INITIAL_FILTERS_VO = "initialFilters";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.getSerializationConfig().withSerializationInclusion(Inclusion.NON_NULL);
    }
    private Set<String> permalinkIdsToRecognize = new HashSet<String>();
    private String filtersFoundContentTemplate;
    private String filtersFoundSelectedMenu;

    /**
     * Check for tagId request parameter and if set add the tag to the initial filter parameters
     *
     * @param request
     *            the current request
     * @param initialFilters
     *            the filters to add the tag details to
     * @throws TagNotFoundException
     *             in case the tag does not exist
     */
    private void addTagFromRequestParameterToInitialFilters(HttpServletRequest request,
            InitalFiltersVO initialFilters) throws TagNotFoundException {
        Long tagId = ParameterHelper.getParameterAsLong(request.getParameterMap(), "tagId", null);
        if (tagId != null) {
            Locale locale = SessionHandler.instance().getCurrentLocale(request);
            setTagFromId(tagId, initialFilters, locale);
        }
    }

    /**
     * Creates the initial filters by invoking {@link #prefillInitalFilters(HttpServletRequest)} and
     * passing the prefilled VO to BlogManagement.fillIntalFilters.
     *
     * @param request
     *            the request
     * @return the initial filters or null
     * @throws NotFoundException
     *             if the resource identified by the permanent link cannot be found
     * @throws AuthorizationException
     *             if the current user has no access to the resource identified by the permanent
     *             link
     */
    protected InitalFiltersVO createInitialFilters(HttpServletRequest request)
            throws NotFoundException, AuthorizationException {
        InitalFiltersVO initialFilters = prefillInitalFilters(request);

        // fill the filters
        if (initialFilters != null) {
            initialFilters = ServiceLocator.findService(InitialFilterDataProvider.class)
                    .fillInitalFilters(initialFilters,
                            SessionHandler.instance().getCurrentLocale(request));
        }
        return initialFilters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getContentTemplate(Map<String, Object> model) {
        if (this.filtersFoundContentTemplate != null
                && Boolean.TRUE.equals(model.get(KEY_PERMALINK_FOUND))) {
            return this.filtersFoundContentTemplate;
        }
        return super.getContentTemplate(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSelectedMenu(Map<String, Object> model) {
        if (this.filtersFoundSelectedMenu != null
                && Boolean.TRUE.equals(model.get(KEY_PERMALINK_FOUND))) {
            return this.filtersFoundSelectedMenu;
        }
        return super.getSelectedMenu(model);
    }

    /**
     * Called when a AuthorizationException is thrown while the initial filters are created. The
     * default implementation sends a 403.
     *
     * @param e
     *            the exception
     * @param response
     *            the response object
     * @return if the response should be considered to be committed
     * @throws IOException
     *             in case of an IOException when writing to the response
     */
    protected boolean handleAuthorizationException(AuthorizationException e,
            HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        LOGGER.debug(e.getMessage());
        return true;
    }

    /**
     * Called when a NotFoundException is thrown while the initial filters are created. The default
     * implementation sends a 404.
     *
     * @param e
     *            the exception
     * @param request
     *            The request.
     * @param response
     *            the response object
     * @return if the response should be considered to be committed
     * @throws IOException
     *             in case of an IOException when writing to the response
     */
    protected boolean handleNotFoundException(NotFoundException e, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        if (e instanceof UserNotFoundException) {
            request.setAttribute(ControllerHelper.ATTRIBUTE_NAME_ERROR_PAGE_NEXT_TARGET,
                    "portal/users");
            LOGGER.debug(e.getMessage(), e);
        } else if (e instanceof BlogNotFoundException) {
            request.setAttribute(ControllerHelper.ATTRIBUTE_NAME_ERROR_PAGE_NEXT_TARGET,
                    "portal/topics");
            LOGGER.debug(e.getMessage(), e);
        } else {
            request.setAttribute("throwable", e);
        }
        return false;
    }

    /**
     * Called after {@link #createInitialFilters(HttpServletRequest)} succeeded, to allow subclasses
     * to do further modifications on the filters
     *
     * @param filters
     *            the filters created, can be null if no filters were created
     * @return the modified filters
     * @throws AuthorizationException
     */
    protected InitalFiltersVO postCreateInitialFilters(InitalFiltersVO filters)
            throws AuthorizationException {
        return filters;
    }

    /**
     * Prefill the initial filters by extracting the values from the request URL that is interpreted
     * as permalink
     *
     * @param request
     *            the request
     * @return the prefilled filters or null if the URL isn't a supported permanent link
     * @throws NotFoundException
     *             in case the permalink refers to a tag entity which does not exist
     */
    protected InitalFiltersVO prefillInitalFilters(HttpServletRequest request)
            throws NotFoundException {
        // do not scan for initial filters if no permalink recognition is defined
        if (permalinkIdsToRecognize.size() == 0) {
            return null;
        }
        String uri = request.getRequestURI().trim();
        uri = StringUtils.substringBefore(uri, ";");
        // cut ".do" at the end
        uri = uri.endsWith(".do") ? uri.substring(0, uri.length() - 3) : uri;
        String[] uriFragments = StringUtils.split(uri, "/");

        String permaLinkIdent = ServiceLocator.instance()
                .getService(PermalinkGenerationManagement.class)
                .extractPermaLinkIdentifier(uriFragments);
        InitalFiltersVO initialFilters = new InitalFiltersVO();

        if (permalinkIdsToRecognize.contains(permaLinkIdent)) {
            initialFilters.setPermalinkFound(true);
            String filter = uriFragments[uriFragments.length - 1];
            try {
                filter = URLDecoder.decode(filter, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOGGER.warn("There was an error encoding a url: " + e.getMessage());
            }
            if (PermalinkGenerator.PERMA_ID_TOPICS.equals(permaLinkIdent)) {

                setTopicFilters(request, initialFilters, filter);

            } else if (PermalinkGenerator.PERMA_ID_USERS.equals(permaLinkIdent)) {

                setUserFilters(request, initialFilters, filter);

            } else if (PermalinkGenerator.PERMA_ID_TAGS.equals(permaLinkIdent)) {

                setTagFilters(request, initialFilters, uriFragments, filter);

            } else if (PermalinkGenerator.PERMA_ID_NOTES.equals(permaLinkIdent)) {

                setNoteFilters(initialFilters, filter);

            } else {
                initialFilters = null;
            }
        } else {
            // none of the entity permalinks, so it is probably one of the overview sites -> check
            // for tags
            addTagFromRequestParameterToInitialFilters(request, initialFilters);
        }
        return initialFilters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean prepareModel(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws Exception {
        try {
            InitalFiltersVO initialFilters = createInitialFilters(request);
            initialFilters = postCreateInitialFilters(initialFilters);
            if (initialFilters != null) {
                String jsonFilters = OBJECT_MAPPER.writeValueAsString(initialFilters);
                model.put(KEY_PERMALINK_FOUND, initialFilters.isPermalinkFound());
                model.put(KEY_INITIAL_FILTERS_JSON, jsonFilters);
                LOGGER.debug("InitalFilters are {}", jsonFilters);
                model.put(KEY_INITIAL_FILTERS_VO, initialFilters);
            }
        } catch (NotFoundException e) {
            LOGGER.debug("Error filling initalFilters={}", e.getMessage());
            return this.handleNotFoundException(e, request, response);

        } catch (AuthorizationException e) {
            LOGGER.debug("Error filling initalFilters={}", e.getMessage());
            return this.handleAuthorizationException(e, response);
        }

        return super.prepareModel(request, response, model);
    }

    /**
     * Set a template that should be rendered when initial filters are defined. If no initial
     * filters are defined the template defined by {@link #setContentTemplate(String))} will be
     * used.
     *
     * @param template
     *            the template to render, if null the template defined by
     *            {@link #setContentTemplate(String))} will always be used. The default value for
     *            this template is null.
     */
    public void setFiltersFoundContentTemplate(String template) {
        this.filtersFoundContentTemplate = template;
    }

    /**
     * Sets the identifier of the menu that should be marked as selected when initial filters are
     * defined. If no initial filters are defined the identifier defined by
     * {@link #setSelectedMenu(String))} will be used.
     *
     * @param menuId
     *            the menu identifier to set, if null the identifier defined by
     *            {@link #setSelectedMenu(String))} will always be used. The default value for this
     *            identifier is null.
     */
    public void setFiltersFoundSelectedMenu(String menuId) {
        this.filtersFoundSelectedMenu = menuId;
    }

    private void setNoteFilters(InitalFiltersVO initialFilters, String filter) {
        Long noteId = StringHelper.getStringAsLong(filter, null);
        initialFilters.setNoteId(noteId);
    }

    /**
     * Set the IDs the view should recognize as permanent link identifier when creating the initial
     * filters.
     *
     * @param ids
     *            the IDs to recognize, this should be any of the PermalinkGenerator.PERMA_ID_*
     *            constants
     */
    public void setPermalinkIdsToRecognize(Set<String> ids) {
        if (ids == null) {
            return;
        }
        this.permalinkIdsToRecognize = ids;
    }

    /**
     * Set the tag within the initial filters.
     *
     * @param tagName
     *            The filter to use.
     * @param initialFilters
     *            The initial filters.
     * @throws TagNotFoundException
     *             in case the tag does not exist
     */
    private void setTag(String tagName, InitalFiltersVO initialFilters) throws TagNotFoundException {
        if (tagName == null || tagName.trim().length() == 0) {
            return;
        }
        TagManagement tagManagement = ServiceLocator.instance().getService(TagManagement.class);
        Tag tag = tagManagement.findTag(tagName, Types.NOTE);
        if (tag != null) {
            initialFilters.setTagId(tag.getId());
            initialFilters.setTagName(tag.getName());
        } else {
            throw new TagNotFoundException("The tag " + tagName + " does not exist");
        }

    }

    private void setTagFilters(HttpServletRequest request, InitalFiltersVO initialFilters,
            String[] uriFragments, String filter) throws TagNotFoundException {
        if (uriFragments[uriFragments.length - 2].matches("\\d+")) {
            Locale locale = SessionHandler.instance().getCurrentLocale(request);
            setTagFromNumber(uriFragments[uriFragments.length - 2], initialFilters, locale);
        } else {
            setTag(filter, initialFilters);
        }
    }

    /**
     * Set the tag within the initial filters.
     *
     * @param tagId
     *            The tag ID
     * @param initialFilters
     *            The initial filters.
     * @param locale
     *            The locale to use.
     * @throws TagNotFoundException
     *             in case the tag does not exist
     */
    private void setTagFromId(Long tagId, InitalFiltersVO initialFilters, Locale locale)
            throws TagNotFoundException {
        TagManagement tagManagement = ServiceLocator.instance().getService(TagManagement.class);
        TagData tag = tagManagement.findTag(tagId, locale);
        if (tag != null) {
            initialFilters.setTagId(tagId);
            initialFilters.setTagName(tag.getName());
        } else {
            throw new TagNotFoundException("The tag with ID " + tagId + " does not exist");
        }
    }

    /**
     * Set the tag within the initial filters.
     *
     * @param tagIdString
     *            The tag ID as string.
     * @param initialFilters
     *            The initial filters.
     * @param locale
     *            The locale to use.
     * @throws TagNotFoundException
     *             in case the tag does not exist
     */
    private void setTagFromNumber(String tagIdString, InitalFiltersVO initialFilters, Locale locale)
            throws TagNotFoundException {
        Long tagId = Long.parseLong(tagIdString);
        setTagFromId(tagId, initialFilters, locale);
    }

    /**
     * Use the request parameter to either use the given filter as topic alias or as topic id
     *
     * @param request
     * @param initialFilters
     * @param filter
     */
    private void setTopicFilters(HttpServletRequest request, InitalFiltersVO initialFilters,
            String filter) {
        final String entityIdentifier = request.getParameter(REQUEST_PARAM_ENTITY_IDENTIFIER);
        if (REQUEST_PARAM_ENTITY_IDENTIFIER_VALUE_ID.equals(entityIdentifier)) {
            Long topicId = StringHelper.getStringAsLong(filter, null);
            initialFilters.setBlogId(topicId);
        } else {
            initialFilters.setBlogAlias(filter);
        }
    }

    /**
     * Use the request parameter to either use the given filter as user alias or as user id
     *
     * @param request
     * @param initialFilters
     * @param filter
     */
    private void setUserFilters(HttpServletRequest request, InitalFiltersVO initialFilters,
            String filter) {
        final String entityIdentifier = request.getParameter(REQUEST_PARAM_ENTITY_IDENTIFIER);
        if (REQUEST_PARAM_ENTITY_IDENTIFIER_VALUE_ID.equals(entityIdentifier)) {
            Long userId = StringHelper.getStringAsLong(filter, null);
            initialFilters.setUserId(userId);
        } else {
            initialFilters.setUserAlias(filter);
        }
    }
}
