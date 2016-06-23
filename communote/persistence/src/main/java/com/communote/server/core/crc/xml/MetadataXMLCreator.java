package com.communote.server.core.crc.xml;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.crc.MetadataRepositoryConnector;
import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ContentMetadata;

/**
 * A class for creating the XML form.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MetadataXMLCreator {

    static final String DATEFORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private static final String CDATA_PREFIX = "<![CDATA[";

    private static final String CDATA_SUFFIX = "]]>";

    /**
     * Creates a String containing the time according to ISO 8601 like YYYY-MM-DDThh:mm:ss.
     * 
     * @param date
     *            The date to create the String from.
     * @return the created String containing the time from the date.
     */
    private static String getTimeString(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        SimpleDateFormat format = new SimpleDateFormat(DATEFORMAT_PATTERN, Locale.ENGLISH);
        return format.format(date);
    }

    /**
     * Writes the ContentMetadata to the writer.
     * 
     * @param writer
     *            The Writer the metadata is to write.
     * @param metadata
     *            The ContentMetadata to write.
     * @param configuration
     *            The Configuration of the calling RepositoryConnector.
     * @throws IOException
     *             If writing metadata fails.
     */
    public static void writeMetadata(MetadataRepositoryConnector repositoryConnector,
            Writer writer, ContentMetadata metadata) throws IOException {

        // write the header information
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<kenmei xmlns=\"http://www.communardo.de/kenmei\">\n");

        // write the ContentId of the content referenced by this metadata
        writer.write("\t<content id=\"" + repositoryConnector.getConfiguration().getConnectorId()
                + ">>"
                + metadata.getContentId().getContentId() + "\">\n");

        // write the values for the content
        writeValue("\t\t", "url", metadata.getUrl(), writer);
        writeValue("\t\t", "date", getTimeString(metadata.getDate()), writer);
        writeValue("\t\t", "format", metadata.getMimeType(), writer);
        writeValue("\t\t", "version", metadata.getVersion(), writer);
        writeValue("\t\t", "filename", CDATA_PREFIX + metadata.getFilename() + CDATA_SUFFIX, writer);
        writeValue("\t\t", "filesize", "" + metadata.getContentSize(), writer);

        // write the versions
        boolean isVersionized = false;
        if (metadata.getVersions() != null && metadata.getVersions().length > 0) {
            ContentId id = null;
            writer.write("\t\t<versionList>\n");

            if (metadata.getVersions()[0] instanceof ContentId) {
                id = (ContentId) metadata.getVersions()[0];
            } else if (metadata.getVersions()[0] instanceof ContentMetadata) {
                id = ((ContentMetadata) metadata.getVersions()[0]).getContentId();
            }

            // write the contentId of the first element as versionsId
            writeValue("\t\t\t", "listId", id.getConnectorId() + ">>" + id.getContentId(), writer);
            writer.write("\t\t\t<listItems>\n");
            for (Object o : metadata.getVersions()) {
                if (o instanceof ContentId) {
                    id = (ContentId) o;
                } else if (o instanceof ContentMetadata) {
                    id = ((ContentMetadata) o).getContentId();
                } else {
                    continue;
                }
                writer.write("\t\t\t\t\t<listItem contentId=\"" + id.getConnectorId() + ">>"
                        + id.getContentId() + "\" />\n");
                if (id.getContentId().equals(metadata.getContentId().getContentId())
                        && id.getConnectorId().equals(metadata.getContentId().getConnectorId())) {
                    isVersionized = true;
                }
            }

            // add the contentId which belongs to this metadata as last version,
            // if it doesn't already appear in the list
            if (!isVersionized) {
                writer.write("\t\t\t\t\t<listItem contentId=\""
                        + metadata.getContentId().getConnectorId() + ">>"
                        + metadata.getContentId().getContentId() + "\" />\n");
            }
            writer.write("\t\t\t</listItems>\n");
            writer.write("\t\t</versionList>\n");
        }
        writer.write("\t</content>\n");

        // write details for the version items
        if (metadata.getVersions() != null && metadata.getVersions().length > 0) {
            writeVersionedItems(repositoryConnector, writer, metadata);
        }
        writer.write("</kenmei>");
    }

    /**
     * Writes closed elements with values.
     * 
     * @param prefix
     *            A prefix to write.
     * @param element
     *            The name of the element.
     * @param value
     *            The value of the element.
     * @param writer
     *            The writer to write.
     * @throws IOException
     *             If writing fails.
     */
    private static void writeValue(String prefix, String element, String value, Writer writer)
            throws IOException {
        if (element != null && !element.equals("") && value != null && !value.equals("")
                && writer != null) {
            writer.write(prefix + "<" + element + ">" + value + "</" + element + ">\n");
        }
    }

    /**
     * Writes the information about the versioned items.
     * 
     * @param writer
     *            The writer.
     * @param metadata
     *            The metadata.
     * @throws IOException
     *             If an error occurs.
     */
    private static void writeVersionedItems(MetadataRepositoryConnector repositoryConnector,
            Writer writer, ContentMetadata metadata)
            throws IOException {
        ContentMetadata metadataItem = null;
        for (int i = 0; i < metadata.getVersions().length; i++) {

            if (metadata.getVersions()[i] instanceof ContentId
                    && !((ContentId) metadata.getVersions()[i]).getContentId().equals(
                            metadata.getContentId().getContentId())) {
                metadataItem = repositoryConnector
                        .getMetadata((ContentId) metadata.getVersions()[i]);
            } else if (metadata.getVersions()[i] instanceof ContentMetadata) {
                metadataItem = (ContentMetadata) metadata.getVersions()[i];
            } else {
                continue;
            }
            writer.write("\t<item about=\"" + metadataItem.getContentId().getConnectorId()
                    + ">>" + metadataItem.getContentId().getContentId() + "\">\n");

            // write the values for the versions
            writeValue("\t\t", "url", metadataItem.getUrl(), writer);
            writeValue("\t\t", "date", getTimeString(metadataItem.getDate()), writer);
            writeValue("\t\t", "format", metadataItem.getMimeType(), writer);
            writeValue("\t\t", "filename", metadataItem.getFilename(), writer);
            writeValue("\t\t", "version", metadataItem.getVersion(), writer);
            writer.write("\t</item>\n");
        }
    }

    /**
     * A non-visible Constructor.
     */
    private MetadataXMLCreator() {
    }
}
