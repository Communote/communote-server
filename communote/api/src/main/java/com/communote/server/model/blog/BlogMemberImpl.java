package com.communote.server.model.blog;

/**
 * @see com.communote.server.model.blog.BlogMember
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogMemberImpl extends BlogMember {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3658233814338516261L;

    /**
     * @see com.communote.server.model.blog.BlogMember#getExternalSystemId()
     * @return null
     */
    @Override
    public String getExternalSystemId() {
        // always return null for non external members
        return null;
    }

}