package com.communote.server.core.blog.export;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.communote.common.util.UriUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.ssl.ChannelManagement;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.tag.Tag;
import com.communote.server.persistence.user.client.ClientUrlHelper;

/**
 * Helper class for the perma links
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DefaultPermalinkGenerator implements PermalinkGenerator {

    /** Logger. */
    private final static Logger LOG = Logger.getLogger(DefaultPermalinkGenerator.class);

    /**
     * {@inheritDoc}.
     */
    @Override
    public String extractPermaLinkIdentifier(String[] uriFragments) {
        String permaLinkIdent = null;
        if (uriFragments.length >= 2) {
            if (uriFragments.length >= 3
                    && StringUtils.lowerCase(uriFragments[uriFragments.length - 3]).equals(
                            PERMA_ID_TAGS.toLowerCase())) {
                permaLinkIdent = PERMA_ID_TAGS.toLowerCase();
            } else {
                permaLinkIdent = StringUtils.lowerCase(uriFragments[uriFragments.length - 2]);
            }

            // extract the "."
            if (permaLinkIdent != null && permaLinkIdent.contains(".")) {
                String[] split = permaLinkIdent.split("\\.");
                if (split.length > 0) {
                    permaLinkIdent = split[0];
                }
            }
        }

        if (permaLinkIdent != null && matchesPortal(uriFragments, permaLinkIdent)) {
            return permaLinkIdent;
        }

        return null;
    }

    @Override
    public final String getBlogLink(String blogAlias) {
        return getBlogLink(blogAlias, false);
    }

    @Override
    public String getBlogLink(String blogAlias, boolean secure) {
        if (StringUtils.isEmpty(blogAlias)) {
            throw new IllegalArgumentException("blogAlias cannot be null or empty");
        }
        return getPermalink(PERMA_ID_TOPICS, blogAlias, secure);
    }

    /**
     * throws UnsupportedOperationException
     *
     * {@inheritDoc}
     *
     */
    public String getDiscussionLink(String blogAlias, Long discussionId, boolean secure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final String getNoteLink(String blogAlias, Long noteId) {
        return getNoteLink(blogAlias, noteId, false);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public String getNoteLink(String blogAlias, Long noteId, boolean secure) {
        if (noteId == null) {
            throw new IllegalArgumentException("noteId cannot be null");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(PERMA_ID_TOPICS);
        sb.append("/");
        sb.append(blogAlias);
        sb.append("/");
        sb.append(PERMA_ID_NOTES);
        return getPermalink(sb.toString(), noteId.toString(), secure);
    }

    /**
     * @param identifier
     *            the permanent link identifier path
     * @param value
     *            the value.
     * @param secure
     *            If true, https will be used.
     * @return the perma link for the identifier and value
     */
    private String getPermalink(String identifier, String value, boolean secure) {
        return ClientUrlHelper.renderConfiguredAbsoluteUrl("/portal/" + identifier + "/" + value,
                secure || useHttps());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTagLink(long tagId) {
        return getTagLink(tagId, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTagLink(long tagId, boolean secure) {
        Tag tag = ServiceLocator.instance().getService(TagManagement.class).findTag(tagId);
        if (tag == null) {
            throw new IllegalArgumentException("tag cannot be null or empty");
        }
        return getPermalink(PERMA_ID_TAGS,
                tagId + "/" + UriUtils.encodeUriComponent(tag.getTagStoreTagId()), secure);
    }

    @Override
    public String getTagLink(String tag) {
        return getTagLink(tag, false);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public String getTagLink(String tag, boolean secure) {
        if (StringUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("tag cannot be null or empty");
        }
        return getPermalink(PERMA_ID_TAGS, UriUtils.encodeUriComponent(tag), secure);
    }

    @Override
    public final String getUserLink(String userAlias) {
        return getUserLink(userAlias, false);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public String getUserLink(String userAlias, boolean secure) {
        if (StringUtils.isEmpty(userAlias)) {
            throw new IllegalArgumentException("userAlias cannot be null or empty");
        }
        return getPermalink(PERMA_ID_USERS, userAlias, secure);
    }

    /**
     * Check if the permalink url matches "portal" on the correct position
     *
     * @param uriFragments
     *            the uri fragments
     * @param permaLinkIdent
     *            the perma link identifier
     * @return true if the fragements are matching the portal path
     */
    private boolean matchesPortal(String[] uriFragments, String permaLinkIdent) {
        // check if the fragment matches "portal"
        int portalIndex = -1;
        if (PERMA_ID_TOPICS.equals(permaLinkIdent) || PERMA_ID_USERS.equals(permaLinkIdent)) {
            portalIndex = uriFragments.length - 3;
        } else if (PERMA_ID_NOTES.equals(permaLinkIdent)) {
            portalIndex = uriFragments.length - 5;
        } else if (PERMA_ID_TAGS.equals(permaLinkIdent)
                && (StringUtils.equalsIgnoreCase("portal", uriFragments[uriFragments.length - 4]) || StringUtils
                        .equalsIgnoreCase("portal", uriFragments[uriFragments.length - 3]))) {
            return true;
        }

        if (portalIndex < 0 || !StringUtils.equalsIgnoreCase("portal", uriFragments[portalIndex])) {
            return false;
        }
        return true;
    }

    /**
     * @return true if the links should be rendered with https
     */
    private boolean useHttps() {
        return ServiceLocator.findService(ChannelManagement.class).isForceSsl(ChannelType.WEB);
    }

}
