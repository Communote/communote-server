package com.communote.server.core.tag.category;

/**
 * <p>
 * Spring Service base class for
 * <code>com.communote.server.service.tag.category.TagCategoryManagement</code>, provides
 * access to all services and entities referenced by this service.
 * </p>
 * 
 * @see com.communote.server.core.tag.category.TagCategoryManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class TagCategoryManagementBase
        implements com.communote.server.core.tag.category.TagCategoryManagement {

    private com.communote.server.persistence.tag.TagDao tagDao;

    private com.communote.server.persistence.tag.GlobalTagCategoryDao globalTagCategoryDao;

    private com.communote.server.persistence.tag.CategorizedTagDao categorizedTagDao;

    private com.communote.server.persistence.tag.AbstractTagCategoryDao abstractTagCategoryDao;

    private com.communote.server.persistence.blog.NoteDao noteDao;

    private com.communote.server.persistence.blog.BlogDao blogDao;

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#assignGlobalCategoryToAllBlogs(Long)
     */
    public void assignGlobalCategoryToAllBlogs(Long categoryId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException {
        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.assignGlobalCategoryToAllBlogs(Long categoryId) - 'categoryId' can not be null");
        }
        try {
            this.handleAssignGlobalCategoryToAllBlogs(categoryId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.assignGlobalCategoryToAllBlogs(Long categoryId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#assignGlobalCategoryToBlog(Long,
     *      Long)
     */
    public com.communote.server.model.blog.Blog assignGlobalCategoryToBlog(
            Long categoryId, Long blogId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.core.tag.category.TagCategoryAlreadyAssignedException,
            com.communote.server.api.core.blog.BlogNotFoundException {
        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.assignGlobalCategoryToBlog(Long categoryId, Long blogId) - 'categoryId' can not be null");
        }
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.assignGlobalCategoryToBlog(Long categoryId, Long blogId) - 'blogId' can not be null");
        }
        try {
            return this.handleAssignGlobalCategoryToBlog(categoryId, blogId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.assignGlobalCategoryToBlog(Long categoryId, Long blogId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#changeCategorizedTagIndex(Long,
     *      Integer)
     */
    public void changeCategorizedTagIndex(Long tagId, Integer newIndex)
            throws com.communote.server.core.tag.category.CategorizedTagNotFoundException {
        if (tagId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.changeCategorizedTagIndex(Long tagId, Integer newIndex) - 'tagId' can not be null");
        }
        if (newIndex == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.changeCategorizedTagIndex(Long tagId, Integer newIndex) - 'newIndex' can not be null");
        }
        try {
            this.handleChangeCategorizedTagIndex(tagId, newIndex);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.changeCategorizedTagIndex(Long tagId, Integer newIndex)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#createCategorizedTag(com.communote.server.core.vo.tag.CategorizedTagVO,
     *      Long, Integer)
     */
    public com.communote.server.model.tag.CategorizedTag createCategorizedTag(
            com.communote.server.core.vo.tag.CategorizedTagVO categorizedTagVO,
            Long categoryId, Integer index)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.core.tag.category.CategorizedTagAlreadyExists {
        if (categorizedTagVO == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.createCategorizedTag(CategorizedTagVO categorizedTagVO, Long categoryId, Integer index) - 'categorizedTagVO' can not be null");
        }
        if (categorizedTagVO.getName() == null || categorizedTagVO.getName().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.createCategorizedTag(CategorizedTagVO categorizedTagVO, Long categoryId, Integer index) - 'categorizedTagVO.name' can not be null or empty");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.createCategorizedTag(CategorizedTagVO categorizedTagVO, Long categoryId, Integer index) - 'categoryId' can not be null");
        }
        try {
            return this.handleCreateCategorizedTag(categorizedTagVO, categoryId, index);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.createCategorizedTag(CategorizedTagVO categorizedTagVO, Long categoryId, Integer index)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#createGlobalTagCategory(com.communote.server.core.vo.tag.GlobalTagCategoryVO)
     */
    public com.communote.server.model.tag.GlobalTagCategory createGlobalTagCategory(
            com.communote.server.core.vo.tag.GlobalTagCategoryVO category)
            throws com.communote.server.core.tag.category.TagCategoryAlreadyExistsException {
        if (category == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.createGlobalTagCategory(GlobalTagCategoryVO category) - 'category' can not be null");
        }
        if (category.getName() == null || category.getName().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.createGlobalTagCategory(GlobalTagCategoryVO category) - 'category.name' can not be null or empty");
        }
        if (category.getPrefix() == null || category.getPrefix().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.createGlobalTagCategory(GlobalTagCategoryVO category) - 'category.prefix' can not be null or empty");
        }
        try {
            return this.handleCreateGlobalTagCategory(category);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.createGlobalTagCategory(GlobalTagCategoryVO category)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#deleteCategorizedTag(Long)
     */
    public void deleteCategorizedTag(Long tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.deleteCategorizedTag(Long tagId) - 'tagId' can not be null");
        }
        try {
            this.handleDeleteCategorizedTag(tagId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.deleteCategorizedTag(Long tagId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#deleteTagCategory(Long)
     */
    public void deleteTagCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.deleteTagCategory(Long categoryId) - 'categoryId' can not be null");
        }
        try {
            this.handleDeleteTagCategory(categoryId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.deleteTagCategory(Long categoryId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#findGlobalTagCategoryByName(String)
     */
    public com.communote.server.model.tag.GlobalTagCategory findGlobalTagCategoryByName(
            String name) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.findGlobalTagCategoryByName(String name) - 'name' can not be null or empty");
        }
        try {
            return this.handleFindGlobalTagCategoryByName(name);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.findGlobalTagCategoryByName(String name)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#findGlobalTagCategoryByPrefix(String)
     */
    public com.communote.server.model.tag.GlobalTagCategory findGlobalTagCategoryByPrefix(
            String prefix) {
        if (prefix == null || prefix.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.findGlobalTagCategoryByPrefix(String prefix) - 'prefix' can not be null or empty");
        }
        try {
            return this.handleFindGlobalTagCategoryByPrefix(prefix);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.findGlobalTagCategoryByPrefix(String prefix)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Gets the reference to <code>abstractTagCategory</code>'s DAO.
     */
    protected com.communote.server.persistence.tag.AbstractTagCategoryDao getAbstractTagCategoryDao() {
        return this.abstractTagCategoryDao;
    }

    /**
     * Gets the reference to <code>blog</code>'s DAO.
     */
    protected com.communote.server.persistence.blog.BlogDao getBlogDao() {
        return this.blogDao;
    }

    /**
     * Gets the reference to <code>categorizedTag</code>'s DAO.
     */
    protected com.communote.server.persistence.tag.CategorizedTagDao getCategorizedTagDao() {
        return this.categorizedTagDao;
    }

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#getCategorizedTags(Long)
     */
    public java.util.List<com.communote.server.model.tag.CategorizedTag> getCategorizedTags(
            Long categoryId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException {
        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.getCategorizedTags(Long categoryId) - 'categoryId' can not be null");
        }
        try {
            return this.handleGetCategorizedTags(categoryId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.getCategorizedTags(Long categoryId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Gets the reference to <code>globalTagCategory</code>'s DAO.
     */
    protected com.communote.server.persistence.tag.GlobalTagCategoryDao getGlobalTagCategoryDao() {
        return this.globalTagCategoryDao;
    }

    /**
     * Gets the reference to <code>note</code>'s DAO.
     */
    protected com.communote.server.persistence.blog.NoteDao getNoteDao() {
        return this.noteDao;
    }

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     * 
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }

    /**
     * Gets the reference to <code>tag</code>'s DAO.
     */
    protected com.communote.server.persistence.tag.TagDao getTagDao() {
        return this.tagDao;
    }

    /**
     * Performs the core logic for {@link #assignGlobalCategoryToAllBlogs(Long)}
     */
    protected abstract void handleAssignGlobalCategoryToAllBlogs(Long categoryId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException;

    /**
     * Performs the core logic for
     * {@link #assignGlobalCategoryToBlog(Long, Long)}
     */
    protected abstract com.communote.server.model.blog.Blog handleAssignGlobalCategoryToBlog(
            Long categoryId, Long blogId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.core.tag.category.TagCategoryAlreadyAssignedException,
            com.communote.server.api.core.blog.BlogNotFoundException;

    /**
     * Performs the core logic for
     * {@link #changeCategorizedTagIndex(Long, Integer)}
     */
    protected abstract void handleChangeCategorizedTagIndex(Long tagId,
            Integer newIndex)
            throws com.communote.server.core.tag.category.CategorizedTagNotFoundException;

    /**
     * Performs the core logic for
     * {@link #createCategorizedTag(com.communote.server.core.vo.tag.CategorizedTagVO, Long, Integer)}
     */
    protected abstract com.communote.server.model.tag.CategorizedTag handleCreateCategorizedTag(
            com.communote.server.core.vo.tag.CategorizedTagVO categorizedTagVO,
            Long categoryId, Integer index)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.core.tag.category.CategorizedTagAlreadyExists;

    /**
     * Performs the core logic for
     * {@link #createGlobalTagCategory(com.communote.server.core.vo.tag.GlobalTagCategoryVO)}
     */
    protected abstract com.communote.server.model.tag.GlobalTagCategory handleCreateGlobalTagCategory(
            com.communote.server.core.vo.tag.GlobalTagCategoryVO category)
            throws com.communote.server.core.tag.category.TagCategoryAlreadyExistsException;

    /**
     * Performs the core logic for {@link #deleteCategorizedTag(Long)}
     */
    protected abstract void handleDeleteCategorizedTag(Long tagId);

    /**
     * Performs the core logic for {@link #deleteTagCategory(Long)}
     */
    protected abstract void handleDeleteTagCategory(Long categoryId);

    /**
     * Performs the core logic for {@link #findGlobalTagCategoryByName(String)}
     */
    protected abstract com.communote.server.model.tag.GlobalTagCategory handleFindGlobalTagCategoryByName(
            String name);

    /**
     * Performs the core logic for {@link #findGlobalTagCategoryByPrefix(String)}
     */
    protected abstract com.communote.server.model.tag.GlobalTagCategory handleFindGlobalTagCategoryByPrefix(
            String prefix);

    /**
     * Performs the core logic for {@link #getCategorizedTags(Long)}
     */
    protected abstract java.util.List<com.communote.server.model.tag.CategorizedTag> handleGetCategorizedTags(
            Long categoryId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException;

    /**
     * Performs the core logic for
     * {@link #removeTagCategoryFromBlog(Long, Long)}
     */
    protected abstract void handleRemoveTagCategoryFromBlog(Long categoryId,
            Long blogId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.api.core.blog.BlogNotFoundException;

    /**
     * Performs the core logic for
     * {@link #updateCategorizedTag(Long, com.communote.server.core.vo.tag.CategorizedTagVO)}
     */
    protected abstract com.communote.server.model.tag.CategorizedTag handleUpdateCategorizedTag(
            Long tagId,
            com.communote.server.core.vo.tag.CategorizedTagVO categorizedTagVO)
            throws com.communote.server.core.tag.category.CategorizedTagAlreadyExists,
            com.communote.server.core.tag.category.CategorizedTagNotFoundException;

    /**
     * Performs the core logic for
     * {@link #updateGlobalTagCategory(Long, com.communote.server.core.vo.tag.GlobalTagCategoryVO)}
     */
    protected abstract com.communote.server.model.tag.GlobalTagCategory handleUpdateGlobalTagCategory(
            Long categoryId,
            com.communote.server.core.vo.tag.GlobalTagCategoryVO globalTagCategoryVO)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.core.tag.category.TagCategoryAlreadyExistsException;

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#removeTagCategoryFromBlog(Long,
     *      Long)
     */
    public void removeTagCategoryFromBlog(Long categoryId, Long blogId)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.api.core.blog.BlogNotFoundException {
        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.removeTagCategoryFromBlog(Long categoryId, Long blogId) - 'categoryId' can not be null");
        }
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.removeTagCategoryFromBlog(Long categoryId, Long blogId) - 'blogId' can not be null");
        }
        try {
            this.handleRemoveTagCategoryFromBlog(categoryId, blogId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.removeTagCategoryFromBlog(Long categoryId, Long blogId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Sets the reference to <code>abstractTagCategory</code>'s DAO.
     */
    public void setAbstractTagCategoryDao(
            com.communote.server.persistence.tag.AbstractTagCategoryDao abstractTagCategoryDao) {
        this.abstractTagCategoryDao = abstractTagCategoryDao;
    }

    /**
     * Sets the reference to <code>blog</code>'s DAO.
     */
    public void setBlogDao(com.communote.server.persistence.blog.BlogDao blogDao) {
        this.blogDao = blogDao;
    }

    /**
     * Sets the reference to <code>categorizedTag</code>'s DAO.
     */
    public void setCategorizedTagDao(
            com.communote.server.persistence.tag.CategorizedTagDao categorizedTagDao) {
        this.categorizedTagDao = categorizedTagDao;
    }

    /**
     * Sets the reference to <code>globalTagCategory</code>'s DAO.
     */
    public void setGlobalTagCategoryDao(
            com.communote.server.persistence.tag.GlobalTagCategoryDao globalTagCategoryDao) {
        this.globalTagCategoryDao = globalTagCategoryDao;
    }

    /**
     * Sets the reference to <code>note</code>'s DAO.
     */
    public void setNoteDao(com.communote.server.persistence.blog.NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    /**
     * Sets the reference to <code>tag</code>'s DAO.
     */
    public void setTagDao(com.communote.server.persistence.tag.TagDao tagDao) {
        this.tagDao = tagDao;
    }

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#updateCategorizedTag(Long,
     *      com.communote.server.core.vo.tag.CategorizedTagVO)
     */
    public com.communote.server.model.tag.CategorizedTag updateCategorizedTag(
            Long tagId,
            com.communote.server.core.vo.tag.CategorizedTagVO categorizedTagVO)
            throws com.communote.server.core.tag.category.CategorizedTagAlreadyExists,
            com.communote.server.core.tag.category.CategorizedTagNotFoundException {
        if (tagId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.updateCategorizedTag(Long tagId, CategorizedTagVO categorizedTagVO) - 'tagId' can not be null");
        }
        try {
            return this.handleUpdateCategorizedTag(tagId, categorizedTagVO);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.updateCategorizedTag(Long tagId, CategorizedTagVO categorizedTagVO)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.tag.category.TagCategoryManagement#updateGlobalTagCategory(Long,
     *      com.communote.server.core.vo.tag.GlobalTagCategoryVO)
     */
    public com.communote.server.model.tag.GlobalTagCategory updateGlobalTagCategory(
            Long categoryId,
            com.communote.server.core.vo.tag.GlobalTagCategoryVO globalTagCategoryVO)
            throws com.communote.server.core.tag.category.TagCategoryNotFoundException,
            com.communote.server.core.tag.category.TagCategoryAlreadyExistsException {
        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.updateGlobalTagCategory(Long categoryId, GlobalTagCategoryVO globalTagCategoryVO) - 'categoryId' can not be null");
        }
        if (globalTagCategoryVO == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.updateGlobalTagCategory(Long categoryId, GlobalTagCategoryVO globalTagCategoryVO) - 'globalTagCategoryVO' can not be null");
        }
        if (globalTagCategoryVO.getName() == null
                || globalTagCategoryVO.getName().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.updateGlobalTagCategory(Long categoryId, GlobalTagCategoryVO globalTagCategoryVO) - 'globalTagCategoryVO.name' can not be null or empty");
        }
        if (globalTagCategoryVO.getPrefix() == null
                || globalTagCategoryVO.getPrefix().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.category.TagCategoryManagement.updateGlobalTagCategory(Long categoryId, GlobalTagCategoryVO globalTagCategoryVO) - 'globalTagCategoryVO.prefix' can not be null or empty");
        }
        try {
            return this.handleUpdateGlobalTagCategory(categoryId, globalTagCategoryVO);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.category.TagCategoryManagementException(
                    "Error performing 'com.communote.server.service.tag.category.TagCategoryManagement.updateGlobalTagCategory(Long categoryId, GlobalTagCategoryVO globalTagCategoryVO)' --> "
                            + rt,
                    rt);
        }
    }
}