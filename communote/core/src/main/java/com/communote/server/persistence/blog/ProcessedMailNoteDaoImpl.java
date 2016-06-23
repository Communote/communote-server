package com.communote.server.persistence.blog;

import java.util.List;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.note.ProcessedMailNote;
import com.communote.server.model.note.ProcessedMailNoteConstants;


/**
 * @see com.communote.server.model.note.ProcessedMailNote
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ProcessedMailNoteDaoImpl extends
        com.communote.server.persistence.blog.ProcessedMailNoteDaoBase {
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected com.communote.server.model.note.ProcessedMailNote handleFindByMailMessageId(
            String mailMessageId) {
        StringBuilder query = new StringBuilder();
        query.append("select message from ");
        query.append(ProcessedMailNoteConstants.CLASS_NAME);
        query.append(" message where message.");
        query.append(ProcessedMailNoteConstants.MAILMESSAGEID);
        query.append("=?");
        List<ProcessedMailNote> results = getHibernateTemplate().find(query.toString(),
                mailMessageId);
        if (results.size() == 0) {
            return null;
        }
        if (results.size() > 1) {
            throw new IllegalDatabaseState(
                    "Cannot have more than one mail message with the same message id. Message Id: "
                            + mailMessageId);
        }
        return results.iterator().next();

    }

}