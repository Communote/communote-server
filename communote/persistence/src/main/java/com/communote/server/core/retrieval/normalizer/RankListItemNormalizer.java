package com.communote.server.core.retrieval.normalizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.communote.common.util.PageableList;
import com.communote.server.core.filter.listitems.NormalizedRankListItem;
import com.communote.server.core.filter.listitems.RankListItem;


/**
 * The rank list item normalizer takes {@link RankListItem}'s and computes for each item a value
 * between 1 and MAX_NORMALIZED_RANK (by default its 7). Doing this a {@link NormalizedRankListItem}
 * is created. This item contains the original item and the normalized ranked.<br>
 * The logarithms of the values will be taken before applying the normalization.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <R>
 *            The type of item to be normalized
 */
public abstract class RankListItemNormalizer<R extends RankListItem> {

    /**
     * the tag normalizer
     */
    public final static RankTagListItemNormalizer TAG_NORMALIZER = new RankTagListItemNormalizer();

    /**
     * the user normalizer
     */
    public final static RankUserListItemNormalizer USER_NORMALIZER = new RankUserListItemNormalizer();

    private final static int MAX_NORMALIZED_RANK = 7;

    /**
     * Create an normalized list item
     * 
     * @param listitem
     *            the list item
     * @param normalizedRank
     *            the normalized rank
     * @return The normalized list item
     */
    private NormalizedRankListItem<R> createNormalizedListItem(R listitem, int normalizedRank) {
        return new NormalizedRankListItem<R>(listitem, normalizedRank);
    }

    /**
     * The comparator to use for sorting the resulting normalized items. If null they will not be
     * sorted.
     * 
     * @param locale
     *            The locale to use.
     * 
     * @return The comparator
     */
    protected abstract Comparator<NormalizedRankListItem<R>> getComparator(Locale locale);

    /**
     * Normalize the tags by computing a value which can be used for font size computation
     * 
     * @param rankTagList
     *            the list from the backend
     * @param normalizedItems
     *            the list to add the items to
     * @param <L>
     *            the type of the list must be consistent with the type of the item to normalize
     * @param locale
     *            The locale to use.
     * @return the normalized ranked tag list
     */
    private <L extends List<NormalizedRankListItem<R>>> L normalize(List<R> rankTagList,
            L normalizedItems, Locale locale) {

        // find max count
        int maxRank = 0;
        int minRank = Integer.MAX_VALUE;
        for (RankListItem tag : rankTagList) {

            if (tag.getRank().intValue() > maxRank) {
                maxRank = tag.getRank().intValue();
            }
            if (tag.getRank().intValue() < minRank) {
                minRank = tag.getRank().intValue();
            }
        }

        double adaptedMinRank = preNormalizationFunction(minRank);
        double adaptedMaxRank = preNormalizationFunction(maxRank) - adaptedMinRank;

        // fill output list
        for (R listItem : rankTagList) {

            // the original rank
            int rank = listItem.getRank().intValue();
            double adaptedRank = preNormalizationFunction(rank) - adaptedMinRank;
            // the normalized rank
            int normalizedRank = 1 + (int) (Math.floor((MAX_NORMALIZED_RANK - 1) * adaptedRank
                    / adaptedMaxRank));
            assert normalizedRank <= MAX_NORMALIZED_RANK : "NormalizedRank '" + normalizedRank
                    + "' must be less than " + MAX_NORMALIZED_RANK;
            NormalizedRankListItem<R> normalizedListItem = createNormalizedListItem(listItem,
                    normalizedRank);
            normalizedItems.add(normalizedListItem);
        }

        Comparator<NormalizedRankListItem<R>> comparator = getComparator(locale);
        if (comparator != null) {
            Collections.sort(normalizedItems, comparator);
        }
        return normalizedItems;
    }

    /**
     * Normalize a PageableList to return also a PageableList the same properties set
     * 
     * @param rankTagList
     *            the pageable list
     * 
     * @param locale
     *            The locale to use.
     * @return the pageable list but with normalized items
     */
    public PageableList<NormalizedRankListItem<R>> normalize(PageableList<R> rankTagList,
            Locale locale) {
        PageableList<NormalizedRankListItem<R>> normalizedItems =
                new PageableList<NormalizedRankListItem<R>>(
                        new ArrayList<NormalizedRankListItem<R>>());
        return normalize(rankTagList, normalizedItems, locale);
    }

    /**
     * This function is used to adapt the rank before the normalization. By default it is the log.
     * 
     * @param value
     *            the value to be adapted
     * @return the value to be used for normalization
     */
    protected double preNormalizationFunction(double value) {
        return Math.log(value);
    }
}
