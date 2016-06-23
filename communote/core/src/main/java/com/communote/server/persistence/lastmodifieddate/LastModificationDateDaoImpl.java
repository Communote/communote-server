package com.communote.server.persistence.lastmodifieddate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.communote.server.model.attachment.AttachmentConstants;
import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.note.NoteConstants;
import com.communote.server.model.note.NoteStatus;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LastModificationDateDaoImpl extends HibernateDaoSupport implements
        LastModificationDateDao {

    private final static Logger LOGGER = LoggerFactory.getLogger(LastModificationDateDaoImpl.class);

    private <T> List<T> executeQuery(Query query,
            int numColumns, LastModificationDateFactory<T> lastModificationDateFactory) {

        query.setReadOnly(true);
        query.setCacheable(false);

        ScrollableResults scrollableResults = query.scroll(ScrollMode.FORWARD_ONLY);

        final List<T> dates = new ArrayList<>();
        int resultsCount = 0;
        int datesCount = 0;
        while (scrollableResults.next()) {
            resultsCount++;
            Object[] row = scrollableResults.get();

            T date = extractLastModificationDate(row, numColumns, lastModificationDateFactory);

            if (date != null) {
                dates.add(date);
                datesCount++;
            }

        }

        if (resultsCount > datesCount) {
            LOGGER.warn("Got {} in result set and {} dates. query= {}", resultsCount, datesCount,
                    query.getQueryString());
        } else {
            LOGGER.debug("Got {} in result set and {} dates.", resultsCount, datesCount);
        }

        return dates;
    }

    /**
     * Extract the last modification date.
     * 
     * row[0] must be the entity id as long row[i] i > 0 must be a date, always te maximum
     * (youngest) date is taken
     * 
     * @param row
     * @param columns
     *            number of columns row should have (including entity id)
     * @return
     */
    private <T> T extractLastModificationDate(Object[] row, int columns,
            LastModificationDateFactory<T> lastModificationDateFactory) {

        T date = null;
        if (row != null && row.length == columns & row[0] != null) {

            Date youngest = null;

            for (int i = 1; i < columns; i++) {
                Date d = (Date) row[i];
                if (youngest == null || (d != null && d.after(youngest))) {
                    youngest = d;
                }
            }

            if (youngest != null) {
                date = lastModificationDateFactory.createLastModificationDate((Long) row[0],
                        youngest);

            }
        }
        return date;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.communote.server.persistence.blog.LastModificationDateDao#getAttachmentLastModificationDates
     * ()
     */
    @Override
    public <T> List<T> getAttachmentLastModificationDates(
            LastModificationDateFactory<T> lastModificationDateFactory) {

        String attachLastDateQuery = "select "
                + "attach." + AttachmentConstants.ID + ", "
                + "note. " + NoteConstants.CRAWLLASTMODIFICATIONDATE + ", "
                + "blog." + BlogConstants.CRAWLLASTMODIFICATIONDATE + " "
                + "from " + NoteConstants.CLASS_NAME + " note "
                + "left join note." + NoteConstants.BLOG + " blog "
                + "left join note." + NoteConstants.ATTACHMENTS + " attach "
                + "where attach." + AttachmentConstants.ID + " is not null "
                + "and note." + NoteConstants.STATUS + " = :status";

        int numColumns = 3;

        Query query = this.getSession().createQuery(attachLastDateQuery);
        query.setParameter("status", NoteStatus.PUBLISHED);

        final List<T> dates = executeQuery(query, numColumns,
                lastModificationDateFactory);

        return dates;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.communote.server.persistence.blog.LastModificationDateDao#getNoteLastModificationDates()
     */
    @Override
    public <T> List<T> getNoteLastModificationDates(
            LastModificationDateFactory<T> lastModificationDateFactory) {

        String noteLastDateQuery = "select "
                + "note." + NoteConstants.ID + ", "
                + "note. " + NoteConstants.CRAWLLASTMODIFICATIONDATE + ", "
                + "blog." + BlogConstants.CRAWLLASTMODIFICATIONDATE + " "
                + "from " + NoteConstants.CLASS_NAME + " note "
                + "left join note." + NoteConstants.BLOG + " blog "
                + "where note." + NoteConstants.STATUS + " = :status";

        int numColumns = 3;

        Query query = this.getSession().createQuery(noteLastDateQuery);
        query.setParameter("status", NoteStatus.PUBLISHED);

        final List<T> dates = executeQuery(query, numColumns,
                lastModificationDateFactory);

        return dates;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.communote.server.persistence.blog.LastModificationDateDao#getTopicLastModificationDates()
     */
    @Override
    public <T> List<T> getTopicLastModificationDates(
            LastModificationDateFactory<T> lastModificationDateFactory) {

        String topicLastDateQuery = "select "
                + "blog." + BlogConstants.ID + ", "
                + "blog." + BlogConstants.CRAWLLASTMODIFICATIONDATE + " "
                + "from " + BlogConstants.CLASS_NAME + " blog ";

        int numColumns = 2;

        Query query = this.getSession().createQuery(topicLastDateQuery);
        final List<T> dates = executeQuery(query, numColumns,
                lastModificationDateFactory);

        return dates;
    }

}
