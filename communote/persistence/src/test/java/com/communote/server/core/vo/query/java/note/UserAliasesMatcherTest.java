package com.communote.server.core.vo.query.java.note;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.vo.query.java.note.UserAliasesMatcher;


/**
 * Test for {@link UserAliasesMatcher}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserAliasesMatcherTest {

    /**
     * The test.
     */
    @Test
    public void testMatches() {
        NoteData note = new NoteData();
        note.setUser(new DetailedUserData());
        String userAlias = UUID.randomUUID().toString();
        note.getUser().setAlias(UUID.randomUUID().toString());
        Matcher<NoteData> matcher = new UserAliasesMatcher(userAlias);
        Assert.assertFalse(matcher.matches(note));
        note.getUser().setAlias(userAlias);
        Assert.assertTrue(matcher.matches(note));
        matcher = new UserAliasesMatcher(userAlias, userAlias + 1);
        Assert.assertTrue(matcher.matches(note));
    }
}
