package com.communote.server.web.fe.portal.user.client.validator;

import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.web.fe.portal.user.client.forms.LdapConfigurationForm;

/**
 * Validator for LDAP configuration.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class LdapConfigurationValidator implements Validator {

    private static final int MAX_INPUT_STRING_LENGTH = 255;
    private static final int MIN_INPUT_STRING_LENGTH = 1;

    /**
     * {@inheritDoc}
     * 
     * @return LdapConfigurationForm.class.isAssignableFrom(clazz);
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return LdapConfigurationForm.class.isAssignableFrom(clazz);
    }

    /**
     * @param target
     *            to validate.
     * @param errors
     *            To write errors in.
     */
    @Override
    public void validate(Object target, Errors errors) {
        LdapConfigurationForm form = (LdapConfigurationForm) target;
        LdapConfiguration config = form.getConfig();

        if (config.isDynamicMode()) {
            ValidationHelper.validateString("domain", config.getServerDomain(), true,
                    MAX_INPUT_STRING_LENGTH, 0, null, errors);
            ValidationHelper.validateString("queryPrefix", config.getQueryPrefix(), true,
                    MAX_INPUT_STRING_LENGTH, 0, null, errors);
        } else {
            ValidationHelper.validateString("url", config.getUrl(), true, MAX_INPUT_STRING_LENGTH,
                    0, null, errors);
        }

        ValidationHelper.validateString("bindUser", config.getManagerDN(), false,
                MAX_INPUT_STRING_LENGTH, MIN_INPUT_STRING_LENGTH, null, errors);
        ValidationHelper.validateString("bindUserPassword", config.getManagerPassword(), false,
                MAX_INPUT_STRING_LENGTH, MIN_INPUT_STRING_LENGTH, null, errors);

        List<LdapSearchBaseDefinition> searchBases = config.getUserSearch().getSearchBases();
        for (int i = 0; i < searchBases.size(); i++) {
            boolean isRequired = i == 0 ? true : false;
            ValidationHelper.validateString("userSearchBases[" + i + "].searchBase", searchBases
                    .get(i).getSearchBase(), isRequired, MAX_INPUT_STRING_LENGTH, 0, null, errors);
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userIdentifier",
                "string.validation.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userAlias", "string.validation.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userEmail", "string.validation.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userFirstName",
                "string.validation.empty");
        ValidationUtils
                .rejectIfEmptyOrWhitespace(errors, "userLastName", "string.validation.empty");

        if (form.isSynchronizeUserGroups()) {
            searchBases = form.getGroupSyncConfig().getGroupSearch().getSearchBases();
            for (int i = 0; i < searchBases.size(); i++) {
                boolean isRequired = i == 0 ? true : false;
                ValidationHelper.validateString("groupSearchBases[" + i + "].searchBase",
                        searchBases.get(i).getSearchBase(), isRequired, MAX_INPUT_STRING_LENGTH, 0,
                        null, errors);
            }

            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "groupMembership",
                    "string.validation.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "groupIdentifier",
                    "string.validation.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "groupName",
                    "string.validation.empty");
        }
        // only assert that LDAP login and password are set when external authentication is allowed
        if (form.isConfigAllowExternalAuthentication()) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ldapPassword",
                    "string.validation.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ldapLogin",
                    "string.validation.empty");
        }
    }
}