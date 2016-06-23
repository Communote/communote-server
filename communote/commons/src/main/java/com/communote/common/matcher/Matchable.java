package com.communote.common.matcher;

/**
 * Interface classes can implement to be able to be marked es matched.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface Matchable {
    /**
     * Method to get the matching status of the entity.
     * 
     * @return <code>True</code>, if the entity was matched by a matcher.
     */
    boolean isMatched();

    /**
     * Method to set the matching status of the entity.
     * 
     * @param matching
     *            <code>True</code>, if this item matches the matcher.
     */
    void setMatched(boolean matching);

}
