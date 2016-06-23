package com.communote.server.core.vo.query.note;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.vo.query.note.LastDiscussionCreationDateComparator;
import com.communote.server.model.note.Note;
import com.communote.server.service.NoteService;


/**
 * This tests the {@link LastDiscussionCreationDateComparator}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LastDicussionCreationDateComparatorTest {
    /**
     * The test.
     * 
     * @throws NoteNotFoundException
     *             The test will fail, if this exception is thrown.
     * @throws InterruptedException
     *             The test will fail, if this exception is thrown.
     */
    @Test
    public void test() throws NoteNotFoundException, InterruptedException {
        Date date1 = new Date();
        Thread.sleep(10);
        Date date2 = new Date();
        Thread.sleep(10);
        Date date3 = new Date();
        Thread.sleep(10);
        Date date4 = new Date();

        Note note1 = Note.Factory.newInstance();
        note1.setId(1L);
        Note note2 = Note.Factory.newInstance();
        note2.setId(2L);

        List<SimpleNoteListItem> answersToNote1 = new ArrayList<SimpleNoteListItem>();
        answersToNote1.add(new SimpleNoteListItem(11L, date3));
        List<SimpleNoteListItem> answersToNote2 = new ArrayList<SimpleNoteListItem>();
        answersToNote2.add(new SimpleNoteListItem(22L, date4));

        NoteService noteService = EasyMock.createMock(NoteService.class);
        EasyMock.expect(noteService.getCommentsOfDiscussion(1L)).andReturn(null).times(3);
        EasyMock.expect(noteService.getCommentsOfDiscussion(2L))
                .andReturn(new ArrayList<SimpleNoteListItem>()).times(3);

        EasyMock.expect(noteService.getCommentsOfDiscussion(1L)).andReturn(answersToNote1).times(3);
        EasyMock.expect(noteService.getCommentsOfDiscussion(2L)).andReturn(null).times(3);

        EasyMock.expect(noteService.getCommentsOfDiscussion(1L)).andReturn(answersToNote1).times(3);
        EasyMock.expect(noteService.getCommentsOfDiscussion(2L)).andReturn(answersToNote2).times(3);

        EasyMock.expect(noteService.getCommentsOfDiscussion(1L)).andReturn(answersToNote1).times(3);
        EasyMock.expect(noteService.getCommentsOfDiscussion(2L)).andReturn(answersToNote1).times(3);

        EasyMock.replay(noteService);

        Comparator<SimpleNoteListItem> comparator = new LastDiscussionCreationDateComparator(
                noteService);

        // Notes without comments.
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date1),
                new SimpleNoteListItem(2L, date2)), 1);
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date1),
                new SimpleNoteListItem(2L, date1)), 0);
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date2),
                new SimpleNoteListItem(2L, date1)), -1);

        // 1 note with comment
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date1),
                new SimpleNoteListItem(2L, date2)), -1);
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date1),
                new SimpleNoteListItem(2L, date1)), -1);
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date2),
                new SimpleNoteListItem(2L, date1)), -1);

        // Both notes with comment with different dates..
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date1),
                new SimpleNoteListItem(2L, date2)), 1);
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date1),
                new SimpleNoteListItem(2L, date1)), 1);
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date2),
                new SimpleNoteListItem(2L, date1)), 1);

        // Both notes with comment with same date.
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date1),
                new SimpleNoteListItem(2L, date2)), 0);
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date1),
                new SimpleNoteListItem(2L, date1)), 0);
        Assert.assertEquals(comparator.compare(new SimpleNoteListItem(1L, date2),
                new SimpleNoteListItem(2L, date1)), 0);
    }
}
