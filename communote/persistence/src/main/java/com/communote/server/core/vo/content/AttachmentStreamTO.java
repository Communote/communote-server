package com.communote.server.core.vo.content;

import java.io.IOException;
import java.io.InputStream;

import com.communote.server.model.attachment.AttachmentStatus;

/**
 * The {@link AttachmentStreamTO} will be an attachment based on an input stream. The stream will be
 * closed as soon one write method as been called.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AttachmentStreamTO extends AttachmentTO {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private boolean inputStreamClosed;

    private InputStream inputStream;

    /**
     * 
     * @param inputStream
     *            the input stream to use
     */
    public AttachmentStreamTO(InputStream inputStream) {
        this(inputStream, null);
    }

    /**
     * 
     * @param inputStream
     *            the inputstream to use
     * @param status
     *            the status to use, can be null
     */
    public AttachmentStreamTO(InputStream inputStream, AttachmentStatus status) {
        super(status);
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream must be set!");
        }
        this.inputStream = inputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void close(InputStream inputStream) throws IOException {
        if (inputStreamClosed) {
            throw new StreamAlreadyClosedExcpetion("Input stream has been closed already");
        }

        super.close(inputStream);

        inputStreamClosed = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InputStream open() throws StreamAlreadyClosedExcpetion {
        if (inputStreamClosed) {
            throw new StreamAlreadyClosedExcpetion("Input stream has been closed already");
        }
        return inputStream;
    }

}
