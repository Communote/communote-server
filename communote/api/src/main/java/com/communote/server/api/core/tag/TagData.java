package com.communote.server.api.core.tag;

import java.io.Serializable;
import java.util.Locale;

import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.tag.TagName;

/**
 * Value object holding details about a tag.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagData extends IdentifiableEntityData implements Serializable, TagName {
    private static final long serialVersionUID = 7901448318043924658L;

    private String tagStoreTagId;

    private String tagStoreAlias;

    private String defaultName;

    private String name;

    private String description;

    private Locale locale;

    /**
     * Does nothing.
     */
    public TagData() {
        // Do nothing.
    }

    /**
     * Contructor for identifier and name
     *
     * @param id
     *            identifier of tag
     * @param defaultName
     *            default name of the tag
     */
    public TagData(Long id, String defaultName) {
        this(defaultName);
        this.setId(id);
    }

    public TagData(String defaultName) {
        super();
        this.defaultName = defaultName;
        // init name with default name, should be overwritten by converters with translation
        this.name = defaultName;
    }

    /**
     * @param tag
     *            The tag to use the data from.
     */
    public TagData(Tag tag) {
        this(tag.getDefaultName());
        setId(tag.getId());
        // TODO not quite correct because it does not take translations into account
        name = tag.getName();
        tagStoreTagId = tag.getTagStoreTagId();
        tagStoreAlias = tag.getTagStoreAlias();
    }

    /**
     * Copies constructor from other TagData
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public TagData(TagData otherBean) {
        copy(otherBean);
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(TagData tagListItem) {
        if (tagListItem != null) {
            name = tagListItem.getName();
            defaultName = tagListItem.getDefaultName();
            tagStoreAlias = tagListItem.getTagStoreAlias();
            tagStoreTagId = tagListItem.getTagStoreTagId();
            description = tagListItem.getTagStoreTagId();
            super.copy(tagListItem);
        }
    }

    /**
     * @return the defaultName
     */
    public String getDefaultName() {
        return defaultName;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     *
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @return the tagStoreAlias
     */
    public String getTagStoreAlias() {
        return tagStoreAlias;
    }

    /**
     * @return the tagStoreTagId
     */
    public String getTagStoreTagId() {
        return tagStoreTagId;
    }

    /**
     * @param defaultName
     *            the defaultName to set
     */
    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param locale
     *            the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param tagStoreAlias
     *            the tagStoreAlias to set
     */
    public void setTagStoreAlias(String tagStoreAlias) {
        this.tagStoreAlias = tagStoreAlias;
    }

    /**
     * @param tagStoreTagId
     *            the tagStoreTagId to set
     */
    public void setTagStoreTagId(String tagStoreTagId) {
        this.tagStoreTagId = tagStoreTagId;
    }

}