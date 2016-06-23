package com.communote.plugins.atlr;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.communote.common.string.StringEscapeHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.processor.NoteContentRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteMetadataRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.blog.export.PermalinkGenerationManagement;
import com.communote.server.core.blog.notes.processors.ExtractTagsNotePreProcessor;
import com.communote.server.core.user.helper.ValidationPatterns;

/**
 * Implementation of the NoteRenderingPreProcessor which replaces @-notifications and hash-tags with
 * links for filtering.
 * 
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AliasAndTagLinkRenderer implements NoteContentRenderingPreProcessor {

    private static final String LINK_END = "</a>";

    private static final String WEB_LINK_CLASS = "class=\"control-atlr-link control-entity-link control-filter-entity-link\"";
    private static final String WEB_ALIAS_LINK_START = "<a " + WEB_LINK_CLASS
            + " href=\"{PERMALINK}\" data-cnt-user-id=\"{ENTITY-ID}\" data-cnt-entity-details=\"{";
    private static final String WEB_ALIAS_LINK_MID = "}\"><span class=\"alias-marker\">@</span>";
    private static final String WEB_TAG_LINK_START = "<a " + WEB_LINK_CLASS
            + " href=\"{PERMALINK}\" data-cnt-tag-id=\"{ENTITY-ID}\" data-cnt-entity-details=\"{";
    private static final String WEB_TAG_LINK_MID =
            "}\"><span class=\"tag-marker\">#</span>";

    private final boolean keepDoubleQuotes;

    private final Pattern aliasPattern;
    private final PermalinkGenerationManagement permalinkGenerator;

    /**
     * Creates a new alias and tag link renderer.
     */
    public AliasAndTagLinkRenderer() {
        permalinkGenerator = ServiceLocator.instance().getService(
                PermalinkGenerationManagement.class);
        aliasPattern = Pattern.compile("(^|[\\s;,\\[(>])@(" + ValidationPatterns.PATTERN_ALIAS
                + ")");
        keepDoubleQuotes = true;
    }

    /**
     * @return {@link NoteMetadataRenderingPreProcessor#DEFAULT_ORDER}
     */
    @Override
    public int getOrder() {
        return NoteMetadataRenderingPreProcessor.DEFAULT_ORDER;
    }

    @Override
    public boolean isCachable() {
        // TODO not cachable due to HTTPS stuff. Well, could cache if force SSL is set ...
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean processNoteContent(NoteRenderContext context, NoteData item)
            throws NoteRenderingPreProcessorException {
        if (!NoteRenderMode.PORTAL.equals(context.getMode())) {
            throw new NoteRenderingPreProcessorException("Unsupported mode " + context.getMode(),
                    null);
        }
        boolean modified = false;
        boolean httpsLinks = context.getRequest() != null ? context.getRequest().isSecure() : true;
        modified = renderTagLinks(item, httpsLinks);

        if (modified) {
            renderAliasLinks(item, httpsLinks);
        } else {
            modified = renderAliasLinks(item, httpsLinks);
        }

        return modified;
    }

    /**
     * Renders all alias-links for the users to be notified who were added by the @-notation.
     * 
     * @param item
     *            the item to be modified
     * @param httpsLinks
     *            whether to render HTTPS permalinks
     * @return whether the item was modified
     */
    private boolean renderAliasLinks(NoteData item, boolean httpsLinks) {
        if (item.getNotifiedUsers() != null && item.getNotifiedUsers().size() != 0) {
            String content = item.getContent();
            int oldLength = content.length();
            Map<String, UserData> usersNotified = new HashMap<String, UserData>(item
                    .getNotifiedUsers().size());
            for (UserData i : item.getNotifiedUsers()) {
                usersNotified.put(i.getAlias().toLowerCase(), i);
            }
            content = renderAliasLinks(content, usersNotified, httpsLinks);
            item.setContent(content);
            String shortContent = item.getShortContent();
            if (shortContent != null && oldLength != content.length()) {
                shortContent = renderAliasLinks(shortContent, usersNotified, httpsLinks);
                item.setShortContent(shortContent);
            }
            // just assume something was replaced
            return true;
        }
        return false;
    }

    /**
     * Replaces all @-alias strings of the existing users that were notified with links.
     * 
     * @param content
     *            the content to process
     * @param usersNotified
     *            a mapping from alias to user details of all users that were notified
     * @param httpsLinks
     *            whether to render HTTPS permalinks
     * @return the processed content
     */
    private String renderAliasLinks(String content, Map<String, UserData> usersNotified,
            boolean httpsLinks) {
        Matcher m = aliasPattern.matcher(content);
        int offset = 0;
        StringBuilder newContent = new StringBuilder();
        while (m.find()) {
            // only rewrite if actual alias is referenced
            String alias = m.group(2);
            String aliasLowerCase = alias.toLowerCase();
            if (usersNotified.containsKey(aliasLowerCase)) {
                newContent.append(content.substring(offset, m.start()));
                newContent.append(m.group(1));
                UserData userListItem = usersNotified.get(aliasLowerCase);
                String replacementStart = WEB_ALIAS_LINK_START.replace("{PERMALINK}",
                        permalinkGenerator.getUserLink(userListItem.getAlias(), httpsLinks));
                replacementStart = replacementStart.replace("{ENTITY-ID}", userListItem.getId()
                        .toString());
                newContent.append(replacementStart);
                newContent.append("&quot;firstName&quot;:&quot;");
                if (userListItem.getFirstName() != null) {
                    newContent.append(StringEscapeHelper.escapeJavaScriptInlineHtml(userListItem
                            .getFirstName()));
                }
                newContent.append("&quot;,&quot;lastName&quot;:&quot;");
                if (userListItem.getLastName() != null) {
                    newContent.append(StringEscapeHelper.escapeJavaScriptInlineHtml(userListItem
                            .getLastName()));
                }
                newContent.append("&quot;,&quot;alias&quot;:&quot;");
                newContent.append(userListItem.getAlias());
                newContent.append("&quot;");
                newContent.append(WEB_ALIAS_LINK_MID);
                // add the alias the way it was written by the author
                newContent.append(alias);
                newContent.append(LINK_END);
                offset = m.end();
            }
        }
        if (offset == 0) {
            return content;
        }
        newContent.append(content.substring(offset));
        return newContent.toString();
    }

    /**
     * Renders all tag-links for the hash-tags found in the note.
     * 
     * @param item
     *            the item to be modified
     * @param httpsLinks
     *            whether to render HTTPS permalinks
     * @return whether something was modified
     */
    private boolean renderTagLinks(NoteData item, boolean httpsLinks) {
        if (item.getTags().size() > 0) {
            String content = item.getContent();
            int oldLength = content.length();

            Map<String, TagData> tags = new HashMap<String, TagData>();
            for (TagData tag : item.getTags()) {
                if (Types.NOTE.getDefaultTagStoreId().equals(tag.getTagStoreAlias())) {
                    tags.put(tag.getDefaultName().toLowerCase(), tag);
                }
            }
            content = renderTagLinks(content, this.keepDoubleQuotes, tags, httpsLinks);
            item.setContent(content);
            // only process shortened content if full content changed
            String shortContent = item.getShortContent();
            if (shortContent != null && oldLength != content.length()) {
                shortContent = renderTagLinks(shortContent, this.keepDoubleQuotes, tags, httpsLinks);
                item.setShortContent(shortContent);
            }
            // just assume something was replaced
            return true;
        }
        return false;
    }

    /**
     * Replaces all hash-tags with links. The tag-value without quotes is inserted between
     * linkReplacementStart and linkReplacementMid. The tag-value is inserted a 2nd time between
     * linkReplacementMid and linkReplacementEnd with quotes iff keepQuotes is true otherwise
     * without quotes.
     * 
     * @param content
     *            the content to process
     * @param keepQuotes
     *            whether double quotes surrounding the tag-value of a hash-tag are to be kept
     * @param tags
     *            Tags to use for rendering.
     * @param httpsLinks
     *            whether to render HTTPS permalinks
     * @return the modified content
     */
    private String renderTagLinks(String content, boolean keepQuotes,
            Map<String, TagData> tags, boolean httpsLinks) {
        Matcher matcher = ExtractTagsNotePreProcessor.TAG_PATTERN.matcher(content);
        int offset = 0;
        StringBuilder newContent = new StringBuilder();
        while (matcher.find()) {
            int tagValueGroup = matcher.group(3) != null ? 3 : 2;
            String tagAlias = matcher.group(tagValueGroup);
            TagData tag = tags.get(tagAlias.toLowerCase());
            if (tag == null) {
                continue;
            }
            newContent.append(content.substring(offset, matcher.start()));
            // add replacement start
            newContent.append(matcher.group(1));
            String replacementStart = WEB_TAG_LINK_START.replace("{PERMALINK}",
                    permalinkGenerator.getTagLink(tag.getId(), httpsLinks));
            replacementStart = replacementStart.replace("{ENTITY-ID}", tag.getId()
                    .toString());
            newContent.append(replacementStart);
            newContent.append("&quot;tagName&quot;:&quot;");
            // no need to JS escape the tag value because the hash-tag recognition won't match tags
            // that contain quotes
            newContent.append(tagAlias);
            newContent.append("&quot;");
            newContent.append(WEB_TAG_LINK_MID);
            if (keepQuotes) {
                newContent.append(matcher.group(2));
            } else {
                newContent.append(tagAlias);
            }
            newContent.append(LINK_END);
            offset = matcher.end();
        }
        if (offset == 0) {
            return content;
        }
        newContent.append(content.substring(offset));
        return newContent.toString();
    }

    @Override
    public boolean replacesContent() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(NoteRenderMode mode, NoteData note) {
        return NoteRenderMode.PORTAL.equals(mode);
    }
}
