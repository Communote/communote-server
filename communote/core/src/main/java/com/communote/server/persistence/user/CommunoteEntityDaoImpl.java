package com.communote.server.persistence.user;

import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.persistence.helper.dao.LazyClassLoaderHelper;
import com.communote.server.persistence.user.CommunoteEntityDaoBase;


/**
 * @see com.communote.server.model.user.CommunoteEntity
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteEntityDaoImpl extends CommunoteEntityDaoBase {
    /**
     * {@inheritDoc}
     */
    @Override
    protected CommunoteEntity handleLoadWithImplementation(Long id) {
        CommunoteEntity entity = load(id);
        if (entity != null) {
            entity = LazyClassLoaderHelper.deproxy(entity, CommunoteEntity.class);
        }
        return entity;
    }

}
