package com.communote.server.service.tag;

import static com.communote.server.api.core.tag.TagStoreType.Types.BLOG;
import static com.communote.server.api.core.tag.TagStoreType.Types.ENTITY;
import static com.communote.server.api.core.tag.TagStoreType.Types.NOTE;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.persistence.tag.DefaultTagStore;


/**
 * Test for {@link TagStoreManagement}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TagStoreManagementTest {

    private final TagStoreManagement tagStoreManagement = new TagStoreManagement();
    private final DefaultTagStore lowPriorityTagStore = new DefaultTagStore("low", 1001, false,
            BLOG);
    private final DefaultTagStore midPriorityTagStore = new DefaultTagStore("mid", 1002, false,
            BLOG);
    private final DefaultTagStore highPriorityTagStore = new DefaultTagStore("high", 1003, false,
            BLOG);
    private final DefaultTagStore sameHighPriorityTagStore = new DefaultTagStore("high2", 1003,
            false,
            BLOG);

    /**
     * This tests that the correct order is preserved, when inserting and retrieving TagStores.
     */
    @Test
    public void testCorrectPriority() {
        Assert.assertEquals(tagStoreManagement.getTagStore(BLOG).getTagStoreId(),
                Types.BLOG.getDefaultTagStoreId());
        Assert.assertEquals(tagStoreManagement.getTagStore(NOTE).getTagStoreId(),
                Types.NOTE.getDefaultTagStoreId());
        Assert.assertEquals(tagStoreManagement.getTagStore(ENTITY).getTagStoreId(),
                Types.ENTITY.getDefaultTagStoreId());
        tagStoreManagement.addTagStore(lowPriorityTagStore);
        Assert.assertEquals(tagStoreManagement.getTagStore(BLOG).getTagStoreId(),
                lowPriorityTagStore.getTagStoreId());
        tagStoreManagement.addTagStore(midPriorityTagStore);
        Assert.assertEquals(tagStoreManagement.getTagStore(BLOG).getTagStoreId(),
                midPriorityTagStore.getTagStoreId());
        tagStoreManagement.addTagStore(highPriorityTagStore);
        Assert.assertEquals(tagStoreManagement.getTagStore(BLOG).getTagStoreId(),
                highPriorityTagStore.getTagStoreId());
        tagStoreManagement.addTagStore(sameHighPriorityTagStore);
        // a tag store with same priority should not override an existing tag store
        Assert.assertEquals(tagStoreManagement.getTagStore(BLOG).getTagStoreId(),
                highPriorityTagStore.getTagStoreId());
        tagStoreManagement.removeTagStore(highPriorityTagStore);
        Assert.assertEquals(tagStoreManagement.getTagStore(BLOG).getTagStoreId(),
                sameHighPriorityTagStore.getTagStoreId());
        tagStoreManagement.removeTagStore(sameHighPriorityTagStore);
        Assert.assertEquals(tagStoreManagement.getTagStore(BLOG).getTagStoreId(),
                midPriorityTagStore.getTagStoreId());
        tagStoreManagement.removeTagStore(midPriorityTagStore);
        Assert.assertEquals(tagStoreManagement.getTagStore(BLOG).getTagStoreId(),
                lowPriorityTagStore.getTagStoreId());
        tagStoreManagement.removeTagStore(lowPriorityTagStore);
        Assert.assertEquals(tagStoreManagement.getTagStore(BLOG).getTagStoreId(),
                Types.BLOG.getDefaultTagStoreId());
    }

    /**
     * Tests
     * {@link TagStoreManagement#getTagStore(String, com.communote.server.api.core.tag.TagStoreType)}
     */
    @Test
    public void testGetTagStore() {
        DefaultTagStore tagStore = new DefaultTagStore(UUID.randomUUID().toString(), NOTE);
        Assert.assertNull(tagStoreManagement.getTagStore(tagStore.getTagStoreId(), null));
        Assert.assertNotNull(tagStoreManagement.getTagStore(tagStore.getTagStoreId(), NOTE));
        tagStoreManagement.addTagStore(tagStore);
        Assert.assertEquals(tagStoreManagement.getTagStore(tagStore.getTagStoreId(), null)
                .getTagStoreId(),
                tagStore.getTagStoreId());
        Assert.assertEquals(tagStoreManagement.getTagStore(tagStore.getTagStoreId(), NOTE)
                .getTagStoreId(),
                tagStore.getTagStoreId());
    }
}
