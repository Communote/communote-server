package com.communote.server.core.database.hibernate;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * For creating an index on PostgreSQL use:
 *
 * CREATE INDEX core_content_full_en_idx ON core_content USING gin(to_tsvector('english', content));
 * CREATE INDEX core_content_full_de_idx ON core_content USING gin(to_tsvector('german', content));
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PostgreSQLFulltextFunction extends FulltextSQLFunction {

    // name of text search configuration as to be used in query, that is with "'", e.g. 'simple' or
    // 'english
    private final String textSearchConfigurationNameQueryPart;

    /**
     * See http://www.postgresql.org/docs/9.1/static/sql-createtsconfig.html or
     * http://www.postgresql.org/docs/9.1/static/textsearch.html for more information
     *
     * @param textSearchConfigurationName
     *            name of the text search configuration to be used, e.g. simple or english (without
     *            any quotes)
     */
    public PostgreSQLFulltextFunction(String textSearchConfigurationName) {
        this.textSearchConfigurationNameQueryPart = "'" + textSearchConfigurationName + "'";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) {
        // 2 arguments without locale, 3 with locale; however locale not used
        if (arguments.size() < 2 || arguments.size() > 3) {
            throw new QueryException("Need 2 or 3 arguments for fulltext function, but got only "
                    + arguments.size() + " args=" + arguments);
        }

        // see KENMEI-4643 for details why this way complicated
        // see KENMEI-4678 why to uses simple as default
        String sql = "to_tsvector(" + textSearchConfigurationNameQueryPart + ", "
                + arguments.get(0) + ") @@ tsquery(plainto_tsquery("
                + textSearchConfigurationNameQueryPart + ", " + arguments.get(1)
                + ") :: varchar || ':*')";
        return sql;
    }

}
