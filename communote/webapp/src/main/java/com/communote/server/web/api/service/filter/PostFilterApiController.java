package com.communote.server.web.api.service.filter;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.communote.common.util.PageableList;
import com.communote.common.util.Pair;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.core.application.ApplicationInformation;
import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.QueryParameters.OrderDirection;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.web.api.service.post.convert.ApiNoteConverter;
import com.communote.server.web.api.service.post.convert.v1_0_0.ApiDetailNoteConverter;
import com.communote.server.web.api.to.listitem.CommonPostListItem;

/**
 * Controller to filter for posts
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class PostFilterApiController extends
BaseFilterApiController<SimpleNoteListItem, NoteQueryParameters, NoteQuery> {

    private static final String PARAM_MAX_TEXT_LENGTH = "maxTextLength";
    private static final String PARAM_FILTER_HTML = "filterHtml";
    private static NoteQuery USER_TAGGED_POST_QUERY_DEFINITION = QueryDefinitionRepository
            .instance().getQueryDefinition(NoteQuery.class);

    /**
     *
     */
    public PostFilterApiController() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected QueryResultConverter<SimpleNoteListItem, IdentifiableEntityData> createQueryConverter(
            HttpServletRequest request) {
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        boolean filterHtml = ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                PARAM_FILTER_HTML, Boolean.FALSE);
        // note: the rendering pre processor extension takes care of plain-text conversion but
        // works slightly differently because it uses another plain-text
        // conversion method (htmlToPlaintextExt) than postProcessList was using. To get a
        // similar result we do not set the beautify option.
        NoteRenderContext renderContext = new NoteRenderContext(filterHtml ? NoteRenderMode.PLAIN
                : NoteRenderMode.HTML, locale);
        if (getRequestUriName(request).endsWith("postsShort")) {
            return new ApiNoteConverter(renderContext);
        }
        if (compareVersions(request, ApplicationInformation.V_1_0_1) >= 0) {
            return new com.communote.server.web.api.service.post.convert.v1_0_1.ApiDetailNoteConverter(
                    renderContext);
        }
        return new ApiDetailNoteConverter(renderContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Pair<NoteQuery, NoteQueryParameters> createQueryInstance(HttpServletRequest request) {
        // no special query instances required, because converter takes care of result item
        // conversion
        return new Pair<NoteQuery, NoteQueryParameters>(USER_TAGGED_POST_QUERY_DEFINITION,
                USER_TAGGED_POST_QUERY_DEFINITION.createInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postConfigureQueryInstance(NoteQueryParameters queryInstance) {
        // sort by date descending (youngest first)
        queryInstance.setSortByDate(OrderDirection.DESCENDING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List postProcessList(HttpServletRequest request, PageableList<? extends IdentifiableEntityData> list) {
        int maxTextLength = ParameterHelper.getParameterAsInteger(request.getParameterMap(),
                PARAM_MAX_TEXT_LENGTH, 0);
        boolean filterHtml = ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                PARAM_FILTER_HTML, Boolean.FALSE);

        if (filterHtml) {
            // note: plain-text conversion is done by note converter, more specifically: the
            // rendering pre-processor extension point
            for (IdentifiableEntityData listItem : list) {
                CommonPostListItem postItem = (CommonPostListItem) listItem;
                if (maxTextLength > 0 && postItem.getText().length() > maxTextLength) {
                    postItem.setText(postItem.getText().substring(0, maxTextLength));
                }
            }
        }
        return list;
    }
}
