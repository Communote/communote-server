package com.communote.server.web.fe.portal.user.system.content;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the {@link FileStorageController}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class FileStorageValidator implements Validator {

    /**
     * @param path
     *            The path.
     * @return true if it was possible to create and delete a file within the new repository.
     */
    private boolean canWrite(String path) {
        File tempFile = new File(path + File.separator
                + UUID.randomUUID().toString());
        try {
            tempFile.createNewFile();
            tempFile.delete();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        return FileStorageForm.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Object target, Errors errors) {
        FileStorageForm form = (FileStorageForm) target;
        if (StringUtils.isBlank(form.getPath())) {
            errors.rejectValue("path", "client.system.content.file.storage.error.empty");
            return;
        }
        File file = new File(form.getPath());
        if (!file.exists() && !file.mkdirs()) {
            errors
                        .rejectValue("path",
                                "client.system.content.file.storage.error.can.not.create");
        } else if (!file.isDirectory()) {
            errors.rejectValue("path", "client.system.content.file.storage.error.no.directory");
        } else if (!canWrite(form.getPath())) {
            errors.rejectValue("path", "client.system.content.file.storage.error.not.writable");
        }
    }
}
