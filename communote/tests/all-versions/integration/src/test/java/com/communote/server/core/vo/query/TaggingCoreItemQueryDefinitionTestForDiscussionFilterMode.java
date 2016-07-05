package com.communote.server.core.vo.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * This class contains tests especially for {@link DiscussionFilterMode}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TaggingCoreItemQueryDefinitionTestForDiscussionFilterMode extends
CommunoteIntegrationTest {
    private Long noDisussionNoteId;
    private Long disussionParentNoteId;
    private Long disussionChildNoteId;
    private Long blogId;

    @Autowired
    private QueryManagement queryManagement;

    /**
     * @param discussionFilterMode
     *            The mode to use.
     * @return {@link NoteQueryParameters}
     */
    private PageableList<SimpleNoteListItem> getNotes(DiscussionFilterMode discussionFilterMode) {
        NoteQueryParameters noteQueryInstance = new NoteQueryParameters();
        TaggingCoreItemUTPExtension filter = new TaggingCoreItemUTPExtension();
        filter.setBlogFilter(new Long[] { blogId });
        noteQueryInstance.setTypeSpecificExtension(filter);
        noteQueryInstance.setDiscussionFilterMode(discussionFilterMode);
        return queryManagement.query(new NoteQuery(), noteQueryInstance);
    }

    /**
     * This setups a test environment with some notes/discussions.
     *
     * @throws Exception
     *             in case the setup failed
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(false, false, user);
        blogId = blog.getId();
        noDisussionNoteId = TestUtils.createAndStoreCommonNote(blog, user.getId(), "no discussion");
        disussionParentNoteId = TestUtils.createAndStoreCommonNote(blog, user.getId(),
                "discussion parent");
        disussionChildNoteId = TestUtils.createAndStoreCommonNote(blog, user.getId(),
                "discussion child", disussionParentNoteId);
    }

    /**
     * Test for {@link DiscussionFilterMode#ALL}
     */
    @Test
    public void testAllDiscussionFilterMode() {
        PageableList<SimpleNoteListItem> result = getNotes(DiscussionFilterMode.ALL);
        Assert.assertEquals(result.size(), 3);
    }

    /**
     * Test for {@link DiscussionFilterMode#IS_DISCUSSION}
     */
    @Test
    public void testIsDiscussionFilterMode() {
        PageableList<SimpleNoteListItem> result = getNotes(DiscussionFilterMode.IS_DISCUSSION);
        Assert.assertEquals(result.size(), 2);
        // sorted by date and ID (descending)
        Assert.assertEquals(result.get(0).getId(), disussionChildNoteId);
        Assert.assertEquals(result.get(1).getId(), disussionParentNoteId);
    }

    /**
     * Test for {@link DiscussionFilterMode#IS_NO_DISCUSSION}
     */
    @Test
    public void testIsNoDiscussionFilterMode() {
        PageableList<SimpleNoteListItem> result = getNotes(DiscussionFilterMode.IS_NO_DISCUSSION);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getId(), noDisussionNoteId);
    }

    /**
     * Test for {@link DiscussionFilterMode#IS_DISCUSSION_ROOT}
     */
    @Test
    public void testIsRootOfDiscussionFilterMode() {
        PageableList<SimpleNoteListItem> result = getNotes(DiscussionFilterMode.IS_DISCUSSION_ROOT);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getId(), disussionParentNoteId);
    }

}
