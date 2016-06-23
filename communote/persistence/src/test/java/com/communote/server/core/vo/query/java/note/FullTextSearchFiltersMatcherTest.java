package com.communote.server.core.vo.query.java.note;

import java.util.UUID;

import org.easymock.EasyMock;
import org.hibernate.criterion.MatchMode;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.converter.Converter;
import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.core.vo.query.java.note.FullTextSearchFiltersMatcher;
import com.communote.server.service.NoteService;

/**
 * Test for {@link FullTextSearchFiltersMatcher}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FullTextSearchFiltersMatcherTest {

    /**
     * The test.
     */
    @Test
    public void testMatches() {
        String prefix = UUID.randomUUID().toString();
        String mainContent = UUID.randomUUID().toString();
        String suffix = UUID.randomUUID().toString();

        NoteData noteListData = new NoteData();
        noteListData.setContent(prefix + mainContent + suffix);
        noteListData.setId(1L);

        NoteService noteService = EasyMock.createMock(NoteService.class);
        EasyMock.expect(noteService.getNote(EasyMock.anyLong(),
                EasyMock.anyObject(Converter.class)))
                .andReturn(noteListData.getContent()).anyTimes();
        EasyMock.replay(noteService);

        Matcher<NoteData> matcher = new FullTextSearchFiltersMatcher(noteService,
                MatchMode.ANYWHERE);
        Assert.assertTrue(matcher.matches(noteListData));

        // Anywhere
        matcher = new FullTextSearchFiltersMatcher(noteService, MatchMode.ANYWHERE, prefix,
                mainContent, suffix);
        Assert.assertTrue(matcher.matches(noteListData));
        matcher = new FullTextSearchFiltersMatcher(noteService, MatchMode.ANYWHERE, UUID
                .randomUUID().toString());
        Assert.assertFalse(matcher.matches(noteListData));
        // Start
        matcher = new FullTextSearchFiltersMatcher(noteService, MatchMode.START, prefix);
        Assert.assertTrue(matcher.matches(noteListData));
        matcher = new FullTextSearchFiltersMatcher(noteService, MatchMode.START, mainContent);
        Assert.assertFalse(matcher.matches(noteListData));
        // End
        matcher = new FullTextSearchFiltersMatcher(noteService, MatchMode.END, suffix);
        Assert.assertTrue(matcher.matches(noteListData));
        matcher = new FullTextSearchFiltersMatcher(noteService, MatchMode.END, mainContent);
        Assert.assertFalse(matcher.matches(noteListData));
        // Exact
        matcher = new FullTextSearchFiltersMatcher(noteService, MatchMode.EXACT, prefix
                + mainContent + suffix);
        Assert.assertTrue(matcher.matches(noteListData));
        matcher = new FullTextSearchFiltersMatcher(noteService, MatchMode.EXACT, mainContent);
        Assert.assertFalse(matcher.matches(noteListData));
    }
}
