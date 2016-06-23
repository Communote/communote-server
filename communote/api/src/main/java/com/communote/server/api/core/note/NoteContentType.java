package com.communote.server.api.core.note;

import java.io.Serializable;

/**
 * An enumeration describing the different content types of a note.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum NoteContentType implements Serializable {

    /**
     * <p>
     * The actual content type is not known.
     * </p>
     */
    UNKNOWN,

    /**
     * <p>
     * The content is HTML content.
     * </p>
     */
    HTML,

    /**
     * <p>
     * The content is pure plain text.
     * </p>
     */
    PLAIN_TEXT;

}