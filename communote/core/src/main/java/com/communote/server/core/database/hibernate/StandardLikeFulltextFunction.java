package com.communote.server.core.database.hibernate;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * Implements the HQL fulltext function with standard SQL LIKE functionality.
 * 
 * Generates lower(content.content) like lower(?)
 * 
 * The field parameter must already contain the %
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StandardLikeFulltextFunction extends FulltextSQLFunction {

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory)
            throws QueryException {
        if (arguments.size() < 2 || arguments.size() > 3) {
            throw new QueryException(
                    "Need exactly 2 arguments for like fulltext function, but got only "
                            + arguments.size()
                            + " args=" + arguments);
        }
        return "lower(" + arguments.get(0) + ") like lower(" + arguments.get(1) + ") AND "
                + factory.getDialect().toBooleanValueString(true);
    }

}
