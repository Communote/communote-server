package com.communote.server.core.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.ArrayHelper;
import com.communote.common.util.Pair;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.tag.TagConstants;
import com.communote.server.model.tag.TagName;


/**
 * A tag parser tags an string and splits them into single words (the tags) by a separator
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractTagParser implements TagParser {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTagParser.class);

    private static final String[] EMPTY_STRING_ARRAY = new String[] { };

    private final String separator;

    private final Pattern splitPattern;

    // comparator to sort tags by name
    private final BeanComparator tagNameComparator = new BeanComparator(TagConstants.DEFAULTNAME);

    /**
     * constructor for setting the separator
     * 
     * @param separator
     *            the tag separator
     */
    protected AbstractTagParser(String separator) {
        this.separator = separator;
        this.splitPattern = Pattern.compile(separator);
    }

    /**
     * {@inheritDoc}
     */
    public <T extends TagName> String buildTagString(Collection<T> tags) {
        return buildTagString(tags, false);
    }

    /**
     * {@inheritDoc}
     */
    public <T extends TagName> String buildTagString(Collection<T> tags, boolean sort) {

        // do nothing if the tags are empty
        if (tags == null || tags.size() == 0) {
            return StringUtils.EMPTY;
        }

        Collection<T> toStringTags;
        if (sort) {
            // sort the tags by name
            List<T> tagsAsList = new ArrayList<T>(tags.size());
            tagsAsList.addAll(tags);
            Collections.sort(tagsAsList, tagNameComparator);
            toStringTags = tagsAsList;
        } else {
            toStringTags = tags;
        }
        StringBuilder stringTagList = new StringBuilder();
        String prefix = StringUtils.EMPTY;
        for (TagName tag : toStringTags) {
            stringTagList.append(prefix);
            stringTagList.append(tag.getName());
            prefix = separator;
        }
        return stringTagList.toString();
    }

    /**
     * {@inheritDoc}
     */
    public <T extends TagName> String buildTagString(String[] tags) {
        // do nothing if the tags are empty
        if (tags == null || tags.length == 0) {
            return StringUtils.EMPTY;
        }
        StringBuilder stringTagList = new StringBuilder();
        String prefix = StringUtils.EMPTY;
        for (String tag : tags) {
            stringTagList.append(prefix);
            stringTagList.append(tag);
            prefix = separator;
        }
        return stringTagList.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String combineTags(String tagList, String tagsToAdd) {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotEmpty(tagList)) {
            sb.append(tagList);
            if (!tagList.endsWith(separator)) {
                sb.append(separator);
            }
        }
        sb.append(tagsToAdd);

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Pair<List<Tag>, List<String>> findOrCreateTags(String unparsedTags) {
        return findTags(unparsedTags, true);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tag> findTags(String unparsedTags) {
        return findTags(unparsedTags, false).getLeft();
    }

    /**
     * Parses the tags and gets them from the database. Depending on the flag create an non existing
     * tag will be created or ignored
     * 
     * @param unparsedTags
     *            The unparsed tags
     * @param create
     *            flag which tells if a missing tag should be created in the database
     * @return The tags
     */
    private Pair<List<Tag>, List<String>> findTags(String unparsedTags, boolean create) {
        String[] tagNames = parseTags(unparsedTags);
        Set<String> lowerTagNames = new HashSet<String>();
        List<Tag> tags = new ArrayList<Tag>(tagNames.length);
        ArrayList<String> tagsWithProblems = new ArrayList<String>();
        for (String tagName : tagNames) {
            if (StringUtils.isNotBlank(tagName)) {
                Tag tag;
                if (create) {
                    try {
                        tag = ServiceLocator.findService(TagManagement.class)
                                .storeTag(new TagTO(tagName, TagStoreType.Types.NOTE));
                    } catch (TagNotFoundException e) {
                        tagsWithProblems.add(tagName);
                        LOGGER.warn("A tag wasn't found or couldn't be created: {}, {}", tagName,
                                e.getMessage());
                        continue;
                    } catch (TagStoreNotFoundException e) {
                        tagsWithProblems.add(tagName);
                        LOGGER.warn("A tag store wasn't found or: {}, {}", tagName, e.getMessage());
                        continue;
                    }
                } else {
                    tag = ServiceLocator.findService(TagManagement.class).findTag(tagName, Types.NOTE);
                }
                // avoid duplicate tag entries, must check the lower case version of the tag because
                // we treat tags with same lower name as equal
                if (!lowerTagNames.contains(tag.getTagStoreTagId())) {
                    lowerTagNames.add(tag.getTagStoreTagId());
                    tags.add(tag);
                }
            }
        }
        return new Pair<List<Tag>, List<String>>(tags, tagsWithProblems);
    }

    /**
     * {@inheritDoc}
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * {@inheritDoc}
     */
    public String[] parseTags(String unparsedTags) {
        String[] tags = unparsedTags == null ? EMPTY_STRING_ARRAY : splitPattern
                .split(unparsedTags);
        tags = ArrayHelper.removeDuplicatesKeepingOrder(tags, true);
        // trim tags
        for (int i = 0; i < tags.length; i++) {
            tags[i] = tags[i].trim();
        }
        return tags;
    }
}
