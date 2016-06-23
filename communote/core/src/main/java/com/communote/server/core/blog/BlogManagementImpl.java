package com.communote.server.core.blog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.communote.common.converter.Converter;
import com.communote.common.converter.IdentityConverter;
import com.communote.common.string.StringHelper;
import com.communote.common.validation.EmailValidator;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.BlogTO;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.blog.MinimalBlogData;
import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.blog.UsedBlogsCacheKey.UsedBlogs;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.converter.blog.BlogToBlogDataConverter;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagNotFoundException;
import com.communote.server.core.tag.TagStoreNotFoundException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserSecurityHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.config.Configuration;
import com.communote.server.model.i18n.Message;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.blog.BlogDao;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.persistence.blog.UserToBlogRoleMappingDao;
import com.communote.server.service.NoteService;

/**
 * Implementation of the topic management service
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("blogManagement")
public class BlogManagementImpl extends BlogManagementBase {

    private final static Logger LOGGER = LoggerFactory.getLogger(BlogManagementImpl.class);

    private static final String DEFAULT_BLOG_ALIAS = "default";

    @Autowired
    private ConfigurationManagement configurationManagement;
    @Autowired
    private BlogDao blogDao;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private UserToBlogRoleMappingDao userToBlogRoleMappingDao;
    @Autowired
    private TagManagement tagManagement;
    @Autowired
    private BlogRightsManagement blogRightsManagement;
    @Autowired
    private TopicPermissionManagement topicPermissionManagement;
    @Autowired
    private TopicHierarchyManagement topicHierarchyManagement;
    @Autowired
    private UserManagement userManagement;
    @Autowired
    private PropertyManagement propertyManagement;
    @Autowired
    private EventDispatcher eventDispatcher;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private NoteService noteService;

    /**
     * converts a collection of topic IDs into BlogListitems
     *
     * @param blogIds
     *            the topic IDs to process
     * @param limit
     *            the number of items to convert, must be <= size of topic ID collection
     * @param skipNonExistingBlogs
     *            true if topic IDs that cannot be resolved into blogs should be ignored
     * @return the list items or null if a non existing topic was contained and skipNonExistingBlogs
     *         was false
     */
    private List<BlogData> convertToBlogListItems(List<Long> blogIds, int limit,
            boolean skipNonExistingBlogs) {
        List<BlogData> result = new ArrayList<BlogData>(limit);
        BlogToBlogDataConverter<BlogData> converter = new BlogToBlogDataConverter<BlogData>(
                BlogData.class, false);
        for (int i = 0; i < limit; i++) {
            Blog blog = blogDao.load(blogIds.get(i));
            if (blog != null) {
                BlogData item = converter.convert(blog);
                result.add(item);
            } else {
                // the topic was deleted, fail if requested
                if (!skipNonExistingBlogs) {
                    return null;
                }
            }
        }
        return result;
    }

    @Override
    public void createDefaultBlog(String topicName) throws NonUniqueBlogIdentifierException,
            BlogIdentifierValidationException {
        Configuration config = configurationManagement.getConfiguration();
        Blog defaultBlog = config.getClientConfig().getDefaultBlog();
        if (defaultBlog == null) {
            defaultBlog = internalCreateDefaultBlog(topicName);
            CommunoteRuntime.getInstance().getConfigurationManager().setDefaultBlog(defaultBlog);
        }
    }

    /**
     * This method deletes a topic, as well as the dependent {@link BloggingUserGroup} and
     * {@link WidgetNewsFeed}s.
     *
     * @param topic
     *            The topic to be deleted.
     * @param clientManagerCanDeletePosts
     *            whether the client manager should be able to delete any UTP
     * @param isClientManager
     *            whether the current user is client manager. Will only be evaluated if
     *            clientManagerCanDeletePosts is true
     * @throws NoteManagementAuthorizationException
     *             if the user has no authorization to this topic
     */
    private void deleteBlog(Blog topic, boolean clientManagerCanDeletePosts, boolean isClientManager)
            throws NoteManagementAuthorizationException {
        // TODO back up the topic before deletion
        long firstNoteId = 0;
        List<Note> notes;
        while (!(notes = noteDao.getNotesForBlog(topic.getId(), firstNoteId, 200)).isEmpty()) {
            for (Note note : notes) {
                firstNoteId = note.getId();
                if (note.getParent() == null) {
                    // TODO use internal system user!
                    noteService.deleteNote(note.getId(), true, clientManagerCanDeletePosts);
                }
            }
            firstNoteId++;
        }
        topic.getTags().clear();
        SecurityContext currentContext = null;
        try {
            if (clientManagerCanDeletePosts && isClientManager) {
                // should not return null
                currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
            }
            topicHierarchyManagement.removeAllConnections(topic.getId());
        } catch (BlogAccessException e) {
            // should not occur since the current user is manager
            LOGGER.error("Unexpected exception", e);
            throw new BlogManagementException(
                    "Unexpected exception while cleaning the topic structure");
        } finally {
            if (currentContext != null) {
                AuthenticationHelper.setSecurityContext(currentContext);
            }
        }
        userToBlogRoleMappingDao.removeAllForBlog(topic.getId());
        // delete topic: will remove externalObjects with their properties and the topic properties
        // automatically through cascading
        blogDao.remove(topic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public String generateUniqueBlogAlias(String aliasBase, String previousAlias)
            throws NonUniqueBlogIdentifierException {

        // replace whitespaces with underscores and remove unsuported characters
        aliasBase = aliasBase.toLowerCase().replace(" ", "_");
        Pattern p = Pattern.compile(BlogManagementHelper.REG_EXP_TOPIC_NAME_IDENTIFIER);
        Matcher m = p.matcher(aliasBase);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            sb.append(m.group());
        }
        String proposal = sb.toString();

        // reduce underscore sequences to one and clean up
        proposal = proposal.replaceAll("__+", "_");

        proposal = proposal.substring(0,
                Math.min(proposal.length(), EmailValidator.MAX_SAFE_LENGTH_LOCAL_PART - 3));

        // remove trailing '-','.' or '_' (see KENMEI-7040)
        for (int i = proposal.length() - 1; i >= 0; i--) {
            if (proposal.endsWith("_") || proposal.endsWith("-") || proposal.endsWith(".")) {
                proposal = proposal.length() == 0 ? "" : proposal.substring(0,
                        proposal.length() - 1);
            } else {
                break;
            }
        }
        if (proposal.length() > 1 && proposal.startsWith("_")) {
            proposal = proposal.substring(1);
        }

        // fall back to 'alias' if empty
        if (proposal.length() == 0 || proposal.equals("_")) {
            proposal = "alias";
        }

        int index = 0;
        int maxTries = 100;
        while (!proposal.equals(previousAlias) && blogDao.findByNameIdentifier(proposal) != null
                && maxTries > 0) {

            index++;
            maxTries--;
            proposal = proposal + index;
        }

        if (maxTries == 0) {
            throw new NonUniqueBlogIdentifierException("Could not find a unique alias for input "
                    + aliasBase);
        }

        return proposal;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public <T> T getBlogByAlias(String alias, Converter<Blog, T> converter)
            throws BlogAccessException {
        // TODO alias to topicId mapping should be cached
        Blog blog = blogDao.findByNameIdentifier(alias);
        if (blog != null) {
            if (!blogRightsManagement.currentUserHasReadAccess(blog.getId(), false)) {
                throw new BlogAccessException("Current user has no access to the topic",
                        blog.getId(), BlogRole.VIEWER, null);
            }
            // for compatibility wrap RT exceptions
            try {
                return converter.convert(blog);
            } catch (RuntimeException e) {
                throw new BlogManagementException(
                        "Error performing getBlogByAlias(String, Converter): " + e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Blog getBlogById(Long blogId, boolean needTags) throws BlogNotFoundException,
            BlogAccessException {
        Blog blog = this.blogRightsManagement.getAndCheckBlogAccess(blogId, BlogRole.VIEWER);

        if (needTags) {
            if (blog.getTags() != null) {
                for (Tag tag : blog.getTags()) {
                    Hibernate.initialize(tag);
                    Set<Message> messages = new HashSet<Message>();
                    if (tag.getNames() != null) {
                        messages.addAll(tag.getNames());
                    }
                    if (tag.getDescriptions() != null) {
                        messages.addAll(tag.getDescriptions());
                    }
                    for (Message message : messages) {
                        Hibernate.initialize(message.getLanguage());
                    }
                }
            }
        }

        return blog;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public <T> T getBlogById(Long blogId, Converter<Blog, T> converter) throws BlogAccessException {
        Blog blog = null;
        try {
            blog = blogRightsManagement.getAndCheckBlogAccess(blogId, BlogRole.VIEWER);
        } catch (BlogNotFoundException e) {
            return null;
        }
        // for compatibility wrap RT exceptions
        try {
            return converter.convert(blog);
        } catch (RuntimeException e) {
            throw new BlogManagementException("Error performing getBlogById(Long, Converter): "
                    + e.getMessage(), e);
        }
    }

    @Override
    public Long getBlogId(String alias) {
        // TODO alias to topicId mapping should be cached
        Blog blog = blogDao.findByNameIdentifier(alias);
        if (blog != null) {
            return blog.getId();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<BlogData> getLastUsedBlogs(int numberOfMaxResults, boolean sortByTitle) {
        if (SecurityHelper.isPublicUser()) {
            return Collections.emptyList();
        }
        UsedBlogsCacheKey key = new UsedBlogsCacheKey(numberOfMaxResults);
        CacheElementProvider<UsedBlogsCacheKey, UsedBlogs> provider = UsedBlogsCacheKey.LAST_USED_BLOGS_PROVIDER;
        List<BlogData> result = getUsedBlogsFromCache(key, provider, numberOfMaxResults);
        return BlogManagementHelper.sortedBlogList(result);
    }

    @Override
    /*
     * note: we are deliberately not checking for topic access. We don't like this because we might
     * leak sensitive data (especially the title) but the product owner wants to have it that way :(
     */
    public MinimalBlogData getMinimalBlogInfo(Long blogId) {
        Blog blog = blogDao.load(blogId);
        if (blog != null) {
            return new MinimalBlogData(blog.getId(), blog.getNameIdentifier(), blog.getTitle());
        }
        return null;
    }

    /**
     * @param key
     *            cache key
     * @param provider
     *            the cache element provider
     * @param numberOfMaxResults
     *            the maximal number of items to fetch
     * @return the the used blogs
     */
    private List<BlogData> getUsedBlogsFromCache(UsedBlogsCacheKey key,
            CacheElementProvider<UsedBlogsCacheKey, UsedBlogs> provider, int numberOfMaxResults) {
        Cache cache = cacheManager.getCache();
        UsedBlogs usedBlogs = cache.get(key, provider);
        // check if the cached item holds enough elements
        if (usedBlogs.getMaxResults() < numberOfMaxResults) {
            cache.invalidate(key, provider);
            usedBlogs = cache.get(key, provider);
        }

        // used blogs might hold more than requested
        int limit = numberOfMaxResults <= usedBlogs.getBlogIds().size() ? numberOfMaxResults
                : usedBlogs.getBlogIds().size();
        // fail if there are blogs that do not exist anymore
        List<BlogData> result = convertToBlogListItems(usedBlogs.getBlogIds(), limit, false);
        if (result == null) {
            cache.invalidate(key, provider);
            usedBlogs = cache.get(key, provider);
        }
        // recalculate limit because usedBlogs can have fewer entries now
        if (usedBlogs.getBlogIds().size() < limit) {
            limit = usedBlogs.getBlogIds().size();
        }
        // to avoid endless loops just skip blogs that were deleted in the meanwhile
        result = convertToBlogListItems(usedBlogs.getBlogIds(), limit, true);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Blog handleCreateBlog(CreationBlogTO blogDetails)
            throws NonUniqueBlogIdentifierException, BlogIdentifierValidationException,
            BlogNotFoundException, BlogAccessException {

        Set<Permission<Blog>> permissions = topicPermissionManagement
                .getPermissionsForCreation(blogDetails);
        if (!permissions.contains(TopicPermissionManagement.PERMISSION_CREATE_TOPIC)) {
            throw new BlogAccessException("Current user is not allowed to create a topic.", 0,
                    TopicPermissionManagement.PERMISSION_CREATE_TOPIC);
        }

        Blog parentTopic = null;
        if (blogDetails.getParentTopicId() != null) {
            // check whether parent topic exists before creating child topic to avoid rolling back
            // transaction manually
            // TODO this is kind of ugly as we are using internal knowledge of addChildTopic since
            // we are checking for management access. Alternative would be to rollback transaction
            // manually or encapsulating the exceptions thrown by addChildTopic in a
            // BlogManagementException which isn't nice either
            parentTopic = blogRightsManagement.getAndCheckBlogAccess(
                    blogDetails.getParentTopicId(), BlogRole.MANAGER);
        }
        Blog blog = internalCreateBlog(blogDetails);
        updateBlogProperties(blog, blogDetails);
        if (parentTopic != null) {
            try {
                topicHierarchyManagement.addChildTopic(parentTopic.getId(), blog.getId());

            } catch (ToplevelTopicCannotBeChildException e) {
                // cannot occur as the new child topic is no top-level topic
                LOGGER.error("Unexpected exception", e);
                throw new BlogManagementException(
                        "Unexpected exception while adding the new topic as a child topic to the provided parent");
            }
        }

        eventDispatcher.fire(new BlogCreatedEvent(blog.getId(), blog.getTitle(), blogDetails
                .getCreatorUserId(), blogDetails.getParentTopicId()));
        return blog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDeleteBlog(Long blogId, Long newBlogId)
            throws NoteManagementAuthorizationException, BlogNotFoundException {
        try {
            Blog topic = blogDao.load(blogId);
            if (topic == null) {
                throw new BlogNotFoundException("Topic for deletion not found", blogId, "");
            }
            UserSecurityHelper.assertIsManagerOfBlog(topic.getId());
            if (newBlogId != null) {
                movePostsToBlog(newBlogId, topic);
            }
            deleteBlog(topic, false, false);
        } catch (RuntimeException e) {
            throw new BlogManagementException("Error while deleting this topic!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDeleteBlogs(Long[] blogIds) throws AuthorizationException {
        boolean clientManager = SecurityHelper.isClientManager();
        List<Blog> blogsToDelete = new ArrayList<Blog>();
        // check authorization
        for (Long blogId : blogIds) {
            Blog b = blogDao.load(blogId);
            if (b != null) {
                if (!SecurityHelper.isInternalSystem() && !clientManager
                        && !blogRightsManagement.currentUserHasManagementAccess(b.getId())) {
                    throw new AuthorizationException(
                            "The current user has not the required rights to delete topic "
                                    + b.getNameIdentifier());
                }
                blogsToDelete.add(b);
            }
        }
        for (Blog b : blogsToDelete) {
            deleteBlog(b, true, clientManager);
        }
    }

    @Override
    protected Blog handleFindBlogByIdentifier(String identifier) throws BlogAccessException {
        Blog blog = blogDao.findByNameIdentifier(identifier);
        if (blog != null) {
            if (!blogRightsManagement.currentUserHasReadAccess(blog.getId(), false)) {
                throw new BlogAccessException("Current user has no read access to topic "
                        + blog.getId(), blog.getId(), BlogRole.VIEWER, null);
            }
        }
        return blog;
    }

    @Override
    protected Blog handleFindBlogByIdentifierWithoutAuthorizationCheck(String alias) {
        return blogDao.findByNameIdentifier(alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Blog handleFindBlogByIdWithoutAuthorizationCheck(Long blogId) {
        return blogDao.load(blogId);
    }

    @Override
    protected List<Blog> handleFindBlogsById(Long[] blogIds) {
        List<Blog> blogs = blogDao.findBlogs(blogIds);
        Iterator<Blog> blogIterator = blogs.iterator();
        while (blogIterator.hasNext()) {
            Blog next = blogIterator.next();
            if (!blogRightsManagement.currentUserHasReadAccess(next.getId(), false)) {

                blogIterator.remove();
                LOGGER.debug("Skipping topic ({}) because user {} has no access", next.getId(),
                        SecurityHelper.getCurrentUserAlias());
            }

        }
        return blogs;
    }

    @Override
    protected long handleGetBlogCount() {
        return blogDao.getBlogCount();
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    protected List<BlogData> handleGetMostUsedBlogs(int numberOfMaxResults, boolean sortByTitle) {
        if (SecurityHelper.isPublicUser()) {
            return Collections.emptyList();
        }
        CacheElementProvider<UsedBlogsCacheKey, UsedBlogs> provider = UsedBlogsCacheKey.MOST_USED_BLOGS_PROVIDER;
        UsedBlogsCacheKey key = new UsedBlogsCacheKey(numberOfMaxResults);
        List<BlogData> result = getUsedBlogsFromCache(key, provider, numberOfMaxResults);
        return BlogManagementHelper.sortedBlogList(result);
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    protected Blog handleUpdateBlog(Long blogId, BlogTO blogTO)
            throws NonUniqueBlogIdentifierException, BlogIdentifierValidationException,
            BlogAccessException {
        Blog blog;
        try {
            blog = topicPermissionManagement.hasAndGetWithPermission(blogId,
                    TopicPermissionManagement.PERMISSION_EDIT_DETAILS,
                    new IdentityConverter<Blog>());
        } catch (NotFoundException e) {
            return null;
        }
        if (!blog.getNameIdentifier().equals(StringUtils.lowerCase(blogTO.getNameIdentifier()))) {
            if (blogDao.findByNameIdentifier(blogTO.getNameIdentifier()) != null) {
                throw new NonUniqueBlogIdentifierException(
                        "Blog with this nameIdentifier already exists, but must be unique!");
            }
        }
        internalUpdateBlogData(blog, blogTO);
        updateBlogProperties(blog, blogTO);

        return blog;
    }

    /**
     * internal init
     */
    @PostConstruct
    private void init() {
        propertyManagement.addObjectPropertyFilter(PropertyType.BlogProperty,
                PropertyManagement.KEY_GROUP, BlogManagement.PROPERTY_KEY_PERSONAL_TOPIC_USER_ID);
    }

    /**
     * Internal create topic. does not check the topic limit.
     *
     * @param blogDetails
     *            the topic to create
     * @return the created topic
     * @throws NonUniqueBlogIdentifierException
     *             in case of an error
     * @throws BlogIdentifierValidationException
     *             if the topic alias is not valid
     */
    private Blog internalCreateBlog(BlogTO blogDetails) throws NonUniqueBlogIdentifierException,
            BlogIdentifierValidationException {
        if (blogDao.findByNameIdentifier(blogDetails.getNameIdentifier()) != null) {
            throw new NonUniqueBlogIdentifierException(
                    "Blog with this nameIdentifier already exists, but must be unique!");
        }
        Blog blog = Blog.Factory.newInstance();
        internalUpdateBlogData(blog, blogDetails);
        blog.setCreationDate(blog.getLastModificationDate());

        try {
            blog = blogDao.create(blog);
        } catch (RuntimeException e) {
            LOGGER.error("On topic topic creation", e);
            throw e;
        }
        // TODO do we have to check the PERMISSION_EDIT_ACCESS_CONTROL_LIST permission?
        // don't use BlogRightsManagement.setAllCanReadAllCanWrite since we don't want that topic
        // rights changed message
        if (BlogManagementHelper.canSetAllCanReadWrite()) {
            blog.setAllCanRead(blogDetails.isAllCanRead());
            blog.setAllCanWrite(blogDetails.isAllCanWrite());
        }

        if (SecurityHelper.isClientManager()) {
            blog.setToplevelTopic(blogDetails.isToplevelTopic());
        }

        try {
            // manager will be assigned here
            blogRightsManagement.addEntity(blog.getId(), blogDetails.getCreatorUserId(),
                    BlogRole.MANAGER);
        } catch (BlogNotFoundException e) {
            LOGGER.error("Error Blog not found after creation: " + e.getMessage(), e);
            throw new BlogManagementException("Blog creation failed.");
        } catch (CommunoteEntityNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            throw new BlogManagementException("Blog creation failed.");
        } catch (BlogAccessException e) {
            // not thrown during creation
            LOGGER.error("unexpected exception", e);
            throw new BlogManagementException("Blog creation failed.");
        }
        return blog;
    }

    /**
     * Creates the default topic of a client.
     *
     * @param blogName
     *            Name of the topic.
     * @throws NonUniqueBlogIdentifierException
     *             if the new topic uses an non unique topic identifier
     * @throws BlogIdentifierValidationException
     *             if the new topic alias is not valid
     * @return the default topic of the client
     */
    private Blog internalCreateDefaultBlog(String blogName)
            throws NonUniqueBlogIdentifierException, BlogIdentifierValidationException {
        BlogTO blogDetails = new BlogTO();
        blogDetails.setTitle(blogName);
        blogDetails.setNameIdentifier(DEFAULT_BLOG_ALIAS);
        blogDetails.setAllCanRead(true);
        blogDetails.setAllCanWrite(true);
        blogDetails.setTags(new HashSet<TagTO>());
        blogDetails.getTags().add(
                new TagTO("default", TagStoreType.Types.BLOG.getDefaultTagStoreId()));
        List<User> clientManager = userManagement.findUsersByRole(
                UserRole.ROLE_KENMEI_CLIENT_MANAGER, UserStatus.ACTIVE);
        for (User manager : clientManager) {
            blogDetails.setCreatorUserId(manager.getId());
            break;
        }
        Blog defaultBlog = internalCreateBlog(blogDetails);
        return defaultBlog;
    }

    /**
     * Internal method for topic update. Does not change the allCan flags.
     *
     * @param blog
     *            the topic
     * @param blogDetails
     *            the topic details
     * @throws NonUniqueBlogIdentifierException
     *             case no perfect identifier is found
     * @throws BlogIdentifierValidationException
     *             if the identifier is not valid
     */
    private void internalUpdateBlogData(Blog blog, BlogTO blogDetails)
            throws NonUniqueBlogIdentifierException, BlogIdentifierValidationException {
        setBlogIdentifier(blog, blogDetails);
        blog.setDescription(blogDetails.getDescription());
        blog.setTitle(blogDetails.getTitle().trim());
        blog.setCreateSystemNotes(blogDetails.isCreateSystemNotes());
        blog.setLastModificationDate(new Timestamp(System.currentTimeMillis()));
        blog.getTags().clear();
        if (blogDetails.getUnparsedTags() != null) {
            Set<String> lowerTagnames = new HashSet<String>(Arrays.asList(blogDetails
                    .getUnparsedTags()));
            for (String tagStr : lowerTagnames) {
                tagStr = StringHelper.cleanString(tagStr);
                if (StringUtils.isEmpty(tagStr)) {
                    continue;
                }
                TagTO tagTO = new TagTO(tagStr, TagStoreType.Types.BLOG);
                try {
                    Tag tag = tagManagement.storeTag(tagTO);
                    blog.getTags().add(tag);
                } catch (TagNotFoundException e) {
                    LOGGER.error("A tag was not found or couldn't be created:" + e.getMessage());
                } catch (TagStoreNotFoundException e) {
                    LOGGER.error("A tag store was not found:" + e.getMessage());
                }
            }
        }
        if (blogDetails.getTags() != null) {
            for (TagTO tagTO : blogDetails.getTags()) {
                try {
                    Tag tag = tagManagement.storeTag(tagTO);
                    blog.getTags().add(tag);
                } catch (TagNotFoundException e) {
                    // TODO Was soll hier passieren? KENMEI-4246
                    throw new RuntimeException(e);
                } catch (TagStoreNotFoundException e) {
                    // TODO Was soll hier passieren? KENMEI-4246
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * This method moves posts if an existing topic to another. Move posts to the new topic. Does no
     * update, saves or delete on the old topic
     *
     * @param targetBlogId
     *            The topic id of the new topic.
     * @param sourceBlog
     *            The old topic.
     * @throws BlogNotFoundException
     *             The exception.
     * @throws NoteManagementAuthorizationException
     *             if the user has no authorization to this topic
     */
    private void movePostsToBlog(Long targetBlogId, Blog sourceBlog) throws BlogNotFoundException,
            NoteManagementAuthorizationException {
        Assert.notNull(targetBlogId, "topic id for the new topic must be set");
        Assert.notNull(sourceBlog, "source topic for post moving must be set");
        Assert.isTrue(!targetBlogId.equals(sourceBlog.getId()),
                "target topic can not have same id as the source topic!");
        UserSecurityHelper.assertIsManagerOfBlog(sourceBlog.getId());
        Blog targetBlog = blogDao.load(targetBlogId);
        if (targetBlog == null) {
            return;
        }
        if (!blogRightsManagement.userHasWriteAccess(targetBlogId,
                SecurityHelper.getCurrentUserId(), false)) {
            throw new AccessDeniedException("You don't have write access to this topic.");
        }

        long firstNoteId = 0;
        List<Note> notes;
        while (!(notes = noteDao.getNotesForBlog(sourceBlog.getId(), firstNoteId, 200)).isEmpty()) {
            notes: for (Note note : notes) {
                firstNoteId = note.getId();
                // posts with creation source SYSTEM shouldn't transfered to the targetBlog
                if (NoteCreationSource.SYSTEM.equals(note.getCreationSource())
                        && note.getParent() == null) {
                    ServiceLocator.instance().getService(NoteService.class)
                            .deleteNote(note.getId(), true, false);
                    continue notes;
                }
                note.setBlog(targetBlog);
                note.getFollowableItems().remove(sourceBlog.getGlobalId());
                note.getFollowableItems().add(targetBlog.getGlobalId());
                note.setCrawlLastModificationDate(new Timestamp(new Date().getTime()));
                noteDao.update(note);
            }
            firstNoteId++;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetGlobalPermissions() {
        SecurityHelper.assertCurrentUserIsClientManager();
        blogDao.resetGlobalPermissions();
    }

    /**
     * Set the topic alias for the topic. If the alias is not contained in the TO this method tries
     * to generate a unique alias from the topic title.
     *
     * @param blog
     *            the topic to change
     * @param blogDetails
     *            the topic details
     * @throws NonUniqueBlogIdentifierException
     *             If the identifier is not unique
     * @throws BlogIdentifierValidationException
     *             if the identifier is not valid
     */
    private void setBlogIdentifier(Blog blog, BlogTO blogDetails)
            throws BlogIdentifierValidationException, NonUniqueBlogIdentifierException {
        String identifier = StringUtils.trimToEmpty(blogDetails.getNameIdentifier());
        if (identifier.length() == 0) {
            identifier = generateUniqueBlogAlias(blogDetails.getTitle(), blog.getNameIdentifier());
        }

        // can not change topic identifier, if external object is linked with topic
        if (blog.getNameIdentifier() != null && !blog.getNameIdentifier().equals(identifier)
                && blog.getExternalObjects().size() != 0) {
            String message = "Can not change topic identifier if an external object is linked with topic.";
            LOGGER.error(message);
            throw new BlogManagementException(message);
        }
        BlogManagementHelper.validateNameIdentifier(identifier);
        blog.setNameIdentifier(identifier);
    }

    /**
     * Update the properties of a topic.
     *
     * @param blog
     *            the topic to update
     * @param blogDetails
     *            the transfer object holding the topic properties
     */
    private void updateBlogProperties(Blog blog, BlogTO blogDetails) {
        if (blogDetails.getProperties() != null) {
            try {
                propertyManagement.setObjectProperties(PropertyType.BlogProperty, blog.getId(),
                        new HashSet<StringPropertyTO>(blogDetails.getProperties()));
            } catch (NotFoundException e) {
                LOGGER.error("Unexpected exception while updating properties topic with id "
                        + blog.getId());
                throw new BlogManagementException("Unexpected exception.", e);
            } catch (AuthorizationException e) {
                LOGGER.error("Unexpected exception while updating properties topic with id "
                        + blog.getId() + " and the current user "
                        + SecurityHelper.getCurrentUserAlias());
                throw new BlogManagementException("Unexpected exception.", e);
            }
        }
    }

}
