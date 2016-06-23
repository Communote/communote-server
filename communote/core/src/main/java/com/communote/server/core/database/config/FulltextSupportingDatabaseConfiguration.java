package com.communote.server.core.database.config;

import org.hibernate.criterion.MatchMode;
import org.hibernate.dialect.function.SQLFunction;

/**
 * Abstract DatabaseConfiguration for database having some fulltext specific stuff. Depending on if
 * {@link #setUseFulltextFeature(boolean)} has been set to true it will use the database
 * {@link #getSpecificFulltextSQLFunction()} for rendering the fulltext query. Otherwise the simple
 * 'like' will be used.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public abstract class FulltextSupportingDatabaseConfiguration extends StandardDatabaseConfiguration {

    /**
     * Take the pattern and match mode an return the parameter value to be used in the fulltext
     * query.
     *
     * @param pattern
     *            the search string
     * @param matchMode
     *            the match mode to use
     *
     * @param wildcard
     *            the wildcard
     * @param prefix
     *            some additional string to prepend
     * @param suffix
     *            some additional string to append
     * @return the parameter value
     */
    public static String getWildcardPattern(String pattern, MatchMode matchMode,
            String wildcard, String prefix, String suffix) {
        return getWildcardPattern(pattern, matchMode, wildcard, wildcard, prefix, suffix);
    }

    /**
     * Take the pattern and match mode an return the parameter value to be used in the fulltext
     * query.
     *
     * @param pattern
     *            the search string
     * @param matchMode
     *            the match mode to use
     *
     * @param wildcardStart
     *            the wildcard
     * @param wildcardEnd
     *            the wildcard
     * @param prefix
     *            some additional string to prepend
     * @param suffix
     *            some additional string to append
     * @return the parameter value
     */
    public static String getWildcardPattern(String pattern, MatchMode matchMode,
            String wildcardStart,
            String wildcardEnd, String prefix, String suffix) {
        String fulltextPattern = null;

        if (MatchMode.ANYWHERE.equals(matchMode)) {
            fulltextPattern = wildcardStart + pattern + wildcardEnd;
        } else if (MatchMode.START.equals(matchMode)) {
            fulltextPattern = pattern + wildcardEnd;
        } else if (MatchMode.END.equals(matchMode)) {
            fulltextPattern = wildcardStart + pattern;
        } else if (MatchMode.EXACT.equals(matchMode)) {
            fulltextPattern = pattern;
        }
        return prefix + fulltextPattern + suffix;

    }

    private boolean useFulltextFeature;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFulltextParameterValue(String pattern,
            org.hibernate.criterion.MatchMode matchMode, boolean doNotUseFulltext) {
        if (useFulltextFeature && !doNotUseFulltext) {
            return getSpecificFulltextParameterValue(pattern, matchMode);
        }
        return matchMode.toMatchString(pattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SQLFunction getFulltextSQLFunction() {
        if (useFulltextFeature) {
            return getSpecificFulltextSQLFunction();
        }
        return super.getFulltextSQLFunction();
    }

    /**
     * Take the pattern and match mode an return the parameter value to be used in the fulltext
     * query.
     *
     * @param pattern
     *            the search string
     * @param matchMode
     *            the match mode to use
     * @return the parameter value
     */
    protected abstract String getSpecificFulltextParameterValue(String pattern, MatchMode matchMode);

    /**
     *
     * @return the database specific fulltext SQL function (subclass will implement)
     */
    protected abstract SQLFunction getSpecificFulltextSQLFunction();

    /**
     * @return true if the fulltext feature was not disabled with a call to
     *         {@link #setUseFulltextFeature(boolean)}
     */
    @Override
    public boolean isUseFulltextFeature() {
        return useFulltextFeature;
    }

    /**
     * Enable or disable the fulltext feature of configuration that supports the fulltext function
     *
     * @param useFeature
     *            true to use the feature
     */
    public void setUseFulltextFeature(boolean useFulltextFeature) {
        this.useFulltextFeature = useFulltextFeature;
    }

}