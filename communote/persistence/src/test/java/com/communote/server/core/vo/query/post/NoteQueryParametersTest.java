package com.communote.server.core.vo.query.post;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.core.vo.query.DiscussionFilterMode;
import com.communote.server.core.vo.query.post.NoteQueryParameters;


/**
 * Unit test for {@link NoteQueryParameters}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteQueryParametersTest {

    /**
     * Test the {@link NoteQueryParameters#isDiscussionIndependentFilter()} functionality.
     */
    @Test
    public void testIsDiscussionIndependentFilter() {
        // create the query parameters
        NoteQueryParameters noteQueryParameters = new NoteQueryParameters();

        // check the defaults
        Assert.assertFalse(noteQueryParameters.isFavorites());
        Assert.assertFalse(noteQueryParameters.isFollowingFeedAndUnfiltered());
        Assert.assertTrue(noteQueryParameters.isDiscussionIndependentFilter());
        Assert.assertFalse(noteQueryParameters.isNotificationFeed());
        Assert.assertFalse(noteQueryParameters.isDiscussionDependentRootNotesFilter());

        noteQueryParameters.setDiscussionFilterMode(DiscussionFilterMode.IS_ROOT);
        Assert.assertFalse(noteQueryParameters.isDiscussionDependentRootNotesFilter());

        //
        // set a blog/topic id or more ids, isDiscussionIndependentFilter should return true in all
        // cases
        //

        noteQueryParameters.getTypeSpecificExtension().setBlogId(12L);
        Assert.assertTrue(noteQueryParameters.isDiscussionIndependentFilter());
        Assert.assertFalse(noteQueryParameters.isDiscussionDependentRootNotesFilter());

        noteQueryParameters.getTypeSpecificExtension().setBlogAliasFilter(new String[] { "test" });
        Assert.assertTrue(noteQueryParameters.isDiscussionIndependentFilter());
        Assert.assertFalse(noteQueryParameters.isDiscussionDependentRootNotesFilter());

        noteQueryParameters.getTypeSpecificExtension().setBlogFilter(new Long[] { 13L, 14l });
        Assert.assertTrue(noteQueryParameters.isDiscussionIndependentFilter());
        Assert.assertFalse(noteQueryParameters.isDiscussionDependentRootNotesFilter());

        //
        // Setting favorites should give a false
        //
        noteQueryParameters.setFavorites(Boolean.TRUE);
        Assert.assertFalse(noteQueryParameters.isDiscussionIndependentFilter());
        Assert.assertTrue(noteQueryParameters.isDiscussionDependentRootNotesFilter());

        // reset favorites
        noteQueryParameters.setFavorites(Boolean.FALSE);
        Assert.assertTrue(noteQueryParameters.isDiscussionIndependentFilter());
        Assert.assertFalse(noteQueryParameters.isDiscussionDependentRootNotesFilter());

        //
        // Setting follow should give a false
        //
        noteQueryParameters.setRetrieveOnlyFollowedItems(true);
        Assert.assertFalse(noteQueryParameters.isDiscussionIndependentFilter());
        Assert.assertTrue(noteQueryParameters.isDiscussionDependentRootNotesFilter());

        // reset follow
        noteQueryParameters.setRetrieveOnlyFollowedItems(false);
        Assert.assertTrue(noteQueryParameters.isDiscussionIndependentFilter());
        Assert.assertFalse(noteQueryParameters.isDiscussionDependentRootNotesFilter());
        //
        // Setting tags should give a false
        //
        noteQueryParameters.getTagIds().add(12l);
        Assert.assertFalse(noteQueryParameters.isDiscussionIndependentFilter());
        Assert.assertTrue(noteQueryParameters.isDiscussionDependentRootNotesFilter());

        // reset tag
        noteQueryParameters.getTagIds().clear();
        Assert.assertTrue(noteQueryParameters.isDiscussionIndependentFilter());
        Assert.assertFalse(noteQueryParameters.isDiscussionDependentRootNotesFilter());

        //
        // Setting a lower tag data should give a true
        //
        noteQueryParameters.setLowerTagDate(new Date());
        Assert.assertTrue(noteQueryParameters.isDiscussionIndependentFilter());
        Assert.assertFalse(noteQueryParameters.isDiscussionDependentRootNotesFilter());

    }
}
