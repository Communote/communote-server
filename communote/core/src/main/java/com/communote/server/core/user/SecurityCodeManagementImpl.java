package com.communote.server.core.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.converter.Converter;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.persistence.common.security.SecurityCodeDao;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("securityCodeManagement")
public class SecurityCodeManagementImpl extends SecurityCodeManagementBase {

    @Autowired
    private SecurityCodeDao securityCodeDao;

    @Override
    protected void handleDeleteAllCodesByUSer(Long userId, Class<? extends SecurityCode> clazz) {
        securityCodeDao.deleteAllCodesByUser(userId, clazz);
    }

    @Override
    protected SecurityCode handleFindByCode(String code) {
        return securityCodeDao.findByCode(code);
    }

    @Override
    protected <T> T handleFindByCode(String code, Converter<SecurityCode, T> converter) {
        SecurityCode secCode = securityCodeDao.findByCode(code);
        if (code != null) {
            return converter.convert(secCode);
        }
        return null;
    }

    @Override
    protected void handleRemoveCode(Long id) {
        securityCodeDao.remove(id);
    }

}
