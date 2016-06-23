package com.communote.server.persistence.tag;

import com.communote.server.model.tag.GlobalTagCategory;

/**
 * @see com.communote.server.model.tag.GlobalTagCategory
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GlobalTagCategoryDaoImpl extends
        com.communote.server.persistence.tag.GlobalTagCategoryDaoBase {

    /**
     * Global tag category vo to entity.
     * 
     * @param globalTagCategoryVO
     *            the global tag category vo
     * @return the global tag category
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#globalTagCategoryVOToEntity(com.communote.server.core.vo.tag.GlobalTagCategoryVO)
     */
    public com.communote.server.model.tag.GlobalTagCategory globalTagCategoryVOToEntity(
            com.communote.server.core.vo.tag.GlobalTagCategoryVO globalTagCategoryVO) {
        GlobalTagCategory category = GlobalTagCategory.Factory.newInstance();
        globalTagCategoryVOToEntity(globalTagCategoryVO, category, true);
        return category;
    }

}
