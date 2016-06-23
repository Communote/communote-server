package com.communote.server.core.tag.category;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface TagCategoryManagement {

    /**
     * 
     */
    public com.communote.server.model.tag.GlobalTagCategory createGlobalTagCategory(
            com.communote.server.core.vo.tag.GlobalTagCategoryVO category)
            throws com.communote.server.core.tag.category.TagCategoryAlreadyExistsException;

    /**
     * 
     */
    public com.communote.server.model.tag.GlobalTagCategory findGlobalTagCategoryByName(
            String name);

    /**
     * 
     */
    public com.communote.server.model.tag.GlobalTagCategory findGlobalTagCategoryByPrefix(
            String prefix);

    /**
     * 
     */
    public java.util.List<com.communote.server.model.tag.CategorizedTag> getCategorizedTags(
            Long categoryId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException;

    /**
     * 
     */
    public com.communote.server.model.tag.GlobalTagCategory updateGlobalTagCategory(
            Long categoryId,
            com.communote.server.core.vo.tag.GlobalTagCategoryVO globalTagCategoryVO)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.core.tag.category.TagCategoryAlreadyExistsException;

    /**
     * 
     */
    public void deleteTagCategory(Long categoryId);

    /**
     * 
     */
    public com.communote.server.model.tag.CategorizedTag createCategorizedTag(
            com.communote.server.core.vo.tag.CategorizedTagVO categorizedTagVO,
            Long categoryId, Integer index)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.core.tag.category.CategorizedTagAlreadyExists;

    /**
     * 
     */
    public com.communote.server.model.tag.CategorizedTag updateCategorizedTag(
            Long tagId,
            com.communote.server.core.vo.tag.CategorizedTagVO categorizedTagVO)
            throws com.communote.server.core.tag.category.CategorizedTagAlreadyExists,
            com.communote.server.core.tag.category.CategorizedTagNotFoundException;

    /**
     * 
     */
    public void deleteCategorizedTag(Long tagId);

    /**
     * 
     */
    public void changeCategorizedTagIndex(Long tagId, Integer newIndex)
            throws com.communote.server.core.tag.category.CategorizedTagNotFoundException;

    /**
     * 
     */
    public com.communote.server.model.blog.Blog assignGlobalCategoryToBlog(
            Long categoryId, Long blogId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.core.tag.category.TagCategoryAlreadyAssignedException,
            com.communote.server.api.core.blog.BlogNotFoundException;

    /**
     * 
     */
    public void assignGlobalCategoryToAllBlogs(Long categoryId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException;

    /**
     * 
     */
    public void removeTagCategoryFromBlog(Long categoryId, Long blogId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.api.core.blog.BlogNotFoundException;

}
