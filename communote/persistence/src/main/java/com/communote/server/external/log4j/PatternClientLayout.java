package com.communote.server.external.log4j;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternParser;

/**
 * New Pattern Layout. This pattern can log the client id and name
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PatternClientLayout extends PatternLayout {

    /**
     * Constructor
     */
    public PatternClientLayout() {
        super();
    }

    /**
     * Constructor
     * 
     * @param pattern
     *            The specified pattern in log4j.properties
     */
    public PatternClientLayout(String pattern) {
        super(pattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PatternParser createPatternParser(String pattern) {
        return new PatternClientParser(pattern);
    }

}
