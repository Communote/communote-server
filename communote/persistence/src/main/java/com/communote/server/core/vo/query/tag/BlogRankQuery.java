package com.communote.server.core.vo.query.tag;

import java.util.ArrayList;
import java.util.List;

import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.vo.query.blog.BlogQuery;
import com.communote.server.model.tag.TagConstants;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class BlogRankQuery extends AbstractBlogTagQuery<RankTagListItem> {
    private final ArrayList<String> constructorParameter = new ArrayList<String>();

    {
        constructorParameter.add("min(" + BlogQuery.ALIAS_TAGS + "." + TagConstants.ID
                + "),count(*), min(" + BlogQuery.ALIAS_TAGS + "."
                + TagConstants.DEFAULTNAME + ")");
    }

    /**
     * Constructor.
     */
    public BlogRankQuery() {
        super(RankTagListItem.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getConstructorParameter() {
        return this.constructorParameter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<RankTagListItem> getResultListItem() {
        return RankTagListItem.class;
    }
}
