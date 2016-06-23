package com.communote.server.core.blog.export;

import java.io.OutputStream;
import java.util.Collection;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.core.blog.NoteWriterException;
import com.communote.server.core.vo.query.post.NoteQueryParameters;


/**
 * <p>
 * Interface for exporters.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface NoteWriter {

    /**
     * <p>
     * 
     * @return the content type for this format.
     *         </p>
     */
    public String getContentType();

    /**
     * <p>
     * 
     * @return The extension of files for this format.
     *         </p>
     */
    public String getFileExtension();

    /**
     * <p>
     * 
     * @return True, if the content dispositon for this is attachment, else it will be threated as
     *         inline.
     *         </p>
     */
    public boolean isAttachment();

    /**
     * @return whether the writer supports HTML as content or only plain text
     */
    public boolean supportsHtmlContent();

    /**
     * <p>
     * 
     * @param exportFormat
     *            The format to be used for export.
     * @return True, if this exporter is valid for the given exporter.
     *         </p>
     */
    public boolean valid(String exportFormat);

    /**
     * Writes the exported content into the output stream.
     * 
     * @param queryInstance
     *            The query instance.
     * @param outputStream
     *            The outputstream.
     * @param noteItems
     *            Teh note items.
     * @param originalRequestUrl
     *            The original request url
     * @throws NoteWriterException
     *             Exception.
     */
    public void write(NoteQueryParameters queryInstance, OutputStream outputStream,
            Collection<NoteData> noteItems, String originalRequestUrl)
            throws NoteWriterException;

}