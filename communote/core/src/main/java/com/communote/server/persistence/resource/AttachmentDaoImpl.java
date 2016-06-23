package com.communote.server.persistence.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.attachment.AttachmentConstants;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteConstants;

/**
 * @see com.communote.server.model.attachment.Attachment
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentDaoImpl extends AttachmentDaoBase {
    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final int transform, final Attachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment.create - 'attachment' can not be null");
        }
        this.getHibernateTemplate().save(attachment);
        attachment.setGlobalId(getGlobalIdDao().createGlobalId(attachment));
        return this.transformEntity(transform, attachment);
    }

    @Override
    public Collection<Long> findOrphanedAttachments(Date upperUploadDate) {
        return getHibernateTemplate().find(
                "SELECT " + AttachmentConstants.ID + " FROM " + AttachmentConstants.CLASS_NAME
                        + " WHERE " + AttachmentConstants.NOTE + " is null AND "
                        + AttachmentConstants.UPLOADDATE + " < ?", upperUploadDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Attachment handleFindContentTypeNull() {
        List<?> result = getHibernateTemplate().find(
                "from " + AttachmentConstants.CLASS_NAME + " as attachment where attachment."
                        + AttachmentConstants.CONTENTTYPE + " is null");
        if (result != null && result.size() > 0) {
            return (Attachment) result.iterator().next();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Note handleFindNoteByContentId(ContentId contentId) {
        List<Object> args = new ArrayList<>(2);
        args.add(contentId.getContentId());
        args.add(contentId.getConnectorId());

        String query = " select note " + " from " + NoteConstants.CLASS_NAME
                + " as note left join note." + NoteConstants.ATTACHMENTS
                + " as attachment where attachment." + AttachmentConstants.CONTENTIDENTIFIER
                + " = ? AND attachment." + AttachmentConstants.REPOSITORYIDENTIFIER + " = ?";

        // Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
        // Query query = session.createQuery(mainQuery.toString());
        // Note note = (Note) query.uniqueResult();

        List result = getHibernateTemplate().find(query, args.toArray());
        if (result.isEmpty()) {
            return null;
        } else {
            return (Note) result.get(0);
        }
    }
}
