package com.communote.plugins.embed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.common.string.StringEscapeHelper;
import com.communote.common.string.StringHelper;
import com.communote.common.util.ParameterHelper;
import com.communote.plugins.core.views.ViewController;
import com.communote.plugins.core.views.ViewControllerException;
import com.communote.plugins.core.views.annotations.Page;
import com.communote.plugins.core.views.annotations.UrlMapping;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.filter.PropertyFilter;
import com.communote.server.model.user.User;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.external.spring.security.CommunoteAuthenticationSuccessHandler;

/**
 * Controller to embed Communote elsewhere
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component(immediate = true)
@Provides(specifications = { EmbedController.class, Controller.class })
@Instantiate
@UrlMapping("/*" + EmbedController.URL)
@Page(menu = "embed", jsMessagesCategory = "portal", jsCategories = { "tinyMCE", "communote-core",
"cnt-embed" }, cssCategories = { "cnt-embed" })
public class EmbedController extends ViewController implements Controller {

    protected static final String URL = "/embed";
    /**
     * known parameters to disable filters
     */
    private static final String[] SHOW_FILTER_PARAMS = new String[] { "fiShowSearch",
        "fiShowTagCloud", "fiShowAuthor", "fiShowTopic", "fiShowContentType", "fiShowDate" };

    /**
     * known boolean parameters to customize the editor view
     */
    private static final String[] SHOW_EDITOR_PARAMS = new String[] { "edShowCreate" };

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbedController.class);

    private static final List<String> KNOWN_VIEWS = new ArrayList<>();

    public static final String MODEL_FIELD_UNRESETABLE_FILTER_PARAMETERS = "unresetableFilterParameters";

    public static final String MODEL_FIELD_CONTEXT_ID = "contextId";
    public static final String MODEL_FIELD_ED_PREDEFINED_PROPERTIES = "edPredefinedProperties";
    public static final String PROPERTY_EMBED_ADDITIONAL_CSS_URL = "embedAdditionalCssUrl";

    /**
     * Add a note property to to the properties which should be passed to the editor.
     *
     * @param mav
     *            the model and view
     * @param keyGroup
     *            the group of the property
     * @param key
     *            the key of the property
     * @param value
     *            the value
     */
    public static void addEdPredefinedNoteProperty(ModelAndView mav, String keyGroup, String key,
            String value) {
        Map<String, Object> model = mav.getModel();
        Object props = model.get(MODEL_FIELD_ED_PREDEFINED_PROPERTIES);
        ArrayNode propsArray;
        if (props instanceof ArrayNode) {
            propsArray = (ArrayNode) props;
        } else {
            propsArray = JsonHelper.getSharedObjectMapper().createArrayNode();
        }
        ObjectNode propsObj = propsArray.addObject();
        propsObj.put("keyGroup", keyGroup);
        propsObj.put("key", key);
        propsObj.put("value", value);
        model.put(MODEL_FIELD_ED_PREDEFINED_PROPERTIES, propsArray);
    }

    public static Object createNotePropertyFilter(String keyGroup, String key, String value,
            PropertyFilter.MatchMode matchMode, boolean negate) {
        ObjectNode propsObj = JsonHelper.getSharedObjectMapper().createObjectNode();
        propsObj.put("name", "npf");
        ArrayNode filterData = propsObj.putArray("value");
        filterData.add("Note");
        filterData.add(keyGroup);
        filterData.add(key);
        filterData.add(value);
        filterData.add(matchMode.name());
        // negation flag is optional
        if (negate) {
            filterData.add(true);
        }
        return propsObj;
    }

    private BlogManagement blogManagement;

    private UserManagement userManagement;
    private List<EmbedControllerRequestPostProcessor> postProcessors = new ArrayList<>();

    private final QueryParametersParameterNameProvider nameProvider;
    private String addedRedirectionTarget;

    /**
     * @param bundleContext
     *            The current bundle context.
     */
    public EmbedController(BundleContext bundleContext) {
        super(bundleContext.getBundle().getSymbolicName());
        this.nameProvider = new FilterWidgetParameterNameProvider();
        KNOWN_VIEWS.add("all");
        KNOWN_VIEWS.add("following");
        KNOWN_VIEWS.add("mentions");
        KNOWN_VIEWS.add("favorites");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {
        doGet(request, response, model);
    }

    /**
     * @return lazily initialized blogManagement
     */
    private BlogManagement getBlogManagement() {
        if (this.blogManagement == null) {
            this.blogManagement = ServiceLocator.findService(BlogManagement.class);
        }
        return this.blogManagement;
    }

    @Override
    public String getContentTemplate() {
        return "/vm/embed/embed-content.vm";
    }

    @Override
    public String getMainTemplate() {
        return "communote.plugins.embed.page";
    }

    public QueryParametersParameterNameProvider getNameProvider() {
        return nameProvider;
    }

    /**
     * Get a collection of topic IDs provided by request parameters.
     *
     * @param request
     *            the current request
     * @param topicIdParamName
     *            the name of the parameter that holds topic IDs
     * @param topicAliasParamName
     *            the name of the parameter that holds topic aliases. Topic aliases that do not
     *            exist are ignored.
     * @return the topic IDs, without duplicates
     */
    private List<Long> getTopicIds(HttpServletRequest request, String topicIdParamName,
            String topicAliasParamName) {
        // get ids, ignore those with a value <= 0
        List<Long> result = StringHelper.getStringAsLongList(
                request.getParameter(topicIdParamName), true);
        if (result == null) {
            result = new ArrayList<>();
        }
        String[] topicAliases = StringUtils.split(request.getParameter(topicAliasParamName), ',');
        if (topicAliases != null && topicAliases.length > 0) {
            BlogManagement blogManagement = getBlogManagement();
            for (String alias : topicAliases) {
                Long topicId = blogManagement.getBlogId(alias);
                if (topicId == null) {
                    LOGGER.debug("Topic with alias {} does not exist", alias);
                } else if (!result.contains(topicId)) {
                    result.add(topicId);
                }
            }
        }
        return result;
    }

    /**
     * @return lazily initialized userManagement
     */
    private UserManagement getUserManagement() {
        if (this.userManagement == null) {
            this.userManagement = ServiceLocator.findService(UserManagement.class);
        }
        return this.userManagement;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = super.handleRequest(request, response);
        for (EmbedControllerRequestPostProcessor processor : postProcessors) {
            processor.process(request, modelAndView);
        }
        // serialize the unresetable parameters to JSON
        serializeModelEntryToJSON(modelAndView, MODEL_FIELD_UNRESETABLE_FILTER_PARAMETERS);
        // serialize the predefined properties
        serializeModelEntryToJSON(modelAndView, MODEL_FIELD_ED_PREDEFINED_PROPERTIES);
        return modelAndView;
    }

    /**
     * Map a viewId from the HTML-Client naming scheme to current and also test other viewIds
     * whether they are valid.
     *
     * @param viewId
     *            the view ID to map and test
     * @return null if the view ID cannot be mapped and is not valid, the mapped view ID otherwise
     */
    private String mapAndFilterViewId(String viewId) {
        if ("me".equals(viewId)) {
            viewId = "mentions";
        } else {
            if (!KNOWN_VIEWS.contains(viewId)) {
                viewId = null;
            }
        }
        return viewId;
    }

    /**
     * Evaluate the value of the msgViewSelected parameter which defines the default view ID
     *
     * @param request
     *            the current request
     * @param viewsToShow
     *            collection of view IDs to show, can be empty.
     * @return msgViewSelected if not null and valid (known and contained in viewsToShow if not
     *         empty), otherwise the first view ID of viewsToShow or known views
     */
    private String parseSelectedViewParameter(HttpServletRequest request, List<String> viewsToShow) {
        // shortcut
        if (viewsToShow.size() == 1) {
            return viewsToShow.get(0);
        }
        String selectedView = mapAndFilterViewId(request.getParameter("msgViewSelected"));
        if (selectedView != null) {
            if (!viewsToShow.contains(selectedView)) {
                selectedView = null;
            }
        }
        if (selectedView == null) {
            // TODO check preference?
            if (viewsToShow.size() == 0) {
                selectedView = KNOWN_VIEWS.get(0);
                // at least show this view
                viewsToShow.add(selectedView);
            } else {
                selectedView = viewsToShow.get(0);
            }
        }
        return selectedView;
    }

    /**
     * Parse parameters that toggle showing the create not widget and expose the parameters to the
     * model. Default is true for all show parameters.
     *
     * @param request
     *            the current request
     * @param model
     *            the model for saving settings required for rendering the create note widget
     */
    private void parseShowEditorParameters(HttpServletRequest request, Map<String, Object> model) {
        for (String paramName : SHOW_EDITOR_PARAMS) {
            boolean show = ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                    paramName, true);
            model.put(paramName, show);
        }
    }

    /**
     * Parse parameters that toggle rendering of the filters and expose the parameters to model with
     * the value or a default.
     *
     * @param request
     *            the current request
     * @param model
     *            the model for saving settings required for rendering the view
     */
    private void parseShowFilterParameters(HttpServletRequest request, Map<String, Object> model) {
        // fiShowFilter can disable other filters and has precedence
        boolean showFilter = ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                "fiShowFilter", true);
        if (!showFilter) {
            for (String paramName : SHOW_FILTER_PARAMS) {
                model.put(paramName, false);
            }
        } else {
            showFilter = false;
            for (String paramName : SHOW_FILTER_PARAMS) {
                boolean shown = ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                        paramName, true);
                if (shown) {
                    showFilter = true;
                }
                model.put(paramName, shown);
            }
        }
        model.put("fiShowFilter", showFilter);
    }

    /**
     * Parse parameter that defines the viewIds of the views to be rendered and expose those viewIds
     * to the model. Additionally the default viewId and the showHorizontalNavigation flag is
     * exposed too.
     *
     * @param request
     *            the current request
     * @param model
     *            the model for saving settings required for rendering the view
     */
    private void parseShowViewParameters(HttpServletRequest request, Map<String, Object> model) {
        List<String> viewsToShow = new ArrayList<>(KNOWN_VIEWS);
        String msgShowViewsParam = request.getParameter("msgShowViews");
        msgShowViewsParam = StringUtils.trimToNull(msgShowViewsParam);
        if (msgShowViewsParam != null) {
            String[] providedViews = msgShowViewsParam.split(",");
            HashSet<String> providedViewsFiltered = new HashSet<>();
            for (int i = 0; i < providedViews.length; i++) {
                String viewId = mapAndFilterViewId(providedViews[i]);
                if (viewId != null) {
                    providedViewsFiltered.add(viewId);
                }
            }
            Iterator<String> viewIdIter = viewsToShow.iterator();
            while (viewIdIter.hasNext()) {
                if (!providedViewsFiltered.contains(viewIdIter.next())) {
                    viewIdIter.remove();
                }
            }
        }
        model.put("showHorizontalNavigation", viewsToShow.size() != 0);
        String defaultViewId = parseSelectedViewParameter(request, viewsToShow);
        model.put("defaultViewId", defaultViewId);
        model.put("viewIds", "'" + StringUtils.join(viewsToShow, "','") + "'");
    }

    /**
     * Parse parameters that filter for tags.
     *
     * @param request
     *            the current request
     * @param model
     *            the model for saving settings required for rendering the view
     * @param unresetableParams
     *            object for adding filter parameters the user should not be able to reset
     */
    private void parseTagIdParameter(HttpServletRequest request, Map<String, Object> model,
            Map<String, Object> unresetableParams) {
        String tagIdsParam = request.getParameter("fiPreselectedTagIds");
        if (tagIdsParam != null) {
            List<Long> tagIds = StringHelper.getStringAsLongList(tagIdsParam, ",", true);
            if (tagIds != null && tagIds.size() > 0) {
                unresetableParams.put(nameProvider.getNameForTagIds(), tagIds.toArray());
            }
        }
    }

    /**
     * Parse parameters that filter for topics and set a topic for the editor.
     *
     * @param request
     *            the current request
     * @param model
     *            the model for saving settings required for rendering the view
     * @param unresetableParams
     *            object for adding filter parameters the user should not be able to reset
     */
    private void parseTopicParameters(HttpServletRequest request, Map<String, Object> model,
            Map<String, Object> unresetableParams) {
        Long edTopicId = ParameterHelper.getParameterAsLong(request.getParameterMap(),
                "edPreselectedTopicId");
        if (edTopicId != null && edTopicId <= 0) {
            // treat 0 as null
            edTopicId = null;
        }
        if (edTopicId == null) {
            String alias = request.getParameter("edPreselectedTopic");
            if (StringUtils.isNotBlank(alias)) {
                edTopicId = getBlogManagement().getBlogId(alias);
                if (edTopicId == null) {
                    LOGGER.debug("Topic with alias {} does not exist", alias);
                }
            }
        }
        if (edTopicId != null) {
            model.put("editorTopicId", edTopicId);
        }
        List<Long> fiTopicIds = getTopicIds(request, "fiPreselectedTopicIds", "fiPreselectedTopics");
        // the topicSelected view of communote is used when the fiTopicIds contains only edTopicId,
        // otherwise the notesOverview view is used
        if (edTopicId != null && fiTopicIds.size() == 1 && fiTopicIds.get(0).equals(edTopicId)) {
            // TODO maybe it is better to always use notesOverview, especially if the views (follow,
            // favorite,...) of this context are always required as in HTMLClient. But than
            // childTopics should be enabled by default. Or should we use another context 'embed'?
            // But than the plugin has to provide labels for the horizontal navigation and adding
            // some unresetable filter parameters for the views (e.g. showNotesForMe) has to be
            // re-implemented.
            model.put(MODEL_FIELD_CONTEXT_ID, "topicSelected");
            unresetableParams.put(nameProvider.getNameForTargetBlogId(), edTopicId);
            // TODO should probably be configurable via parameter
            unresetableParams.put(nameProvider.getNameForIncludeChildTopics(), true);
        } else {
            if (fiTopicIds.size() == 1) {
                unresetableParams.put(nameProvider.getNameForTargetBlogId(), fiTopicIds.get(0));
            } else if (fiTopicIds.size() > 1) {
                unresetableParams.put(nameProvider.getNameForBlogIds(), toStringArray(fiTopicIds));
            }
        }
    }

    /**
     * Parse parameters that filter for users.
     *
     * @param request
     *            the current request
     * @param model
     *            the model for saving settings required for rendering the view
     * @param unresetableParams
     *            object for adding filter parameters the user should not be able to reset
     */
    private void parseUserParameters(HttpServletRequest request, Map<String, Object> model,
            Map<String, Object> unresetableParams) {
        // get ids, ignore those with a value <= 0
        List<Long> result = StringHelper.getStringAsLongList(
                request.getParameter("fiPreselectedAuthorIds"), true);
        if (result == null) {
            result = new ArrayList<>();
        }
        String[] userAliases = StringUtils.split(request.getParameter("fiPreselectedAuthors"), ',');
        if (userAliases != null && userAliases.length > 0) {
            UserManagement userManagement = getUserManagement();
            for (String alias : userAliases) {
                User user = userManagement.findUserByAlias(alias);
                if (user == null) {
                    LOGGER.debug("User with alias {} does not exist", alias);
                } else if (!result.contains(user.getId())) {
                    result.add(user.getId());
                }
            }
        }
        if (result.size() > 0) {
            // must be strings because FilterParameterStore is type sensitive and expects strings
            unresetableParams.put(nameProvider.getNameForUserIds(), toStringArray(result));
        }
    }

    /**
     * Parse parameters to define a viewType for the CPL and disable the rendering of the viewType
     * switch tool of the horizontal navigation.
     *
     * @param request
     *            the current request
     * @param model
     *            the model for saving settings
     */
    private void parseViewTypeParameters(HttpServletRequest request, Map<String, Object> model) {
        String viewTypeParam = request.getParameter("msgPreselectedViewType");
        if (viewTypeParam != null) {
            viewTypeParam = viewTypeParam.toLowerCase();
            if (viewTypeParam.equals("stream")) {
                model.put("cplPredefinedViewType", TimelineFilterViewType.CLASSIC.name());
            } else if (viewTypeParam.equals("discussion")) {
                model.put("cplPredefinedViewType", TimelineFilterViewType.COMMENT.name());
            }
        }
        model.put("horizontalNavShowViewTypeSwitch", ParameterHelper.getParameterAsBoolean(
                request.getParameterMap(), "msgShowViewTypeSwitch", true));
    }

    @Override
    public Map<String, ? extends Object> processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ViewControllerException {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, Object> unresetableFilterParameters = new HashMap<String, Object>();
        model.put(MODEL_FIELD_CONTEXT_ID, "notesOverview");
        parseTagIdParameter(request, model, unresetableFilterParameters);
        parseUserParameters(request, model, unresetableFilterParameters);
        parseTopicParameters(request, model, unresetableFilterParameters);
        parseShowEditorParameters(request, model);
        parseShowFilterParameters(request, model);
        parseViewTypeParameters(request, model);
        parseShowViewParameters(request, model);
        // save unresetable parameters as object, so that registered
        // EmbedControllerRequestPostProcessors can modify it. JSON serialization is done later.
        model.put(MODEL_FIELD_UNRESETABLE_FILTER_PARAMETERS, unresetableFilterParameters);
        model.put("page", "embed");
        String embedContext = request.getParameter("embedContext");
        if (embedContext != null) {
            embedContext = embedContext.trim();
            if (embedContext.length() > 0) {
                model.put("embedContext", StringEscapeHelper.escapeNonWordCharacters(embedContext));
            }
        }
        return model;
    }

    /**
     * Add a processor to be invoked before rendering the view
     *
     * @param processor
     *            the processor to add
     */
    public synchronized void registerProcessor(EmbedControllerRequestPostProcessor processor) {
        List<EmbedControllerRequestPostProcessor> newPostProcessors = new ArrayList<>(
                postProcessors);
        newPostProcessors.add(processor);
        postProcessors = newPostProcessors;
    }

    /**
     * Serialize an entry of the model into a JSON string.
     *
     * @param modelAndView
     *            the model and view providing the model
     * @param entryKey
     *            the key of the model entry to serialize
     * @throws IOException
     *             in case serialization failed
     */
    private void serializeModelEntryToJSON(ModelAndView modelAndView, String entryKey)
            throws IOException {
        Object entry = modelAndView.getModel().get(entryKey);
        if (entry != null) {
            try {
                modelAndView.getModel().put(entryKey,
                        JsonHelper.getSharedObjectMapper().writeValueAsString(entry));
            } catch (IOException e) {
                LOGGER.error("Serializing {} to JSON string failed.", entryKey, e);
                throw e;
            }
        }
    }

    @Validate
    public void start() {
        CommunoteAuthenticationSuccessHandler authenticationSuccessHandler = WebServiceLocator
                .findService(CommunoteAuthenticationSuccessHandler.class);

        addedRedirectionTarget = authenticationSuccessHandler.addRedirectionTarget(URL, false);
    }

    @Invalidate
    public void stop() {
        CommunoteAuthenticationSuccessHandler authenticationSuccessHandler = WebServiceLocator
                .findService(CommunoteAuthenticationSuccessHandler.class);
        authenticationSuccessHandler.removeRedirectionTarget(addedRedirectionTarget);
    }

    /**
     * Create a string array from the provided collection.
     *
     * @param items
     *            the items to convert to strings
     * @return the array with strings
     */
    private String[] toStringArray(List<?> items) {
        String[] result = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            result[i] = items.get(i).toString();
        }
        return result;
    }

    /**
     * Remove a previously added processor
     *
     * @param processor
     *            the processor to remove
     */
    public synchronized void unregisterProcessor(EmbedControllerRequestPostProcessor processor) {
        if (postProcessors.contains(processor)) {
            List<EmbedControllerRequestPostProcessor> newPostProcessors = new ArrayList<>(
                    postProcessors);
            newPostProcessors.remove(processor);
            postProcessors = newPostProcessors;
        }
    }
}
