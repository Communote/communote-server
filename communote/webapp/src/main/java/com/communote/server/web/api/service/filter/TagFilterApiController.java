package com.communote.server.web.api.service.filter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.communote.common.util.PageableList;
import com.communote.common.util.Pair;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.TimelineQueryParameters;
import com.communote.server.core.vo.query.tag.RankTagQueryParameters;
import com.communote.server.core.vo.query.tag.RelatedRankTagQuery;

/**
 * Api Controller to filter for tags
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class TagFilterApiController extends BaseFilterApiController {

    private static RelatedRankTagQuery RELATED_RANK_TAG_QUERYDEFINITION = QueryDefinitionRepository
            .instance().getQueryDefinition(RelatedRankTagQuery.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected Pair<RelatedRankTagQuery, TimelineQueryParameters> createQueryInstance(
            HttpServletRequest request) {
        return new Pair<RelatedRankTagQuery, TimelineQueryParameters>(
                RELATED_RANK_TAG_QUERYDEFINITION, new RankTagQueryParameters(
                        RELATED_RANK_TAG_QUERYDEFINITION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postConfigureQueryInstance(TimelineQueryParameters queryInstance) {
        RankTagQueryParameters tagQueryInstance = (RankTagQueryParameters) queryInstance;
        tagQueryInstance.sortByTagCountDesc();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List postProcessList(HttpServletRequest request, PageableList list) {
        return new ArrayList(list);
    }

}
