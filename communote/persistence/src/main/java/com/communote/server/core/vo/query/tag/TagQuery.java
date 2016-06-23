package com.communote.server.core.vo.query.tag;

import com.communote.server.api.core.tag.TagData;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagQuery extends AbstractTagQuery<TagData> {

    @Override
    public Class<TagData> getResultListItem() {
        return TagData.class;
    }

}
