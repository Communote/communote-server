package com.communote.server.core.crc.xml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ContentMetadata;

/**
 * A class for parsing the metadata XML file.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MetadataXMLHandler extends DefaultHandler {

    /**
     * the metadata-object to return
     */
    private final ContentMetadata metadata;

    /**
     * a string representing an element value
     */
    private String elementValue;

    /**
     * a list containing the ids of the versions
     */
    private List<ContentId> versions;

    /**
     * represents the state of the current values
     */
    private boolean item = false;

    /**
     * a list containing the metadata of the versions
     */
    private List<ContentMetadata> metadataList;

    /**
     * a metadata-object to fill with version-information
     */
    private ContentMetadata metadataItem;

    /**
     * Initializes this Handler.
     */
    public MetadataXMLHandler() {
        metadata = new ContentMetadata();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        char[] c = new char[length];
        for (int i = 0; i < length; i++, start++) {
            c[i] = ch[start];
        }
        elementValue += new String(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

        if (qName.equals("url")) {
            if (item) {
                metadataItem.setUrl(elementValue);
            } else {
                metadata.setUrl(elementValue);
            }
        } else if (qName.equals("date")) {
            if (item) {
                metadataItem.setDate(getDate());
            } else {
                metadata.setDate(getDate());
            }
        } else if (qName.equals("format")) {
            if (item) {
                metadataItem.setMimeType(elementValue);
            } else {
                metadata.setMimeType(elementValue);
            }
        } else if (qName.equals("item")) {
            if (metadataList == null) {
                metadataList = new ArrayList<ContentMetadata>();
            }
            if (metadataItem != null) {
                metadataList.add(metadataItem);
            }
            metadataItem = null;
            item = false;
        } else if (qName.equals("kenmei")) {
            if (metadataList != null) {
                metadata.setVersions(metadataList.toArray());
            } else if (versions != null) {
                metadata.setVersions(versions.toArray());
            }
        } else if (qName.equals("version")) {
            if (item) {
                metadataItem.setVersion(elementValue);
            } else {
                metadata.setVersion(elementValue);
            }
        } else if (qName.equals("filesize")) {
            metadata.setContentSize(Long.parseLong(elementValue));
        } else if (qName.equals("filename")) {
            metadata.setFilename(elementValue);
        }
    }

    /**
     * Parses a ContentId from a String.
     *
     * @param idString
     *            The String containing the ContentId.
     * @return The ContentId.
     */
    private ContentId getContentIdFromString(String idString) {
        // LOG.info("id: " + idString);
        ContentId contentId = new ContentId();
        String[] splits = idString.split(">>");
        if (splits.length == 2) {
            contentId.setConnectorId(splits[0]);
            contentId.setContentId(splits[1]);
            return contentId;
        }
        return null;
    }

    /**
     * Parses a date
     *
     * @return the parsed date.
     * @throws SAXException
     */
    private Date getDate() throws SAXException {
        SimpleDateFormat format = new SimpleDateFormat(MetadataXMLCreator.DATEFORMAT_PATTERN,
                Locale.ENGLISH);
        try {
            return format.parse(elementValue);
        } catch (ParseException e) {
            throw new SAXException("Date value 'elementValue' cannot be parsed", e);
        }
    }

    /**
     * Returns the read metadata.
     *
     * @return the read metadata.
     */
    public ContentMetadata getMetadata() {
        return metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName,
            Attributes attributes) throws SAXException {
        elementValue = "";

        if (qName.equals("listItems")) {
            versions = new ArrayList<ContentId>();
        } else if (qName.equals("listItem")) {
            if (attributes != null) {
                versions.add(getContentIdFromString(attributes.getValue("contentId")));
            }
        } else if (qName.equals("content")) {
            if (attributes != null) {
                metadata.setContentId(getContentIdFromString(attributes.getValue("id")));
            }
        } else if (qName.equals("item")) {
            metadataItem = new ContentMetadata();
            item = true;
            metadataItem.setContentId(getContentIdFromString(attributes.getValue("about")));
        }
    }
}