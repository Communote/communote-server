package com.communote.server.core.attachment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.util.Pair;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.core.crc.FilesystemConnector;
import com.communote.server.core.crc.RepositoryConnector;
import com.communote.server.core.crc.RepositoryConnectorConfiguration;
import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ExtendedContentId;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.persistence.crc.ContentRepositoryException;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.RepositoryConnectorTestUtils;

/**
 * Test class for the {@link FilesystemConnector}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FilesystemConnectorTest extends CommunoteIntegrationTest {

    /**
     * the number of contents to store
     */
    private final int numberOfContents = 5;

    /**
     * an array to store the send contents
     */
    private AttachmentTO[] contentIn;

    /**
     * the connector for testing
     */
    private RepositoryConnector connector;

    /**
     * a counter to generate ids
     */
    private final long counter = System.currentTimeMillis();

    /**
     * the directors to work for the connector
     */
    private String storageDir = null;

    /**
     * Compares, whether two BinaryContentTO arrays are equal
     *
     * @param contentIn
     *            A BinaryContentTO array.
     * @param contentOut
     *            A BinaryContentTO array to compare with.
     * @param logger
     *            A Logger to log the result.
     * @throws IOException
     *             in case of an io error
     */
    private void compareResults(AttachmentTO[] contentIn, AttachmentTO[] contentOut)
            throws IOException {
        for (int i = 0; i < contentIn.length; i++) {

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            contentIn[i].write(outStream);
            byte[] in = outStream.toByteArray();

            outStream = new ByteArrayOutputStream();
            contentOut[i].write(outStream);
            byte[] out = outStream.toByteArray();

            Assert.assertEquals(out.length, in.length);
            Assert.assertEquals(out, in);
            // TODO cannot assume a certain mime type because mimetype is extracted when attachment
            // is stored. The random bytes of the content can lead to certain mimetype (had a test
            // run which found video/x-unknown). Maybe only create plaintext attachments?
            // Assert.assertEquals(contentOut[i].getMetadata().getMimeType(),
            // "application/octet-stream");
            Assert.assertEquals(contentOut[i].getMetadata().getContentId(), contentIn[i]
                    .getMetadata().getContentId());

            if (contentIn[i].getMetadata().getDate().getTime() > contentOut[i].getMetadata()
                    .getDate().getTime() + 1000
                    || contentIn[i].getMetadata().getDate().getTime() < contentOut[i].getMetadata()
                            .getDate().getTime() - 1000) {
                Assert.fail("time not OK, expected: "
                        + contentIn[i].getMetadata().getDate().getTime() + ", but found: "
                        + contentOut[i].getMetadata().getDate().getTime());
            }

        }
    }

    /**
     * Test to create FilesystemConnector.
     *
     * @throws FileNotFoundException
     *             if "kenmei.crc.file.repository.storage.dir" not exists.
     */
    @Test(groups = { "FilesystemConnectorTests" })
    public void testCreateConnector() throws FileNotFoundException {
        storageDir = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.FILE_SYSTEM_REPOSITORY_STORAGE_DIR_ROOT);

        File file = new File(storageDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new FileNotFoundException("cannot create storage directory: " + storageDir
                        + ", cannot execute FilesystemConnectorTest");
            }
        }

        RepositoryConnectorConfiguration configuration = new RepositoryConnectorConfiguration(
                "test_filesystem_connector", true);
        connector = new FilesystemConnector(configuration);
    }

    /**
     * Test deletes the stored content.
     *
     * @throws ContentRepositoryException
     *             if test failed.
     */
    @Test(groups = { "FilesystemConnectorTests" }, dependsOnMethods = { "testGetContent" })
    public void testDeleteContent() throws ContentRepositoryException {

        // delete the content
        for (int i = 0; i < numberOfContents; i++) {
            connector.deleteContent(contentIn[i].getMetadata().getContentId());
        }
    }

    /**
     * Test to receive the stored content.
     *
     * @throws ContentRepositoryException
     *             if test failed.
     * @throws IOException
     *             in case of an io error
     */
    @Test(groups = { "FilesystemConnectorTests" }, dependsOnMethods = { "testStoreContent" })
    public void testGetContent() throws ContentRepositoryException, IOException {

        AttachmentTO[] contentOut = new AttachmentTO[numberOfContents];

        // get the content
        for (int i = 0; i < numberOfContents; i++) {
            contentOut[i] = connector.getContent(new ExtendedContentId(contentIn[i].getMetadata()
                    .getContentId()));
        }

        compareResults(contentIn, contentOut);
    }

    /**
     * Test to delete the Connector.
     *
     * @throws ContentRepositoryException
     */
    @Test(groups = { "FilesystemConnectorTests" }, dependsOnMethods = { "testDeleteContent" })
    public void testRemoveConnector() throws ContentRepositoryException {
        connector.removeConnector();
    }

    /**
     * Test to store new content.
     *
     * @throws ContentRepositoryException
     *             if test failed.
     */
    @Test(groups = { "FilesystemConnectorTests" }, dependsOnMethods = { "testCreateConnector" })
    public void testStoreContent() throws ContentRepositoryException {

        ContentId contentId;
        contentIn = new AttachmentTO[numberOfContents];

        // store the content
        for (int i = 0; i < numberOfContents; i++) {
            Pair<AttachmentTO, AttachmentTO> pair = RepositoryConnectorTestUtils
                    .createAttachmentTO(i * 20, "" + counter);
            contentId = connector.storeContent(pair.getLeft());
            contentIn[i] = pair.getRight();
            contentIn[i].getMetadata().setContentId(contentId);
        }
    }
}
