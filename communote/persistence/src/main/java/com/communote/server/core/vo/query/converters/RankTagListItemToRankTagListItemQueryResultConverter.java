package com.communote.server.core.vo.query.converters;

import java.util.Locale;

import com.communote.common.util.PageableList;
import com.communote.server.core.filter.listitems.NormalizedRankListItem;
import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.retrieval.normalizer.RankListItemNormalizer;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.model.tag.Tag;


/**
 * This converter adds localized information to the RankTagListItem.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RankTagListItemToRankTagListItemQueryResultConverter extends
        DirectQueryResultConverter<RankTagListItem, NormalizedRankListItem<RankTagListItem>> {

    private final TagToTagDataQueryResultConverter converter;
    private final TagManagement tagManagement;
    private final Locale locale;

    /**
     * @param locale
     *            The locale the converter should respect.
     * @param tagManagement
     *            The TagManagement to use.
     */
    public RankTagListItemToRankTagListItemQueryResultConverter(Locale locale,
            TagManagement tagManagement) {
        this.tagManagement = tagManagement;
        this.locale = locale;
        converter = new TagToTagDataQueryResultConverter(locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableList<NormalizedRankListItem<RankTagListItem>> convert(
            PageableList<RankTagListItem> queryResult) {
        // not perfect that we first have to build normalized rank list items just to fullfill the
        // generic constraints
        PageableList<NormalizedRankListItem<RankTagListItem>> resultList = super
                .convert(queryResult);

        PageableList<RankTagListItem> rankList = resultList
                .createEmptyListWithMetaData(RankTagListItem.class);
        rankList.setMinNumberOfElements(resultList.getMinNumberOfElements());
        rankList.setOffset(resultList.getOffset());
        for (NormalizedRankListItem<RankTagListItem> normalizedItem : resultList) {
            rankList.add(normalizedItem.getItem());
        }

        resultList = RankListItemNormalizer.TAG_NORMALIZER
                .normalize(rankList, locale);
        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean convert(RankTagListItem source, NormalizedRankListItem<RankTagListItem> target) {
        target.getItem().copy(source);
        Tag tag = tagManagement.findTag(source.getId());
        converter.convert(tag, target.getItem());
        return true;
    }

    /**
     * @return {@link RankTagListItem}
     */
    @Override
    public NormalizedRankListItem<RankTagListItem> create() {
        return new NormalizedRankListItem<RankTagListItem>(new RankTagListItem(), 0);
    }
}