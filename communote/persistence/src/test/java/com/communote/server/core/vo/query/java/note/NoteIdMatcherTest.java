package com.communote.server.core.vo.query.java.note;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.core.vo.query.java.note.NoteIdMatcher;


/**
 * Test for {@link NoteIdMatcher}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteIdMatcherTest {

    /**
     * The test.
     */
    @Test
    public void testMatches() {
        NoteData note = new NoteData();
        note.setId(new Random().nextLong());

        Matcher<NoteData> matcher = new NoteIdMatcher(null);
        Assert.assertTrue(matcher.matches(note));
        matcher = new NoteIdMatcher(note.getId() + 1);
        Assert.assertFalse(matcher.matches(note));
        matcher = new NoteIdMatcher(note.getId());
        Assert.assertTrue(matcher.matches(note));
    }
}
