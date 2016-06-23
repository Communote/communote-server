package com.communote.server.core.vo.query.java.note;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.vo.query.java.note.FavoriteMatcher;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FavoriteMatcherTest {
    /**
     * The test.
     */
    @Test
    public void testMatches() {
        NoteData note = new NoteData();
        note.setId(0L);
        FavoriteManagement favoriteManagement = EasyMock.createMock(FavoriteManagement.class);
        EasyMock.expect(favoriteManagement.isFavorite(EasyMock.anyLong())).andAnswer(
                new IAnswer<Boolean>() {
                    @Override
                    public Boolean answer() throws Throwable {
                        return EasyMock.getCurrentArguments()[0].equals(1L);
                    }
                }).anyTimes();
        EasyMock.replay(favoriteManagement);

        Matcher<NoteData> matcher = new FavoriteMatcher(favoriteManagement);
        Assert.assertFalse(matcher.matches(note));

        note.setId(1L);
        Assert.assertTrue(matcher.matches(note));
    }
}
