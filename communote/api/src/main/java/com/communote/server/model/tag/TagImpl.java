package com.communote.server.model.tag;

import com.communote.server.model.global.GlobalId;

/**
 * @see com.communote.server.model.tag.Tag
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagImpl extends Tag {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3431749183125894204L;

    /**
     * {@inheritDoc}
     */
    @Override
    public GlobalId getFollowId() {
        return getGlobalId();
    }

    /**
     * @return getDefaultName()
     */
    @Override
    public String getName() {
        return getDefaultName();
    }
}
