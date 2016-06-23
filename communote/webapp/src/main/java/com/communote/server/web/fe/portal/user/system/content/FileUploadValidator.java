package com.communote.server.web.fe.portal.user.system.content;

import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the {@link FileStorageController}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class FileUploadValidator implements Validator {

    private final static Pattern PATTERN_POSITIVE_INTEGER = Pattern.compile("\\d+");

    /**
     * {@inheritDoc}
     */
    public boolean supports(Class<?> clazz) {
        return FileUploadForm.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Object target, Errors errors) {
        FileUploadForm form = (FileUploadForm) target;
        if (!PATTERN_POSITIVE_INTEGER.matcher(form.getAttachmentSize()).matches()) {
            errors.rejectValue("attachmentSize",
                    "string.validation.numbers.positive");
        }
        if (!PATTERN_POSITIVE_INTEGER.matcher(form.getImageSize()).matches()) {
            errors.rejectValue("imageSize", "string.validation.numbers.positive");
        }
    }
}
