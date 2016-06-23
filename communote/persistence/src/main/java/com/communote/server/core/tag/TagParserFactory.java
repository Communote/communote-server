package com.communote.server.core.tag;

import com.communote.server.core.tag.impl.CommaTagParser;

/**
 * A factory to get a tag parser
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagParserFactory {

    private final static TagParserFactory INSTANCE = new TagParserFactory();

    /**
     * Get the instance
     * 
     * @return The instance
     */
    public static TagParserFactory instance() {
        return INSTANCE;
    }

    /**
     * The default tag parser to use
     * 
     * @return The default tag parser
     */
    public TagParser getDefaultTagParser() {
        return CommaTagParser.instance();
    }

}
