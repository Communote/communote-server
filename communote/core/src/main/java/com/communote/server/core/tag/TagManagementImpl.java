package com.communote.server.core.tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.converters.TagToTagDataQueryResultConverter;
import com.communote.server.core.vo.query.tag.RankTagQuery;
import com.communote.server.core.vo.query.tag.TagQueryParameters;
import com.communote.server.model.global.GlobalId;
import com.communote.server.model.i18n.Message;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.Language;
import com.communote.server.persistence.common.messages.MessageDao;
import com.communote.server.persistence.global.GlobalIdDao;
import com.communote.server.persistence.tag.TagDao;
import com.communote.server.persistence.tag.TagStore;
import com.communote.server.persistence.user.LanguageDao;

/**
 * Implements the core logic for {@link TagManagement}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("tagManagement")
public class TagManagementImpl extends TagManagementBase {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TagManagementImpl.class);

    @Autowired
    private TagDao tagDao;

    @Autowired
    private LanguageDao languageDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private TagStoreManagement tagStoreManagement;

    @Autowired
    private GlobalIdDao globalIdDao;

    @Autowired
    private QueryManagement queryManagement;

    private final static String TAG_NAME_PREFIX = "com.communote.tag.name.";
    private final static String TAG_DESCRIPTION_PREFIX = "com.communote.tag.description.";

    /**
     * Checks all conditions for a valid tag.
     * 
     * @param tag
     *            The tag.
     */
    private void assertValidTag(TagTO tag) {
        if (tag.getDefaultName() == null || tag.getDefaultName().trim().length() == 0) {
            throw new TagManagementException(
                    "There was a tag without default name, what is not allowed.",
                    "tag.management.error.no-default-name", tag.getName());
        }

    }

    /**
     * Creates a tag in the database. Instead of calling this method directly, storeTag should be
     * used.
     * 
     * Throws a DataIntegrityViolationException if the tag already exists
     * 
     * The operation is run in a new transaction.
     * 
     * @param tag
     *            The TO describing the tag to create filled with all details.
     * @param tagStore
     *            The tag store.
     * @return The tag created within the database.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Tag createTagInDatabaseNewTx(TagTO tag, TagStore tagStore) {
        Tag databaseTag = Tag.Factory.newInstance(tag.getTagStoreTagId(),
                tag.getDefaultName(), tag.getTagStoreAlias());
        databaseTag = tagDao.create(databaseTag);
        handleAssignGlobalIdForTag(databaseTag);
        updateNamesAndDescriptions(tag, databaseTag, tagStore);
        return databaseTag;
    }

    /**
     * Retrieves a tag by its TagStore definition.
     * 
     * @param tagStoreTagId
     *            the ID of the within the tag store
     * @param tagStoreAlias
     *            the alias of the tag store
     * @return the found tag or null if not found
     */
    private Tag findByTagStore(String tagStoreTagId, String tagStoreAlias) {
        return tagDao.findByTagStore(tagStoreTagId, tagStoreAlias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tag findTag(Long id) {
        Tag tag = tagDao.load(id);
        if (tag != null) {
            Hibernate.initialize(tag.getNames());
            Hibernate.initialize(tag.getDescriptions());
        }
        return tag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagData findTag(Long id, Locale locale) {
        Tag tag = tagDao.load(id);
        if (tag != null) {
            return new TagToTagDataQueryResultConverter(locale).convert(tag);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Tag findTag(String name, TagStoreType.Types type) {
        TagStore tagStore = tagStoreManagement.getTagStore(null, type);
        // TODO this makes the assumption that the tagStoreTagId can be calculated from the
        // defaultName and all the other fields are ignored. This works for the DefaultTagStore but
        // must not for others. This shortcut should only be used if the store is the default
        // internal TagStore. For other stores a search on defaultName + tagStoreTagId should be
        // done (and more than 1 result should be returned)
        String tagStoreTagId = tagStore.getTagStoreTagId(Tag.Factory.newInstance(
                type.getDefaultTagStoreId(), name, name));
        Tag tag = tagDao.findByTagStore(tagStoreTagId, tagStore.getTagStoreId());
        if (tag != null) {
            Hibernate.initialize(tag.getNames());
            Hibernate.initialize(tag.getDescriptions());
        }
        return tag;
    }

    /**
     * @param tag
     *            The tag.
     * @return An existing tag for the specified TagTO or null, if none available.
     * @throws TagNotFoundException
     *             Thrown, when a tag id was specified, but the tag can't be found.
     */
    private Tag findTag(TagTO tag) throws TagNotFoundException {
        if (tag.getId() != null) {
            Tag existingTag = findTag(tag.getId());
            if (existingTag == null) {
                throw new TagNotFoundException("The tag with the id " + tag.getId()
                        + "doesn't exists.");
            }
            return existingTag;
        }
        return findByTagStore(tag.getTagStoreTagId(), tag.getTagStoreAlias());
    }

    /**
     * Find the tag by its name in a default tag store, using a little trick. If the store is not
     * one of the default tag stores null is returned.
     * 
     * @param name
     *            the name of the tag to find
     * @param tagStore
     *            the default tag store
     * @return the found tag or null
     */
    private Tag findTagByNameInDefaultStore(String name, TagStore tagStore) {
        if (tagStoreManagement.isDefaultTagStore(tagStore)) {
            String tagStoreTagId = tagStore.getTagStoreTagId(Tag.Factory.newInstance(
                    "dummy", name, tagStore.getTagStoreId()));
            return tagDao.findByTagStore(tagStoreTagId, tagStore.getTagStoreId());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Tag findTagByTagStoreNewTx(String tagStoreTagId, String tagStoreAlias) {
        Tag tag = findByTagStore(tagStoreTagId, tagStoreAlias);
        if (tag != null) {
            tag.getFollowId().getFollowers();
            Hibernate.initialize(tag.getNames());
            Hibernate.initialize(tag.getDescriptions());
        }
        return tag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public int getCount(Long id) {
        RankTagQuery queryDefinition = new RankTagQuery();
        TagQueryParameters queryInstance = queryDefinition.createInstance();
        queryInstance.getTagIds().add(id);
        queryInstance.setHideSelectedTags(false);
        TaggingCoreItemUTPExtension typeSpecificExtension = new TaggingCoreItemUTPExtension();
        typeSpecificExtension.setTopicAccessLevel(TopicAccessLevel.READ);
        typeSpecificExtension.setUserId(SecurityHelper.assertCurrentUserId());
        queryInstance.setTypeSpecificExtension(typeSpecificExtension);
        List<?> result = queryManagement.executeQueryComplete(queryDefinition, queryInstance);
        if (result == null || result.isEmpty()) {
            return 0;
        }
        for (Object object : result) {
            RankTagListItem rankTagListItem = (RankTagListItem) object;
            if (rankTagListItem.getId().equals(id)) {
                return rankTagListItem.getRank().intValue();
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleAssignGlobalIdForTag(Tag tag) {
        if (tag.getGlobalId() != null) {
            return;
        }
        GlobalId gid = globalIdDao.createGlobalId(tag);
        tag.setGlobalId(gid);
        // in case the entity is detached (when called from outside)
        tagDao.update(tag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Tag handleCreateTag(String defaultName) throws TagNotFoundException,
            TagStoreNotFoundException {
        TagTO tag = new TagTO(defaultName, Types.NOTE.getDefaultTagStoreId());
        return storeTag(tag);
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link #storeTag(TagTO)} instead.
     */
    @Override
    @Deprecated
    protected Tag handleFindOrCreateTag(String name) {
        Tag tag = findTag(name, Types.NOTE);

        if (tag == null) {
            TagTO newTag = new TagTO(name, Types.NOTE.getDefaultTagStoreId());
            TagManagement tagManagement = ServiceLocator.instance().getService(TagManagement.class);
            Exception exception = null;
            try {
                // Create the tag using a new transaction to force a unique constraint exception
                tag = tagManagement.storeTag(newTag);
            } catch (DataIntegrityViolationException e) {
                // Catch the exception, this is the PostgreSQL handling
                exception = e;
            } catch (TagManagementException e) {
                // Catch the exception, this is the MySQL handling. It is not 100% clear why this
                // exception is thrown here, maybe MySQL recognizes the unique constraint
                // conflict earlier
                exception = e;
            } catch (TagNotFoundException e) {
                exception = e;
            } catch (TagStoreNotFoundException e) {
                exception = e;
            }
            if (tag == null) {
                // In case the tag was created by somebody else asynchronously, the unique
                // constraint leads to an exception, so we just load the tag again
                // use a new transaction, assures to avoid nested transaction conflicts
                tag = tagManagement.findTagNewTx(name);
                if (tag == null && exception != null) {
                    LOGGER.error("Tag '{}' has not been retrieved.", name, exception);
                }
            }
        }
        // in case the tag could not be found nor created throw an exception
        if (tag == null) {
            LOGGER.error("Tag '{}' could not be found nor created!", name);
            throw new TagManagementException("Tag for name '" + name
                    + "' could not be created nor found!");
        }
        return tag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Tag handleFindTagNewTx(String name) {
        Tag tag = findTag(name, Types.NOTE);
        if (tag != null) {
            tag.getFollowId().getFollowers();
        }
        return tag;
    }

    @Override
    public void removeNoteTag(long oldTagId, Long newTagId) throws AuthorizationException,
            TagNotFoundException {
        if (!SecurityHelper.isClientManager() && !SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException("User not manager or internal system user.");
        }
        Tag tag = tagDao.load(oldTagId);
        if (tag == null) {
            return;
        }
        TagStore store = tagStoreManagement.getTagStore(tag.getTagStoreAlias(), null);
        if (store == null || store.getTypes().length > 1 || !Types.NOTE.equals(store.getTypes()[0])) {
            throw new IllegalArgumentException(
                    "The tag to remove is not in a supported NoteTagStore");
        }
        if (newTagId != null) {
            if (newTagId.equals(oldTagId)) {
                // nothing to do
                return;
            }
            Tag newTag = tagDao.load(newTagId);
            if (newTag == null) {
                throw new TagNotFoundException("The new tag that should be assigned does not exist");
            }
            store = tagStoreManagement.getTagStore(newTag.getTagStoreAlias(), null);
            if (store == null || !store.canHandle(Types.NOTE)) {
                throw new TagNotFoundException(
                        "The new tag that should be assigned is not in a NoteTagStore");
            }
        }
        List<Long> followers = tagDao.getFollowers(oldTagId);
        tagDao.removeNoteTag(oldTagId, newTagId);
        if (followers.size() > 0) {
            SecurityContext orgContext = null;
            if (!SecurityHelper.isInternalSystem()) {
                orgContext = AuthenticationHelper.setInternalSystemToSecurityContext();
            }
            try {
                ServiceLocator.findService(FollowManagement.class).tagRemoved(oldTagId, newTagId,
                        followers);
            } catch (TagNotFoundException | AuthorizationException e) {
                LOGGER.error("Unexpected exception updating followers after merging tags", e);
                throw new TagManagementException("Unexpected exception", e);
            } finally {
                if (orgContext != null) {
                    AuthenticationHelper.setSecurityContext(orgContext);
                }
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void renameTag(final long tagId, final String newName) throws AuthorizationException,
            TagNotFoundException, TagAlreadyExistsException {
        if (!SecurityHelper.isClientManager() && !SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException("User not manager or internal system user.");
        }
        final TagStore defaultNoteTagStore = tagStoreManagement.getTagStore(
                Types.NOTE.getDefaultTagStoreId(), null);
        if (defaultNoteTagStore == null) {
            throw new IllegalStateException("Default NoteTagStore does not exist");
        }
        RunInTransaction renameTagAction = new RunInTransaction() {

            @Override
            public void execute() throws TransactionException {
                Tag tag = tagDao.load(tagId);
                if (tag == null) {
                    // no TransactionException because there is no need for a rollback
                    throw new RuntimeException(new TagNotFoundException(
                            "The tag to rename does not exist"));
                }
                if (!defaultNoteTagStore.getTagStoreId().equals(tag.getTagStoreAlias())) {
                    throw new IllegalArgumentException("Only internal tags can be renamed.");
                }
                // try to avoid ConstraintViolations while writing by checking if the new tag exist
                Tag existingTag = findTagByNameInDefaultStore(newName, defaultNoteTagStore);
                if (existingTag != null && !existingTag.getId().equals(tagId)) {
                    throw new RuntimeException(new TagAlreadyExistsException(
                            "There is already a tag with the new name", existingTag.getId(),
                            newName, tag.getTagStoreAlias()));
                }
                tag.setDefaultName(newName);
                // TODO this is a weird concept. Why should the tagStore return another
                // tagStoreTagId only because of a change to the defaultName? Works for the
                // DefaultTagStore with the toLowerCase stuff, but this is internal knowledge we
                // rely on.
                tag.setTagStoreTagId(defaultNoteTagStore.getTagStoreTagId(tag));
            }
        };
        try {
            ServiceLocator.findService(TransactionManagement.class).executeInNew(renameTagAction);
        } catch (DataIntegrityViolationException e) {
            Tag existingTag = findTagByNameInDefaultStore(newName, defaultNoteTagStore);
            throw new TagAlreadyExistsException(e.getMessage(), existingTag.getId(), newName,
                    defaultNoteTagStore.getTagStoreId());
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                if (cause instanceof TagNotFoundException) {
                    throw (TagNotFoundException) cause;
                } else if (cause instanceof TagAlreadyExistsException) {
                    throw (TagAlreadyExistsException) cause;
                }
            }
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tag storeTag(TagTO tag) throws TagNotFoundException,
            TagStoreNotFoundException {
        TagStore tagStore = tagStoreManagement.getTagStore(tag.getTagStoreAlias(),
                tag.getTagStoreAlias() == null ? tag.getTagStoreType() : null);
        if (tagStore == null) {
            throw new TagStoreNotFoundException("No TagStore found for the given tag: "
                    + tag.getTagStoreAlias() + " type: " + tag.getTagStoreType());
        }
        tag.setTagStoreAlias(tagStore.getTagStoreId());
        if (tag.getTagStoreTagId() == null) {
            tag.setTagStoreTagId(tagStore.getTagStoreTagId(tag));
        }
        Tag existingTag = findTag(tag);
        if (existingTag != null) {
            updateNamesAndDescriptions(tag, existingTag, tagStore);
        } else {
            assertValidTag(tag);
            TagManagement tagManagement = ServiceLocator.instance().getService(TagManagement.class);
            try {
                existingTag = tagManagement.createTagInDatabaseNewTx(tag, tagStore);
            } catch (DataIntegrityViolationException e) {
                // In case the tag was created by somebody else asynchronously, the unique
                // constraint leads to an exception, so we just load the tag again
                // using a new transaction to avoid transaction isolation effects
                existingTag = tagManagement.findTagByTagStoreNewTx(tag.getTagStoreTagId(),
                        tag.getTagStoreAlias());
                if (existingTag != null) {
                    updateNamesAndDescriptions(tag, existingTag, tagStore);
                } else {
                    LOGGER.error("The tag can't be found, but should: {}", e.getMessage(), e);
                }
            }
        }
        return existingTag;
    }

    /**
     * @param tag
     *            The tag containing desciptions.
     * @param databaseTag
     *            The database tag.
     * @param languageCodeToMessage
     *            The language code mapping.
     */
    private void updateDescriptions(TagTO tag, Tag databaseTag,
            HashMap<String, Message> languageCodeToMessage) {
        if (databaseTag.getDescriptions() == null) {
            databaseTag.setDescriptions(new HashSet<Message>());
        }
        for (Message description : databaseTag.getDescriptions()) {
            languageCodeToMessage.put((TAG_DESCRIPTION_PREFIX
                    + databaseTag.getId() + description.getLanguage().getLanguageCode())
                    .toLowerCase(), description);
        }
        for (Message description : tag.getDescriptions()) {
            Language language = description.getLanguage();
            if (languageCodeToMessage.get((TAG_DESCRIPTION_PREFIX + databaseTag.getId()
                    + language.getLanguageCode()).toLowerCase()) == null) {
                description.setMessageKey(TAG_DESCRIPTION_PREFIX + databaseTag.getId());
                Language databaseLanguage = languageDao.findByLanguageCode(language
                        .getLanguageCode());
                if (databaseLanguage == null) {
                    LOGGER.info("Tried to add a tag description for non existing language: {}",
                            language.getLanguageCode());
                    continue;
                }
                description.setLanguage(databaseLanguage);
                // TODO will fail if name has an ID that is not null
                messageDao.create(description);
                databaseTag.getDescriptions().add(description);
            }
        }
    }

    /**
     * @param tag
     *            The tag containing names.
     * @param databaseTag
     *            The database tag.
     * @param languageCodeToMessage
     *            The language code mapping.
     */
    private void updateNames(TagTO tag, Tag databaseTag,
            HashMap<String, Message> languageCodeToMessage) {
        if (databaseTag.getNames() == null) {
            databaseTag.setNames(new HashSet<Message>());
        }
        for (Message name : databaseTag.getNames()) {
            languageCodeToMessage.put((TAG_NAME_PREFIX + databaseTag.getId()
                    + name.getLanguage().getLanguageCode()).toLowerCase(), name);
        }
        // Register non-existing localizations
        for (Message name : tag.getNames()) {
            Language language = name.getLanguage();
            if (languageCodeToMessage.get((TAG_NAME_PREFIX + databaseTag.getId()
                    + language.getLanguageCode()).toLowerCase()) == null) {
                name.setMessageKey(TAG_NAME_PREFIX + databaseTag.getId());
                Language databaseLanguage = languageDao.findByLanguageCode(language
                        .getLanguageCode());
                if (databaseLanguage == null) {
                    LOGGER.info("Tried to add a tag description for non existing language: {}",
                            language.getLanguageCode());
                    continue;
                }
                name.setLanguage(databaseLanguage);
                // TODO will fail if name has an ID that is not null
                messageDao.create(name);
                databaseTag.getNames().add(name);
            }
        }
    }

    /**
     * Adds new localized names or description to the database.
     * 
     * @param tag
     *            The tag with possible new localizations.
     * @param databaseTag
     *            The existing tag.
     * @param tagStore
     *            The TagStore.
     */
    private void updateNamesAndDescriptions(TagTO tag, Tag databaseTag, TagStore tagStore) {
        if (tagStore == null || !tagStore.isMultilingual()) {
            return;
        }
        HashMap<String, Message> languageCodeToMessage = new HashMap<>();
        updateNames(tag, databaseTag, languageCodeToMessage);
        updateDescriptions(tag, databaseTag, languageCodeToMessage);
    }
}
