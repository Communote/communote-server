package com.communote.server.core.vo.query.java.note;

import java.util.Date;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;


/**
 * This matcher checks, if the note is within the given time range.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DateRangeMatcher extends Matcher<NoteData> {

    private final Date endDate;
    private final Date startDate;

    /**
     * Constructor.
     * 
     * @param startDate
     *            The lower date bound (start date). Might be null.
     * @param endDate
     *            The upper date bound (end date). Might be null.
     */
    public DateRangeMatcher(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(NoteData entity) {
        if (startDate == null && endDate == null) {
            return true; // not set, always true.
        }
        Date date = entity.getCreationDate();
        return (startDate == null || date.after(startDate))
                && (endDate == null || date.before(endDate));
    }
}
