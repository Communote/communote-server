package com.communote.server.web.api.service.post;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.communote.common.matcher.Matcher;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.filter.listitems.CountListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.QueryParameters.OrderDirection;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.NoteQueryParametersConfigurator;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.java.note.MatcherFactory;
import com.communote.server.core.vo.query.post.CountNoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.service.NoteService;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.to.ApiResult;

/**
 * This controller returns the number of posts written within the given filter.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO Refactor to "normal" controller, when BaseRestApiController is removed.
public class PostCounterApiController extends BaseRestApiController<Map<String, Long>, Object> {

    private static final String RESULT_MEMBER_COUNT = "count";

    private static final String PARAM_NOTE_IDS = "noteIds";

    private final TimelineQueryParametersConfigurator<NoteQueryParameters> queryInstanceConfigurator =
            new NoteQueryParametersConfigurator(FilterWidgetParameterNameProvider.INSTANCE);

    private final CountNoteQuery countNoteQuery = new CountNoteQuery();

    /**
     * Returns the number of posts.
     *
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Long> doGet(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws ApiException {

        NoteQueryParameters noteQueryParameters = countNoteQuery.createInstance();
        queryInstanceConfigurator.configure(request.getParameterMap(), noteQueryParameters);
        TaggingCoreItemUTPExtension extension = noteQueryParameters
                .getTypeSpecificExtension();
        extension.setTopicAccessLevel(TopicAccessLevel.READ);
        noteQueryParameters.setSortByDate(OrderDirection.DESCENDING);

        noteQueryParameters.setIncludeStartDate(false);

        // do not limit because we filter by date of newest note
        noteQueryParameters.setLimitResultSet(false);
        CountListItem item;
        Long[] noteIds = ParameterHelper.getParameterAsLongArray(request.getParameterMap(),
                PARAM_NOTE_IDS, new Long[] { });
        if (noteIds.length > 0) {
            item = queryMatchesSomeNote(request, noteIds, noteQueryParameters);
        } else {
            List<CountListItem> result = ServiceLocator.findService(QueryManagement.class)
                    .executeQueryComplete(countNoteQuery, noteQueryParameters);
            if (result.size() == 0) {
                HashMap<String, Long> countWrapper = new HashMap<String, Long>(1);
                countWrapper.put(RESULT_MEMBER_COUNT, 0L);
                return countWrapper;
            }
            item = result.get(0);
        }

        // doing a count so the paging is useless but we need the offset
        int offset = noteQueryParameters.getResultSpecification().getOffset();
        if (offset != 0) {
            item = new CountListItem(item.getCount() - offset);
        }
        // to maintain compatibility with old org.json framework based implementation do not return
        // members of superclasses
        HashMap<String, Long> countWrapper = new HashMap<String, Long>(1);
        countWrapper.put(RESULT_MEMBER_COUNT, item.getCount());
        return countWrapper;
    }

    /**
     * <p>
     * Test whether the query parameters match at least one of the notes provided in the noteIds
     * array.
     * </p>
     * Note: doesn't return the exact match count for performance reasons. The FE doesn't need this
     * info anyway.
     *
     * @param request
     *            the request
     * @param noteIds
     *            IDs of the notes to test for a match
     * @param noteQueryParameters
     *            the query parameters extracted from the request
     * @return a CountListItem with value of 0 if none matches otherwise the value will be 1
     */
    private CountListItem queryMatchesSomeNote(HttpServletRequest request, Long[] noteIds,
            NoteQueryParameters noteQueryParameters) {
        noteQueryParameters.getResultSpecification().setOffset(0);

        NoteService noteService = ServiceLocator.instance().getService(NoteService.class);
        NoteRenderContext context = new NoteRenderContext(NoteRenderMode.PORTAL, SessionHandler
                .instance().getCurrentLocale(request));
        Matcher<NoteData> matcher = MatcherFactory.createMatcher(noteQueryParameters);
        for (int i = 0; i < noteIds.length; i++) {
            try {
                NoteData noteItem = noteService.getNote(noteIds[i], context);
                if (matcher.matches(noteItem)) {
                    return new CountListItem(1L);
                }
            } catch (NoteNotFoundException e) {
                // ignore note
            } catch (AuthorizationException e) {
                // ignore note
            }
        }
        return new CountListItem(0L);
    }
}
