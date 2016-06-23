package com.communote.server.core.vo.query.java.note;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.blog.UserBlogData;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.vo.query.java.note.FollowingMatcher;


/**
 * Test for {@link FollowingMatcher}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FollowingMatcherTest {

    /**
     * The test.
     */
    @Test
    public void testMatches() {
        IAnswer<Boolean> answer = new IAnswer<Boolean>() {
            @Override
            public Boolean answer() throws Throwable {
                Long input = (Long) EasyMock.getCurrentArguments()[0];
                return input != null && input.equals(1L);
            }
        };

        FollowManagement followManagement = EasyMock.createMock(FollowManagement.class);
        expect(followManagement.followsBlog(EasyMock.anyLong())).andAnswer(answer).anyTimes();
        expect(followManagement.followsTag(EasyMock.anyLong())).andAnswer(answer).anyTimes();
        expect(followManagement.followsUser(EasyMock.anyLong())).andAnswer(answer).anyTimes();
        expect(followManagement.followsDiscussion(EasyMock.anyLong())).andAnswer(answer).anyTimes();
        EasyMock.replay(followManagement);

        NoteData note = new NoteData();
        note.setBlog(new UserBlogData());
        note.setUser(new DetailedUserData());
        Matcher<NoteData> matcher = new FollowingMatcher(followManagement);

        // Discussion
        note.setDiscussionId(0L);
        Assert.assertFalse(matcher.matches(note));
        note.setDiscussionId(1L);
        Assert.assertTrue(matcher.matches(note));
        note.setDiscussionId(-1L);
        Assert.assertFalse(matcher.matches(note));
        // Author
        note.getUser().setId(0L);
        Assert.assertFalse(matcher.matches(note));
        note.getUser().setId(1L);
        Assert.assertTrue(matcher.matches(note));
        note.getUser().setId(-1L);
        Assert.assertFalse(matcher.matches(note));
        // Topic
        note.getBlog().setId(0L);
        Assert.assertFalse(matcher.matches(note));
        note.getBlog().setId(1L);
        Assert.assertTrue(matcher.matches(note));
        note.getBlog().setId(-1L);
        Assert.assertFalse(matcher.matches(note));
        // Tags
        note.setTags(new ArrayList<TagData>());
        note.getTags().add(new TagData(0L, "Hallo"));
        Assert.assertFalse(matcher.matches(note));
        note.getTags().add(new TagData(1L, "Hallo"));
        Assert.assertTrue(matcher.matches(note));
    }
}
