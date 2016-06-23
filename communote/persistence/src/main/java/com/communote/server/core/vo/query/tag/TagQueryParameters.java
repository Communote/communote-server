package com.communote.server.core.vo.query.tag;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.NotImplementedException;

import com.communote.server.core.vo.query.TimelineQueryParameters;
import com.communote.server.core.vo.query.logical.AtomicTagFormula;
import com.communote.server.core.vo.query.logical.CompoundTagFormula;
import com.communote.server.core.vo.query.logical.LogicalTagFormula;
import com.communote.server.model.tag.TagConstants;

/**
 * Query instance to find tags. By default it will set
 * {@link #setLimitResultSetAvodingDuplicates(boolean)} to false.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagQueryParameters extends TimelineQueryParameters {

    private boolean hideSelectedTags = true;
    private Collection<Long> tagIdsToExclude = new HashSet<>();
    private final AbstractTagQuery<?> query;

    /**
     * Construct me with the right query definition
     * 
     * @param query
     *            the valid definition
     */
    public TagQueryParameters(AbstractTagQuery<?> query) {
        this.query = query;
        setLimitResultSetAvodingDuplicates(false);
    }

    /**
     * Create parameter names for excluding the tags of the query. The returned parameters will
     * already be prefixed with a colon.
     * 
     * @param formula
     *            the formula to process
     * @return the comma separated list of parameter names
     */
    protected String createParameterNames(LogicalTagFormula formula) {
        if (formula.isNegated()) {
            throw new NotImplementedException("Negated tag formulas cannot be used"
                    + " in a RelatedRankedTagQuery");
        }
        StringBuilder sb = new StringBuilder();
        if (formula instanceof AtomicTagFormula) {
            sb.append(":");
            sb.append(createParameterName((AtomicTagFormula) formula));
        } else {
            String prefix = ":";
            CompoundTagFormula compoundFormula = (CompoundTagFormula) formula;
            for (AtomicTagFormula atom : compoundFormula.getPositiveAtoms()) {
                sb.append(prefix);
                sb.append(createParameterName(atom));
                prefix = ", :";
            }
        }

        return sb.toString();

    }

    /**
     * @return Set of tags to exclude from the search.
     */
    public Collection<Long> getTagIdsToExclude() {
        return tagIdsToExclude;
    }

    /**
     * @return the hideSelectedTags
     */
    @Override
    public boolean isHideSelectedTags() {
        return hideSelectedTags;
    }

    /**
     * @param hideSelectedTags
     *            the hideSelectedTags to set
     */
    @Override
    public void setHideSelectedTags(boolean hideSelectedTags) {
        this.hideSelectedTags = hideSelectedTags;
    }

    /**
     * @param tagIdsToExclude
     *            Set of tags to exclude from the search.
     */
    public void setTagIdsToExclude(Collection<Long> tagIdsToExclude) {
        if (tagIdsToExclude == null) {
            return;
        }
        this.tagIdsToExclude = tagIdsToExclude;
    }

    /**
     * Sort by the tag count descending
     */
    public void sortByTagNameAsc() {
        addSortField(query.getResultObjectPrefix(), TagConstants.DEFAULTNAME, SORT_ASCENDING);
    }
}
