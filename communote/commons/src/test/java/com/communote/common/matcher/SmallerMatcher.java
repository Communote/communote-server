package com.communote.common.matcher;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SmallerMatcher extends Matcher<Long> {

    private final Long bigger;

    /**
     * Constructor.
     * 
     * @param bigger
     *            The entity to compare must be smaller than this number
     */
    public SmallerMatcher(Long bigger) {
        this.bigger = bigger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(Long entity) {
        return entity < bigger;
    }

}
