package com.communote.server.core.vo.query.java.note;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.vo.query.java.note.UserIdsMatcher;


/**
 * Test for {@link UserIdsMatcher}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserIdsMatcherTest {

    /**
     * The test.
     */
    @Test
    public void testMatches() {
        NoteData note = new NoteData();
        note.setUser(new DetailedUserData());
        Long userId = new Random().nextLong();
        note.getUser().setId(userId + 1);
        Matcher<NoteData> matcher = new UserIdsMatcher(userId);
        Assert.assertFalse(matcher.matches(note));
        note.getUser().setId(userId);
        Assert.assertTrue(matcher.matches(note));
        matcher = new UserIdsMatcher(userId, userId - 1);
        Assert.assertTrue(matcher.matches(note));
    }
}
