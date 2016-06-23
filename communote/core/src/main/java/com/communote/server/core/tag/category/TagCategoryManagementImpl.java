package com.communote.server.core.tag.category;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.Hibernate;

import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.core.tag.category.CategorizedTagAlreadyExists;
import com.communote.server.core.tag.category.CategorizedTagNotFoundException;
import com.communote.server.core.tag.category.CategoryNameAlreadyExistsException;
import com.communote.server.core.tag.category.CategoryPrefixAlreadyExistsException;
import com.communote.server.core.tag.category.TagCategoryAlreadyAssignedException;
import com.communote.server.core.tag.category.TagCategoryManagement;
import com.communote.server.core.tag.category.TagCategoryManagementBase;
import com.communote.server.core.tag.category.TagCategoryManagementException;
import com.communote.server.core.tag.category.TagCategoryNotFoundException;
import com.communote.server.core.vo.tag.CategorizedTagVO;
import com.communote.server.core.vo.tag.GlobalTagCategoryVO;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.tag.AbstractTagCategory;
import com.communote.server.model.tag.CategorizedTag;
import com.communote.server.model.tag.GlobalTagCategory;


/**
 * The Class TagCategoryManagementImpl offers methods to manage tag categories and categorized tags.
 * 
 * @see TagCategoryManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagCategoryManagementImpl extends TagCategoryManagementBase {

    /**
     * Checks if a tag already exists in a tag category tag list, throws an exception if the tag was
     * found.
     * 
     * @param tag
     *            the tag
     * @param category
     *            the category
     * @throws CategorizedTagAlreadyExists
     *             exception if the tag already exists.
     */
    private void checkTagAlreadyExists(CategorizedTag tag, AbstractTagCategory category)
            throws CategorizedTagAlreadyExists {
        List<CategorizedTag> tags = category.getTags();
        if (tags != null && tags.size() > 0) {
            for (CategorizedTag t : tags) {
                if (t.getName().equalsIgnoreCase(tag.getName())) {
                    throw new CategorizedTagAlreadyExists("tag '" + tag.getName()
                            + "' already exists on tag category '" + category.getPrefix() + "'");
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TagCategoryNotFoundException
     * @see TagCategoryManagement#assignGlobalCategoryToAllBlogs(Long)
     */
    @Override
    protected void handleAssignGlobalCategoryToAllBlogs(Long categoryId)
            throws TagCategoryNotFoundException {
        throw new UnsupportedOperationException("Since v1.1 not supported");
        // GlobalTagCategory category = loadGlobalTagCategory(categoryId);
        // List<Blog> blogs = getBlogDao().getBlogsWithoutTagCategory(categoryId);
        // if (blogs != null && blogs.size() > 0) {
        // for (Blog blog : blogs) {
        // blog.getTagCategories().add(category);
        // // getBlogDao().blogModified(blog);
        // blog.setLastModificationDate(new Timestamp(new Date().getTime()));
        // }
        // }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TagCategoryNotFoundException
     * @throws TagCategoryAlreadyAssignedException
     * @throws BlogNotFoundException
     * @see TagCategoryManagement#assignGlobalCategoryToBlog(Long, Long)
     */
    @Override
    protected Blog handleAssignGlobalCategoryToBlog(Long categoryId, Long blogId)
            throws TagCategoryNotFoundException, TagCategoryAlreadyAssignedException,
            BlogNotFoundException {
        throw new UnsupportedOperationException("Since v1.1 not supported");
        // GlobalTagCategory category = loadGlobalTagCategory(categoryId);
        // Blog blog = loadBlog(blogId);
        // if (blog.getTagCategories().contains(category)) {
        // throw new TagCategoryAlreadyAssignedException("global tag category '"
        // + category.getPrefix() + "' is already assigned to blog '"
        // + blog.getNameIdentifier() + "'");
        // } else {
        // blog.getTagCategories().add(category);
        // // getBlogDao().blogModified(blog);
        // blog.setLastModificationDate(new Timestamp(new Date().getTime()));
        // }
        // return blog;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws CategorizedTagNotFoundException
     * @see TagCategoryManagement#changeCategorizedTagIndex(Long, Integer)
     */
    @Override
    protected void handleChangeCategorizedTagIndex(Long tagId, Integer newIndex)
            throws CategorizedTagNotFoundException {
        CategorizedTag tag = loadCategorizedTag(tagId);
        if (tag.getCategory() != null) {
            int currentIndex = tag.getCategory().getTags().indexOf(tag);
            if (currentIndex >= 0) {
                if (currentIndex != newIndex) {
                    tag.getCategory().getTags().remove(currentIndex);
                    if (newIndex == null || newIndex > tag.getCategory().getTags().size()) {
                        newIndex = tag.getCategory().getTags().size() - 1;
                    }
                    if (newIndex < 0) {
                        newIndex = 0;
                    }
                    tag.getCategory().getTags().add(newIndex, tag);
                    getAbstractTagCategoryDao().update(tag.getCategory());
                }
            } else {
                throw new TagCategoryManagementException("tag with id '" + tagId
                        + "' was not found in its parent category tag list");
            }
        } else {
            throw new TagCategoryManagementException("tag with id '" + tagId + "' has no category");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TagCategoryNotFoundException
     * @throws CategorizedTagAlreadyExists
     * @see TagCategoryManagement#createCategorizedTag(CategorizedTagVO, Long, Integer)
     */
    @Override
    protected CategorizedTag handleCreateCategorizedTag(CategorizedTagVO categorizedTagVO,
            Long categoryId, Integer index) throws TagCategoryNotFoundException,
            CategorizedTagAlreadyExists {
        AbstractTagCategory category = loadTagCategory(categoryId);
        CategorizedTag tag = getCategorizedTagDao().categorizedTagVOToEntity(categorizedTagVO);
        checkTagAlreadyExists(tag, category);
        tag.setCategory(category);
        getCategorizedTagDao().create(tag);
        if (index == null || index < 0 || index >= category.getTags().size()) {
            category.getTags().add(tag);
        } else {
            category.getTags().add(index, tag);
        }
        getAbstractTagCategoryDao().update(category);
        return tag;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws CategoryNameAlreadyExistsException
     * @throws CategoryPrefixAlreadyExistsException
     * @see TagCategoryManagement#createGlobalTagCategory(GlobalTagCategoryVO)
     */
    @Override
    protected GlobalTagCategory handleCreateGlobalTagCategory(GlobalTagCategoryVO category)
            throws CategoryNameAlreadyExistsException, CategoryPrefixAlreadyExistsException {
        if (getGlobalTagCategoryDao().findByName(category.getName()) != null) {
            throw new CategoryNameAlreadyExistsException("a global tag category with name '"
                    + category.getName() + "' already exists");
        }
        if (getGlobalTagCategoryDao().findByPrefix(category.getPrefix()) != null) {
            throw new CategoryPrefixAlreadyExistsException("a global tag category with prefix '"
                    + category.getPrefix() + "' already exists");
        }
        GlobalTagCategory entity = getGlobalTagCategoryDao().globalTagCategoryVOToEntity(category);
        getGlobalTagCategoryDao().create(entity);
        return entity;
    }

    /**
     * {@inheritDoc}
     * 
     * @see TagCategoryManagement#deleteCategorizedTag(Long)
     */
    @Override
    protected void handleDeleteCategorizedTag(Long tagId) {
        CategorizedTag tag = getCategorizedTagDao().load(tagId);
        if (tag != null) {
            List<Note> taggedItems = getNoteDao().getNotesByTag(tagId);
            for (Note item : taggedItems) {
                item.getTags().remove(tag);
                getNoteDao().update(item);
            }
            AbstractTagCategory category = tag.getCategory();
            category.getTags().remove(tag);
            tag.setCategory(null);
            getAbstractTagCategoryDao().update(category);
            getCategorizedTagDao().remove(tag);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see TagCategoryManagement#deleteTagCategory(Long)
     */
    @Override
    protected void handleDeleteTagCategory(Long categoryId) {
        throw new UnsupportedOperationException("Since v1.1 not supported");
        // AbstractTagCategory category = getAbstractTagCategoryDao().load(categoryId);
        // if (category != null) {
        // List<Blog> blogs = getBlogDao().getBlogsByTagCategory(categoryId);
        // if (blogs != null && blogs.size() > 0) {
        // for (Blog blog : blogs) {
        // blog.getTagCategories().remove(category);
        // // getBlogDao().blogModified(blog);
        // blog.setLastModificationDate(new Timestamp(new Date().getTime()));
        // }
        // }
        // if (category.getTags() != null && category.getTags().size() > 0) {
        // for (CategorizedTag tag : category.getTags()) {
        // List<Note> taggedItems = getNoteDao().getNotesByTag(tag.getId());
        // for (Note item : taggedItems) {
        // item.getTags().remove(tag);
        // getNoteDao().update(item);
        // }
        // }
        // getCategorizedTagDao().remove(category.getTags());
        // }
        // getAbstractTagCategoryDao().remove(category);
        // }
    }

    /**
     * {@inheritDoc}
     * 
     * @see TagCategoryManagement#findGlobalTagCategoryByName(String)
     */
    @Override
    protected GlobalTagCategory handleFindGlobalTagCategoryByName(String name) {
        return getGlobalTagCategoryDao().findByName(name);
    }

    /**
     * {@inheritDoc}
     * 
     * @see TagCategoryManagement#findGlobalTagCategoryByPrefix(String)
     */
    @Override
    protected GlobalTagCategory handleFindGlobalTagCategoryByPrefix(String prefix) {
        return getGlobalTagCategoryDao().findByPrefix(prefix);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TagCategoryNotFoundException
     * @see TagCategoryManagement#getCategorizedTags(Long)
     */
    @Override
    protected List<CategorizedTag> handleGetCategorizedTags(Long categoryId)
            throws TagCategoryNotFoundException {
        AbstractTagCategory category = loadTagCategory(categoryId);
        if (!Hibernate.isInitialized(category.getTags())) {
            Hibernate.initialize(category.getTags());
        }
        return new LinkedList<CategorizedTag>(category.getTags());
    }

    /**
     * {@inheritDoc}
     * 
     * @throws BlogNotFoundException
     * @throws TagCategoryNotFoundException
     * @see TagCategoryManagement#removeTagCategoryFromBlog(Long, Long)
     */
    @Override
    protected void handleRemoveTagCategoryFromBlog(Long categoryId, Long blogId)
            throws BlogNotFoundException, TagCategoryNotFoundException {
        throw new UnsupportedOperationException("Since v1.1 not supported");
        // AbstractTagCategory category = loadTagCategory(categoryId);
        // Blog blog = loadBlog(blogId);
        // if (blog.getTagCategories() != null && blog.getTagCategories().contains(category)) {
        // blog.getTagCategories().remove(category);
        // // getBlogDao().blogModified(blog);
        // blog.setLastModificationDate(new Timestamp(new Date().getTime()));
        // }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws CategorizedTagNotFoundException
     * @throws CategorizedTagAlreadyExists
     * @see TagCategoryManagement#updateCategorizedTag(Long, CategorizedTagVO)
     */
    @Override
    protected CategorizedTag handleUpdateCategorizedTag(Long tagId,
            CategorizedTagVO categorizedTagVO) throws CategorizedTagNotFoundException,
            CategorizedTagAlreadyExists {
        CategorizedTag tag = loadCategorizedTag(tagId);
        if (!tag.getName().equalsIgnoreCase(categorizedTagVO.getName())) {
            checkTagAlreadyExists(
                    getCategorizedTagDao().categorizedTagVOToEntity(categorizedTagVO), tag
                            .getCategory());
        }
        getCategorizedTagDao().categorizedTagVOToEntity(categorizedTagVO, tag, true);
        return tag;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws CategoryNameAlreadyExistsException
     * @see TagCategoryManagement#updateGlobalTagCategory(Long, GlobalTagCategoryVO)
     */
    @Override
    protected GlobalTagCategory handleUpdateGlobalTagCategory(Long categoryId,
            GlobalTagCategoryVO globalTagCategoryVO) throws TagCategoryNotFoundException,
            CategoryNameAlreadyExistsException, CategoryPrefixAlreadyExistsException {
        GlobalTagCategory category = loadGlobalTagCategory(categoryId);
        if (!category.getName().equalsIgnoreCase(globalTagCategoryVO.getName())
                && getGlobalTagCategoryDao().findByName(globalTagCategoryVO.getName()) != null) {
            throw new CategoryNameAlreadyExistsException("a global tag category with name '"
                    + globalTagCategoryVO.getName() + "' already exists");
        }
        if (!category.getPrefix().equalsIgnoreCase(globalTagCategoryVO.getPrefix())
                && getGlobalTagCategoryDao().findByPrefix(globalTagCategoryVO.getPrefix()) != null) {
            throw new CategoryPrefixAlreadyExistsException("a global tag category with prefix '"
                    + globalTagCategoryVO.getPrefix() + "' already exists");
        }

        getGlobalTagCategoryDao().globalTagCategoryVOToEntity(globalTagCategoryVO, category, true);
        getGlobalTagCategoryDao().update(category);
        return category;
    }

    /**
     * Loads a blog by its id and throws an exception if the blog was not found.
     * 
     * @param blogId
     *            the blog id
     * @return the blog
     * @throws BlogNotFoundException
     *             exception if the blog was not found.
     */
    private Blog loadBlog(Long blogId) throws BlogNotFoundException {
        Blog blog = getBlogDao().load(blogId);
        if (blog == null) {
            throw new BlogNotFoundException("blog with id '" + blogId + "' was not found", blogId,
                    null);
        }
        return blog;
    }

    /**
     * Load a categorized tag and throws an exception if the tag was not found.
     * 
     * @param tagId
     *            the tag id
     * @return the categorized tag
     * @throws CategorizedTagNotFoundException
     *             exception if the tag was not found
     */
    private CategorizedTag loadCategorizedTag(Long tagId) throws CategorizedTagNotFoundException {
        CategorizedTag tag = getCategorizedTagDao().load(tagId);
        if (tag == null) {
            throw new CategorizedTagNotFoundException("categorized tag with id '" + tagId
                    + "' was not found");
        }
        return tag;
    }

    /**
     * Loads a global tag category and throws an exception if the tag category was not found.
     * 
     * @param categoryId
     *            the category id
     * @return the global tag category
     * @throws TagCategoryNotFoundException
     *             the tag category not found exception
     */
    private GlobalTagCategory loadGlobalTagCategory(Long categoryId)
            throws TagCategoryNotFoundException {
        GlobalTagCategory category = getGlobalTagCategoryDao().load(categoryId);
        if (category == null) {
            throw new TagCategoryNotFoundException("global tag category with id '" + categoryId
                    + "' was not found");
        }
        return category;
    }

    /**
     * Loads a tag category and throws an exception if the tag category was not found.
     * 
     * @param categoryId
     *            the category id
     * @return the abstract tag category
     * @throws TagCategoryNotFoundException
     *             the tag category not found exception
     */
    private AbstractTagCategory loadTagCategory(Long categoryId)
            throws TagCategoryNotFoundException {
        AbstractTagCategory category = getAbstractTagCategoryDao().load(categoryId);
        if (category == null) {
            throw new TagCategoryNotFoundException("tag category with id '" + categoryId
                    + "' was not found");
        }
        return category;
    }

}
