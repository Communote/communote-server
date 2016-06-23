package com.communote.server.persistence.tag;

import com.communote.server.core.vo.tag.CategorizedTagVO;
import com.communote.server.model.tag.CategorizedTag;


/**
 * @see com.communote.server.model.tag.CategorizedTag
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CategorizedTagDaoImpl extends
        com.communote.server.persistence.tag.CategorizedTagDaoBase {

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.tag.CategorizedTagDao#categorizedTagVOToEntity(com.communote.server.core.vo.tag.CategorizedTagVO)
     */
    public CategorizedTag categorizedTagVOToEntity(CategorizedTagVO categorizedTagVO) {
        CategorizedTag tag = CategorizedTag.Factory.newInstance();
        categorizedTagVOToEntity(categorizedTagVO, tag, true);
        return tag;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.tag.CategorizedTagDaoBase#categorizedTagVOToEntity(com.communote.server.core.vo.tag.CategorizedTagVO,
     *      com.communote.server.model.tag.CategorizedTag, boolean)
     */
    @Override
    public void categorizedTagVOToEntity(
            com.communote.server.core.vo.tag.CategorizedTagVO source,
            com.communote.server.model.tag.CategorizedTag target, boolean copyIfNull) {
        if (copyIfNull || source.getName() != null) {
            target.setDefaultName(source.getName());
            if (source.getName() != null) {
                target.setTagStoreTagId(source.getName().toLowerCase());
            } else {
                target.setTagStoreTagId(null);
            }
        }
    }

}
