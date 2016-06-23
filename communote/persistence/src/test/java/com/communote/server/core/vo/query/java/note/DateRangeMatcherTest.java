package com.communote.server.core.vo.query.java.note;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.core.vo.query.java.note.DateRangeMatcher;


/**
 * Test for {@link DateMatcher}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DateRangeMatcherTest {

    /**
     * The test.
     */
    @Test
    public void testMatches() {
        NoteData note = new NoteData();
        long creationTimestamp = System.currentTimeMillis();
        note.setCreationDate(new Date(creationTimestamp));

        Matcher<NoteData> matcher = new DateRangeMatcher(null, null);
        Assert.assertTrue(matcher.matches(note));
        matcher = new DateRangeMatcher(new Date(creationTimestamp - 1), null);
        Assert.assertTrue(matcher.matches(note));
        matcher = new DateRangeMatcher(null, new Date(creationTimestamp - 1));
        Assert.assertFalse(matcher.matches(note));
        matcher = new DateRangeMatcher(null, new Date(creationTimestamp + 1));
        Assert.assertTrue(matcher.matches(note));
        matcher = new DateRangeMatcher(new Date(creationTimestamp + 1), null);
        Assert.assertFalse(matcher.matches(note));
        matcher = new DateRangeMatcher(new Date(creationTimestamp - 1), new Date(
                creationTimestamp + 1));
        Assert.assertTrue(matcher.matches(note));

    }
}
