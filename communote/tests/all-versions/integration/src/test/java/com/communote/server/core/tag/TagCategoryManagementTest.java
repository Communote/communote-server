package com.communote.server.core.tag;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.blog.BlogManagementTest;
import com.communote.server.core.tag.category.TagCategoryAlreadyAssignedException;
import com.communote.server.core.tag.category.TagCategoryManagement;
import com.communote.server.core.tag.category.TagCategoryNotFoundException;
import com.communote.server.core.vo.tag.CategorizedTagVO;
import com.communote.server.core.vo.tag.GlobalTagCategoryVO;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.tag.CategorizedTag;
import com.communote.server.model.tag.GlobalTagCategory;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Testing the functionality of {@link TagCategoryManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated as long tag categories are not used
 */
@Deprecated
public class TagCategoryManagementTest {

    /** The Logger. */
    private final static Logger LOG = Logger.getLogger(TagCategoryManagementTest.class);

    /** The Constant GLOBAL_TAG_CATEGORY1_PREFIX defines the prefixes for the first tag category. */
    public final static String GLOBAL_TAG_CATEGORY1_PREFIX = "one";

    /** The Constant GLOBAL_TAG_CATEGORY2_PREFIX defines the prefixes for the second tag category. */
    public final static String GLOBAL_TAG_CATEGORY2_PREFIX = "two";

    /** The Constant GLOBAL_TAG_CATEGORY3_PREFIX defines the prefixes for the third tag category. */
    public final static String GLOBAL_TAG_CATEGORY3_PREFIX = "three";

    /**
     * Assign tag categories to blogs.
     */
    @Test(groups = { "globalTagCategories" }, dependsOnMethods = { "updateCategorizedTags" })
    public void assignTagCategoriesToBlogs() throws Exception {
        TagCategoryManagement tcm = ServiceLocator.findService(TagCategoryManagement.class);
        // assign one to all blogs
        GlobalTagCategory categoryOne = loadGlobalTagCategory(GLOBAL_TAG_CATEGORY1_PREFIX);
        try {
            tcm.assignGlobalCategoryToAllBlogs(categoryOne.getId());
        } catch (TagCategoryNotFoundException e) {
            Assert.fail("assign failed", e);
        }

        // assign three to all blogs
        GlobalTagCategory categoryThree = loadGlobalTagCategory(GLOBAL_TAG_CATEGORY3_PREFIX);
        try {
            tcm.assignGlobalCategoryToAllBlogs(categoryThree.getId());
        } catch (TagCategoryNotFoundException e) {
            Assert.fail("assign failed", e);
        }

        // assign two to a single blog
        Blog blog = loadBlog(BlogManagementTest.TEST_BLOG_IDENTIFIER);
        GlobalTagCategory categoryTwo = loadGlobalTagCategory(GLOBAL_TAG_CATEGORY2_PREFIX);
        try {
            tcm.assignGlobalCategoryToBlog(categoryTwo.getId(), blog.getId());
        } catch (TagCategoryNotFoundException e) {
            Assert.fail("assign failed", e);
        } catch (TagCategoryAlreadyAssignedException e) {
            Assert.fail("assign failed", e);
        } catch (BlogNotFoundException e) {
            Assert.fail("assign failed", e);
        }

        // try to assign three to a single blog, should fail
        try {
            tcm.assignGlobalCategoryToBlog(categoryThree.getId(), blog.getId());
            Assert.fail("can not assign category three to blog '" + blog.getId()
                    + "', should be already assigned");
        } catch (TagCategoryNotFoundException e) {
            Assert.fail("assign failed", e);
        } catch (TagCategoryAlreadyAssignedException e) {
        } catch (BlogNotFoundException e) {
            Assert.fail("assign failed", e);
        }
    }

    @BeforeClass
    protected void beforeClassPutGlobalClientInThreadLocal() throws Exception {
        ClientTO client = ServiceLocator.findService(ClientRetrievalService.class).findClient(
                ClientHelper.getGlobalClientId());
        ClientAndChannelContextHolder.setClient(client);
    }

    /**
     * Checks if the given tag category entity equals to the tag category value object.
     *
     * @param expected
     *            the expected values
     * @param actual
     *            the actual values
     */
    private void checkTagCategoryData(GlobalTagCategoryVO expected, GlobalTagCategory actual) {
        ToStringBuilder s1 = new ToStringBuilder(ToStringStyle.MULTI_LINE_STYLE)
                .append("description", actual.getDescription()).append("name", actual.getName())
                .append("prefix", actual.getPrefix());
        boolean result = true;
        result = result && StringUtils.equals(expected.getDescription(), actual.getDescription());
        result = result && StringUtils.equals(expected.getName(), actual.getName());
        result = result && StringUtils.equals(expected.getPrefix(), actual.getPrefix());
        Assert.assertEquals(
                result,
                true,
                "value object and entity not equal, entity: "
                        + s1.toString()
                        + ", source vo: "
                        + ToStringBuilder.reflectionToString(expected,
                                ToStringStyle.MULTI_LINE_STYLE));
    }

    /**
     * Compares the data of an tag entity and its value object.
     *
     * @param expected
     *            the expected values
     * @param actual
     *            the actual values
     */
    private void checkTagData(CategorizedTagVO expected, CategorizedTag actual) {
        Assert.assertEquals(expected.getName(), actual.getName(), "tag names not equal");
        Assert.assertEquals(expected.getName().toLowerCase(), actual.getTagStoreTagId(),
                "lower names not equal");
    }

    /**
     * Checks if the given tag is at the expected position in the tag list.
     *
     * @param tags
     *            the tag list
     * @param tag
     *            the tag
     * @param expectedIndex
     *            the expected index of the tag
     */
    private void checkTagIndex(List<CategorizedTag> tags, CategorizedTag tag, Integer expectedIndex) {
        Assert.assertTrue(tags.size() > expectedIndex,
                "tag list has not the expected size, actual size is: " + tags.size());
        Assert.assertTrue(tags.get(expectedIndex).equals(tag), "tag has not the expected index '"
                + expectedIndex + "'in the tag list");
    }

    /**
     * Preparations for the test
     *
     * @throws Exception
     *             in case the setup failed
     */
    @BeforeClass
    public void classInitialze() throws Exception {
        // check for test blogs, if not existing create them
        BlogManagement bm = ServiceLocator.findService(BlogManagement.class);
        Blog testBlog = bm.findBlogByIdentifier(BlogManagementTest.TEST_BLOG_IDENTIFIER);
        if (testBlog == null) {
            bm.createBlog(new BlogManagementTest().generateBlogTO());
        }
    }

    /**
     * Cleanup method.
     */
    @AfterClass
    protected void cleanUp() {
        BlogManagement bm = ServiceLocator.findService(BlogManagement.class);
        // remove the created test blogs
        try {
            Blog testBlog = bm.findBlogByIdentifier(BlogManagementTest.TEST_BLOG_IDENTIFIER);
            if (testBlog != null) {
                bm.deleteBlog(testBlog.getId(), null);
            }
        } catch (Exception e) {
            LOG.error("Clean up after test failed. ", e);
        }

    }

    /**
     * Creates categorized tags.
     *
     * @param prefix
     *            the prefix of the tag category
     * @param names
     *            the list of tags which will be created
     * @throws Exception
     *             an exception occurred
     */
    @Test(groups = { "globalTagCategories" }, dependsOnMethods = { "createGlobalTagCategory" }, dataProvider = "categorizedTags")
    public void createCategorizedTags(String prefix, String[] names) throws Exception {
        TagCategoryManagement tm = ServiceLocator.findService(TagCategoryManagement.class);
        GlobalTagCategory category = tm.findGlobalTagCategoryByPrefix(prefix);
        Assert.assertNotNull(category, "category with prefix " + prefix + " not found");
        for (String name : names) {
            LOG.debug("create tag '" + prefix + ":" + name + "'");
            CategorizedTagVO tagVO = new CategorizedTagVO(name);
            CategorizedTag tag = tm.createCategorizedTag(tagVO, category.getId(), null);
            checkTagData(tagVO, tag);
        }
    }

    /**
     * Creates global tag categories.
     *
     * @param categoryVO
     *            the category value objects
     * @throws Exception
     *             an exception occurred
     */
    @Test(groups = { "globalTagCategories" }, dataProvider = "globalTagCategories")
    public void createGlobalTagCategory(GlobalTagCategoryVO categoryVO) throws Exception {
        TagCategoryManagement tm = ServiceLocator.findService(TagCategoryManagement.class);
        LOG.debug("create global tag category: " + categoryVO.getPrefix());
        GlobalTagCategory category = tm.createGlobalTagCategory(categoryVO);
        checkTagCategoryData(categoryVO, category);
    }

    /**
     * Delete categorized tags.
     *
     * @throws Exception
     *             an exception occurred
     */
    @Test(groups = { "globalTagCategories" }, dependsOnMethods = { "assignTagCategoriesToBlogs" })
    public void deleteCategorizedTags() throws Exception {
        TagCategoryManagement tm = ServiceLocator.findService(TagCategoryManagement.class);
        GlobalTagCategory category = loadGlobalTagCategory(GLOBAL_TAG_CATEGORY3_PREFIX);
        List<CategorizedTag> tags = tm.getCategorizedTags(category.getId());
        LOG.debug("remove " + tags.get(0).getName() + " from category " + category.getPrefix());
        tm.deleteCategorizedTag(tags.get(0).getId());
    }

    /**
     * Delete global tag category.
     *
     * @throws Exception
     *             an exception occurred
     */
    @Test(groups = { "globalTagCategories" }, dependsOnMethods = { "deleteCategorizedTags" })
    public void deleteGlobalTagCategory() throws Exception {
        TagCategoryManagement tm = ServiceLocator.findService(TagCategoryManagement.class);
        GlobalTagCategory category = loadGlobalTagCategory(GLOBAL_TAG_CATEGORY3_PREFIX);
        LOG.debug("remove category " + category.getPrefix());
        tm.deleteTagCategory(category.getId());
    }

    /**
     * Gets the categorized tags which should be created.
     *
     * @return the categorized tags
     */
    @DataProvider(name = "categorizedTags")
    public Object[][] getCategorizedTags() {
        return new Object[][] {
                { GLOBAL_TAG_CATEGORY1_PREFIX,
                        new String[] { "one", "two", "three", "four", "five", "six" } },
                { GLOBAL_TAG_CATEGORY2_PREFIX,
                        new String[] { "one", "two", "three", "four", "five", "six" } },
                { GLOBAL_TAG_CATEGORY3_PREFIX,
                        new String[] { "one", "two", "three", "four", "five", "six" } } };
    }

    /**
     * Gets the global tag categories which should be created.
     *
     * @return the global tag categories
     */
    @DataProvider(name = "globalTagCategories")
    public Object[][] getGlobalTagCategories() {
        return new Object[][] {
                { new GlobalTagCategoryVO("category_one", GLOBAL_TAG_CATEGORY1_PREFIX,
                        "category one description", false) },
                { new GlobalTagCategoryVO("category_two", GLOBAL_TAG_CATEGORY2_PREFIX,
                        "category two description", false) },
                { new GlobalTagCategoryVO("category_three", GLOBAL_TAG_CATEGORY3_PREFIX,
                        "category three description", false) } };
    }

    /**
     * Loads a blog.
     *
     * @param id
     *            the id
     * @return the blog
     */
    private Blog loadBlog(String id) throws Exception {
        Blog blog = ServiceLocator.findService(BlogManagement.class).findBlogByIdentifier(id);
        Assert.assertNotNull(blog, "blog not found with id '" + id + "'");
        return blog;
    }

    /**
     * Load global tag category.
     *
     * @param prefix
     *            the prefix
     * @return the global tag category
     */
    private GlobalTagCategory loadGlobalTagCategory(String prefix) {
        TagCategoryManagement tcm = ServiceLocator.findService(TagCategoryManagement.class);
        GlobalTagCategory category = tcm.findGlobalTagCategoryByPrefix(prefix);
        Assert.assertNotNull(category, "global tag category with prefix '" + prefix + "' not found");
        return category;
    }

    /**
     * Update categorized tags.
     *
     * @throws Exception
     *             the exception
     */
    @Test(groups = { "globalTagCategories" }, dependsOnMethods = { "createCategorizedTags" })
    public void updateCategorizedTags() throws Exception {
        TagCategoryManagement tm = ServiceLocator.findService(TagCategoryManagement.class);
        GlobalTagCategory categoryTwo = loadGlobalTagCategory(GLOBAL_TAG_CATEGORY2_PREFIX);
        List<CategorizedTag> tags = tm.getCategorizedTags(categoryTwo.getId());
        Assert.assertNotNull(tags, "tags can not be null");
        Assert.assertTrue(tags.size() > 2, "taglist must have at least three elements");

        CategorizedTag tag = tags.get(0);
        Assert.assertNotNull(tag, "tag can not be null");
        LOG.debug("rename tag " + tag.getName() + " to 'rename_first'");
        CategorizedTagVO categorizedTagVO = new CategorizedTagVO("rename_first");
        tag = tm.updateCategorizedTag(tag.getId(), categorizedTagVO);
        checkTagData(categorizedTagVO, tag);
        checkTagIndex(tm.getCategorizedTags(categoryTwo.getId()), tag, 0);

        tag = tags.get(1);
        Assert.assertNotNull(tag, "tag can not be null");
        LOG.debug("move tag " + tag.getName() + " to the end");
        LOG.debug("current tag order:");
        for (int i = 0; i < tags.size(); i++) {
            CategorizedTag t = tags.get(i);
            LOG.debug(i + ". " + t.getId() + " - " + categoryTwo.getPrefix() + ":" + t.getName());
        }
        tm.changeCategorizedTagIndex(tag.getId(), tags.size() - 1);
        tags = tm.getCategorizedTags(categoryTwo.getId());
        LOG.debug("new tag order:");
        for (int i = 0; i < tags.size(); i++) {
            CategorizedTag t = tags.get(i);
            LOG.debug(i + ". " + t.getId() + " - " + categoryTwo.getPrefix() + ":" + t.getName());
        }
        checkTagIndex(tags, tag, tags.size() - 1);
    }

    /**
     * Update global tag category.
     *
     * @throws Exception
     *             the exception
     */
    @Test(groups = { "globalTagCategories" }, dependsOnMethods = { "createCategorizedTags" })
    public void updateGlobalTagCategory() throws Exception {
        TagCategoryManagement tm = ServiceLocator.findService(TagCategoryManagement.class);
        GlobalTagCategory category = loadGlobalTagCategory(GLOBAL_TAG_CATEGORY1_PREFIX);
        GlobalTagCategoryVO newData = new GlobalTagCategoryVO("update_category_one", "one",
                "update test for category one", true);
        category = tm.updateGlobalTagCategory(category.getId(), newData);
        checkTagCategoryData(newData, category);
    }
}
