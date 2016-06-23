package com.communote.server.persistence.property;

import java.util.List;

import com.communote.server.core.vo.IdDateTO;
import com.communote.server.model.property.BinaryPropertyConstants;
import com.communote.server.model.property.PropertyConstants;

/**
 * @see com.communote.server.model.property.BinaryProperty
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BinaryPropertyDaoImpl extends BinaryPropertyDaoBase {

    /**
     * {@inheritDoc}
     */
    @Override
    protected IdDateTO handleFindIdByKey(String keyGroup, String key) {
        @SuppressWarnings("unchecked")
        List<IdDateTO> result = getHibernateTemplate().find(
                "select new " + IdDateTO.class.getName() + "(p.id, p."
                        + PropertyConstants.LASTMODIFICATIONDATE + ") from "
                        + BinaryPropertyConstants.CLASS_NAME + " p where p."
                        + PropertyConstants.KEYGROUP + "=? and p." + PropertyConstants.PROPERTYKEY
                        + "=?", new Object[] { keyGroup, key });
        return result.size() > 0 ? result.iterator().next() : null;
    }
}