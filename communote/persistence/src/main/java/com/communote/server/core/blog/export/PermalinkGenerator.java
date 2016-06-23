package com.communote.server.core.blog.export;

/**
 * Interface for defining generation permalinks.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface PermalinkGenerator {

    /**
     * The permalink identifier for topics.
     */
    public final static String PERMA_ID_TOPICS = "topics";

    /**
     * The permalink identifier for users
     */
    public final static String PERMA_ID_USERS = "users";

    /**
     * The permalink identifier for tags
     */
    public final static String PERMA_ID_TAGS = "tags";

    /**
     * The permalink identifier for notes
     */
    public final static String PERMA_ID_NOTES = "notes";

    /**
     * Get the type of perma link (one of the constants {@link #PERMA_ID_TOPICS} etc)
     * 
     * @param uriFragments
     *            the splitted uri fragments
     * @return the perma link identifier (that is 'blogs', 'users', 'tags' ...)
     */
    public abstract String extractPermaLinkIdentifier(String[] uriFragments);

    /**
     * Calls {@link #getBlogLink(String, false)}.
     * 
     * @param blogAlias
     *            the alias of the blog to get the link for
     * @return the absolute perma link for the blog
     */
    public abstract String getBlogLink(String blogAlias);

    /**
     * @param blogAlias
     *            the alias of the blog to get the link for
     * @param secure
     *            If true https will be used.
     * @return the absolute perma link for the blog
     */
    public abstract String getBlogLink(String blogAlias, boolean secure);

    /**
     * Calls {@link #getNoteLink(String, Long, false)}.
     * 
     * @param blogAlias
     *            the blog alias of the note to get the link for
     * @param noteId
     *            the id of the note to get the link for
     * @return the perma link for the note
     */
    public abstract String getNoteLink(String blogAlias, Long noteId);

    /**
     * @param blogAlias
     *            the blog alias of the note to get the link for
     * @param noteId
     *            the id of the note to get the link for
     * @param secure
     *            If true, https will be used.
     * @return the perma link for the note
     */
    public abstract String getNoteLink(String blogAlias, Long noteId, boolean secure);

    /**
     * Calls {@link #getTagLink(String, false)}.
     * 
     * @param tagId
     *            The tag to get the link for
     * @return The permanent link for the tag.
     */
    public abstract String getTagLink(long tagId);

    /**
     * @param tagId
     *            the tag to get the link for
     * @param secure
     *            If true, https will be used.
     * @return the perma link for the tag
     */
    public abstract String getTagLink(long tagId, boolean secure);

    /**
     * Calls {@link #getTagLink(String, false)}.
     * 
     * @param tag
     *            The tag to get the link for
     * @return The permanent link for the tag.
     * @deprecated Use {@link #getTagLink(long)} instead.
     */
    @Deprecated
    public abstract String getTagLink(String tag);

    /**
     * @param tag
     *            the tag to get the link for
     * @param secure
     *            If true, https will be used.
     * @return the perma link for the tag
     * @deprecated Use {@link #getTagLink(long, boolean)} instead.
     */
    @Deprecated
    public abstract String getTagLink(String tag, boolean secure);

    /**
     * Calls {@link #getUserLink(String,false)}.
     * 
     * @param userAlias
     *            the alias of the user to get the link for
     * @return the perma link for the user
     */
    public abstract String getUserLink(String userAlias);

    /**
     * @param userAlias
     *            the alias of the user to get the link for
     * @param secure
     *            If true, https will be used.
     * @return the perma link for the user
     */
    public abstract String getUserLink(String userAlias, boolean secure);

}