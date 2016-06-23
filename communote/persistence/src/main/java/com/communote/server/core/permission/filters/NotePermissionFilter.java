package com.communote.server.core.permission.filters;

import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.security.permission.PermissionFilter;
import com.communote.server.model.note.Note;

/**
 * Permission filter for notes.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface NotePermissionFilter extends PermissionFilter<Note, NoteStoringTO> {

    /**
     * the default value for the order. This value should be used if the filter has no specific
     * requirements to the invocation order.
     */
    int DEFAULT_ORDER_VALUE = 1000;
}
