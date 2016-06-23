package com.communote.server.api.core.tag;

import com.communote.server.model.tag.TagImpl;

/**
 * Tag which adds information about the tag able it is used for.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
// TODO must not inherit from tag!
public class TagTO extends TagImpl {

    private static final long serialVersionUID = 8677851887481973910L;
    private TagStoreType type;

    /**
     * @param defaultName
     *            The default name of this tag.
     * @param tagStoreAlias
     *            The alias of the TagStore the tag is from.
     */
    public TagTO(String defaultName, String tagStoreAlias) {
        setDefaultName(defaultName);
        setTagStoreAlias(tagStoreAlias);
    }

    /**
     * @param defaultName
     *            The default name of this tag.
     * @param type
     *            The type of this tag.
     */
    public TagTO(String defaultName, TagStoreType type) {
        setDefaultName(defaultName);
        setTagStoreType(type);
    }

    /**
     * @return The tagable.
     */
    public TagStoreType getTagStoreType() {
        return type;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setTagStoreType(TagStoreType type) {
        this.type = type;
    }
}
