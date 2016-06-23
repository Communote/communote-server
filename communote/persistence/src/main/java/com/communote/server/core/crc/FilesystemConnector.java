package com.communote.server.core.crc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;

import com.communote.common.io.MimeTypeHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.crc.vo.ExtendedContentId;
import com.communote.server.core.crc.xml.MetadataXMLCreator;
import com.communote.server.core.crc.xml.MetadataXMLHandler;
import com.communote.server.core.vo.content.AttachmentFileTO;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.persistence.crc.ContentRepositoryException;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.persistence.user.client.ClientStatisticDao;

/**
 * A class for the ContentRepositoryConnector to use a file system.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FilesystemConnector implements MetadataRepositoryConnector {

    /** Id for default file system connector. */
    public static final String DEFAULT_FILESYSTEM_CONNECTOR = "default_filesystem_connector";

    private static final long serialVersionUID = 2190913266985390739L;

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemConnector.class);

    /**
     * the filename of the file the content is to store in
     */
    private final static String CONTENT_FILE_NAME = "content";

    /**
     * the filename of the file to store the metadata
     */
    private final static String METADATA_FILE_NAME = "metadata.xml";

    private final String connectorSubdir;

    private final RepositoryConnectorConfiguration connectorConfiguration;

    /**
     * Constructor for a new FilesystemConnector, based on the given FilesystemConfiguration.
     *
     * @param connectorConfiguration
     *            The configuration for this connector.
     */
    public FilesystemConnector(RepositoryConnectorConfiguration connectorConfiguration) {
        Assert.notNull(connectorConfiguration.getConnectorId(),
                "The connector ID must not be empty");
        this.connectorConfiguration = connectorConfiguration;
        this.connectorSubdir = File.separator + connectorConfiguration.getConnectorId()
                + File.separator;
        // check the path configuration
        File file = new File(getPath());
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new IllegalArgumentException("cannot create directory "
                        + file.getAbsolutePath());
            }
        }

        if (!file.isDirectory()) {
            throw new IllegalArgumentException("destined directory isn't a directory: "
                    + file.getAbsolutePath());
        } else {
            if (!file.canRead() || !file.canWrite()) {
                throw new IllegalArgumentException("cannot access to destined directory: "
                        + file.getAbsolutePath());
            }
            LOGGER.debug(
                    "creating FilesystemConnector with FilesystemConfiguration pointing to: {}",
                    file.getAbsolutePath());
        }
    }

    /**
     * Deletes all files from the repository.
     *
     * @throws ContentRepositoryException
     *             Exception.
     */
    @Override
    public void clearContent() throws ContentRepositoryException {
        File repositoryPath = new File(getPath());
        if (repositoryPath.exists()) {
            if (repositoryPath.isDirectory()) {
                for (File file : repositoryPath.listFiles()) {
                    if (file.isDirectory()) {
                        try {
                            FileUtils.deleteDirectory(file);
                        } catch (IOException e) {
                            LOGGER.error("There was an error deleting a directory: {}",
                                    file.getAbsolutePath(), e);
                        }
                    } else {
                        if (!file.delete()) {
                            LOGGER.error("There was an error deleting a file: {}",
                                    file.getAbsolutePath());
                        }
                    }
                }
            } else {
                LOGGER.warn("repository path is not a directory: "
                        + repositoryPath.getAbsolutePath());
            }
        }
    }

    /**
     * Decrement the repository size
     *
     * @param contentId
     *            the content id
     * @throws ContentRepositoryException
     *             in case of an error
     */
    private void decrementRepositorySize(ContentId contentId) throws ContentRepositoryException {
        // to update the content repository size get the size of the file, which
        // needs to be deleted
        ContentMetadata metadata = this.getMetadata(contentId);
        if (metadata != null) {

            long fileSize = metadata.getContentSize();
            // update the saved content repository size on the database
            getClientStatisticDao().decrementRepositorySize(fileSize);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteContent(ContentId contentId) throws ContentRepositoryException {

        decrementRepositorySize(contentId);

        // generate destined filename
        String fileName = getPath() + contentId.getContentId();
        File file = new File(fileName);

        // check whether file exists
        if (!file.exists()) {
            LOGGER.warn("content directory " + file.getAbsolutePath() + " not exists for "
                    + contentId.getConnectorId() + ">>" + contentId.getContentId()
                    + " on connector " + connectorConfiguration.getConnectorId());
        } else {

            // and delete it
            for (File child : file.listFiles()) {
                if (!child.delete()) {
                    throw new ContentRepositoryException("cannot delete " + child.getAbsolutePath()
                            + " for " + contentId.getConnectorId() + ">>"
                            + contentId.getContentId() + " on connector "
                            + connectorConfiguration.getConnectorId());
                }
            }
            if (!file.delete()) {
                throw new ContentRepositoryException("cannot delete " + file.getAbsolutePath()
                        + " on " + contentId.getConnectorId() + ">>" + contentId.getContentId()
                        + " on connector " + connectorConfiguration.getConnectorId());
            }
        }
    }

    private ClientStatisticDao getClientStatisticDao() {
        return ServiceLocator.findService(ClientStatisticDao.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepositoryConnectorConfiguration getConfiguration() {
        return connectorConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttachmentTO getContent(ExtendedContentId contentId) throws ContentRepositoryException {
        // generate destined filename
        ContentMetadata metadata = getMetadata(contentId);
        if (metadata == null) {
            throw new ContentRepositoryException("cannot read metadata for "
                    + contentId.getConnectorId() + ">>" + contentId.getContentId());
        }
        File file = getFile(contentId, metadata.getFilename());

        // read the binary content
        AttachmentTO contentTo;
        try {
            contentTo = new AttachmentFileTO(file);
        } catch (IOException e) {
            LOGGER.error(
                    "cannot read content for " + contentId.getConnectorId() + ">>"
                            + contentId.getContentId(), e);
            throw new ContentRepositoryException("cannot read content for "
                    + contentId.getConnectorId() + ">>" + contentId.getContentId());
        }
        contentTo.setMetadata(metadata);
        contentTo.getMetadata().setContentId(contentId);
        return contentTo;
    }

    /**
     * Tries to load the file from the filesystem.
     *
     * @param contentId
     *            The contentId.
     * @param filename
     *            The files name.
     * @return The File.
     * @throws ContentRepositoryException
     *             Exception.
     */
    private File getFile(ContentId contentId, String filename) throws ContentRepositoryException {
        String pathToFile = getPath() + contentId.getContentId() + File.separator
                + CONTENT_FILE_NAME;
        File file = new File(pathToFile + getFileExtension(filename));

        // check whether it is available
        if (!file.exists()) {
            file = new File(pathToFile + ".bin");
            if (!file.exists()) {
                String message = pathToFile + " does not exist for " + contentId.getConnectorId()
                        + ">>" + contentId.getContentId();
                LOGGER.debug(message);
                throw new ContentRepositoryException(message);
            }
        }
        if (!file.isFile()) {
            String message = pathToFile + " is no file for " + contentId.getConnectorId() + ">>"
                    + contentId.getContentId();
            throw new ContentRepositoryException(message);
        }
        if (!file.canRead()) {
            String message = "cannot read " + pathToFile + " for " + contentId.getConnectorId()
                    + ">>" + contentId.getContentId();
            LOGGER.debug(message);
            throw new ContentRepositoryException(message);
        }
        return file;
    }

    /**
     *
     * @param filename
     *            Name of the file.
     * @return The part after the last "." (including a sealing .) or an empty string if there is no
     *         extension.
     */
    private String getFileExtension(String filename) {
        String[] splittedFileName = StringUtils.split(filename, ".");
        if (splittedFileName == null || splittedFileName.length <= 1) {
            return StringUtils.EMPTY;
        }
        return "." + splittedFileName[splittedFileName.length - 1];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentMetadata getMetadata(ContentId contentId) {
        ContentMetadata metadata = null;
        File file = new File(getPath() + contentId.getContentId() + File.separator
                + METADATA_FILE_NAME);

        // parse the metadata XML file to metadata
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
            MetadataXMLHandler metadataHandler = new MetadataXMLHandler();
            saxParser.parse(file, metadataHandler);
            metadata = metadataHandler.getMetadata();
        } catch (ParserConfigurationException e) {
            LOGGER.warn(
                    "Reading xml metadata failed for {}>>{} on file {}: {}",
                    new Object[] { contentId.getConnectorId(), contentId.getContentId(),
                            file.getAbsolutePath(), e.getMessage() });
        } catch (SAXException e) {
            LOGGER.warn(
                    "Reading xml metadata failed for {}>>{} on file {}: {}",
                    new Object[] { contentId.getConnectorId(), contentId.getContentId(),
                            file.getAbsolutePath(), e.getMessage() });
        } catch (IOException e) {
            LOGGER.warn(
                    "Reading xml metadata failed for {}>>{} on file {}: {}",
                    new Object[] { contentId.getConnectorId(), contentId.getContentId(),
                            file.getAbsolutePath(), e.getMessage() });
        }
        return metadata;
    }

    /**
     * @return Path to the storage for the actual client.
     */
    private String getPath() {
        return ApplicationProperty.FILE_SYSTEM_REPOSITORY_STORAGE_DIR_ROOT.getValue()
                + File.separator + ClientHelper.getCurrentClientId() + connectorSubdir;
    }

    @Override
    public long getRepositorySize() {
        return ServiceLocator.findService(ClientStatisticDao.class).getRepositorySize();
    }

    @Override
    public long getRepositorySizeLimit() {
        // TODO should be implemented her and not in a helper!
        return ContentRepositoryManagementHelper.getSizeLimit();
    }

    @Override
    public float getRepositorySizeLimitRatio() {
        return this.getRepositorySizeLimitRatio(0);
    }

    @Override
    public float getRepositorySizeLimitRatio(long sizeOfNewDataToStore) {
        float ratio = 0f;
        final long repositorySizeLimit = getRepositorySizeLimit();

        if (repositorySizeLimit > 0) {
            // compare bytes
            long sizeWithNewData = getRepositorySize() + sizeOfNewDataToStore;

            ratio = sizeWithNewData / (float) repositorySizeLimit;
        }
        return ratio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeConnector() {
        LOGGER.info("remove FilesystemConnector " + connectorConfiguration.getConnectorId());
        File file = new File(getPath());
        int length = file.list().length;
        if (length == 0) {
            LOGGER.info("no more data in " + getPath() + ", removing directory");
            file.delete();
        } else {
            LOGGER.info("still " + length + " data in " + getPath() + ", not removing directory");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentId storeContent(AttachmentTO contentTo) throws ContentRepositoryException {

        Date date = new Date();

        // generate destined path using the current time and the hash code for
        // the content
        int hash = Math.abs(contentTo.hashCode());
        String fileName = getPath() + date.getTime() + "_" + hash;
        File file = new File(fileName);

        // check whether destined path already exists
        if (file.exists()) {
            LOGGER.debug("path to content already exists: {}", fileName);
            throw new ContentRepositoryException("path to content already exists: " + fileName);
        }

        // otherwise create it and write the content to store in it
        file.mkdir();
        file = new File(fileName + File.separator + CONTENT_FILE_NAME
                + getFileExtension(contentTo.getMetadata().getFilename()));

        String maxAttachmentSize = ApplicationProperty.ATTACHMENT_MAX_UPLOAD_SIZE.getValue();
        Long maxAttachmentSizeAsLong = NumberUtils.toLong(maxAttachmentSize, 10485760);
        try {
            contentTo.write(file, maxAttachmentSizeAsLong);
        } catch (IOException e) {
            LOGGER.error("Error writing to file " + file.getName() + " " + e.getMessage(), e);
            throw new ContentRepositoryException("Error writing to file " + file.getName() + " "
                    + e.getMessage(), e);
        }

        // create the returning ContentId
        ContentId contentId = new ContentId();
        contentId.setConnectorId(contentTo.getMetadata().getContentId().getConnectorId());
        contentId.setContentId(date.getTime() + "_" + hash);
        contentTo.getMetadata().setContentId(contentId);
        contentTo.getMetadata().setMimeType(MimeTypeHelper.getMimeType(file));

        LOGGER.debug("store content {}>>{}", contentId.getConnectorId(), contentId.getContentId());

        // write the content metadata into a XML file
        try {
            storeMetadata(contentTo.getMetadata(), fileName);
        } catch (ContentRepositoryException e) {
            LOGGER.warn("exception occured on storing metadata", e);
        }

        // seems wrong because size on disk can be larger than size of content but is intended
        // because what counts w.r.t. to limits is the latter
        // TODO definitely is wrong w.r.t. thread-safety
        getClientStatisticDao().incrementRepositorySize(contentTo.getMetadata().getContentSize());

        return contentId;
    }

    /**
     * Stores the metadata in a XML file.
     *
     * @param metadata
     *            The metadata to store.
     * @param fileName
     *            The filename of the metadata file.
     * @throws ContentRepositoryException
     *             if storing ContentMetadata failed.
     */
    private void storeMetadata(ContentMetadata metadata, String fileName)
            throws ContentRepositoryException {
        LOGGER.debug("store metadata for {}>>{}", metadata.getContentId().getConnectorId(),
                metadata.getContentId().getContentId());
        File file;
        file = new File(fileName + File.separator + METADATA_FILE_NAME);
        // FileWriter writer = null;
        OutputStreamWriter writer = null;
        try {
            // writer = new FileWriter(file);
            writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));
            MetadataXMLCreator.writeMetadata(this, writer, metadata);
        } catch (IOException e) {
            throw new ContentRepositoryException("storing metadata failed for "
                    + metadata.getContentId().getConnectorId() + ">>"
                    + metadata.getContentId().getContentId());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMetadata(ContentMetadata metadata) throws ContentRepositoryException {
        LOGGER.debug("update metadata for {}>>{}", metadata.getContentId().getConnectorId(),
                metadata.getContentId().getContentId());
        String pathname = getPath() + metadata.getContentId().getContentId();
        File file = new File(pathname + File.separator + METADATA_FILE_NAME);
        if (!file.delete()) {
            LOGGER.info("no metadata file in " + pathname + " for "
                    + connectorConfiguration.getConnectorId() + ">>"
                    + metadata.getContentId().getContentId() + " to update, creating metadata file");
        }
        storeMetadata(metadata, pathname);
    }
}
