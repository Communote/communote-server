package com.communote.server.core.vo.query.post;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.config.database.DateField;
import com.communote.server.core.vo.query.DiscussionFilterMode;
import com.communote.server.core.vo.query.QueryConfigurationException;
import com.communote.server.core.vo.query.QueryParameters.OrderDirection;
import com.communote.server.core.vo.query.TagConstraintConnectorEnum;
import com.communote.server.core.vo.query.TaggingCoreItemQueryDefinition;
import com.communote.server.core.vo.query.logical.AtomicTagFormula;
import com.communote.server.core.vo.query.user.UserQueryParameters;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteConstants;
import com.communote.server.model.user.note.UserNoteEntityConstants;
import com.communote.server.persistence.blog.NoteDao;

/**
 * This query definition represents queries for notes.
 *
 * @param <R>
 *            Type of the result for this query.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractNoteQuery<R> extends
TaggingCoreItemQueryDefinition<R, NoteQueryParameters> {

    private static final String ALIAS_ROOT_NOTE = "rootNote";

    private static final String PREFIX_ALIAS_ROOT_NOTE = ALIAS_ROOT_NOTE + ".";

    private String selectClauseAllNotes;
    private String selectClauseRootNotes;

    /**
     * This method asserts that the retrieveOnly* filters are only set if the order direction for
     * date and id is descending.
     *
     * @param queryParameters
     *            The parameters to asserts.
     */
    private void assertRetrieveOnlyFilters(NoteQueryParameters queryParameters) {
        if (queryParameters.getRetrieveOnlyNotesAfterId() == null
                && queryParameters.getRetrieveOnlyNotesBeforeId() == null) {
            return;
        }
        if (!OrderDirection.DESCENDING.equals(queryParameters.getSortById())
                || !OrderDirection.DESCENDING.equals(queryParameters.getSortByDate())) {
            throw new QueryConfigurationException(
                    "When using a 'retrieveOnly*' filter the ordering of id and date must be descending.");
        }
    }

    /**
     * Build a query depending if its to get the overall count or not
     *
     * @param queryParameters
     *            the QueryInstance
     * @return the query string with named parameters
     */
    @Override
    public String buildQuery(NoteQueryParameters queryParameters) {
        StringBuilder mainQuery = new StringBuilder();
        StringBuilder whereQuery = new StringBuilder();

        renderSelectClause(queryParameters, mainQuery);

        subQueryFindNoteWithTags(mainQuery, whereQuery, queryParameters,
                TagConstraintConnectorEnum.OR);

        if (whereQuery.length() > 0) {
            whereQuery.append(AND);
        }
        whereQuery.append(getNoteAlias() + NoteConstants.STATUS);
        whereQuery.append(" = :" + NoteQueryParameters.PARAM_NOTE_STATUS + " ");

        handleJoinRootNotes(queryParameters, mainQuery, whereQuery, AND);
        assertRetrieveOnlyFilters(queryParameters);
        handleSubQueryBeforeNoteId(queryParameters, whereQuery, AND);
        handleSubQueryAfterNoteId(queryParameters, whereQuery, AND);

        if (queryParameters.getOriginalPostId() != null) {
            whereQuery.append(AND + getNoteAlias() + NoteConstants.ORIGIN + ".id");
            whereQuery.append(" = " + queryParameters.getOriginalPostId());
        }
        if (DiscussionFilterMode.IS_ROOT.equals(queryParameters.getDiscussionFilterMode())) {
            String alias = queryParameters.isDiscussionDependentRootNotesFilter()
                    ? PREFIX_ALIAS_ROOT_NOTE : getNoteAlias();
            whereQuery.append(AND + alias
                    + NoteConstants.LASTDISCUSSIONNOTECREATIONDATE
                    + " is not null");
        }
        mainQuery.append(" where ");
        mainQuery.append(whereQuery);

        renderOrderbyClause(mainQuery, queryParameters);

        return mainQuery.toString();
    }

    /**
     * Create a new empty query instance
     *
     * @return the instance
     */
    @Override
    public NoteQueryParameters createInstance() {
        return new NoteQueryParameters();
    }

    /**
     *
     * @return the constructor parameters to be used if the root notes filtering is active.
     */
    protected abstract String[] getConstructorParametersForRootNotes();

    /**
     * Get the field for date filtering. Overrides the behavior by using the root note filter.
     *
     * @param parameters
     *            the parameter to consult
     * @return The qualified field that can be used for comparison (e.g. 'utr.creationDate');
     */
    @Override
    protected String getDateFilterField(NoteQueryParameters parameters) {
        String field = parameters.isDiscussionDependentRootNotesFilter() ? PREFIX_ALIAS_ROOT_NOTE
                : this.getNoteAlias();
        field += parameters.getDiscussionFilterMode().equals(DiscussionFilterMode.IS_ROOT)
                ? NoteConstants.LASTDISCUSSIONNOTECREATIONDATE
                        : NoteConstants.CREATIONDATE;

        return field;
    }

    /**
     * @return The alias of the root note
     */
    protected String getRootNoteAlias() {
        return ALIAS_ROOT_NOTE;
    }

    /**
     * @return The prefix for the root note, that is it ends with a '.'
     */
    protected String getRootNoteAliasPrefix() {
        return PREFIX_ALIAS_ROOT_NOTE;
    }

    /**
     * Joins the root notes on the discussion id into the query. Is used if the filtering for the
     * root notes mode is active.
     *
     * @param parameters
     *            the parameters
     * @param mainQuery
     *            the main query holding the joins
     * @param whereQuery
     *            the where query holding the filters
     * @param prefix
     *            the prefix for the where query to use
     * @return the prefix for further filtering
     */
    private String handleJoinRootNotes(NoteQueryParameters parameters, StringBuilder mainQuery,
            StringBuilder whereQuery,
            String prefix) {
        if (parameters.isDiscussionDependentRootNotesFilter()) {
            mainQuery.append(" , " + NoteConstants.CLASS_NAME + " " + ALIAS_ROOT_NOTE + " ");
            whereQuery.append(prefix);
            whereQuery.append(getNoteAlias() + NoteConstants.DISCUSSIONID + " = "
                    + PREFIX_ALIAS_ROOT_NOTE + NoteConstants.ID + " ");
            prefix = AND;
        }
        return prefix;
    }

    /**
     * Method to handle
     * {@link com.communote.server.core.vo.query.TimelineQueryParameters#getRetrieveOnlyNotesAfterId()}
     *
     * @param parameters
     *            The query parameters.
     * @param whereQuery
     *            The where query to append.
     * @param prefix
     *            The current prefix.
     */
    private void handleSubQueryAfterNoteId(NoteQueryParameters parameters,
            StringBuilder whereQuery, String prefix) {
        if (parameters.getRetrieveOnlyNotesAfterId() == null) {
            return;
        }
        NoteDao noteDao = ServiceLocator.findService(NoteDao.class);
        String dateToFilter = DiscussionFilterMode.IS_ROOT.equals(parameters
                .getDiscussionFilterMode()) ? NoteConstants.LASTDISCUSSIONNOTECREATIONDATE
                        : NoteConstants.CREATIONDATE;
        Note note = noteDao.load(parameters.getRetrieveOnlyNotesAfterId());
        if (note == null) {
            // note can be deleted, so check for notes having the same date but a higher ID
            // the result can be null (and will be in most of the cases)
            note = noteDao.findNearestNote(parameters.getRetrieveOnlyNotesAfterId(),
                    parameters.getRetrieveOnlyNotesAfterDate(), true);
        }
        if (parameters.getLowerTagDate() != null
                && note != null && note.getCreationDate().before(parameters.getLowerTagDate())) {
            // the other date filter is set and it is more restrictive than the note's date
            return;
        }

        // add a filter to the query that retrieves notes that have a lower id on the same date or
        // just have been created before the date of that note
        parameters.addParameter("retrieveAfterNoteCreationDate",
                note != null ? note.getCreationDate() : parameters
                        .getRetrieveOnlyNotesAfterDate());
        whereQuery.append(prefix + "(" + getNoteAlias() + dateToFilter
                + " > :retrieveAfterNoteCreationDate");

        if (note != null) {
            parameters.addParameter("retrieveOnlyNotesAfterId",
                    parameters.getRetrieveOnlyNotesAfterId());
            whereQuery.append(" OR (" + getNoteAlias()
                    + dateToFilter + " = :retrieveAfterNoteCreationDate  AND "
                    + getNoteAlias() + NoteConstants.ID + " > :retrieveOnlyNotesAfterId)");
        }
        whereQuery.append(")");

        parameters.setSortByDate(OrderDirection.DESCENDING);
        parameters.setSortById(OrderDirection.DESCENDING);
    }

    /**
     * Method to handle
     * {@link com.communote.server.core.vo.query.TimelineQueryParameters#getRetrieveOnlyNotesBeforeId()}
     *
     * @param parameters
     *            The query parameters.
     * @param whereQuery
     *            The where query to append.
     * @param prefix
     *            The current prefix.
     */
    private void handleSubQueryBeforeNoteId(NoteQueryParameters parameters,
            StringBuilder whereQuery, String prefix) {
        if (parameters.getRetrieveOnlyNotesBeforeId() == null
                || parameters.getRetrieveOnlyNotesBeforeDate() == null) {
            return;
        }
        String dateToFilter = DiscussionFilterMode.IS_ROOT.equals(parameters
                .getDiscussionFilterMode()) ? NoteConstants.LASTDISCUSSIONNOTECREATIONDATE
                        : NoteConstants.CREATIONDATE;

        if (parameters.getUpperTagDate() != null
                && parameters.getRetrieveOnlyNotesBeforeDate().after(parameters.getUpperTagDate())) {
            // the other date filter is set and it is more restrictive than the note's date
            return;
        }

        String noteAlias = parameters.isDiscussionDependentRootNotesFilter() ? getRootNoteAliasPrefix()
                : getNoteAlias();
        // add a filter to the query that retrieves notes that have a lower id on the same date or
        // just have been created before the date of that note
        parameters
        .addParameter(
                "retrieveBeforeNoteCreationDate", parameters
                .getRetrieveOnlyNotesBeforeDate());
        whereQuery.append(prefix + "(" + noteAlias + dateToFilter
                + " < :retrieveBeforeNoteCreationDate");

        parameters.addParameter("retrieveOnlyNotesBeforeId",
                parameters.getRetrieveOnlyNotesBeforeId());
        whereQuery.append(" OR (" + noteAlias
                + dateToFilter + " = :retrieveBeforeNoteCreationDate AND "
                + noteAlias + NoteConstants.ID + " < :retrieveOnlyNotesBeforeId )");
        whereQuery.append(")");
        parameters.setSortByDate(OrderDirection.DESCENDING);
        parameters.setSortById(OrderDirection.DESCENDING);
        prefix = AND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean needDistinct(NoteQueryParameters queryParameters) {
        if (queryParameters.isAllowDuplicateResults()) {
            return false;
        }
        boolean needDistinct = super.needDistinct(queryParameters);
        needDistinct = needDistinct || queryParameters.getLogicalTags() != null
                && queryParameters.getLogicalTags() instanceof AtomicTagFormula;
        needDistinct = needDistinct || StringUtils.isNotBlank(queryParameters.getTagPrefix());
        needDistinct = needDistinct || queryParameters.isDiscussionDependentRootNotesFilter()
                && !queryParameters.isSortByDayDateAndRank();
        return needDistinct;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean needUserInQuery(NoteQueryParameters queryParameters) {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Adapted ordering for the date.
     * </p>
     */
    @Override
    protected void renderOrderbyClause(StringBuilder mainQuery, NoteQueryParameters queryParameters) {
        String orderBy = queryParameters.getSortString();

        String alias = queryParameters.isDiscussionDependentRootNotesFilter()
                ? PREFIX_ALIAS_ROOT_NOTE
                        : getNoteAlias();

        String field = DiscussionFilterMode.IS_ROOT.equals(queryParameters
                .getDiscussionFilterMode())
                ? NoteConstants.LASTDISCUSSIONNOTECREATIONDATE
                        : NoteConstants.CREATIONDATE;

        if (!queryParameters.isSortByDayDateAndRank() || !queryParameters.isRankFilterActive()) {
            orderBy = OrderDirection.appendOrderBy(orderBy, alias + field,
                    queryParameters.getSortByDate());
        } else {
            orderBy = OrderDirection.appendOrderBy(
                    orderBy,
                    "date_part(" + DateField.YEAR + ", " + alias + field + ")",
                    queryParameters.getSortByDate());
            orderBy = OrderDirection.appendOrderBy(
                    orderBy,
                    "date_part(" + DateField.DOY + ", " + alias + field + ")",
                    queryParameters.getSortByDate());

            String rankField = ALIAS_USER_NOTE_ENTITY + "." + UserNoteEntityConstants.RANK;

            orderBy = OrderDirection.appendOrderBy(orderBy, rankField,
                    OrderDirection.DESCENDING);
        }

        orderBy = OrderDirection.appendOrderBy(orderBy, alias + NoteConstants.ID,
                queryParameters.getSortById());

        if (StringUtils.isNotBlank(orderBy)) {
            orderBy = orderBy.replace(UserQueryParameters.PLACEHOLDER_USER_ALIAS, getUserAlias());
            mainQuery.append(" order by ");
            mainQuery.append(orderBy);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void renderSelectClause(NoteQueryParameters queryInstance, StringBuilder mainQuery) {
        mainQuery.append("select ");
        if (needDistinct(queryInstance)) {
            mainQuery.append(" distinct ");
        }

        if (queryInstance.isDiscussionDependentRootNotesFilter()) {
            mainQuery.append(selectClauseRootNotes);
        } else {
            mainQuery.append(selectClauseAllNotes);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        selectClauseAllNotes = getSelectClause(getConstructorParameters());
        selectClauseRootNotes = getSelectClause(getConstructorParametersForRootNotes());
        super.setupQueries();

    }

}
