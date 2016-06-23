package com.communote.server.core.vo.query.post;

import com.communote.common.util.PageableList;
import com.communote.server.core.filter.listitems.CountListItem;

/**
 * Query to count the number of notes. Combined with an end date this query can be used to check for
 * new notes.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CountNoteQuery extends AbstractNoteQuery<CountListItem> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConstructorParameters() {
        return new String[] { "count(distinct " + getNoteAlias() + "id)" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConstructorParametersForRootNotes() {
        return getConstructorParameters();
    }

    /**
     * @return {@link CountListItem#class}
     */
    @Override
    public Class<CountListItem> getResultListItem() {
        return CountListItem.class;
    }

    @Override
    protected boolean needDistinct(NoteQueryParameters queryInstance) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return The unchanged input result.
     */
    @Override
    public PageableList<?> postQueryExecution(NoteQueryParameters queryInstance, PageableList result) {
        return result;
    }

    /**
     * This method does nothing, as we don't need any ordering.
     *
     * {@inheritDoc}
     */
    @Override
    protected void renderOrderbyClause(StringBuilder mainQuery, NoteQueryParameters queryInstance) {
        // Do nothing.
    }
}
