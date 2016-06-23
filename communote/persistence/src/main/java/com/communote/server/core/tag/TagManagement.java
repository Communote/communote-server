package com.communote.server.core.tag;

import java.util.Locale;

import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.model.tag.Tag;
import com.communote.server.persistence.tag.TagStore;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface TagManagement {

    /**
     * Assigns the globalId to a tag if there is not yet a suitable globalId a new one will be
     * created. If the tag already has a globalId nothing will change.
     * 
     * @param tag
     *            The tag.
     */
    public void assignGlobalIdForTag(Tag tag);

    /**
     * Create the given tag using a new transaction, the new transacion will only be create when
     * using this method via <code>ServiceLocator.getTagMangement()</code>
     * 
     * @param name
     *            Name of the tag.
     * @return The created tag.
     * @throws TagNotFoundException
     *             TagNotFoundException.
     * @throws TagStoreNotFoundException
     *             Exception.
     * @deprecated Use createTag(Tag tag) instead.
     */
    @Deprecated
    public Tag createTag(String name) throws TagNotFoundException, TagStoreNotFoundException;

    /**
     * Creates a tag in the database. Instead of calling this method directly, always use storeTag!
     * 
     * The operation is run in a new transaction.
     * 
     * Throws DataIntegrityViolationException if the tag already exists
     * 
     * @param tag
     *            The TO describing the tag to create filled with all details.
     * @param tagStore
     *            The tag store.
     * @return The tag created within the database.
     * 
     */
    public Tag createTagInDatabaseNewTx(TagTO tag, TagStore tagStore);

    /**
     * Tries to find the tag with the given name. If it does not exists it will be created using the
     * {@link create} method using a new transaction. If this one fails, due to a already exists
     * constraint, it will be tried to recover.
     * 
     * @param name
     *            Name of the tag.
     * @return The found or created tag.
     * @deprecated Use findOrCreateTag(Tag tag) instead.
     */
    @Deprecated
    public Tag findOrCreateTag(String name);

    /**
     * @param id
     *            Id of the tag.
     * @return The tag by it's id.
     */
    @Transactional(readOnly = true)
    public Tag findTag(Long id);

    /**
     * Method to find a tag and convert it to a final type.
     * 
     * @param id
     *            Id of the tag.
     * @param converter
     *            The converter to use.
     * @param <T>
     *            Final type of the tag.
     * @return Converted tag for the given id.
     */
    @Transactional(readOnly = true)
    public <T> T findTag(Long id, Converter<Tag, T> converter);

    /**
     * @param id
     *            Id of the tag.
     * @param locale
     *            The locale to use.
     * @return The localized tag as TagData or null if the tag does not exist.
     */
    @Transactional(readOnly = true)
    public TagData findTag(Long id, Locale locale);

    /**
     * Finds a tag
     * 
     * @param name
     *            Name of the tag.
     * @param type
     *            Type of the tag.
     * @return The found tag or null.
     */
    public Tag findTag(String name, TagStoreType.Types type);

    /**
     * Retrieves a tag by its TagStore definition.
     * 
     * The operation is run in a new transaction.
     * 
     * @param tagStoreTagId
     *            the ID of the within the tag store
     * @param tagStoreAlias
     *            the alias of the tag store
     * @return the found tag or null if not found
     */
    public Tag findTagByTagStoreNewTx(String tagStoreTagId, String tagStoreAlias);

    /**
     * Retrieves a tag by its TagStore definition.
     * 
     * The operation is run in a new transaction.
     * 
     * @param tagStoreTagId
     *            the ID of the within the tag store
     * @param tagStoreAlias
     *            the alias of the tag store
     * @param converter
     *            The converter to use.
     * @param <T>
     *            Final type of the tag.
     * @return Converted tag for the given id.
     */
    public <T> T findTagByTagStoreNewTx(String tagStoreTagId, String tagStoreAlias,
            Converter<Tag, T> converter);

    /**
     * Find the given tag using a _new_ transaction, the new transacion will only be create then
     * using this methode over the <code>ServiceLocator.getTagMangement()</code>
     * 
     * This method should be only called internal from #findOrCreateTag!
     * 
     * @param name
     *            Name of the tag.
     * @return The found tag or null.
     */
    public Tag findTagNewTx(String name);

    /**
     * @param id
     *            The id of the tag.
     * @return Returns the usage count of the given tag within the users access restrictions.
     */
    public int getCount(Long id);

    /**
     * Remove a note tag. Optionally a new tag can be provided to add it to the notes as a
     * replacement of the old tag. If the tag to remove does not exist nothing will happen. If there
     * are followers of the oldTag these users will automatically follow the newTag if a new tag
     * should be set.
     * 
     * @param oldTagId
     *            the ID of the tag to remove
     * @param newTagId
     *            optional ID of tag to add the notes from which the other tag is removed
     * @throws AuthorizationException
     *             in case the current user is not the internal system user or a client manager
     * @throws TagNotFoundException
     *             in case newTagId is referring to non-existing tag
     */
    public void removeNoteTag(long oldTagId, Long newTagId) throws AuthorizationException,
            TagNotFoundException;

    /**
     * Rename an existing internal note tag.
     * 
     * @param tagId
     *            Id of the tag.
     * @param newName
     *            The new default name.
     * @throws AuthorizationException
     *             in case the current user is not the internal system user or a client manager
     * @throws TagNotFoundException
     *             in case the tag to rename does not exist
     * @throws IllegalArgumentException
     *             in case the tag to rename is not an internal tag
     * @throws TagAlreadyExistsException
     *             in case the there is already an existing internal note tag with the new name
     */
    public void renameTag(long tagId, String newName) throws AuthorizationException,
            TagNotFoundException, IllegalArgumentException, TagAlreadyExistsException;

    /**
     * Creates a new tag based on the given parameters or updates an existing tag.
     * 
     * @param tag
     *            The tag to create.
     * @return The created tag.
     * @throws TagNotFoundException
     *             TagNotFoundException.
     * @throws TagStoreNotFoundException
     *             TagStoreNotFoundException.
     */
    public Tag storeTag(TagTO tag) throws TagNotFoundException, TagStoreNotFoundException;

}
