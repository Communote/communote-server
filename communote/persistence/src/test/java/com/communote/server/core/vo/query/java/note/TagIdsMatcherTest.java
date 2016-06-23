package com.communote.server.core.vo.query.java.note;

import java.util.ArrayList;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.vo.query.java.note.TagIdsMatcher;


/**
 * Test for {@link TagIdsMatcher}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagIdsMatcherTest {

    /**
     * The test.
     */
    @Test
    public void testMatches() {
        NoteData note = new NoteData();
        Long tag1Id = new Random().nextLong();
        Long tag2Id = tag1Id + 1;
        note.setTags(new ArrayList<TagData>());
        note.getTags().add(new TagData(tag1Id, "test1"));
        note.getTags().add(new TagData(tag2Id, "test2"));

        Matcher<NoteData> matcher = new TagIdsMatcher();
        Assert.assertTrue(matcher.matches(note));
        matcher = new TagIdsMatcher(tag1Id);
        Assert.assertTrue(matcher.matches(note));
        matcher = new TagIdsMatcher(tag2Id);
        Assert.assertTrue(matcher.matches(note));
        matcher = new TagIdsMatcher(tag1Id, tag2Id);
        Assert.assertTrue(matcher.matches(note));
        matcher = new TagIdsMatcher(tag1Id - 1, tag2Id);
        Assert.assertFalse(matcher.matches(note));
        matcher = new TagIdsMatcher(tag1Id, tag2Id + 1);
        Assert.assertFalse(matcher.matches(note));
    }
}
