package com.communote.server.core.tag.impl;

import com.communote.server.core.tag.AbstractTagParser;
import com.communote.server.core.tag.TagParser;

/**
 * This simple tag parser just splits the tags using blank as separator
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlankTagParser extends AbstractTagParser {

    private final static BlankTagParser INSTANCE = new BlankTagParser();

    private final static String BLANK = " ";

    /**
     * Get the instance
     * 
     * @return The instance
     */
    public static TagParser instance() {
        return INSTANCE;
    }

    /**
     * Construct a blank tag parser
     */
    public BlankTagParser() {
        super(BLANK);
    }

}
