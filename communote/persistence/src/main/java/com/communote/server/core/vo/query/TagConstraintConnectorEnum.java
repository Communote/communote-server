package com.communote.server.core.vo.query;

/**
 * Enum for connecting tags in a query (either 'or' or 'and')
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum TagConstraintConnectorEnum {

    /**
     * and for and
     */
    AND(" and "),
    /**
     * or for or
     */
    OR(" or ");

    private String value;

    /**
     * Construct with the value to be used in the query
     * 
     * @param value
     *            the query value
     */
    TagConstraintConnectorEnum(String value) {
        this.value = value;
    }

    /**
     * get the value of this enum entry
     * 
     * @return the value
     */
    public String value() {
        return value;
    }
}
