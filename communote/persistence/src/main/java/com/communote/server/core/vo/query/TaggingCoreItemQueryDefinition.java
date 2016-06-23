package com.communote.server.core.vo.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.logical.AtomicTagFormula;
import com.communote.server.core.vo.query.logical.CompoundTagFormula;
import com.communote.server.core.vo.query.logical.LogicalTagFormula;
import com.communote.server.core.vo.query.user.AbstractUserQuery;
import com.communote.server.model.attachment.AttachmentConstants;
import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.blog.UserToBlogRoleMappingConstants;
import com.communote.server.model.i18n.MessageConstants;
import com.communote.server.model.note.ContentConstants;
import com.communote.server.model.note.NoteConstants;
import com.communote.server.model.note.NoteStatus;
import com.communote.server.model.tag.TagConstants;
import com.communote.server.model.user.CommunoteEntityConstants;
import com.communote.server.model.user.LanguageConstants;
import com.communote.server.model.user.UserConstants;
import com.communote.server.model.user.note.UserNoteEntityConstants;

/**
 * The TaggingCoreItemQueryDefinition is the base QueryDefinition for all tagging items such as Tag,
 * UserTaggedResource.
 *
 * <b>Attention:</b> This class must be thread safe.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <I>
 *            The valid instance class
 * @param <R>
 *            Typte of the results of this query.
 */
// TODO Rename to TimelineQuery and have fun with Checkstyle.
public abstract class TaggingCoreItemQueryDefinition<R, I extends TimelineQueryParameters> extends
        AbstractUserQuery<R, I> {

    /** alias of the note */
    private static final String ALIAS_NOTE = "utr";
    /** alias of the user note entity */
    protected static final String ALIAS_USER_NOTE_ENTITY = "une";

    /** alias of the blog */
    private static final String ALIAS_BLOG = TaggingCoreItemUTPExtension.ALIAS_BLOG;
    private static final String RESULT_ALIAS_NOTE = ALIAS_NOTE + ".";
    private static final String RESULT_RESOURCE_PREFIX = "resource.";

    /** hql Alias for the tagged item user */
    public static final String ALIAS_USER = "item_user";

    private static final String ALIAS_ATTACHMENT = "attachment";

    private static final String PARAM_CURRENT_USER_ID = "currentUserId";
    /**
     * the select clause, without 'select'
     */
    private String selectClause;

    /**
     * The parameters of the list item constructor
     *
     * @return the parameters
     */
    protected abstract String[] getConstructorParameters();

    /**
     * Get the field for date filtering.
     *
     * @param parameters
     *            the parameter to consult
     * @return The qualified field that can be used for comparison (e.g. 'utr.creationDate');
     */
    protected String getDateFilterField(I parameters) {
        String field = parameters.getDiscussionFilterMode().equals(DiscussionFilterMode.IS_ROOT) ? NoteConstants.LASTDISCUSSIONNOTECREATIONDATE
                : NoteConstants.CREATIONDATE;
        field = RESULT_ALIAS_NOTE + field;
        return field;
    }

    /**
     * @param instance
     *            The {@link QueryInstance}.
     * @return List of all full text search fields.
     */
    protected String[] getFullTextSearchFields(I instance) {
        return new String[] { RESULT_RESOURCE_PREFIX + ContentConstants.CONTENT };
    }

    /**
     *
     * @return the alias of the note within the query (includes a "." at the end
     */
    public String getNoteAlias() {
        return RESULT_ALIAS_NOTE;
    }

    /**
     * The list item type
     *
     * @return the class of the list item to generate
     */
    public abstract Class<R> getResultListItem();

    /**
     * Render the select clause to be used for initializing
     *
     * @param constructorParameters
     *            the constructor parameters (without any "," or " " seperation)
     * @return the select clause part (without "select" and without "distinct")
     */
    protected String getSelectClause(String... constructorParameters) {
        StringBuilder selectClause = new StringBuilder();
        if (getResultListItem() != null) {
            selectClause.append(" new ");
            selectClause.append(getResultListItem().getName());
            selectClause.append("( ");
        }
        selectClause.append(StringUtils.join(constructorParameters, ", "));
        if (getResultListItem() != null) {
            selectClause.append(") ");
        }
        return selectClause.toString();
    }

    @Override
    public String getUserAlias() {
        return ALIAS_USER;
    }

    /**
     * This method returns a {@link StringBuilder} depended on the input {@link StringBuilder}.
     *
     * @param parentWhereQuery
     *            Null or an existing {@link StringBuilder}.
     * @return A {@link StringBuilder}.
     */
    private StringBuilder getWhereQuery(StringBuilder parentWhereQuery) {
        if (parentWhereQuery != null) {
            return parentWhereQuery;
        }
        return new StringBuilder();
    }

    /**
     * This method handles the resource id of a filter.
     *
     * @param instance
     *            {@link QueryInstance}.
     * @param whereQuery
     *            Teh where clause to add the new statement.
     * @param prefix
     *            The prefix to use.
     * @return The new prefix.
     */
    private String handeSubQueryResourceId(I instance, StringBuilder whereQuery, String prefix) {
        if (instance.getResourceId() != null) {
            whereQuery.append(prefix + "(" + RESULT_RESOURCE_PREFIX + ".id=:");
            whereQuery.append(TimelineQueryParameters.PARAM_RESOURCE_ID + ") ");
            prefix = AND;
        }
        return prefix;
    }

    /**
     * This method handles all sub formulas of a given formula.
     *
     * @param formula
     *            The parent formula.
     * @param tagQualifierPrefix
     *            The tag qualifier prefix.
     * @param instance
     *            The {@link QueryInstance}.
     * @param wroteSomething
     *            Wrote something?
     * @param booleanConnector
     *            The boolean connector.
     * @param query
     *            The query.
     */
    private void handleSubFormulas(CompoundTagFormula formula, String tagQualifierPrefix,
            I instance, boolean wroteSomething, String booleanConnector, StringBuilder query) {
        CompoundTagFormula[] formulas = formula.getSubformulas();
        if (formulas.length != 0) {
            if (wroteSomething) {
                query.append(booleanConnector);
            }
            for (int i = 0; i < formulas.length; i++) {
                String subQuery = renderCompoundFormula(formulas[i], tagQualifierPrefix, instance);
                // skip empty sub-formulas
                if (subQuery.length() == 0) {
                    continue;
                }
                query.append(subQuery);
                if (i != formulas.length - 1) {
                    query.append(booleanConnector);
                }
            }
        }
    }

    /**
     * This method handles the start and end date of a filter.
     *
     * @param parameters
     *            {@link QueryInstance} containing all needed information.
     * @param whereQuery
     *            The where clause to append to.
     * @param prefix
     *            The prefix to use.
     * @return The new prefix to use.
     */
    private String handleSubQueryDateBounds(I parameters, StringBuilder whereQuery, String prefix) {
        String field = getDateFilterField(parameters);
        if (parameters.getLowerTagDate() != null) {
            whereQuery.append(prefix);
            if (parameters.isIncludeStartDate()) {
                whereQuery.append("( " + field + " >= :"
                        + TimelineQueryParameters.PARAM_LOWER_TAG_DATE + ")");
            } else {
                whereQuery.append("( " + field + " > :"
                        + TimelineQueryParameters.PARAM_LOWER_TAG_DATE + ")");
            }
            prefix = AND;
        }
        if (parameters.getUpperTagDate() != null) {
            whereQuery.append(prefix);
            whereQuery.append("( " + field + " <= :" + TimelineQueryParameters.PARAM_UPPER_TAG_DATE
                    + ")");
            prefix = AND;
        }
        return prefix;
    }

    /**
     * Handle the discussion id and note id, that is filter for the discussion (and ignore the note
     * id) or filter for the single note.
     *
     * @param instance
     *            {@link QueryInstance} containing all needed information.
     * @param whereQuery
     *            The where clause to add statement to.
     * @param prefix
     *            The prefix to use.
     * @return The new prefix to use.
     */
    private String handleSubQueryDiscussionNoteId(I instance, StringBuilder whereQuery,
            String prefix) {
        if (instance.getDiscussionId() != null) {
            whereQuery.append(prefix);
            whereQuery.append("(" + RESULT_ALIAS_NOTE + NoteConstants.DISCUSSIONID + " = :"
                    + TimelineQueryParameters.PARAM_DISCUSSION_ID + ") ");
            prefix = AND;
        } else if (instance.getNoteId() != null) {
            whereQuery.append(prefix);
            whereQuery.append("(" + RESULT_ALIAS_NOTE + "id = :"
                    + TimelineQueryParameters.PARAM_NOTE_ID);
            if (instance.getDiscussionFilterMode().equals(DiscussionFilterMode.IS_ROOT)) {
                whereQuery.append(" OR  " + RESULT_ALIAS_NOTE + "id in (SELECT "
                        + NoteConstants.DISCUSSIONID + " FROM " + NoteConstants.CLASS_NAME
                        + " WHERE id = :" + TimelineQueryParameters.PARAM_NOTE_ID + ")");
            }
            whereQuery.append(") ");
            prefix = AND;
        }
        return prefix;
    }

    /**
     * Handles the subquery for excluding note status.
     *
     * @param instance
     *            The instance with parameters.
     * @param whereQuery
     *            The existing where query.
     * @param prefix
     *            Actual prefix.
     * @return Next prefix.
     */
    private String handleSubQueryExcludeNoteStatus(I instance, StringBuilder whereQuery,
            String prefix) {
        NoteStatus[] statuss = instance.getExcludeNoteStatus();
        if (statuss == null || statuss.length == 0) {
            return prefix;
        }
        if (statuss.length == 1) {
            whereQuery.append(prefix + RESULT_ALIAS_NOTE + NoteConstants.STATUS + "<>'"
                    + statuss[0] + "'");
        } else {
            String[] statusStr = new String[statuss.length];
            for (int i = 0; i < statuss.length; i++) {
                statusStr[i] = "'" + statuss[i].getValue() + "'";
            }
            whereQuery.append(prefix);
            whereQuery.append(" " + RESULT_ALIAS_NOTE + NoteConstants.STATUS + " NOT IN (");
            whereQuery.append(StringUtils.join(statusStr, ",") + ")");
        }
        return AND;
    }

    /**
     * Adds the user to the query
     *
     * @param mainQuery
     *            Query to add statements to.
     * @param instance
     *            {@link QueryInstance}.
     * @param prefix
     *            the prefix to use
     * @return the prefix for further usage
     */
    private String handleSubQueryNeedUser(StringBuilder mainQuery, I instance, String prefix) {
        if (includeUsersWithoutTags(instance)) {
            mainQuery.append(" right join ");
        } else {
            mainQuery.append(" left join ");
        }
        mainQuery.append(RESULT_ALIAS_NOTE + NoteConstants.USER);
        mainQuery.append(" " + ALIAS_USER + " ");

        return prefix;
    }

    /**
     * This method creates the where clause for tags and tag prefix. It will use the first tag
     * applicable for a direct filter query. all other tags will be filtered by sub query.
     *
     * @param instance
     *            The {@link QueryInstance}.
     * @param whereQuery
     *            The where clause.
     * @param prefix
     *            Prefix for Where clause.
     * @param tagConnector
     *            Connector for tags.
     * @return The new prefix.
     */
    private String handleSubQueryTags(I instance, StringBuilder whereQuery, String prefix,
            TagConstraintConnectorEnum tagConnector) {
        String innerPrefix = StringUtils.EMPTY;
        StringBuilder subQuery = new StringBuilder();
        innerPrefix = renderTagPrefixConstraint(instance.getTagPrefix(), subQuery, "tag.",
                StringUtils.EMPTY, instance);

        // get a copy of the tag ids since it will be modified
        Set<Long> tagIds = new HashSet<Long>(instance.getTagIds());
        // get a copy of the tag store tag id map (its only a copy of the map not the included
        // sets!)
        Map<String, Set<String>> tagStoreTagIds = new HashMap<String, Set<String>>(
                instance.getTagStoreTagIds());

        // only render it if there is not tag prefix
        if (StringUtils.isBlank(instance.getTagPrefix())) {
            innerPrefix = renderSingleTag(instance, subQuery, tagIds, tagStoreTagIds, innerPrefix);
        }

        // now use the remaining tags to filter for by a sub query

        innerPrefix = renderSubQueryTagsFilterByTagId(tagIds, innerPrefix, subQuery);
        innerPrefix = renderSubQueryTagsFilterByTagStores(instance, tagStoreTagIds, innerPrefix,
                subQuery);

        // the tag store aliases are one part that will lead to a distinct since tags must be joined
        renderSubQueryTagsFilterByTagStoreAliases(instance, instance.getTagStoreAliases(),
                subQuery, innerPrefix);

        // the logical tags are one part that will lead to a distinct since tags must be joined
        if (instance.getLogicalTags() != null) {
            renderLogicalTagConstraints(instance.getLogicalTags(), subQuery, "tag.", innerPrefix,
                    "=", instance);
            innerPrefix = AND;
        }

        if (subQuery.length() > 0) {
            whereQuery.append(prefix);
            whereQuery.append(subQuery);
        }
        return AND;
    }

    /**
     * Include users in the result who have not tagged anything.
     *
     * @param instance
     *            the query instance to consider
     * @return if these users shall be in the result set
     */
    protected boolean includeUsersWithoutTags(I instance) {
        return false;
    }

    private boolean needAttachmentsInQuery(I parameters) {
        return (parameters.getAttachmentContentIds() != null && parameters
                .getAttachmentContentIds().length > 0)
                || parameters.getAttachmentRepositoryConnectorId() != null;
    }

    /**
     * @param queryInstance
     *            the query instance to use
     * @return true if the query should use the distinct keyword for selects
     */
    protected boolean needDistinct(I queryInstance) {
        if (queryInstance.isAllowDuplicateResults()) {
            return false;
        }
        return needAttachmentsInQuery(queryInstance);
    }

    /**
     * need the resource in a query?
     *
     * @param queryInstance
     *            the query instance
     * @return if the resource is needed
     */
    protected boolean needResourceInQuery(I queryInstance) {
        return queryInstance.getFullTextSearchFilters() != null
                && queryInstance.getFullTextSearchFilters().length > 0;
    }

    /**
     * Whether the tag entity is needed in the query.
     *
     * @param queryInstance
     *            the current query instance
     * @return true if the query returns tags or the result is filtered by a tag prefix or a single
     *         tag
     */
    @Override
    protected boolean needTagInQuery(I queryInstance) {
        boolean needed = this.queryReturnsTags() || !queryInstance.getTagIds().isEmpty()
                || !queryInstance.getTagStoreTagIds().isEmpty()
                || !queryInstance.getTagStoreAliases().isEmpty()
                || queryInstance.getLogicalTags() != null
                && queryInstance.getLogicalTags() instanceof AtomicTagFormula
                || StringUtils.isNotBlank(queryInstance.getTagPrefix());
        return needed;
    }

    /**
     * need the user in a query?
     *
     * @param queryInstance
     *            the query instance
     * @return if the user is needed
     */
    protected abstract boolean needUserInQuery(I queryInstance);

    /**
     * Determines whether a query returns tags. The default return value is false, thus query
     * definitions for tag retrieval must overwrite this method.
     *
     * @return false
     */
    protected boolean queryReturnsTags() {
        return false;
    }

    /**
     * Renders a disjunction of an array of atoms.
     *
     * @param atoms
     *            the atoms to render
     * @param tagQualifierPrefix
     *            the prefix of the tag entity
     * @param instance
     *            the query instance
     * @return the rendered disjunction
     */
    private String renderAtomDisjunction(AtomicTagFormula[] atoms, String tagQualifierPrefix,
            I instance) {
        StringBuilder result = new StringBuilder();
        result.append(tagQualifierPrefix);
        result.append(TagConstants.TAGSTORETAGID);
        result.append(" in ( ");
        for (int i = 0; i < atoms.length; i++) {
            result.append(":");
            result.append(instance.createParameterName(atoms[i]));
            if (i != atoms.length - 1) {
                result.append(", ");
            }
        }
        result.append(" ) ");
        return result.toString();
    }

    private String renderAttachmentFilters(I parameters, StringBuilder whereQuery, String prefix) {

        boolean ignoreRepoFilter = false;
        if (parameters.getAttachmentRepositoryConnectorId() != null) {
            whereQuery.append(prefix);
            whereQuery.append(ALIAS_ATTACHMENT + "." + AttachmentConstants.REPOSITORYIDENTIFIER
                    + "= :" + TimelineQueryParameters.PARAM_ATTACHMENT_REPO_CONNECTOR_ID);
            prefix = AND;
        }
        if (parameters.getAttachmentContentIds() != null) {

            final String[] contentIds = parameters.getAttachmentContentIds();
            final String[] repoIds = parameters.getAttachmentRepositoryConnectorIds();
            List<String> predicates = new ArrayList<>();
            for (int i = 0; i < contentIds.length; i++) {
                if (contentIds[i] == null) {
                    continue;
                }
                final String pNameContentId = "aContentId" + i;
                parameters.addParameter(pNameContentId, contentIds[i]);
                String p = ALIAS_ATTACHMENT + "." + AttachmentConstants.CONTENTIDENTIFIER + " = :"
                        + pNameContentId;
                String repoId = !ignoreRepoFilter && repoIds != null && repoIds.length > i ? repoIds[i]
                        : null;
                if (repoId != null) {
                    final String pNameRepoConnectorId = "aRepoConnId" + i;
                    parameters.addParameter(pNameRepoConnectorId, repoId);
                    p += AND + ALIAS_ATTACHMENT + "." + AttachmentConstants.REPOSITORYIDENTIFIER
                            + " = :" + pNameRepoConnectorId;
                }
                predicates.add(" ( " + p + " ) ");
            }
            if (predicates.size() > 0) {

                whereQuery.append(prefix);
                whereQuery.append("(");
                whereQuery.append(StringUtils.join(predicates, " OR "));
                whereQuery.append(")");
                prefix = AND;
            }
        }

        return prefix;
    }

    /**
     * Renders a compound formula.
     *
     * @param formula
     *            the formula to render
     * @param tagQualifierPrefix
     *            the prefix of the tag entity
     * @param instance
     *            The {@link QueryInstance}.
     * @return the rendered formula or an empty string if the formula has no sub-elements
     */
    private String renderCompoundFormula(CompoundTagFormula formula, String tagQualifierPrefix,
            I instance) {
        String utrIdSelection = "select 1 from " + NoteConstants.CLASS_NAME
                + " utr2 left join utr2." + NoteConstants.TAGS + " tag where " + RESULT_ALIAS_NOTE
                + "id = utr2.id AND ";

        StringBuilder query = new StringBuilder();
        String body = renderCompoundFormulaBody(formula, tagQualifierPrefix, utrIdSelection,
                instance);
        if (body.length() != 0) {
            if (formula.isNegated()) {
                query.append(" NOT");
            }
            query.append(" EXISTS ( ");
            query.append(utrIdSelection);
            query.append(body);
            query.append(" ) ");
        }
        return query.toString();
    }

    /**
     * Does actual rendering of the body of a compound formula.
     *
     * @param formula
     *            the formula to render
     * @param tagQualifierPrefix
     *            the prefix of the tag entity
     * @param utrIdSelection
     *            string with select clause
     * @param instance
     *            the query instance
     * @return the rendered body of the formula or an empty string if the formula has no
     *         sub-elements
     */
    private String renderCompoundFormulaBody(CompoundTagFormula formula, String tagQualifierPrefix,
            String utrIdSelection, I instance) {

        boolean wroteSomething = false;
        String booleanConnector;
        if (formula.isDisjunction()) {
            booleanConnector = OR;
        } else {
            booleanConnector = AND;
        }
        StringBuilder query = new StringBuilder();
        // add positive atoms
        AtomicTagFormula[] atoms = formula.getPositiveAtoms();
        if (atoms.length != 0) {
            query.append(renderAtomDisjunction(atoms, tagQualifierPrefix, instance));
            if (!formula.isDisjunction()) {
                query.append("group by utr2.id having count(utr2.id)=");
                query.append(atoms.length);
            }
            wroteSomething = true;
        }
        // add negated atoms
        atoms = formula.getNegatedAtoms();
        if (atoms.length != 0) {
            if (wroteSomething) {
                query.append(booleanConnector);
            }
            // De Morgan transformation
            query.append("NOT EXISTS ( ");
            query.append(utrIdSelection);
            query.append(renderAtomDisjunction(atoms, tagQualifierPrefix, instance));
            if (formula.isDisjunction()) {
                query.append("group by utr2.id having count(utr2.id)=");
                query.append(atoms.length);
            }
            query.append(" ) ");
            wroteSomething = true;
        }
        handleSubFormulas(formula, tagQualifierPrefix, instance, wroteSomething, booleanConnector,
                query);
        return query.toString();
    }

    /**
     * Renders the filter for notes that are direct messages.<br />
     * The filter works as follows <li>{@code directMessages = true} >> shows only direct messages
     * for the current user</li> <li>{@code directMessages = false} >> all notes including the
     * direct messages of the current user</li>
     *
     * @param queryParameters
     *            {@link QueryInstance}.
     * @param whereQuery
     *            the query to
     * @param prefix
     *            the prefix
     * @return The next prefix.
     */
    private String renderDirectMessage(I queryParameters, StringBuilder whereQuery, String prefix) {
        if (SecurityHelper.isInternalSystem()) {
            return "";
        }
        // never return DMs if current user is public user
        if (SecurityHelper.isPublicUser()) {
            whereQuery.append(prefix);
            whereQuery.append(RESULT_ALIAS_NOTE + NoteConstants.DIRECT + " = false ");
        } else {
            whereQuery.append(prefix + " ( ");
            if (!queryParameters.isDirectMessage()) {
                whereQuery.append(RESULT_ALIAS_NOTE + NoteConstants.DIRECT + " = false or ");
            }
            whereQuery.append(" ( " + RESULT_ALIAS_NOTE + NoteConstants.DIRECT
                    + " = true and exists (select du from " + NoteConstants.CLASS_NAME
                    + " n left join n." + NoteConstants.DIRECTUSERS
                    + " du where n.id = utr.id and du.id = :" + PARAM_CURRENT_USER_ID);

            // this is nothing else than a join if blog to notes of the inner query. but why here:
            // see KENMEI-5083 "MSSQL - bad Performance with TagCloud-request"
            if (CommunoteRuntime.getInstance().getConfigurationManager().getDatabaseConfiguration()
                    .isExtendSubselectsWithOuterConditions()) {
                whereQuery.append(" AND " + ALIAS_NOTE + "." + NoteConstants.BLOG + " = "
                        + ALIAS_BLOG + "." + BlogConstants.ID);
            }
            whereQuery.append(" ))) ");
        }
        return OR;
    }

    /**
     * Renders the subquery for {@link com.communote.server.core.vo.query.DiscussionFilterMode}
     *
     * @param instance
     *            The query instance.
     * @param whereQuery
     *            The where to append.
     * @param prefix
     *            The current prefix.
     * @return The next prefix.
     */
    private String renderDiscussionFilterMode(I instance, StringBuilder whereQuery, String prefix) {
        String comparator = null;
        switch (instance.getDiscussionFilterModeForFiltering()) {
        case ALL:
        default:
            break;
        case IS_DISCUSSION:
            whereQuery.append(prefix);
            whereQuery.append("(" + RESULT_ALIAS_NOTE + NoteConstants.PARENT
                    + " is not null or  exists (from " + NoteConstants.CLASS_NAME
                    + " discussion where discussion." + NoteConstants.PARENT + " = " + ALIAS_NOTE
                    + " and " + RESULT_ALIAS_NOTE + NoteConstants.DIRECT + "=false))");
            prefix = AND;
            break;
        case IS_ROOT:
            whereQuery.append(prefix);
            whereQuery.append("(" + RESULT_ALIAS_NOTE + NoteConstants.PARENT + " is null)");
            prefix = AND;
            break;
        case IS_DISCUSSION_ROOT:
            comparator = " 1<";
            break;
        case IS_NO_DISCUSSION:
            comparator = " 1=";
            break;
        }
        if (comparator != null) {
            whereQuery.append(prefix + comparator + "(SELECT count(*) FROM "
                    + NoteConstants.CLASS_NAME + " WHERE " + NoteConstants.DISCUSSIONID + " = "
                    + getNoteAlias() + NoteConstants.ID + ")");
            prefix = AND;
        }
        return prefix;
    }

    /**
     * Renders the filter for favorite notes.<br />
     * The filter works as follows <li>{@code favorites = null} >> no filter</li> <li>
     * {@code favorites = true} >> only favorites posts</li> <li>{@code favorites = false} >> only
     * non favorites posts</li>
     *
     * @param instance
     *            {@link QueryInstance}.
     * @param mainQuery
     *            the query to append
     * @param whereQuery
     *            the query to
     * @param prefix
     *            the prefix
     * @return The next prefix.
     */
    private String renderFavorites(I instance, StringBuilder mainQuery, StringBuilder whereQuery,
            String prefix) {
        if (!instance.isFavorites()) {
            return prefix;
        }
        mainQuery.append("LEFT JOIN " + getNoteAlias() + NoteConstants.FAVORITEUSERS
                + " favoriteUser");
        whereQuery.append(prefix);
        whereQuery.append(" favoriteUser." + CommunoteEntityConstants.ID + " = :"
                + PARAM_CURRENT_USER_ID + " ");
        return AND;
    }

    /**
     * render follow sub query
     *
     * @param instance
     *            instance
     * @param whereQuery
     *            query
     * @param prefix
     *            prefix
     */
    private void renderFollowedItemsOnlyQuery(I instance, StringBuilder whereQuery, String prefix) {
        if (!instance.isRetrieveOnlyFollowedItems()) {
            return;
        }
        whereQuery.append(prefix);
        whereQuery.append(" (utr.id in (select fnote.id from " + NoteConstants.CLASS_NAME
                + " as fnote " + "inner join fnote." + NoteConstants.FOLLOWABLEITEMS
                + " as fitems " + "inner join fnote." + NoteConstants.BLOG + " as fblog "
                + "where fitems.id in (select fuseritems.id from " + UserConstants.CLASS_NAME
                + " as fuser inner join fuser." + UserConstants.FOLLOWEDITEMS
                + " as fuseritems where fuser.id=:" + PARAM_CURRENT_USER_ID);
        whereQuery.append(" )");
        TaggingCoreItemUTPExtension extension = instance.getTypeSpecificExtension();
        if (extension != null && extension instanceof TaggingCoreItemUTPExtension
                && extension.getTopicAccessLevel() != null) {
            extension.renderAccessQuery(whereQuery, " AND ", "fblog");
        }
        whereQuery.append(")) ");
    }

    /**
     * Render the condition to do a fulltext search on the note content if necessary.
     *
     * @param instance
     *            the query instance
     * @param whereQuery
     *            the where clause to append the condition to
     * @param prefix
     *            the prefix to prepend before adding this condition
     * @return the new prefix to prepend before adding further conditions. This will be the provided
     *         prefix if nothing was appended.
     */
    private String renderFulltextSearch(I instance, StringBuilder whereQuery, String prefix) {
        String[] fullTextSearchParamNames = instance.getFullTextSearchParameterNames();
        if (fullTextSearchParamNames != null && fullTextSearchParamNames.length > 0) {
            whereQuery.append(prefix);
            renderSearch(whereQuery, getFullTextSearchFields(instance), fullTextSearchParamNames,
                    true, true);
            prefix = AND;
        }
        return prefix;
    }

    /**
     * Renders the tag constraints described by a logical formula.
     *
     * @param formula
     *            a formula describing a constraint like (("TagA" and "TagB") or ("TagC" and not
     *            "TagD"))
     * @param query
     *            the query to which the tag constraints should be appended
     * @param tagQualifierPrefix
     *            the prefix of the tag entity
     * @param prefix
     *            the prefix to use
     * @param comparator
     *            the tag comparator, e.g. '=', '<>'
     * @param instance
     *            The {@link QueryInstance}.
     * @return whether something was added to the query
     */
    protected boolean renderLogicalTagConstraints(LogicalTagFormula formula, StringBuilder query,
            String tagQualifierPrefix, String prefix, String comparator, I instance) {
        if (formula != null && formula instanceof AtomicTagFormula) {
            String tagParam = instance.createParameterName((AtomicTagFormula) formula);
            return renderTagConstraints(tagParam, query, tagQualifierPrefix, prefix, comparator);
        }
        boolean result = false;
        if (formula != null && formula instanceof CompoundTagFormula) {
            CompoundTagFormula cf = (CompoundTagFormula) formula;
            String renderedFormula = renderCompoundFormula(cf, tagQualifierPrefix, instance);
            if (renderedFormula.length() > 0) {
                query.append(prefix + " ( " + renderedFormula + " ) ");
                result = true;
            }
        }
        return result;
    }

    /**
     * Render the main query that will include the note as join
     *
     * @param mainQuery
     *            the main query
     * @param parameters
     *            the parameters
     */
    private void renderMainNoteEntity(StringBuilder mainQuery, I parameters) {
        // if the rank filter is active we can join from user note entity to the note.
        if (parameters.isRankFilterActive()) {
            mainQuery.append(UserNoteEntityConstants.CLASS_NAME + " " + ALIAS_USER_NOTE_ENTITY);
            mainQuery.append(" left join " + ALIAS_USER_NOTE_ENTITY + "."
                    + UserNoteEntityConstants.NOTE);
        } else {
            mainQuery.append(NoteConstants.CLASS_NAME);
        }
        mainQuery.append(" " + ALIAS_NOTE + " ");
    }

    /**
     * Render the main query that will include the tags as join or direct inclusion
     *
     * @param mainQuery
     *            the main query
     * @param parameters
     *            the parameters
     */
    private void renderMainTagEntity(StringBuilder mainQuery, I parameters) {
        if (!needTagInQuery(parameters)) {
            return;
        }
        mainQuery.append(queryReturnsTags() ? " inner join " : " left join ");
        mainQuery.append(RESULT_ALIAS_NOTE + NoteConstants.TAGS + " tag ");
        // join tag translation if required
        if (StringUtils.isNotBlank(parameters.getTagPrefix())
                && parameters.isMultilingualTagPrefixSearch()) {
            mainQuery.append(" LEFT JOIN tag." + TagConstants.NAMES + " tagName LEFT JOIN tagName."
                    + MessageConstants.LANGUAGE + " language");
        }
    }

    /**
     * This method renders the combination of notifications (including direct messages) and
     * following.
     *
     * @param queryParameters
     *            Parameters of the query.
     * @param mainQuery
     *            The main query.
     * @param whereQuery
     *            The where clause.
     * @param prefix
     *            The prefix to use.
     * @return The next prefix.
     */
    private String renderNotificationsAndFollowing(I queryParameters, StringBuilder mainQuery,
            StringBuilder whereQuery, String prefix) {
        StringBuilder innerWhere = new StringBuilder();
        String innerPrefix = "";
        if (queryParameters.isRetrieveOnlyFollowedItems() && queryParameters.isDirectMessage()) {
            innerPrefix = renderDirectMessage(queryParameters, innerWhere, innerPrefix);
            if (queryParameters.isQueryForAdditionalDMs()) {
                innerPrefix = AND;
            }
        } else if (renderDirectMessage(queryParameters, whereQuery, prefix).equals(OR)) {
            prefix = AND;
        }
        innerPrefix = renderNotifiedUsers(queryParameters, mainQuery, innerWhere, innerPrefix);
        renderFollowedItemsOnlyQuery(queryParameters, innerWhere, innerPrefix);
        if (innerWhere.length() > 0) {
            whereQuery.append(prefix + "(" + innerWhere.toString() + ")");
        }
        return prefix;
    }

    /**
     * Method to render the filter for notified users.
     *
     * @param parameters
     *            Parameters of the query.
     * @param mainQuery
     *            The main query.
     * @param whereQuery
     *            The where clause
     * @param prefix
     *            The prefix to use.
     * @return The next prefix to use.
     */
    private String renderNotifiedUsers(I parameters, StringBuilder mainQuery,
            StringBuilder whereQuery, String prefix) {
        if (ArrayUtils.isEmpty(parameters.getUserToBeNotified())) {
            return prefix;
        }
        whereQuery.append(prefix + "( exists( SELECT innerNote." + NoteConstants.ID + " FROM "
                + NoteConstants.CLASS_NAME + " innerNote " + "LEFT JOIN innerNote."
                + NoteConstants.USERSTOBENOTIFIED + " innerUser WHERE innerNote."
                + NoteConstants.ID + " = " + ALIAS_NOTE + "." + NoteConstants.ID
                + " AND innerUser." + CommunoteEntityConstants.ID + " in (:"
                + TimelineQueryParameters.PARAM_USER_TO_BE_NOTIFIED + "))");
        if (parameters.getTypeSpecificExtension() != null
                && parameters.getTypeSpecificExtension().isShowDiscussionParticipation()) {
            whereQuery.append(" or " + ALIAS_NOTE + "." + NoteConstants.DISCUSSIONID + " IN"
                    + "(SELECT innerNote." + NoteConstants.DISCUSSIONID + " FROM "
                    + NoteConstants.CLASS_NAME + " innerNote WHERE innerNote." + NoteConstants.USER
                    + ".id= :" + TimelineQueryParameters.PARAM_USER_ID
                    + " AND (EXISTS (SELECT innerNote2." + NoteConstants.DISCUSSIONID + " FROM "
                    + NoteConstants.CLASS_NAME + " innerNote2 WHERE innerNote."
                    + NoteConstants.DISCUSSIONID + "=innerNote2." + NoteConstants.DISCUSSIONID
                    + " GROUP BY innerNote2." + NoteConstants.DISCUSSIONID
                    + " HAVING COUNT(innerNote2." + NoteConstants.DISCUSSIONID + ")>1)))");
        }
        if (parameters.isMentionTopicReaders()) {
            whereQuery.append(" OR " + ALIAS_NOTE + "." + NoteConstants.MENTIONTOPICREADERS
                    + " is true");
        }
        if (parameters.isMentionTopicAuthors()) {
            whereQuery.append(" OR (" + ALIAS_NOTE + "." + NoteConstants.MENTIONTOPICAUTHORS
                    + " is true AND exists (SELECT n." + NoteConstants.ID + " FROM "
                    + NoteConstants.CLASS_NAME + " n WHERE n." + NoteConstants.BLOG + "="
                    + ALIAS_NOTE + "." + NoteConstants.BLOG + " AND n." + NoteConstants.USER
                    + " = " + parameters.getTypeSpecificExtension().getUserId() + "))");
        }
        if (parameters.isMentionTopicManagers()) {
            whereQuery.append(" OR (" + ALIAS_NOTE + "." + NoteConstants.MENTIONTOPICMANAGERS
                    + " is true AND " + ALIAS_NOTE + "." + NoteConstants.BLOG + "."
                    + BlogConstants.ID + " in (SELECT DISTINCT "
                    + UserToBlogRoleMappingConstants.BLOGID + " FROM "
                    + UserToBlogRoleMappingConstants.CLASS_NAME + " WHERE "
                    + UserToBlogRoleMappingConstants.USERID + " = "
                    + parameters.getTypeSpecificExtension().getUserId() + " AND "
                    + UserToBlogRoleMappingConstants.NUMERICROLE + " >= 3))");
        }
        if (parameters.isMentionDiscussionAuthors()) {
            whereQuery.append(" OR (" + ALIAS_NOTE + "." + NoteConstants.MENTIONDISCUSSIONAUTHORS
                    + " is true AND exists (SELECT n." + NoteConstants.ID + " FROM "
                    + NoteConstants.CLASS_NAME + " n WHERE n." + NoteConstants.DISCUSSIONID + " = "
                    + ALIAS_NOTE + "." + NoteConstants.DISCUSSIONID + " AND n."
                    + NoteConstants.USER + " = "
                    + parameters.getTypeSpecificExtension().getUserId() + " ))");
        }
        whereQuery.append(")");
        return OR;
    }

    /**
     * Renders the rank filters on the user note entity. Besides filtering for the rank it will
     * limit the result to entities of the current user.
     *
     * @param parameters
     *            the parameters to use
     * @param whereQuery
     *            the query to add filters to
     * @param prefix
     *            the prefix to use for beginning the query
     * @return the prefix next parts should use
     */
    private String renderRanks(I parameters, StringBuilder whereQuery, String prefix) {
        if (parameters.isRankFilterActive()) {
            whereQuery.append(prefix);
            whereQuery.append(" " + ALIAS_USER_NOTE_ENTITY + "." + UserNoteEntityConstants.USER
                    + ".id = :" + TimelineQueryParameters.PARAM_USER_ID);
            prefix = AND;
        }
        if (parameters.getMinimumRank() != null) {
            whereQuery.append(prefix);
            whereQuery.append(" " + ALIAS_USER_NOTE_ENTITY + "." + UserNoteEntityConstants.RANK
                    + " >= :" + TimelineQueryParameters.PARAM_MIN_RANK + " ");
            prefix = AND;
        }
        if (parameters.getMaximumRank() != null) {
            whereQuery.append(prefix);
            whereQuery.append(" " + ALIAS_USER_NOTE_ENTITY + "." + UserNoteEntityConstants.RANK
                    + " <= :" + TimelineQueryParameters.PARAM_MAX_RANK + " ");
            prefix = AND;
        }
        return prefix;
    }

    /**
     * Render the select clause based on if its the count query or not
     *
     * @param queryInstance
     *            the query instance to use
     * @param mainQuery
     *            the main query
     */
    protected void renderSelectClause(I queryInstance, StringBuilder mainQuery) {
        mainQuery.append("select ");
        if (needDistinct(queryInstance) && !queryInstance.isAllowDuplicateResults()) {
            mainQuery.append(" distinct ");
        }
        mainQuery.append(selectClause);
    }

    /**
     * Renders the filter for a single tag, that is not a subquery
     *
     * @param instance
     *            the instance
     * @param whereQuery
     *            the query to append to
     * @param tagIds
     *            the set of tag ids to use, might be altered
     * @param tagStoreTagIds
     *            the map of tag store aliases to tag store tag ids, might be altered
     * @param innerPrefix
     *            the prefix to use
     * @return the prefix to use
     */
    private String renderSingleTag(I instance, StringBuilder whereQuery, Set<Long> tagIds,
            Map<String, Set<String>> tagStoreTagIds, String innerPrefix) {
        // states if a filter for a tag has been rendered
        boolean renderedSingleTag = false;

        // is a tag ids given ?
        if (tagIds != null && !tagIds.isEmpty()) {
            // tag the first
            Long tagId = tagIds.iterator().next();
            // remove it since it should not be included further
            tagIds.remove(tagId);

            whereQuery.append(innerPrefix);

            // render filter
            whereQuery.append(" tag." + TagConstants.ID + " = " + tagId + " ");

            innerPrefix = AND;
            renderedSingleTag = true;
        }
        // now check the tag store tag ids
        if (!renderedSingleTag && tagStoreTagIds != null && !tagStoreTagIds.isEmpty()) {

            for (Entry<String, Set<String>> tagStore : tagStoreTagIds.entrySet()) {
                if (tagStore.getValue() == null || tagStore.getValue().isEmpty()) {
                    continue;
                }
                // there is a filter
                String tagStoreQueryName = instance.addParameter(tagStore.getKey());
                String tagStoreTagId = tagStore.getValue().iterator().next();
                String tagStoreTagIdQueryName = instance.addParameter(tagStoreTagId);

                // render it
                whereQuery.append(innerPrefix);
                whereQuery
                        .append("tag." + TagConstants.TAGSTOREALIAS + "= " + tagStoreQueryName
                                + " AND tag." + TagConstants.TAGSTORETAGID + " = "
                                + tagStoreTagIdQueryName);

                // make a copy of the set and put it back
                Set<String> copy = new HashSet<String>(tagStore.getValue());
                tagStoreTagIds.put(tagStore.getKey(), copy);

                // and remove the tag store tag id
                copy.remove(tagStoreTagId);

                innerPrefix = AND;
                renderedSingleTag = true;
                // break since only one tag can be filtered for
                break;
            }
        }
        return innerPrefix;
    }

    /**
     * Renders the query for filtering by tag ids using an exists subquery
     *
     * @param tagIds
     *            the ids of tas to filter for
     * @param innerPrefix
     *            The inner prefix.
     * @param subQuery
     *            The sub query.
     * @return The next inner prefix.
     */
    private String renderSubQueryTagsFilterByTagId(Set<Long> tagIds, String innerPrefix,
            StringBuilder subQuery) {
        if (tagIds == null || tagIds.isEmpty()) {
            return innerPrefix;
        }
        subQuery.append(innerPrefix);
        subQuery.append("EXISTS(SELECT 1 FROM " + NoteConstants.CLASS_NAME
                + " note3 LEFT JOIN note3." + NoteConstants.TAGS + " tag3 WHERE note3."
                + NoteConstants.ID + " = utr." + NoteConstants.ID + " AND tag3." + TagConstants.ID
                + " IN (");
        subQuery.append(StringUtils.join(tagIds, ","));
        subQuery.append(") GROUP BY note3. " + NoteConstants.ID + " HAVING count(note3."
                + NoteConstants.ID + ") = ");
        subQuery.append(tagIds.size());
        subQuery.append(")");
        return AND;
    }

    /**
     * Renders the query to filter for tag with a tag store within the tag store aliases. It does
     * not use a sub query.
     *
     * @param instance
     *            the instance to use
     * @param tagStoreAliases
     *            the tag store aliases to filter for
     * @param query
     *            the query to write to
     * @param prefix
     *            the prefix to add before writing own sub query
     */
    private void renderSubQueryTagsFilterByTagStoreAliases(I instance, Set<String> tagStoreAliases,
            StringBuilder query, String prefix) {
        if (tagStoreAliases == null || tagStoreAliases.isEmpty()) {
            return;
        }
        query.append(prefix);
        query.append("tag." + TagConstants.TAGSTOREALIAS + " IN (");
        String seperator = "";
        for (String tagStoreAlias : tagStoreAliases) {
            query.append(seperator + instance.addParameter(tagStoreAlias.toString()));
            seperator = ",";
        }
        query.append(")");
    }

    /**
     * Renders the query for filtering by TagStores.
     *
     * @param instance
     *            The {@link QueryInstance}.
     * @param innerPrefix
     *            The inner prefix.
     * @param tagStoreTagIds
     *            the map of tag store aliases to tag store tag ids
     * @param subQuery
     *            The sub query.
     * @return The next inner prefix.
     */
    private String renderSubQueryTagsFilterByTagStores(I instance,
            Map<String, Set<String>> tagStoreTagIds, String innerPrefix, StringBuilder subQuery) {
        if (instance.getTagStoreTagIds().isEmpty()) {
            return innerPrefix;
        }
        for (Entry<String, Set<String>> tagStore : tagStoreTagIds.entrySet()) {
            if (tagStore.getValue() == null || tagStore.getValue().isEmpty()) {
                continue;
            }
            String tagStoreQueryName = instance.addParameter(tagStore.getKey());
            subQuery.append(innerPrefix);
            subQuery.append("EXISTS(SELECT 1 FROM " + NoteConstants.CLASS_NAME
                    + " note LEFT JOIN note." + NoteConstants.TAGS + " tagTagStore WHERE note."
                    + NoteConstants.ID + " = utr." + NoteConstants.ID + " AND tagTagStore."
                    + TagConstants.TAGSTOREALIAS + "= " + tagStoreQueryName + " AND tagTagStore."
                    + TagConstants.TAGSTORETAGID + " IN (");
            String seperator = "";
            for (String tagStoreTagId : tagStore.getValue()) {
                String tagStoreTagIdQueryName = instance.addParameter(tagStoreTagId);
                subQuery.append(seperator + tagStoreTagIdQueryName);
                seperator = ",";
            }
            subQuery.append(") GROUP BY note." + NoteConstants.ID + " HAVING count(note."
                    + NoteConstants.ID + ") = ");
            subQuery.append(tagStore.getValue().size());
            subQuery.append(")");
            innerPrefix = AND;
        }
        return innerPrefix;
    }

    /**
     * Render the tag constraint for one tag parameter
     *
     * @param tagParamName
     *            The parameter for a single tag
     * @param query
     *            the where clause query
     * @param tagQualifierPrefix
     *            the prefix of the tag entity
     * @param prefix
     *            the prefix to use
     * @param comparator
     *            the tag comparator, e.g. '=', '<>'
     * @return If something has been appended
     */
    protected boolean renderTagConstraints(String tagParamName, StringBuilder query,
            String tagQualifierPrefix, String prefix, String comparator) {
        if (StringUtils.isNotBlank(tagParamName)) {
            query.append(prefix);
            query.append(" ( ");
            query.append(tagQualifierPrefix);
            query.append(TagConstants.TAGSTORETAGID);
            query.append(comparator);
            query.append(":");
            query.append(tagParamName);
            query.append(") ");
            return true;
        }
        return false;
    }

    /**
     * Render the tag prefix constraint: The tags must start with the given prefix
     *
     * @param tagPrefix
     *            the tag prefix
     * @param query
     *            the where clause query
     * @param tagQualifierPrefix
     *            the prefix of the tag entity
     * @param prefix
     *            the prefix to use
     * @param queryInstance
     *            QueryInstance The instance to use.
     * @return The next prefix to use.
     */
    protected String renderTagPrefixConstraint(String tagPrefix, StringBuilder query,
            String tagQualifierPrefix, String prefix, I queryInstance) {
        if (StringUtils.isBlank(tagPrefix)) {
            return prefix;
        }
        if (queryInstance.isMultilingualTagPrefixSearch()) {
            String languageCode = queryInstance.addParameter(queryInstance.getLanguageCode());
            // part for matching default name of tag without an existing translation
            query.append(prefix);
            query.append(" ((lower(");
            query.append(tagQualifierPrefix);
            query.append(TagConstants.DEFAULTNAME + ") like lower(:"
                    + TimelineQueryParameters.PARAM_TAG_PREFIX + ") AND tagName."
                    + MessageConstants.MESSAGE + " is null) ");
            // part for matching default name of tag with existing translations but not in the
            // language of the user
            query.append("OR (lower(");
            query.append(tagQualifierPrefix);
            query.append(TagConstants.DEFAULTNAME + ") like lower(:"
                    + TimelineQueryParameters.PARAM_TAG_PREFIX + ") AND tagName."
                    + MessageConstants.MESSAGE + " is not null AND language."
                    + LanguageConstants.LANGUAGECODE + " <> ");
            query.append(languageCode);
            // part for matching translation (custom message)
            query.append(") OR (lower(tagName." + MessageConstants.MESSAGE + ") like lower(:"
                    + TimelineQueryParameters.PARAM_TAG_PREFIX + ") AND language."
                    + LanguageConstants.LANGUAGECODE + " = ");
            query.append(languageCode);
            query.append("))");
        } else {
            query.append(prefix);
            query.append(" lower(" + tagQualifierPrefix + TagConstants.DEFAULTNAME
                    + ") like lower(:" + TimelineQueryParameters.PARAM_TAG_PREFIX + ")");
        }
        return AND;
    }

    /**
     * Render the condition to do a search on the topic titles if necessary.
     *
     * @param instance
     *            the query instance
     * @param whereQuery
     *            the where clause to append the condition to
     * @param prefix
     *            the prefix to prepend before adding this condition
     * @return the new prefix to prepend before adding further conditions. This will be the provided
     *         prefix if nothing was appended.
     */
    private String renderTopicSearch(I instance, StringBuilder whereQuery, String prefix) {
        String[] topicSearchParamNames = instance.getTopicSearchParameterNames();
        if (topicSearchParamNames != null && topicSearchParamNames.length > 0) {
            whereQuery.append(prefix);
            renderSearch(whereQuery, new String[] { ALIAS_BLOG + "." + BlogConstants.TITLE },
                    topicSearchParamNames, true, false);
            prefix = AND;
        }
        return prefix;
    }

    /**
     * @param instance
     *            The {@link QueryInstance}
     * @param prefix
     *            The prefix for the query.
     * @param query
     *            The query to be appended.
     * @return <code>True</code> when the user aliases were rendered.
     */
    private boolean renderUserAliases(I instance, StringBuilder query, String prefix) {
        if (instance.getUserAliases().isEmpty()) {
            return false;
        }
        query.append(prefix + " " + ALIAS_USER + "." + UserConstants.ALIAS + " IN(");
        String separator = "";
        for (String userAlias : instance.getUserAliases()) {
            query.append(separator + instance.addParameter(userAlias));
            separator = ",";
        }
        query.append(")");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        selectClause = getSelectClause(getConstructorParameters());
    }

    /**
     * Builds a query to find all user tagged resource. Starts with the from clause.
     * <p>
     * If using the from clause directly, the following constants can be used: "utr", "tag"
     *
     * @param mainQuery
     *            the query
     * @param parentWhereQuery
     *            The where query by clause
     * @param parameters
     *            the instance containing the parameters and configuration
     * @param tagConnector
     *            the connector for the tag constraints (either and or or)
     */
    protected void subQueryFindNoteWithTags(StringBuilder mainQuery,
            StringBuilder parentWhereQuery, I parameters, TagConstraintConnectorEnum tagConnector) {
        mainQuery.append(" from ");
        renderMainNoteEntity(mainQuery, parameters);
        renderMainTagEntity(mainQuery, parameters);
        if (needResourceInQuery(parameters)) {
            mainQuery.append(" left join " + RESULT_ALIAS_NOTE + NoteConstants.CONTENT
                    + " resource ");
        }
        if (needAttachmentsInQuery(parameters)) {
            mainQuery.append(" left join " + RESULT_ALIAS_NOTE + NoteConstants.ATTACHMENTS
                    + " attachment ");
        }

        StringBuilder whereQuery = getWhereQuery(parentWhereQuery);
        boolean needUser = needUserInQuery(parameters);
        String prefix = StringUtils.EMPTY;
        if (parameters.getUserIds() != null && parameters.getUserIds().length > 0) {
            needUser = true;
            whereQuery.append(prefix);
            whereQuery.append(ALIAS_USER + ".id IN ("
                    + StringUtils.join(parameters.getUserIds(), ",") + ")");
            prefix = AND;
        }
        if (parameters.getUserIdsToIgnore().length > 0) {
            needUser = true;
            whereQuery.append(prefix);
            whereQuery.append(ALIAS_USER + ".id NOT IN ("
                    + StringUtils.join(parameters.getUserIdsToIgnore(), ",") + ")");
            prefix = AND;
        }

        String[] userSearchParamNames = parameters.getUserSearchParameterNames();
        if (userSearchParamNames != null && userSearchParamNames.length > 0) {
            needUser = true;
            whereQuery.append(prefix);
            renderSearch(whereQuery, getUserSearchFields(parameters), userSearchParamNames, true,
                    false);
            prefix = AND;
        }

        prefix = renderFulltextSearch(parameters, whereQuery, prefix);

        prefix = renderTopicSearch(parameters, whereQuery, prefix);

        if (parameters.getTypeSpecificExtension().renderSubQuery(mainQuery, whereQuery, prefix,
                RESULT_ALIAS_NOTE)) {
            prefix = AND;
        }
        // force empty results when there is no user by setting -1 userId
        Long currentUserId = SecurityHelper.getCurrentUserId();
        parameters.addParameter(PARAM_CURRENT_USER_ID, currentUserId != null ? currentUserId : -1L);
        prefix = renderFavorites(parameters, mainQuery, whereQuery, prefix);
        prefix = renderNotificationsAndFollowing(parameters, mainQuery, whereQuery, prefix);
        prefix = renderDiscussionFilterMode(parameters, whereQuery, prefix);
        needUser = needUser | renderUserAliases(parameters, whereQuery, prefix);
        prefix = handleSubQueryDateBounds(parameters, whereQuery, prefix);
        prefix = handeSubQueryResourceId(parameters, whereQuery, prefix);
        prefix = handleSubQueryDiscussionNoteId(parameters, whereQuery, prefix);
        prefix = handleSubQueryTags(parameters, whereQuery, prefix, tagConnector);
        prefix = handleSubQueryExcludeNoteStatus(parameters, whereQuery, prefix);
        if (needUser) {
            prefix = handleSubQueryNeedUser(mainQuery, parameters, prefix);
        }
        prefix = renderPropertyFilters(parameters, whereQuery, prefix, RESULT_ALIAS_NOTE);
        prefix = renderRanks(parameters, whereQuery, prefix);
        prefix = renderAttachmentFilters(parameters, whereQuery, prefix);
    }
}