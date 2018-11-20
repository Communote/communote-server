package com.communote.plugins.mediaparser.mediatype;

import com.communote.plugins.mediaparser.RichMediaDescription;

/**
 * Property constants that represents supported rich media sources.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface RichMediaType {

    /**
     * Checks the link and extracts the ID if the link is of the right source
     *
     * @param link
     *            The link to extract from.
     *
     * @return the description or {@code null}
     */
    public RichMediaDescription extractRichMediaDescription(String link);

    /**
     * @return the typeId
     */
    public String getTypeId();

}