package com.communote.server.core.vo.query.search;

import java.util.Date;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DateFilter extends Filter {

    private Date fromDate;
    private Date toDate;

    /**
     * @return
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * @return
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * @param fromDate
     */
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * @param toDate
     */
    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
}
