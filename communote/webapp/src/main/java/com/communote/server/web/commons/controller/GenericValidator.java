package com.communote.server.web.commons.controller;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            Type of the class for this validator.
 */
public abstract class GenericValidator<T> implements Validator {
    /**
     * @see Validator#validate(Object, Errors)
     * @param form
     *            The form to check.
     * @param errors
     *            Errors.
     */
    public abstract void doValidate(T form, Errors errors);

    /**
     * @param clazz
     *            to test.
     * @return True if clazz could be casted to T and element.getClass().isAssignableFrom(clazz) is
     *         valid.
     */
    public boolean supports(Class<?> clazz) {
        try {
            T element = (T) clazz;
            return element != null;
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Calls doValidate.
     * 
     * {@inheritDoc}
     */
    public void validate(Object target, Errors errors) {
        doValidate((T) target, errors);
    }
}
