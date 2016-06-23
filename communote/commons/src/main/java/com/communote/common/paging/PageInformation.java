package com.communote.common.paging;

/**
 * Holding different meta information about the page positions. Which is the offset, the count of
 * elements per page and the overall count.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PageInformation {

    private final int offset;
    private final int elementsPerPage;
    private final int countOverallElements;
    private final int pagingInterval;
    private final int pageNumber;
    private final int pageCount;

    /**
     * Construct a new page information
     * 
     * @param offset
     *            the offset
     * @param elementsPerPage
     *            the elements per page
     * @param countOverallElements
     *            the count of all elements
     * @param pagingInterval
     *            the interval (how many pages to show)
     */
    public PageInformation(int offset, int elementsPerPage, int countOverallElements,
            int pagingInterval) {
        this.offset = offset;
        this.elementsPerPage = elementsPerPage;
        this.countOverallElements = countOverallElements;
        this.pagingInterval = pagingInterval;
        double rel = (double) offset / (double) elementsPerPage;
        this.pageNumber = ((int) Math.ceil(rel) + 1);
        rel = elementsPerPage < 1 ? 0 : (double) countOverallElements / (double) elementsPerPage;
        this.pageCount = ((int) Math.ceil(rel));
    }

    /**
     * get the overall count of all elements
     * 
     * @return the overall count
     */
    public int getCountOverallElements() {
        return countOverallElements;
    }

    /**
     * Get the the number of pages which is computed by the countOverallElements end
     * elementsPerPage.
     * 
     * @return the page count
     */
    public int getCountPages() {
        return this.pageCount;
    }

    /**
     * Get the number of elements of the page
     * 
     * @return the elements per page
     */
    public int getElementsPerPage() {
        return elementsPerPage;
    }

    /**
     * The first number of the element of the page
     * 
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get the current page number which is computed by the offset end elementsPerPage. It starts
     * with 1!
     * 
     * @return the page number
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    /**
     * Get the paging interval
     * 
     * @return the number of paging buttons
     */
    public int getPagingInterval() {
        return pagingInterval;
    }
}
