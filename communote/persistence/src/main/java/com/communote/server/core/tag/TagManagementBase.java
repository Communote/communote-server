package com.communote.server.core.tag;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.Converter;
import com.communote.server.model.tag.Tag;


/**
 * <p>
 * Spring Service base class for
 * <code>com.communote.server.service.tag.TagManagement</code>, provides access to all
 * services and entities referenced by this service.
 * </p>
 * 
 * @see com.communote.server.core.tag.TagManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class TagManagementBase implements TagManagement {

    /**
     * {@inheritDoc}
     */
    public void assignGlobalIdForTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.TagManagement.assignGlobalIdForTagNewTx(Tag tag) - 'tag' can not be null");
        }
        try {
            this.handleAssignGlobalIdForTag(tag);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.TagManagementException(
                    "Error performing 'com.communote.server.service.tag.TagManagement.assignGlobalIdForTag(Tag tag)' --> "
                            + rt, rt);
        }
    }

    /**
     * @throws TagNotFoundException
     *             Exception.
     * @throws TagStoreNotFoundException
     *             Exception.
     * @see com.communote.server.core.tag.TagManagement#createTag(String)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Tag createTag(String name) throws TagNotFoundException, TagStoreNotFoundException {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.TagManagement.createTag(String name) - 'name' can not be null or empty");
        }
        try {
            return this.handleCreateTag(name);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.TagManagementException(
                    "Error performing 'com.communote.server.service.tag.TagManagement.createTag(String name)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.core.tag.TagManagement#findOrCreateTag(String)
     */
    public Tag findOrCreateTag(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.TagManagement.findOrCreateTag(String name) - 'name' can not be null or empty");
        }
        try {
            return this.handleFindOrCreateTag(name);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.TagManagementException(
                    "Error performing 'com.communote.server.service.tag.TagManagement.findOrCreateTag(String name)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T findTag(Long id, Converter<Tag, T> converter) {
        Tag tag = findTag(id);
        return tag == null ? null : converter.convert(tag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T findTagByTagStoreNewTx(String tagStoreTagId, String tagStoreAlias,
            Converter<Tag, T> converter) {
        Tag tag = findTagByTagStoreNewTx(tagStoreTagId, tagStoreAlias);
        return tag == null ? null : converter.convert(tag);
    }

    /**
     * @see com.communote.server.core.tag.TagManagement#findTagNewTx(String)
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Tag findTagNewTx(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.tag.TagManagement.findTagNewTx(String name) - 'name' can not be null or empty");
        }
        try {
            return this.handleFindTagNewTx(name);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.tag.TagManagementException(
                    "Error performing 'com.communote.server.service.tag.TagManagement.findTagNewTx(String name)' --> "
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
     * Performs the core logic for {@link #assignGlobalIdForTag(Tag)}
     * 
     * @param tag
     *            the tag to modify
     */
    protected abstract void handleAssignGlobalIdForTag(Tag tag);

    /**
     * Performs the core logic for {@link #createTag(String)}
     * 
     * @param name
     *            The name of the tag.
     * @throws TagNotFoundException
     *             TagNotFoundException.
     * @throws TagStoreNotFoundException
     *             Exception.
     */
    protected abstract Tag handleCreateTag(String name) throws TagNotFoundException,
            TagStoreNotFoundException;

    /**
     * Performs the core logic for {@link #findOrCreateTag(String)}
     * 
     * @param name
     *            The name of the tag.
     */
    protected abstract Tag handleFindOrCreateTag(String name);

    /**
     * Performs the core logic for {@link #findTagNewTx(String)}
     * 
     * @param name
     *            The name of the tag.
     */
    protected abstract Tag handleFindTagNewTx(String name);

}