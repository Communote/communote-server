package com.communote.server.core.blog;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogTO;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.model.blog.Blog;

/**
 * Spring Service base class for <code>BlogManagement</code>, provides access to all services and
 * entities referenced by this service.
 *
 * @see BlogManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class BlogManagementBase implements BlogManagement {

    /**
     * {@inheritDoc}
     */
    @Override
    public Blog createBlog(CreationBlogTO blogDetails) throws NonUniqueBlogIdentifierException,
            BlogIdentifierValidationException, BlogNotFoundException, BlogAccessException {
        if (blogDetails == null) {
            throw new IllegalArgumentException(
                    "BlogManagement.createBlog(BlogTO blogDetails) - 'blogDetails' can not be null");
        }
        if (blogDetails.getTitle() == null || blogDetails.getTitle().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "BlogManagement.createBlog(BlogTO blogDetails) - 'blogDetails.title' can not be null or empty");
        }
        try {
            return this.handleCreateBlog(blogDetails);
        } catch (RuntimeException rt) {
            throw new BlogManagementException(
                    "Error performing 'BlogManagement.createBlog(BlogTO blogDetails)' --> " + rt,
                    rt);
        }
    }

    /**
     * @see BlogManagement#deleteBlog(Long, Long)
     */
    @Override
    public void deleteBlog(Long blogId, Long newBlogId)
            throws NoteManagementAuthorizationException, BlogNotFoundException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogManagement.deleteBlog(Long blogId, Long newBlogId) - 'blogId' can not be null");
        }
        try {
            this.handleDeleteBlog(blogId, newBlogId);
        } catch (RuntimeException rt) {
            throw new BlogManagementException(
                    "Error performing 'BlogManagement.deleteBlog(Long blogId, Long newBlogId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogManagement#deleteBlogs(Long[])
     */
    @Override
    public void deleteBlogs(Long[] blogIds)
            throws com.communote.server.api.core.security.AuthorizationException {
        if (blogIds == null) {
            throw new IllegalArgumentException(
                    "BlogManagement.deleteBlogs(Long[] blogIds) - 'blogIds' can not be null");
        }
        try {
            this.handleDeleteBlogs(blogIds);
        } catch (RuntimeException rt) {
            throw new BlogManagementException(
                    "Error performing 'BlogManagement.deleteBlogs(Long[] blogIds)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Blog findBlogByIdentifier(String identifier) throws BlogAccessException {
        if (identifier == null || identifier.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "BlogManagement.findBlogByIdentifier(String identifier) - 'identifier' can not be null or empty");
        }
        try {
            return this.handleFindBlogByIdentifier(identifier);
        } catch (RuntimeException rt) {
            throw new BlogManagementException(
                    "Error performing 'BlogManagement.findBlogByIdentifier(String identifier)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Blog findBlogByIdentifierWithoutAuthorizationCheck(String alias) {
        if (alias == null || alias.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "BlogManagement.findBlogByIdentifierWithoutAuthorizationCheck(String alias) - 'alias' can not be null or empty");
        }
        try {
            return this.handleFindBlogByIdentifierWithoutAuthorizationCheck(alias);
        } catch (RuntimeException rt) {
            throw new BlogManagementException(
                    "Error performing 'BlogManagement.findBlogByIdentifierWithoutAuthorizationCheck(String alias)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Blog findBlogByIdWithoutAuthorizationCheck(Long blogId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogManagement."
                            + "findBlogByIdWithoutAuthorizationCheck(Long blogId) - 'blogId' can not be null");
        }
        try {
            return this.handleFindBlogByIdWithoutAuthorizationCheck(blogId);
        } catch (RuntimeException rt) {
            throw new BlogManagementException("Error performing 'BlogManagement."
                    + "findBlogByIdWithoutAuthorizationCheck(Long blogId)' --> " + rt, rt);
        }
    }

    /**
     * @see BlogManagement#findBlogsById(Long[])
     */
    @Override
    @Transactional(readOnly = true)
    public java.util.List<Blog> findBlogsById(Long[] blogIds) {
        if (blogIds == null) {
            throw new IllegalArgumentException(
                    "BlogManagement.findBlogsById(Long[] blogIds) - 'blogIds' can not be null");
        }
        try {
            return this.handleFindBlogsById(blogIds);
        } catch (RuntimeException rt) {
            throw new BlogManagementException(
                    "Error performing 'BlogManagement.findBlogsById(Long[] blogIds)' --> " + rt, rt);
        }
    }

    /**
     * @see BlogManagement#getBlogCount()
     */
    @Override
    @Transactional(readOnly = true)
    public long getBlogCount() {
        try {
            return this.handleGetBlogCount();
        } catch (RuntimeException rt) {
            throw new BlogManagementException(
                    "Error performing 'BlogManagement.getBlogCount()' --> " + rt, rt);
        }
    }

    /**
     * @see BlogManagement#getMostUsedBlogs(com.communote.server.core.filter.ResultSpecification)
     */
    @Override
    @Transactional(readOnly = true)
    public List<BlogData> getMostUsedBlogs(int numberOfMaxResults, boolean sortByTitle) {
        try {
            return this.handleGetMostUsedBlogs(numberOfMaxResults, sortByTitle);
        } catch (RuntimeException rt) {
            throw new BlogManagementException(
                    "Error performing 'BlogManagement.getMostUsedBlogs(ResultSpecification resultSpecification)' --> "
                            + rt, rt);
        }
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
     * Performs the core logic for {@link #createBlog(CreationBlogTO)}
     *
     * @throws BlogAccessException
     *             in case the current user is not manager of the topic that should be added as the
     *             parent topic
     * @throws BlogNotFoundException
     *             in case the topic to be added as the parent topic does not exist
     */
    protected abstract Blog handleCreateBlog(CreationBlogTO blogDetails)
            throws NonUniqueBlogIdentifierException, BlogIdentifierValidationException,
            BlogNotFoundException, BlogAccessException;

    /**
     * Performs the core logic for {@link #deleteBlog(Long, Long)}
     */
    protected abstract void handleDeleteBlog(Long blogId, Long newBlogId)
            throws NoteManagementAuthorizationException, BlogNotFoundException;

    /**
     * Performs the core logic for {@link #deleteBlogs(Long[])}
     */
    protected abstract void handleDeleteBlogs(Long[] blogIds)
            throws com.communote.server.api.core.security.AuthorizationException;

    /**
     * Performs the core logic for {@link #findBlogByIdentifier(String)}
     */
    protected abstract Blog handleFindBlogByIdentifier(String identifier)
            throws BlogAccessException;

    /**
     * Performs the core logic for {@link #findBlogByIdentifierWithoutAuthorizationCheck(String)}
     */
    protected abstract Blog handleFindBlogByIdentifierWithoutAuthorizationCheck(String alias);

    /**
     * Retrieves a blog by its ID. It is not checked whether the current user has read access to the
     * blog.
     *
     * @param blogId
     *            the ID of the blog
     * @return the blog or null if there is no such blog
     */
    protected abstract Blog handleFindBlogByIdWithoutAuthorizationCheck(Long blogId);

    /**
     * Performs the core logic for {@link #findBlogsById(Long[])}
     */
    protected abstract java.util.List<Blog> handleFindBlogsById(Long[] blogIds);

    /**
     * Performs the core logic for {@link #getBlogCount()}
     */
    protected abstract long handleGetBlogCount();

    /**
     * Performs the core logic for {@link #getMostUsedBlogs(int,boolean )}
     */
    protected abstract List<BlogData> handleGetMostUsedBlogs(int numberOfMaxResults,
            boolean sortByTitle);

    /**
     * Performs the core logic for {@link #updateBlog(Long, BlogTO)}
     *
     * @param blogId
     *            ID of the blog to update
     * @param blog
     *            transfer object holding the details of the blog to be updated
     * @return the updated blog
     * @throws NonUniqueBlogIdentifierException
     *             in case the new blog alias is not unique
     * @throws BlogIdentifierValidationException
     *             in case the new blog alias is not valid
     * @throws BlogAccessException
     *             in case the current user has not the required permission to update the blog
     */
    protected abstract Blog handleUpdateBlog(Long blogId, BlogTO blog)
            throws NonUniqueBlogIdentifierException, BlogIdentifierValidationException,
            BlogAccessException;

    @Override
    public Blog updateBlog(Long blogId, BlogTO blog) throws NonUniqueBlogIdentifierException,
            BlogIdentifierValidationException, BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogManagement.updateBlog(Long blogId, BlogTO blog) - 'blogId' can not be null");
        }
        if (blog == null) {
            throw new IllegalArgumentException(
                    "BlogManagement.updateBlog(Long blogId, BlogTO blog) - 'blog' can not be null");
        }
        if (blog.getTitle() == null || blog.getTitle().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "BlogManagement.updateBlog(Long blogId, BlogTO blog) - 'blog.title' can not be null or empty");
        }
        try {
            return this.handleUpdateBlog(blogId, blog);
        } catch (BlogManagementException e) {
            throw e;
        } catch (RuntimeException rt) {
            throw new BlogManagementException(
                    "Error performing 'BlogManagement.updateBlog(BlogTO blog)' --> " + rt, rt);
        }
    }
}
