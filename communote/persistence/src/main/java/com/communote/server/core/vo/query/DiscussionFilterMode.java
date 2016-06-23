package com.communote.server.core.vo.query;

/**
 * Defines possible filter modes for discussions.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum DiscussionFilterMode {
    /** All notes will be respected (default). */
    ALL,
    /** Only root notes of discussions will be returned. */
    IS_DISCUSSION_ROOT,
    /** Only discussions will be returned. */
    IS_DISCUSSION,
    /** No discussions will be returned. */
    IS_NO_DISCUSSION,
    /**
     * Only root notes will be returned. Those are all notes with no parent. If a date filter is
     * active it will be matched against the last discussion date and not the creation date.
     */
    IS_ROOT;
}