package com.communote.server.web.fe.portal.user.client.validator;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.web.fe.portal.user.client.forms.ConfluenceAuthConfigurationForm;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfluenceAuthConfigurationValidator implements Validator {

    private static final int MAX_INPUT_STRING_LENGTH = 255;
    private static final int MIN_INPUT_STRING_LENGTH = 1;

    /**
     * {@inheritDoc}
     */
    public boolean supports(Class clazz) {
        return ConfluenceAuthConfigurationForm.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Object target, Errors errors) {
        ConfluenceAuthConfigurationForm form = (ConfluenceAuthConfigurationForm) target;
        ConfluenceConfiguration config = form.getConfig();

        // only validate if active
        if (config.isAllowExternalAuthentication() || config.isSynchronizeUserGroups()) {
            ValidationHelper.validateString("config.basePath", config.getBasePath(), true,
                    MAX_INPUT_STRING_LENGTH, MIN_INPUT_STRING_LENGTH, null, errors);
            ValidationHelper.validateString("config.adminPassword", form.getConfig()
                    .getAdminPassword(), true, MAX_INPUT_STRING_LENGTH, MIN_INPUT_STRING_LENGTH,
                    null, errors);
            ValidationHelper.validateString("config.adminLogin", form.getConfig().getAdminLogin(),
                    true, MAX_INPUT_STRING_LENGTH, MIN_INPUT_STRING_LENGTH, null, errors);

            try {
                new URL(config.getBasePath());
            } catch (MalformedURLException e) {
                errors.rejectValue("config.basePath",
                        "client.authentication.confluence.invalid.url");
            }
        }
    }

}
