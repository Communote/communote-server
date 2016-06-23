package com.communote.server.core.tag;

import java.util.Collection;
import java.util.List;

import com.communote.common.util.Pair;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.tag.TagName;


/**
 * A tag parser tags an string and splits them into single words (the tags)
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface TagParser {

    /**
     * This method builds a string tag list out of a collection of tags. It is the reverse method of
     * findTags. The tags will not be sorted.
     * 
     * @param tags
     *            the tag list
     * @param <T>
     *            The type implementing the {@link TagName} interface
     * @return the tag list as string, separated by a tag separator
     */
    public <T extends TagName> String buildTagString(Collection<T> tags);

    /**
     * This method builds a string tag list out of a collection of tags. It is the reverse method of
     * findTags
     * 
     * @param tags
     *            the tag list
     * @param sort
     *            true if the list should be sorted by the name of the tags
     * @param <T>
     *            The type implementing the {@link TagName} interface
     * @return the tag list as string, separated by a tag separator
     */
    public <T extends TagName> String buildTagString(Collection<T> tags, boolean sort);

    /**
     * This method builds a string tag list out of a collection of tags. It is the reverse method of
     * findTags. The tags will not be sorted.
     * 
     * @param tags
     *            the tag list
     * @param <T>
     *            The type implementing the {@link TagName} interface
     * @return the tag list as string, separated by a tag separator
     */
    public <T extends TagName> String buildTagString(String[] tags);

    /**
     * Combines two tags to a list.
     * 
     * @param tagList
     *            A previous list of tags (or a single tag)
     * @param tagsToAdd
     *            The new tags to combine (or a single tag)
     * @return The combined list
     */
    public String combineTags(String tagList, String tagsToAdd);

    /**
     * Parses the tags and gets them from the database, and creates them if necassary.
     * 
     * @param unparsedTags
     *            The unparsed tags
     * @return A pair, where the left side are the created or found tags and the right site the tags
     *         with problems.
     */
    public Pair<List<Tag>, List<String>> findOrCreateTags(String unparsedTags);

    /**
     * Parses the tags and gets them from the database. If the tag is not existing it will be
     * ignored.
     * 
     * @param unparsedTags
     *            The unparsed tags
     * @return The tags
     */
    public List<Tag> findTags(String unparsedTags);

    /**
     * Get the separator which separates the tags
     * 
     * @return the separator
     */
    public String getSeparator();

    /**
     * Parse a string of one or more tags into an array of tags. A null value will result in an
     * empty array.
     * 
     * @param unparsedTags
     *            The unparsed tags
     * @return The tags
     */
    public String[] parseTags(String unparsedTags);
}
