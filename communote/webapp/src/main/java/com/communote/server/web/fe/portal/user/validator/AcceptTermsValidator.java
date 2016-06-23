package com.communote.server.web.fe.portal.user.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.communote.server.web.fe.portal.user.forms.AcceptTermsForm;

/**
 * Validator for the accept terms form.
 * 
 * @see com.communote.server.web.fe.portal.user.controller.AcceptTermsController
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AcceptTermsValidator implements Validator {

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.validation.Validator#supports(Class)
     */
    public boolean supports(Class<?> clazz) {
        return AcceptTermsForm.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.validation.Validator#validate(Object,
     *      org.springframework.validation.Errors)
     */
    public void validate(Object object, Errors errors) {
        AcceptTermsForm form = (AcceptTermsForm) object;
        if (!form.isTermsAgreed()) {
            errors.rejectValue("termsAgreed", "error.register.agree.terms.conditions",
                    "The terms of use are not agreed!");
        }
    }

}
