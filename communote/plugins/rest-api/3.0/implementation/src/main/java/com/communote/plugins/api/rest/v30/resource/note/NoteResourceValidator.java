package com.communote.plugins.api.rest.v30.resource.note;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.communote.common.util.HTMLHelper;
import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.validation.DefaultValidator;
import com.communote.plugins.api.rest.v30.resource.validation.ParameterValidationError;
import com.communote.plugins.api.rest.v30.resource.validation.ParameterValidationException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
class NoteResourceValidator extends
        DefaultValidator<CreateNoteParameter, EditNoteParameter,
        DeleteNoteParameter, GetNoteParameter, DefaultParameter> {

    /**
     * Checks the min and max length of a note.
     * 
     * @param text
     *            the content to check
     * @param maxTextLength
     *            max length of the note
     * @param isHtml
     *            whether the text is html
     * @return list of errors
     */
    private List<ParameterValidationError> checkNoteLength(String text, Boolean isHtml,
            Integer maxTextLength) {

        if (maxTextLength == null) {
            maxTextLength = Integer.MAX_VALUE;
        }
        if (isHtml == null) {
            isHtml = false;
        }

        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        boolean noText;
        if (text != null && isHtml) {
            noText = !HTMLHelper.containsNonEmptyTextNodes(text);
        } else {
            noText = StringUtils.isBlank(text);
        }
        if (noText) {
            ParameterValidationError error = new ParameterValidationError();
            error.setSource("text");
            error.setMessageKey("error.blogpost.create.no.content");
            errors.add(error);
        }

        if (!noText) {
            errors.addAll(checkNoteMaxLength(text, maxTextLength));
        }

        return errors;

    }

    /**
     * Check the max length of the note content.
     * 
     * @param postText
     *            the post content
     * @param postMaxLength
     *            max length of the note
     * @return list of errors if any
     */
    private List<ParameterValidationError> checkNoteMaxLength(String postText, int postMaxLength) {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        if (postMaxLength > 0 && postText.length() > postMaxLength) {
            ParameterValidationError error = new ParameterValidationError();
            error.setSource("text");
            error.setMessageKey("error.blogpost.content.max.length.exceeded");
            error.setParameters(postMaxLength);
            errors.add(error);
        }
        return errors;
    }

    /**
     * checks whether topic id is set
     * 
     * @param topicId
     *            topic id
     * @return list of errors
     */
    private List<ParameterValidationError> checkTopicIdExistence(Long topicId) {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        if (topicId == null) {
            ParameterValidationError error = new ParameterValidationError();
            error.setSource("blogId");
            error.setMessageKey("error.blogpost.create.no.blogid");
            errors.add(error);
        }
        return errors;
    }

    @Override
    public void validateCreate(CreateNoteParameter createPrameter)
            throws ParameterValidationException {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        errors.addAll(checkNoteLength(createPrameter.getText(), createPrameter.getIsHtml(),
                createPrameter.getMaxTextLength()));
        errors.addAll(checkTopicIdExistence(createPrameter.getTopicId()));
        if (errors.size() > 0) {
            ParameterValidationException exception = new ParameterValidationException();
            exception.setErrors(errors);
            throw exception;
        }

    }

    @Override
    public void validateEdit(EditNoteParameter editPrameter) throws ParameterValidationException {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        if (editPrameter.getTopicId() == null) {
            errors.addAll(checkNoteLength(editPrameter.getText(), editPrameter.getIsHtml(),
                    editPrameter.getMaxTextLength()));
        }
        if (errors.size() > 0) {
            ParameterValidationException exception = new ParameterValidationException();
            exception.setErrors(errors);
            throw exception;
        }
    }

}
