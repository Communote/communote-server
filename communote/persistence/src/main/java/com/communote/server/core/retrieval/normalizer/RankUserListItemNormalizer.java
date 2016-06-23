package com.communote.server.core.retrieval.normalizer;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;

import com.communote.server.core.filter.listitems.NormalizedRankListItem;
import com.communote.server.core.filter.listitems.RankUserListItem;


/**
 * Normalizer for user list items.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RankUserListItemNormalizer extends RankListItemNormalizer<RankUserListItem> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Comparator<NormalizedRankListItem<RankUserListItem>> getComparator(Locale locale) {
        ComparatorChain chain = new ComparatorChain();
        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.SECONDARY);
        chain.addComparator(new BeanComparator("item.lastName", collator));
        chain.addComparator(new BeanComparator("item.firstName", collator));
        chain.addComparator(new BeanComparator("item.email", collator));
        chain.addComparator(new BeanComparator("item.alias", collator));
        return chain;
    }

}
