package com.communote.server.core.vo.query.java.note;

import java.util.ArrayList;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.vo.query.java.note.UsersToBeNotifiedMatcher;


/**
 * Test for {@link UsersToBeNotifiedMatcher}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UsersToBeNotifiedMatcherTest {

    /**
     * The test.
     */
    @Test
    public void testMatches() {
        NoteData note = new NoteData();
        Long user1Id = new Random().nextLong();
        Long user2Id = user1Id + 1;
        note.setNotifiedUsers(new ArrayList<DetailedUserData>());
        note.getNotifiedUsers().add(new DetailedUserData());
        note.getNotifiedUsers().add(new DetailedUserData());
        note.getNotifiedUsers().get(0).setId(user1Id);
        note.getNotifiedUsers().get(1).setId(user2Id);

        Matcher<NoteData> matcher = new UsersToBeNotifiedMatcher();
        Assert.assertTrue(matcher.matches(note));
        matcher = new UsersToBeNotifiedMatcher(user1Id - 1);
        Assert.assertFalse(matcher.matches(note));
        matcher = new UsersToBeNotifiedMatcher(user1Id);
        Assert.assertTrue(matcher.matches(note));
        matcher = new UsersToBeNotifiedMatcher(user2Id);
        Assert.assertTrue(matcher.matches(note));
        matcher = new UsersToBeNotifiedMatcher(user1Id, user1Id);
        Assert.assertTrue(matcher.matches(note));
    }
}
