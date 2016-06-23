package com.communote.server.core.tag.impl;

import com.communote.server.core.tag.AbstractTagParser;
import com.communote.server.core.tag.TagParser;

/**
 * This simple tag parser just splits the tags using a ',' as separator
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommaTagParser extends AbstractTagParser {

    private final static CommaTagParser INSTANCE = new CommaTagParser();

    private final static String COMMA = ",";

    /**
     * Get the instance
     * 
     * @return The instance
     */
    public static TagParser instance() {
        return INSTANCE;
    }

    /**
     * Construct a colon tag parser
     */
    public CommaTagParser() {
        super(COMMA);
    }

}
