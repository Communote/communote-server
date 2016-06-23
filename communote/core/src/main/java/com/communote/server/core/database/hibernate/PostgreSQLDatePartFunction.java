package com.communote.server.core.database.hibernate;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.Mapping;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import com.communote.server.api.core.config.database.DateField;

/**
 * usage: day_of_date(field, note.creationDate) => returns the day as integer
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PostgreSQLDatePartFunction implements SQLFunction {

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
        return StandardBasicTypes.BIG_INTEGER;
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasParenthesesIfNoArguments() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) {
        // 1 arguments is the date field to use
        if (arguments.size() != 2) {
            throw new QueryException(
                    "Need exactly two arguments for day of date function, but got only "
                            + arguments.size()
                            + " args=" + arguments);
        }
        DateField dateField;
        try {
            dateField = DateField.valueOf(arguments.get(0).toString());
        } catch (Exception e) {
            throw new QueryException(
                    "Invalid datefield used: " + arguments.get(0) + ". " + e.getMessage(), e);
        }

        String sql = "date_part('" + dateField + "'," + arguments.get(1) + ")";
        return sql;
    }

}
