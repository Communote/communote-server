package com.communote.plugins.api.rest.v30.resource.topic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.communote.common.validation.EmailValidator;
import com.communote.plugins.api.rest.v30.resource.validation.ParameterValidationError;
import com.communote.plugins.api.rest.v30.resource.validation.ParameterValidationException;
import com.communote.plugins.api.rest.v30.resource.validation.ValidationHelper;
import com.communote.plugins.api.rest.v30.resource.validation.Validator;
import com.communote.server.core.blog.helper.BlogManagementHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicResourceValidator extends
        Validator<CreateTopicParameter, EditTopicParameter,
        DeleteTopicParameter, GetTopicParameter, GetCollectionTopicParameter> {

    /** The Constant MAX_INPUT_STRING_LENGTH. */
    private static final int MAX_TITLE_INPUT_STRING_LENGTH = 1024;
    /** The Constant MIN_INPUT_STRING_LENGTH. */
    private static final int MIN_INPUT_STRING_LENGTH = 1;

    /**
     * @param alias
     *            alias to be checked
     * @return list of errors
     */
    private List<ParameterValidationError> checkAlias(String alias) {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        if (StringUtils.isNotBlank(alias)) {
            String trimmedIdent = StringUtils.trimToEmpty(alias);
            errors.addAll(ValidationHelper.checkAgainstRegularExpression(trimmedIdent,
                    BlogManagementHelper.REG_EXP_TOPIC_NAME_IDENTIFIER));
            errors.addAll(ValidationHelper.checkMaxStringLengthLimit(trimmedIdent,
                    EmailValidator.MAX_SAFE_LENGTH_LOCAL_PART));
        }
        for (ParameterValidationError parameterValidationError : errors) {
            parameterValidationError.setSource("alias");
        }
        return errors;
    }

    /**
     * @param title
     *            blog title
     * @return list of errors
     */
    private List<ParameterValidationError> checkTitle(String title) {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();

        errors.addAll(checkTitleEmptiness(title));

        if (StringUtils.isNotBlank(title)) {
            errors.addAll(ValidationHelper
                    .checkMinStringLengthLimit(title, MIN_INPUT_STRING_LENGTH));
            errors.addAll(ValidationHelper.checkMaxStringLengthLimit(title,
                    MAX_TITLE_INPUT_STRING_LENGTH));
        }
        for (ParameterValidationError parameterValidationError : errors) {
            parameterValidationError.setSource("title");
        }

        return errors;
    }

    /**
     * check whether title is empty
     * 
     * @param title
     *            title
     * @return list of errors
     */
    private List<ParameterValidationError> checkTitleEmptiness(String title) {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        if (StringUtils.isBlank(title)) {
            ParameterValidationError error = new ParameterValidationError();
            error.setSource("title");
            error.setMessageKey("string.validation.empty");
            errors.add(error);
        }
        return errors;
    }

    @Override
    public void validateCreate(CreateTopicParameter createParameter)
            throws ParameterValidationException {

        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();

        errors.addAll(checkTitle(createParameter.getTitle()));

        errors.addAll(checkAlias(createParameter.getAlias()));

        if (errors.size() > 0) {
            ParameterValidationException exception = new ParameterValidationException();
            exception.setErrors(errors);
            throw exception;
        }

    }

    @Override
    public void validateDelete(DeleteTopicParameter deleteParameter)
            throws ParameterValidationException {
        // TODO Auto-generated method stub

    }

    @Override
    public void validateEdit(EditTopicParameter editParameter) throws ParameterValidationException {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();

        errors.addAll(checkTitle(editParameter.getTitle()));

        errors.addAll(checkAlias(editParameter.getAlias()));

        if (errors.size() > 0) {
            ParameterValidationException exception = new ParameterValidationException();
            exception.setErrors(errors);
            throw exception;
        }

    }

    @Override
    public void validateGetSingle(GetTopicParameter getSingleParameter)
            throws ParameterValidationException {
        // TODO Auto-generated method stub

    }

    @Override
    public void validateList(GetCollectionTopicParameter listParameter)
            throws ParameterValidationException {
        // TODO Auto-generated method stub

    }

}
