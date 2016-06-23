package com.communote.server.core.retrieval.normalizer;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;

import com.communote.server.core.filter.listitems.NormalizedRankListItem;
import com.communote.server.core.filter.listitems.RankTagListItem;


/**
 * Normalizer for tag list items.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RankTagListItemNormalizer extends RankListItemNormalizer<RankTagListItem> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Comparator<NormalizedRankListItem<RankTagListItem>> getComparator(Locale locale) {
        Collator primaryCollator = Collator.getInstance(locale);
        primaryCollator.setStrength(Collator.SECONDARY);
        Collator secondaryCollator = Collator.getInstance(locale);
        secondaryCollator.setStrength(Collator.TERTIARY);
        ComparatorChain chain = new ComparatorChain();
        chain.addComparator(new BeanComparator("item.name", primaryCollator));
        chain.addComparator(new BeanComparator("item.name", secondaryCollator));
        return chain;
    }

}
