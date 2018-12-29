package com.communote.server.core.vo.content;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.io.IOHelper;
import com.communote.common.virusscan.VirusScanner;
import com.communote.common.virusscan.exception.InitializeException;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.common.virusscan.exception.VirusScannerException;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.storing.ResourceStoringManagementException;
import com.communote.server.core.vo.AbstractTransferObject;
import com.communote.server.model.attachment.AttachmentStatus;

/**
 * The {@link AttachmentTO} encapsulates the logic to access an attachment by streams. The streams
 * source can be either a file or a stream which is defined by the subclasses.
 *
 * The encapsulation is necessary to assure the closing of the input streams as they got accessed.
 * Use the write methods to write an attachment either to a file or to another stream.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AttachmentTO extends AbstractTransferObject implements Serializable {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7608343167727912676L;

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentTO.class);

    private AttachmentStatus status;

    private Date uploadDate;

    private Long uploaderId;

    private ContentMetadata metadata;

    private long contentLength;

    private VirusScanner scanner;

    private List<StringPropertyTO> properties;

    private Long noteId;

    /**
     *
     * @param status
     *            set the status, can be null
     */
    public AttachmentTO(AttachmentStatus status) {
        super();
        this.status = status;
    }

    /**
     * Close the stream
     *
     * @param inputStream
     *            the input stream
     * @throws IOException
     *             in case of an io error
     */
    protected void close(InputStream inputStream) throws IOException {
        IOUtils.closeQuietly(inputStream);
    }

    /**
     * Get the content length. That will be length of the meta data, if set, or the length that
     * could be predetermined (e.g. if the source is a file). However after the write method has
     * been called the length is updated (also in the meta data, if not null).
     *
     * @return the content length
     */
    public long getContentLength() {
        if (this.metadata != null && this.metadata.getContentSize() > 0) {
            return this.metadata.getContentSize();
        }
        return contentLength;
    }

    /**
     *
     * @return the metadata
     */
    public ContentMetadata getMetadata() {
        return this.metadata;
    }

    /**
     *
     * @return id of the note the attachment is associated with. null if no note connected.
     */
    public Long getNoteId() {
        return noteId;
    }

    public List<StringPropertyTO> getProperties() {
        return properties;
    }

    /**
     * <p>
     * The status of the attachment to be used
     * </p>
     *
     * @return the status
     */
    public AttachmentStatus getStatus() {
        return this.status;
    }

    /**
     * @return The date the attachment was uploaded.
     */
    public Date getUploadDate() {
        return uploadDate;
    }

    /**
     * @return Id of the user, who uploaded the attachment.
     */
    public Long getUploaderId() {
        return uploaderId;
    }

    /**
     * Open the inputstream
     *
     * @return the inputstream to read from
     * @throws IOException
     *             in case of an io error
     */
    abstract protected InputStream open() throws IOException;

    public void sendInResponse(HttpServletRequest request, HttpServletResponse response,
            String characterEncoding, boolean dipositionTypeAsAttachment) throws IOException {

        String contentType = null;

        if (this.getMetadata() != null) {
            contentType = this.getMetadata().getMimeType();
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        // TODO setting the encoding here is not that useful because we are using the stream and not
        // the writer. Moreover, binary types have no character encoding and for text types the
        // attachment should tell us the encoding of the content.
        // can't set 'no encoding' with null (to overwrite value set by EncodingFilter), null is
        // ignored. Empty string causes a warning.
        if (StringUtils.isNotBlank(characterEncoding)) {
            response.setCharacterEncoding(characterEncoding);
        }
        long contentLength = this.getContentLength();
        // RFC 2183 --> http://www.ietf.org/rfc/rfc2183.txt
        String dispositionType = dipositionTypeAsAttachment ? "attachment" : "inline";
        String contentDispositionHeader = dispositionType + "; filename=\""
                + this.getMetadata().getFilename() + "\"; ";
        if (contentLength > 0) {
            contentDispositionHeader += "size=" + contentLength + ";";
            response.setContentLength((int) contentLength);
        }
        response.setHeader("Content-Disposition", contentDispositionHeader);
        response.setHeader("Content-Description", this.getMetadata().getFilename());
        response.flushBuffer();

        ServletOutputStream out = response.getOutputStream();
        this.write(out);
        out.flush();

    }

    /**
     * Set the content length, that will set it on the metadata, if not null.
     *
     * @param contentLength
     *            the content length
     */
    public void setContentLength(long contentLength) {
        if (this.metadata != null) {
            this.metadata.setContentSize(contentLength);
        } else {
            this.contentLength = contentLength;
        }
    }

    /**
     * Sets the metadata, it will also update metadata.getContentSize if the content size might be
     * known and the metadata content length is not set
     *
     * @param metadata
     *            the metadata to be set
     */
    public void setMetadata(ContentMetadata metadata) {
        this.metadata = metadata;
        if (this.contentLength > 0 && this.metadata != null && this.metadata.getContentSize() <= 0) {
            setContentLength(this.contentLength);
        }
    }

    /**
     * NoteId is ignored on creating a note and associating it. Use create note of
     * {@link NoteManagement} to connect attachments with notes.
     *
     * @param noteId
     *            id of the note the attachment is associated with. null if no note connected.
     */
    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public void setProperties(List<StringPropertyTO> properties) {
        this.properties = properties;
    }

    /**
     *
     * @param status
     *            set the attachment status
     */
    public void setStatus(AttachmentStatus status) {
        this.status = status;
    }

    /**
     * @param uploadDate
     *            The date the attachment was uploaded.
     */
    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    /**
     * @param uploaderId
     *            Method to set the uploaders id.
     */
    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    /**
     *
     * @param scanner
     *            the virus scanner to be used then writing
     */
    public void setVirusScanner(VirusScanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public String toString() {
        return "AttachmentTO [status=" + status + ", uploadDate=" + uploadDate + ", uploaderId="
                + uploaderId + ", metadata=" + metadata + ", contentLength=" + contentLength
                + ", scanner=" + scanner + ", properties=" + properties + ", noteId=" + noteId
                + "]";
    }

    /**
     * Scan the input stream for virus, only effective if {@link #scanner} is set
     *
     * @param in
     *            the stream to scan
     * @return the copied input stream
     */
    private InputStream virusScan(InputStream in) {
        InputStream copy;
        try {
            if (scanner != null) {
                copy = scanner.scan(in);
            } else {
                copy = in;
            }
        } catch (InitializeException e) {
            LOGGER.error("Virus scanner not initalized: " + e.getMessage(), e);
            throw new ResourceStoringManagementException("Virus scanner not initialized", e);
        } catch (VirusScannerException e) {
            LOGGER.error("Unable to scan content!: " + e.getMessage(), e);
            throw new ResourceStoringManagementException("Unable to scan content", e);
        } catch (VirusFoundException e) {
            LOGGER.warn("Virus found! userId=" + SecurityHelper.getCurrentUserId() + " "
                    + e.getMessage());
            throw new ResourceStoringManagementException("Virus was detected in byte array", e);
        }
        return copy;
    }

    /**
     * Open the stream and write its content to the file. After writing the stream will be closed
     * and the number of written bytes will be stored in the content length member of the metadata (
     * {@link #metadata#setContentLength(long)}) if metadata is not null.
     *
     * @param file
     *            the file to write to
     * @throws IOException
     *             in case of an error
     */
    public synchronized void write(File file) throws IOException {
        write(file, -1);
    }

    /**
     * Open the stream and write at most maxLength bytes of the content to the file. After writing
     * the stream will be closed and the number of written bytes will be stored in the content
     * length member of the metadata ( {@link #metadata#setContentLength(long)}) if metadata is not
     * null.
     *
     * @param file
     *            the file to write to
     * @param maxLength
     *            See {@link IOHelper#write(InputStream, OutputStream, long)} for more information.
     * @throws IOException
     *             in case of an error
     */
    public synchronized void write(File file, long maxLength) throws IOException {
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            write(bufferedOutputStream, maxLength);
        } finally {
            IOUtils.closeQuietly(bufferedOutputStream);
        }
    }

    /**
     * Write the attachment to the output stream. Will not close the output stream.
     *
     * @param outStream
     *            the output stream
     * @throws IOException
     *             in case of an error
     */
    public synchronized void write(OutputStream outStream) throws IOException {
        this.write(outStream, -1);
    }

    /**
     * Write the attachment to the output stream. Will not close the output stream.
     *
     * @param outStream
     *            the output stream
     * @param maxLength
     *            See {@link IOHelper#write(InputStream, OutputStream, long)} for more information.
     * @throws IOException
     *             in case of an error
     */
    public synchronized void write(OutputStream outStream, long maxLength) throws IOException {
        InputStream inputStream = open();
        try {
            inputStream = virusScan(inputStream);
            long contentLength = IOHelper.write(inputStream, outStream, maxLength);
            setContentLength(contentLength);
        } finally {
            close(inputStream);
        }
    }
}
