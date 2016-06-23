package com.communote.server.api.core.blog;

import java.util.List;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.blog.Blog;

/**
 * Service for retrieving, creating, updating and removing blogs/topics.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface BlogManagement {

    /**
     * Key of the property which marks a topic as the personal topic of a user. The property value
     * holds the ID of the user/owner of the personal topic.
     */
    public static String PROPERTY_KEY_PERSONAL_TOPIC_USER_ID = "topic.personal.user.id";

    /**
     * Create a new blog and set the user referenced in the transfer object as its manager. The
     * all-can access rights will set if the client configuration allows this.
     *
     * @param blogDetails
     *            transfer object holding the details of the blog to be created
     * @throws NonUniqueBlogIdentifierException
     *             in case the blog alias is not unique
     * @throws BlogIdentifierValidationException
     *             in case the blog alias is not valid
     * @throws BlogAccessException
     *             in case the current user is not manager of the topic that should be added as the
     *             parent topic
     * @throws BlogNotFoundException
     *             in case the topic to be added as the parent topic does not exist
     */
    public Blog createBlog(CreationBlogTO blogDetails) throws NonUniqueBlogIdentifierException,
    BlogIdentifierValidationException, BlogNotFoundException, BlogAccessException;

    /**
     * Create a default topic if there is no such topic yet.
     *
     * @param blogName
     *            the name of the topic to create
     *
     * @throws NonUniqueBlogIdentifierException
     *             in case the blog alias of the default blog is not unique
     * @throws BlogIdentifierValidationException
     *             in case the blog alias of the default blog is not valid
     */
    public void createDefaultBlog(String blogName) throws NonUniqueBlogIdentifierException,
    BlogIdentifierValidationException;

    /**
     * Delete a blog with all its notes
     *
     * @param blogId
     *            The ID of the blog to delete
     * @param newBlogId
     *            An optional ID of a blog to move the notes to before deleting the blog
     * @throws BlogNotFoundException
     *             if the blog to delete does not exist
     * @throws NoteManagementAuthorizationException
     *             if the current user is not manager of the blog to delete
     *
     */
    public void deleteBlog(Long blogId, Long newBlogId)
            throws NoteManagementAuthorizationException, BlogNotFoundException;

    /**
     * Delete a selection of blogs. The current user must be client manager or manager of all the
     * blogs for this operation to succeed.
     *
     * @param blogIds
     *            The IDs of the blogs to delete
     * @throws AuthorizationException
     *             if the current user is not client manager or the manager of all the blogs
     */
    public void deleteBlogs(Long[] blogIds) throws AuthorizationException;

    /**
     * Retrieve a blog by its alias. Throws an authorization exception if the current user does not
     * have read access to the blog.
     *
     * @param identifier
     *            the name identifier/alias of the blog
     * @return the blog or null if there is no such blog
     * @throws BlogAccessException
     *             in case the current user has no read access to the topic
     */
    public Blog findBlogByIdentifier(String identifier) throws BlogAccessException;

    /**
     * Retrieve a blog by its alias. It is not checked whether the current user has read access to
     * the blog.
     *
     * @param alias
     *            the name identifier/alias of the blog
     * @return the blog or null if there is no such blog
     */
    public Blog findBlogByIdentifierWithoutAuthorizationCheck(String alias);

    /**
     * Retrieve a blog by its ID. It is not checked whether the current user has read access to the
     * blog.
     *
     * @param blogId
     *            the ID of the blog
     * @return the blog or null if there is no such blog
     */
    public Blog findBlogByIdWithoutAuthorizationCheck(Long blogId);

    /**
     * Return the blogs for the given IDs. Topics that do not exist or the user is not allowed to
     * read will be ignored.
     *
     * @param blogIds
     *            the IDs of the blogs to retrieve
     * @return the found blogs
     */
    public List<Blog> findBlogsById(Long[] blogIds);

    /**
     * Tries to find a valid blog alias that is not yet used by another blog
     *
     * @param aliasBase
     *            the alias value to start with. If this value is not a valid alias, it will be
     *            converted into a valid string.
     * @param previousAlias
     *            if this alias is generated it will not be considered as a duplicate. Providing
     *            this value is especially useful when generating an alias for a blog update. Can be
     *            null.
     * @return a proposal for the blog alias which might match the previousAlias if given
     * @throws NonUniqueBlogIdentifierException
     *             in case no unique identifier could be generated
     */
    public String generateUniqueBlogAlias(String aliasBase, String previousAlias)
            throws NonUniqueBlogIdentifierException;

    /**
     * Return the details of a blog. The data of the blog will be converted with the provided
     * converter and the resulting object will be returned. In case there is no blog for the ID null
     * is returned. In case the current user has no access to the blog an exception will be thrown.
     *
     * @param <T>
     *            the type of the resulting object
     * @param alias
     *            the alias of the topic
     * @param converter
     *            the converter to use for creating the result object
     * @return the blog data or null if there is no matching topic
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    public <T> T getBlogByAlias(String alias, Converter<Blog, T> converter)
            throws BlogAccessException;

    /**
     * Retrieve a blog by its ID. <br>
     * Usually {@link #getBlogById(Long, Converter)} should be used instead.
     *
     * @param topicId
     *            the ID of the blog
     * @param needTags
     *            If true tags will also be loaded.
     * @return the topic
     * @throws BlogAccessException
     *             in case the current user has no read access to the topic
     * @throws BlogNotFoundException
     *             in case the topic does not exist
     *
     */
    public Blog getBlogById(Long topicId, boolean needTags) throws BlogNotFoundException,
    BlogAccessException;

    /**
     * Return the details of a blog. The data of the blog will be converted with the provided
     * converter and the resulting object will be returned. In case there is no blog for the ID null
     * is returned. In case the current user has no access to the blog an exception will be thrown.
     *
     * @param <T>
     *            the type of the resulting object
     * @param blogId
     *            the ID of the blog
     * @param converter
     *            the converter to use for creating the result object
     * @return the blog data
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    public <T> T getBlogById(Long blogId, Converter<Blog, T> converter) throws BlogAccessException;

    /**
     * @return the number of blogs
     */
    public long getBlogCount();

    /**
     * Get the ID of a topic identified by the alias. The access to the topic is not checked.
     *
     * @param alias
     *            the alias of the topic
     * @return the ID of the topic or null if the topic does not exist
     */
    public Long getBlogId(String alias);

    /**
     * Retrieve a collection of the writable blogs the current user has written to. The blogs are
     * sorted by the creation date of the notes where the blogs with the latest note are on top.
     *
     * @param numberOfMaxResults
     *            The maximal number of blogs to return
     * @param sortByTitle
     *            If true, the result will be sorted by title.
     * @return the sorted blog collection
     */
    public List<BlogData> getLastUsedBlogs(int numberOfMaxResults, boolean sortByTitle);

    /**
     * Return the minimal set of information on a blog, even if the current user is not allowed to
     * read it.
     *
     * @param blogId
     *            the ID of the topic
     * @return the info on the topic
     */
    public MinimalBlogData getMinimalBlogInfo(Long blogId);

    /**
     * Retrieve a collection of writable blogs the current user has written to. The collection is
     * sorted by the number of created notes in descending order.
     *
     * @param numberOfMaxResults
     *            The maximal number of blogs to return
     * @param sortByTitle
     *            If true, the result will be sorted by title.
     * @return the collection of blogs
     */
    public List<BlogData> getMostUsedBlogs(int numberOfMaxResults, boolean sortByTitle);

    /**
     * Removes "all read" and "all write" permissions from all blogs.
     */
    public void resetGlobalPermissions();

    /**
     * Update the blog data including title, description alias, tags and properties but not access
     * rights. If the blog to update does not exist nothing will happen.
     *
     * @param blogId
     *            ID of the blog to update
     * @param blogDetails
     *            transfer object holding the details of the blog to be updated
     * @return the updated blog
     * @throws NonUniqueBlogIdentifierException
     *             in case the new blog alias is not unique
     * @throws BlogIdentifierValidationException
     *             in case the new blog alias is not valid
     * @throws BlogAccessException
     *             in case the current user has not the required permission to update the blog
     */
    public Blog updateBlog(Long blogId, BlogTO blogDetails)
            throws NonUniqueBlogIdentifierException, BlogIdentifierValidationException,
            BlogAccessException;

}
