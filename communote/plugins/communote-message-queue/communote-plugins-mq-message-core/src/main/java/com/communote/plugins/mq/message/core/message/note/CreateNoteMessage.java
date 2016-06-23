package com.communote.plugins.mq.message.core.message.note;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.message.core.data.note.Note;

/**
 * Message to be sent for creating a note
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CreateNoteMessage extends BaseMessage {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private Note note;

    /**
     * @return the POJO that holds the details for creating the note
     */
    public Note getNote() {
        return this.note;
    }

    /**
     * Set the creation details
     * 
     * @param note
     *            the POJO that holds the details for creating the note
     */
    public void setNote(Note note) {
        this.note = note;
    }
}
