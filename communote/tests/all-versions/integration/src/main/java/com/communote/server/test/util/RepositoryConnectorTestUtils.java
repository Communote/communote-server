package com.communote.server.test.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

import com.communote.common.util.Pair;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.core.crc.FilesystemConnector;
import com.communote.server.core.crc.RepositoryConnector;
import com.communote.server.core.crc.RepositoryConnectorConfiguration;
import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.vo.content.AttachmentStreamTO;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.model.attachment.AttachmentStatus;

/**
 * Helper for the RepositoryConnector test classes
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RepositoryConnectorTestUtils {

    /**
     * Creates a unique BinaryContentTO to send to a RepositoryConnector.
     *
     * @param size
     *            The size of the binary content to create in bytes.
     * @param connectorId
     *            The connectorId where the content should be send.
     * @return A pair of an identical AttachmentTO.
     */
    public static Pair<AttachmentTO, AttachmentTO> createAttachmentTO(int size, String connectorId) {

        // create and set up ContentId
        ContentId contentId = new ContentId();
        contentId.setConnectorId(connectorId);

        // create and set up ContentMetadata
        ContentMetadata metadata = new ContentMetadata();
        metadata.setMimeType("TEXT/HTML");
        metadata.setDate(new Date());
        metadata.setContentId(contentId);
        metadata.setUrl("http://localhost/communote-test/");
        metadata.setFilename("filename.dat");
        metadata.setVersion("1.0");

        byte[] b = new byte[size];
        for (int i = 0; i < size; i++) {
            b[i] = (byte) Math.round(Math.random() * 127);
        }
        AttachmentTO leftContentTo = new AttachmentStreamTO(new ByteArrayInputStream(b.clone()));
        leftContentTo.setMetadata(metadata);
        leftContentTo.setStatus(AttachmentStatus.PUBLISHED);

        AttachmentTO rightContentTo = new AttachmentStreamTO(new ByteArrayInputStream(b.clone()));
        rightContentTo.setMetadata(metadata);
        rightContentTo.setStatus(AttachmentStatus.PUBLISHED);

        metadata.setContentSize(b.length);

        return new Pair<AttachmentTO, AttachmentTO>(leftContentTo, rightContentTo);
    }

    /**
     * Creates a new FilesystemConnector
     *
     * @param connectorId
     *            A long value set as connectorId.
     * @param supportsMetadata
     *            True, if metadata should be supported.
     * @return the new FilesystemConnector
     * @throws FileNotFoundException
     *             if "kenmei.crc.file.repository.storage.dir" not exists.
     */
    public static RepositoryConnectorConfiguration createFilesystemConfiguration(
            String connectorId, boolean supportsMetadata) throws FileNotFoundException {
        String storageDir = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.FILE_SYSTEM_REPOSITORY_STORAGE_DIR_ROOT);

        File file = new File(storageDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new FileNotFoundException("cannot create storage dir: " + storageDir
                        + ", cannot execute FilesystemConnectorTest");
            }
        }

        RepositoryConnectorConfiguration configuration = new RepositoryConnectorConfiguration(
                connectorId, supportsMetadata);

        return configuration;
    }

    /**
     * Creates a new FilesystemConnector
     *
     * @param connectorId
     *            A long value set as connectorId.
     * @param supportsMetadata
     *            True, if metadata should be supported.
     * @return the new FilesystemConnector
     */
    public static RepositoryConnector createFilesystemConnector(String connectorId,
            boolean supportsMetadata) {
        String storageDir = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.FILE_SYSTEM_REPOSITORY_STORAGE_DIR_ROOT);

        RepositoryConnectorConfiguration configuration = new RepositoryConnectorConfiguration(
                connectorId, supportsMetadata);
        File file = new File(storageDir);
        file.mkdir();

        RepositoryConnector connector = new FilesystemConnector(configuration);
        return connector;
    }

    private RepositoryConnectorTestUtils() {
        // Do nothing.
    }
}
