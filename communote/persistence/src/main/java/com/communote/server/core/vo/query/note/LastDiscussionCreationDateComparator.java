package com.communote.server.core.vo.query.note;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.service.NoteService;


/**
 * This comparator sorts the notes by last discussion creation date.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LastDiscussionCreationDateComparator implements Comparator<SimpleNoteListItem> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(LastDiscussionCreationDateComparator.class);

    private final NoteService noteService;

    /**
     * Constructor.
     * 
     * @param noteService
     *            The service to be used to load discussions.
     */
    public LastDiscussionCreationDateComparator(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * {@inheritDoc}
     * 
     * @return <ul>
     *         <li><b>-1</b>, if the first discussion has a greater last discussion creation date
     *         then the second one.
     *         <li><b>0</b>, if both discussions have the same creation date.
     *         <li><b>1</b>, if the fist discussion has a smaller last discussion creation date then
     *         the second one.
     *         </ul>
     */
    @Override
    public int compare(SimpleNoteListItem discussion1, SimpleNoteListItem discussion2) {
        long discussion1Date = getLastDiscussionCreationTime(discussion1);
        long discussion2Date = getLastDiscussionCreationTime(discussion2);
        long distance = discussion1Date - discussion2Date;
        return distance == 0 ? 0 : distance > 0 ? -1 : 1;
    }

    /**
     * @param rootNote
     *            Root note of the discussion.
     * @return The last discussion creation date as long.
     */
    public long getLastDiscussionCreationTime(SimpleNoteListItem rootNote) {
        try {
            List<SimpleNoteListItem> comments = noteService.getCommentsOfDiscussion(rootNote
                    .getId());
            if (comments == null || comments.size() == 0) {
                return rootNote.getCreationDate().getTime();
            }
            long result = 0;
            for (SimpleNoteListItem item : comments) {
                long creationTime = item.getCreationDate().getTime();
                if (creationTime > result) {
                    result = creationTime;
                }
            }
            return result;
        } catch (NoteNotFoundException e) {
            LOGGER.warn("The discussion {} doesn't exists anymore.", rootNote.getId());
        }
        return 0;
    }
}
