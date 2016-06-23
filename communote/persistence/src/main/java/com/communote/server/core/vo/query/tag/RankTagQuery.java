package com.communote.server.core.vo.query.tag;

import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.model.tag.TagConstants;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RankTagQuery extends AbstractTagQuery<RankTagListItem> {

    /**
     * the parameters of the list item constructor
     */
    private final static String[] CONSTRUCTOR_PARAMETER = new String[] {
            "min(tag." + TagConstants.ID + ")",
            "count(*)",
            "min(tag." + TagConstants.DEFAULTNAME + ")"
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConstructorParameters() {
        return CONSTRUCTOR_PARAMETER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<RankTagListItem> getResultListItem() {
        return RankTagListItem.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        super.setupQueries();
    }
}
