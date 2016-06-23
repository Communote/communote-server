package com.communote.server.api.core.config.database;

import org.hibernate.criterion.MatchMode;
import org.hibernate.dialect.function.SQLFunction;

/**
 * Provides database specific implementations of certain additional features like the HQL 'fulltext'
 * function.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface DatabaseConfiguration {

    /**
     * Determine the class name of the connection provider to use.
     *
     * @return the connection provider
     */
    public String getConnectionProviderClassName();

    /**
     * Get the SQLFunction that will translate the HQL 'datepart' function to SQL.
     * <p>
     * Usage: <code>day_of_date(dateField, alias)</code> where dateField must be one of the
     * {@link DateField} constants
     *
     * Example in HQL to return the year of the publication date of a note:
     * <code>"date_part(" + DateField.YEAR + ", note.publication_date)"</code>
     * </p>
     *
     * @return the SQLFunction that will render the HQL 'datepart' function or null if not supported
     */
    public SQLFunction getDatepartSQLFunction();

    /**
     * Get the (database specific) parameter value for the given pattern
     *
     * @param pattern
     *            the pattern
     * @param matchMode
     *            the hibernate match mode
     * @return the parameter value to be used in the final query
     */
    public String getFulltextParameterValue(String pattern, MatchMode matchMode);

    /**
     * Get the (database specific) parameter value for the given pattern
     *
     * @param pattern
     *            the pattern
     * @param matchMode
     *            the hibernate match mode
     * @param doNotUseFulltext
     *            true if the parameter should use the like wildcards matching even if fulltext is
     *            enabled
     * @return the parameter value to be used in the final query
     */
    public String getFulltextParameterValue(String pattern, MatchMode matchMode,
            boolean doNotUseFulltext);

    /**
     * Return the SQL function that translates the HQL 'fulltext' function to SQL.
     * <p>
     * The HQL 'fulltext' function takes two arguments: The field to be search and the string to
     * search for. In HQL the fulltext function has always to be matched against 1, e.g.
     * <code>fulltext(content.content, 'mysearch') = 1</code>. BUT if you want the negation use:
     * <code>not (fulltext(content.content,'mysearch'))</code>
     * </p>
     * <p>
     * When building queries with the HQL fulltext function always use the
     * {@link #getFulltextParameterValue(String, MatchMode)} to set the parameter values because the
     * syntax differs per database
     * </p>
     *
     * @return The SQLFunction that will render the HQL 'fulltext' function
     */
    public SQLFunction getFulltextSQLFunction();

    /**
     * Determine if subselects should be extended by using conditions of the outer clause.
     *
     * <p>
     * See the MSSQL Implementation for an example.
     * </p>
     *
     * @return true if the subselect should be extended
     */
    public boolean isExtendSubselectsWithOuterConditions();

    /**
     * @return true if the fulltext feature is supported
     */
    public boolean isUseFulltextFeature();

}
