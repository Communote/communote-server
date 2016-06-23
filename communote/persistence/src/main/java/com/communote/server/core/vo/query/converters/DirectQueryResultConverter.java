package com.communote.server.core.vo.query.converters;

import com.communote.common.converter.Converter;
import com.communote.server.core.vo.query.QueryResultConverter;


/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <I>
 *            In
 * @param <O>
 *            Out
 */
// TODO the implements of Converter is just a quick hack would be cleaner if the
// QueryResultConverter is the implementor of the interface
public abstract class DirectQueryResultConverter<I, O> extends QueryResultConverter<I, O> implements
        Converter<I, O> {
    /**
     * @param source
     *            The source object
     * @return The resulting object
     */
    @Override
    public O convert(I source) {
        O target = create();
        convert(source, target);
        return target;
    }
}
