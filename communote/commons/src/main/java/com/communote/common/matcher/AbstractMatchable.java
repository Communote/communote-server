package com.communote.common.matcher;

/**
 * Default implementation for the {@link Matchable} interface.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractMatchable implements Matchable {

    private boolean matching;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMatched() {
        return matching;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMatched(boolean matching) {
        this.matching = matching;
    }

}
