package com.communote.server.api.core.security;

import com.communote.common.converter.Converter;
import com.communote.server.model.security.SecurityCode;

/**
 * Management class for security codes.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface SecurityCodeManagement {

    /**
     * Delete all codes of the given user.
     *
     * @param userId
     *            The user.
     * @param clazz
     *            The type of security code.
     */
    public void deleteAllCodesByUser(Long userId, Class<? extends SecurityCode> clazz);

    /**
     * Find the security code by the code value.
     * 
     * @param code
     *            The code value
     * @return The code as object or null, if there is none.
     */
    public SecurityCode findByCode(String code);

    /**
     * Find the security code by the code value and convert it.
     *
     * @param code
     *            The code value
     * @param converter
     *            the converter to create the result object from the found code
     * @param <T>
     *            type of the conversion result
     * @return The converted code or null, if there is none.
     */
    public <T> T findByCode(String code, Converter<SecurityCode, T> converter);

    /**
     * Remove the given code.
     *
     * @param id
     *            Id of the code to remove.
     */
    public void removeCode(Long id);
}
