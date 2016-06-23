package com.communote.server.core.vo.blog;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.vo.blog.DiscussionNoteData;


/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DiscussionNoteDataTest {
    /**
     * Generates tags from the given list.
     * 
     * @param tags
     *            List of tags.
     * @return Collection of TagData.
     */
    private List<TagData> generateTags(String[] tags) {
        List<TagData> innerTags = new ArrayList<TagData>();
        for (int i = 0; i < RandomUtils.nextInt(tags.length) + 1; i++) {
            innerTags.add(new TagData(tags[RandomUtils.nextInt(tags.length - 1)]));
        }
        return innerTags;
    }

    /**
     * This method tests {@link DiscussionNoteData#getAllTags(java.util.Locale)}.
     */
    @Test
    public void testGetAllTags() {
        // NullPointerChecks
        Assert.assertEquals(new DiscussionNoteData().getAllTags(Locale.ENGLISH).length, 0);
        Assert.assertEquals(new DiscussionNoteData().getAllTags(null).length, 0);

        String[] tags = { "a", "aB", "cc", "dd", "elf", "drei", "vier", "z", "A", "Z" };
        DiscussionNoteData parent = new DiscussionNoteData();
        parent.setTags(generateTags(tags));
        for (int i = 0; i < RandomUtils.nextInt(20) + 10; i++) {
            DiscussionNoteData comment = new DiscussionNoteData();
            comment.setTags(generateTags(tags));
            parent.getComments().add(comment);
        }
        String[] allTags = parent.getAllTags(Locale.ENGLISH);
        Assert.assertTrue(allTags.length <= tags.length);
        Collator collator = Collator.getInstance(Locale.ENGLISH);
        for (int i = 0; i < allTags.length - 1; i++) {
            Assert.assertTrue(collator.compare(allTags[i], allTags[i + 1]) < 0);
        }
        Assert.assertTrue(collator.compare(allTags[0], allTags[allTags.length - 1]) < 0);
        // assert no doubles
        HashSet<String> predefinedTags = new HashSet<String>();
        predefinedTags.addAll(Arrays.asList(tags));
        HashSet<String> foundTags = new HashSet<String>();
        for (int i = 0; i < allTags.length; i++) {
            String tag = allTags[i];
            Assert.assertTrue(predefinedTags.contains(tag));
            Assert.assertFalse(foundTags.contains(tag));
            foundTags.add(tag);
        }
    }
}
