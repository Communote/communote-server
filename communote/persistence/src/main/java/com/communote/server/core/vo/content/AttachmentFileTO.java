package com.communote.server.core.vo.content;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.communote.server.model.attachment.AttachmentStatus;

/**
 * The {@link AttachmentFileTO} will be based on a file. The stream to the file will be opened and
 * closed during the {@link #write(File)} methods.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AttachmentFileTO extends AttachmentTO {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final File file;

    /**
     * 
     * @param file
     *            the file the attachment will be based on. A check will be done if the file exists.
     * @throws IOException
     *             in case of an io error
     */
    public AttachmentFileTO(File file) throws IOException {
        this(file, null);
    }

    /**
     * 
     * @param file
     *            the file the attachment will be based on. A check will be done if the file exists.
     * 
     * @param status
     *            the status to set
     * @throws IOException
     *             in case of an io error
     */
    public AttachmentFileTO(File file, AttachmentStatus status) throws IOException {
        super(status);
        if (file == null) {
            throw new IllegalArgumentException("file must be set!");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getName());
        }
        this.file = file;
        this.setContentLength(file.length());
    }

    /**
     * 
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream open() throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }
}
