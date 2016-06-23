package com.communote.server.core.tag;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;

import com.communote.server.api.core.tag.TagData;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagHelper {

    /**
     * sort the result set for the user locale
     * 
     * @param locale
     *            The locale to use.
     * @param results
     *            the result
     * @param <L>
     *            the list item
     */
    @SuppressWarnings("unchecked")
    public static <L extends TagData> void sortResults(Locale locale,
            List<L> results) {
        if (results.size() == 0) {
            return;
        }
        Collator primaryCollator = Collator.getInstance(locale);
        primaryCollator.setStrength(Collator.SECONDARY);
        Collator secondaryCollator = Collator.getInstance(locale);
        secondaryCollator.setStrength(Collator.TERTIARY);
        ComparatorChain chain = new ComparatorChain();
        chain.addComparator(new BeanComparator("name", primaryCollator));
        chain.addComparator(new BeanComparator("name", secondaryCollator));
        Collections.sort(results, chain);
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private TagHelper() {
        // Do nothing
    }
}
