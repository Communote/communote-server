package com.communote.server.web.fe.portal.blog.validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.communote.common.validation.EmailValidator;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.web.fe.portal.blog.forms.BlogManagementForm;

/**
 * Validates the user data input for creating a new blog.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogManagementValidator implements Validator {

    /** The Constant MAX_INPUT_STRING_LENGTH. */
    private static final int MAX_TITLE_INPUT_STRING_LENGTH = 1024;
    /** The Constant MIN_INPUT_STRING_LENGTH. */
    private static final int MIN_INPUT_STRING_LENGTH = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return BlogManagementForm.class.equals(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Object target, Errors errors) {
        BlogManagementForm form = (BlogManagementForm) target;
        ValidationHelper.validateString("title", StringUtils.trim(form.getTitle()), true,
                MAX_TITLE_INPUT_STRING_LENGTH, MIN_INPUT_STRING_LENGTH, null, errors);

        // is not mandatory
        ValidationHelper.validateString("nameIdentifier",
                StringUtils.trimToEmpty(form.getNameIdentifier()), false,
                EmailValidator.MAX_SAFE_LENGTH_LOCAL_PART, 0,
                BlogManagementHelper.REG_EXP_TOPIC_NAME_IDENTIFIER,
                errors);
    }
}
