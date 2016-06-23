package com.communote.server.core.database.hibernate;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.Mapping;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

/**
 * The fulltext function takes two arguments: The field to be search and the string to search for.
 * 
 * The fulltext function must always be matched in HQL against = 1, e.g. fulltext(content.content,
 * 'mysearch') = 1 BUT if you want the negation use: not (fulltext(content.content,'mysearch'))
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class FulltextSQLFunction implements SQLFunction {

    /**
     * {@inheritDoc}
     */
    public Type getReturnType(Type columnType, Mapping mapping) throws QueryException {
        return StandardBasicTypes.BOOLEAN;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasArguments() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasParenthesesIfNoArguments() {
        return false;
    }

}
