package com.communote.server.web.api.service.post.convert;

import com.communote.server.api.core.blog.BlogData;
import com.communote.server.core.vo.query.QueryResultConverter;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogListItemConverter extends
        QueryResultConverter<BlogData, com.communote.server.web.api.to.listitem.BlogListItem> {

    @Override
    public boolean convert(BlogData source,
            com.communote.server.web.api.to.listitem.BlogListItem target) {
        // code taken from
        // com.communote.server.core.vo.query.blog.BlogInfoQueryParameters.transformResultItem(Object)
        // to get rid of this class from core
        target.setId(source.getId());
        target.setLastModificationDate(source.getLastModificationDate());
        // not setting the creation date since we don't want to change the old REST API, this is ok
        // since this instance isn't used anywhere else (the transform stuff should be used anyway)
        target.setTitle(source.getTitle());
        return true;
    }

    @Override
    public com.communote.server.web.api.to.listitem.BlogListItem create() {
        // TODO Auto-generated method stub
        return new com.communote.server.web.api.to.listitem.BlogListItem();
    }

}
