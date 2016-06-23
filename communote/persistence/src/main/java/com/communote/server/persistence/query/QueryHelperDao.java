package com.communote.server.persistence.query;

/**
 * @see com.communote.server.persistence.query.QueryHelper
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface QueryHelperDao {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * <p>
     * execute the query definition
     * </p>
     */
    public com.communote.common.util.PageableList executeQuery(
            com.communote.server.core.vo.query.Query query,
            com.communote.server.core.vo.query.QueryParameters queryParameters);

    /**
     * <p>
     * execute the query definition and convert the result
     * </p>
     */
    public com.communote.common.util.PageableList executeQuery(
            com.communote.server.core.vo.query.Query query,
            com.communote.server.core.vo.query.QueryParameters queryParameters,
            com.communote.server.core.vo.query.QueryResultConverter resultConverter,
            com.communote.server.core.filter.ResultSpecification resultSpecification);

    /**
     * <p>
     * execute the query instance but not take of max counts or offset; retrieve the complete list
     * </p>
     */
    public java.util.List executeQueryComplete(com.communote.server.core.vo.query.Query query,
            com.communote.server.core.vo.query.QueryParameters queryParameters);

}
