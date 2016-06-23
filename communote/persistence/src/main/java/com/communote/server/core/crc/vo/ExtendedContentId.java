package com.communote.server.core.crc.vo;

import com.communote.server.api.ServiceLocator;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.note.Note;
import com.communote.server.persistence.resource.AttachmentDao;

/**
 * Extends the content ID in a way that provides access to the associated note and attachment
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ExtendedContentId extends ContentId {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Note note;
    private Attachment attachment;

    public ExtendedContentId(ContentId contentId) {
        super(contentId);
    }

    public Attachment getAttachment() {
        if (attachment == null) {
            attachment = getAttachmentDao().find(this.getContentId(), this.getConnectorId());
        }
        return attachment;
    }

    private AttachmentDao getAttachmentDao() {
        return ServiceLocator.findService(AttachmentDao.class);
    }

    public Note getNote() {
        if (note == null) {
            if (attachment != null) {
                note = attachment.getNote();
            } else {
                note = getAttachmentDao().findNoteByContentId(this);
            }
        }
        return note;
    }

}