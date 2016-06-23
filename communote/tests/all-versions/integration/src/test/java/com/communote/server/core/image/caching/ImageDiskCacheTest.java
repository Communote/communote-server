package com.communote.server.core.image.caching;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.image.ImageSize;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.image.ByteArrayImage;
import com.communote.server.api.core.image.Image;
import com.communote.server.test.CommunoteIntegrationTest;

/**
 * Tests for {@link ImageDiskCache}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageDiskCacheTest extends CommunoteIntegrationTest {

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(ImageDiskCacheTest.class);

    private static final String PROVIDER_ID = "imageDiskCacheProvider";
    private static final String IMAGE_TYPE = "testImage";

    private String clientId;
    private ClientTO orgClient;
    private File cacheRootDirectory;
    private ImageDiskCache imageDiskCache;

    /**
     * Assert that the file exists and has the provided payload as content.
     *
     * @param imageFile
     *            the file to test
     * @param payload
     *            the content
     * @throws IOException
     *             in case reading the file failed
     */
    private void assertFileContent(File imageFile, byte[] payload) throws IOException {
        Assert.assertNotNull(imageFile);
        Assert.assertTrue(imageFile.exists());
        FileInputStream imageFileStream = new FileInputStream(imageFile);
        Assert.assertTrue(IOUtils.contentEquals(imageFileStream, new ByteArrayInputStream(payload)));
        IOUtils.closeQuietly(imageFileStream);
    }

    /**
     * Assert that a range of images which were created with
     * {@link #createImagesForRemoveTest(String, String, String, int, ImageSize, ImageSize)} do not
     * exist.
     *
     * @param typeName
     *            the name of the image type
     * @param providerId
     *            the ID of the image provider
     * @param imageIdBase
     *            prefix of each image ID
     * @param startIdx
     *            index where to start checking for images
     * @param endIdx
     *            last index to check
     * @param size1
     *            the size of the first scaled image
     * @param size2
     *            the size of the 2nd scaled image
     */
    private void assertImagesDoNotExist(String typeName, String providerId, String imageIdBase,
            int startIdx, int endIdx, ImageSize size1, ImageSize size2) {
        for (int i = startIdx; i <= endIdx; i++) {
            String imageId = imageIdBase + i;
            File imageFile = imageDiskCache.getUnscaledImage(typeName, providerId, imageId);
            Assert.assertNull(imageFile, "Unscaled image of type " + typeName + " and provider "
                    + providerId + " with ID " + imageId + " exists.");
            imageFile = imageDiskCache.getScaledImage(typeName, providerId, imageId, size1);
            Assert.assertNull(imageFile, "Scaled image in size1 of type " + typeName
                    + " and provider " + providerId + " with ID " + imageId + " exists.");
            imageFile = imageDiskCache.getScaledImage(typeName, providerId, imageId, size2);
            Assert.assertNull(imageFile, "Scaled image in size2 of type " + typeName
                    + " and provider " + providerId + " with ID " + imageId + " exists.");
        }
    }

    /**
     * Assert that a range of images which were created with
     * {@link #createImagesForRemoveTest(String, String, String, int, ImageSize, ImageSize)} exist.
     *
     * @param typeName
     *            the name of the image type
     * @param providerId
     *            the ID of the image provider
     * @param imageIdBase
     *            prefix of each image ID
     * @param startIdx
     *            index where to start checking for images
     * @param endIdx
     *            last index to check
     * @param size1
     *            the size of the first scaled image
     * @param size2
     *            the size of the 2nd scaled image
     */
    private void assertImagesExist(String typeName, String providerId, String imageIdBase,
            int startIdx, int endIdx, ImageSize size1, ImageSize size2) {
        for (int i = startIdx; i <= endIdx; i++) {
            String imageId = imageIdBase + i;
            File imageFile = imageDiskCache.getUnscaledImage(typeName, providerId, imageId);
            Assert.assertNotNull(imageFile, "Unscaled image of type " + typeName + " and provider "
                    + providerId + " with ID " + imageId + " does not exist");
            imageFile = imageDiskCache.getScaledImage(typeName, providerId, imageId, size1);
            Assert.assertNotNull(imageFile, "Scaled image in size1 of type " + typeName
                    + " and provider " + providerId + " with ID " + imageId + " does not exist");
            imageFile = imageDiskCache.getScaledImage(typeName, providerId, imageId, size2);
            Assert.assertNotNull(imageFile, "Scaled image in size2 of type " + typeName
                    + " and provider " + providerId + " with ID " + imageId + " does not exist");
        }
    }

    /**
     * Assert that a range of scaled images which were created with
     * {@link #createImagesForRemoveTest(String, String, String, int, ImageSize, ImageSize)} do not
     * exist.
     *
     * @param typeName
     *            the name of the image type
     * @param providerId
     *            the ID of the image provider
     * @param imageIdBase
     *            prefix of each image ID
     * @param startIdx
     *            index where to start checking for images
     * @param endIdx
     *            last index to check
     * @param size1
     *            the size of the first scaled image
     * @param size2
     *            the size of the 2nd scaled image
     */
    private void assertScaledImagesDoNotExist(String typeName, String providerId,
            String imageIdBase, int startIdx, int endIdx, ImageSize size1, ImageSize size2) {
        for (int i = startIdx; i <= endIdx; i++) {
            String imageId = imageIdBase + i;
            File imageFile = imageDiskCache.getScaledImage(typeName, providerId, imageId, size1);
            Assert.assertNull(imageFile, "Scaled image in size1 of type " + typeName
                    + " and provider " + providerId + " with ID " + imageId + " exists.");
            imageFile = imageDiskCache.getScaledImage(typeName, providerId, imageId, size2);
            Assert.assertNull(imageFile, "Scaled image in size2 of type " + typeName
                    + " and provider " + providerId + " with ID " + imageId + " exists.");
        }
    }

    /**
     * Assert that a range of unscaled images which were created with
     * {@link #createImagesForRemoveTest(String, String, String, int, ImageSize, ImageSize)} exist.
     *
     * @param typeName
     *            the name of the image type
     * @param providerId
     *            the ID of the image provider
     * @param imageIdBase
     *            prefix of each image ID
     * @param startIdx
     *            index where to start checking for images
     * @param endIdx
     *            last index to check
     * @param size1
     *            the size of the first scaled image
     * @param size2
     *            the size of the 2nd scaled image
     */
    private void assertUnscaledImagesExist(String typeName, String providerId, String imageIdBase,
            int startIdx, int endIdx, ImageSize size1, ImageSize size2) {
        for (int i = startIdx; i <= endIdx; i++) {
            String imageId = imageIdBase + i;
            File imageFile = imageDiskCache.getUnscaledImage(typeName, providerId, imageId);
            Assert.assertNotNull(imageFile, "Unscaled image of type " + typeName + " and provider "
                    + providerId + " with ID " + imageId + " does not exist");
        }
    }

    /**
     * Cleanup
     *
     * @throws IOException
     *             in case removing temp directory failed
     */
    @AfterClass
    public void cleanup() throws IOException {
        ClientAndChannelContextHolder.setClient(orgClient);
        FileUtils.deleteDirectory(cacheRootDirectory);
    }

    /**
     * Create numberOfImages unscaled images along with 2 scaled versions of each unscaled image.
     *
     * @param typeName
     *            the name of the image type
     * @param providerId
     *            the ID of the image provider
     * @param imageIdBase
     *            prefix of each image ID
     * @param numberOfImages
     *            the amount of images to create
     * @param size1
     *            the size of the first scaled image
     * @param size2
     *            the size of the 2nd scaled image
     * @throws IOException
     *             in case image creation failed
     */
    private void createImagesForRemoveTest(String typeName, String providerId, String imageIdBase,
            int numberOfImages, ImageSize size1, ImageSize size2) throws IOException {
        for (int i = 1; i <= numberOfImages; i++) {
            byte[] payload = RandomStringUtils.random(1000).getBytes();
            Image image = new ByteArrayImage(payload, "image/unknown", new Date(), providerId,
                    false);
            imageDiskCache.storeUnscaledImage(typeName, providerId, imageIdBase + i, image);
            // create scaled versions with 2 sizes
            imageDiskCache.storeScaledImage(typeName, providerId, imageIdBase + i, image, size1);
            imageDiskCache.storeScaledImage(typeName, providerId, imageIdBase + i, image, size2);
        }
    }

    /**
     * Modify last byte of the payload
     *
     * @param payload
     *            the byte array to modify
     */
    private void modifyPayload(byte[] payload) {
        int idx = payload.length - 1;
        if (payload[idx] == 8) {
            payload[idx] = 7;
        } else {
            payload[idx] = 8;
        }
    }

    /**
     * Set the current client
     *
     * @param clientId
     *            the client ID of the client
     */
    private void setClient(String clientId) {
        ClientTO client = new ClientTO();
        client.setClientId(clientId);
        ClientAndChannelContextHolder.setClient(client);
    }

    /**
     * prepare the tests
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass
    public void setup() throws Exception {
        orgClient = ClientAndChannelContextHolder.getClient();
        clientId = UUID.randomUUID().toString();
        setClient(clientId);
        File communoteCacheRoot = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties().getCacheRootDirectory();
        cacheRootDirectory = new File(communoteCacheRoot, "ImageDiskCacheTest");
        cacheRootDirectory.mkdirs();
        imageDiskCache = new ImageDiskCache(cacheRootDirectory.getAbsolutePath());
        LOGGER.debug("Using the following directory for cache: " + cacheRootDirectory);
    }

    /**
     * Test for removing stored scaled and unscaled images
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testRemoveDefaultImages() throws Exception {
        String typeName1 = UUID.randomUUID().toString();
        String typeName2 = typeName1 + "2";
        String providerId1 = PROVIDER_ID + "RemoveDefault";
        String providerId2 = providerId1 + "2";
        ImageSize size1 = new ImageSize(RandomUtils.nextInt(100) + 1, RandomUtils.nextInt(100) + 1);
        ImageSize size2 = new ImageSize(size1.getWidth() + 1, size1.getHeight() + 1);
        byte[] payload = RandomStringUtils.random(1000).getBytes();
        Image image = new ByteArrayImage(payload, "image/unknown", new Date(), providerId1, true);
        imageDiskCache.storeDefaultImage(typeName1, providerId1, image, size1);
        imageDiskCache.storeDefaultImage(typeName1, providerId1, image, size2);
        imageDiskCache.storeDefaultImage(typeName2, providerId1, image, size1);
        imageDiskCache.storeDefaultImage(typeName2, providerId1, image, size2);
        image = new ByteArrayImage(payload, "image/unknown", new Date(), providerId2, true);
        imageDiskCache.storeDefaultImage(typeName1, providerId2, image, size1);
        imageDiskCache.storeDefaultImage(typeName1, providerId2, image, size2);
        String imageIdBase = UUID.randomUUID().toString() + "_";
        createImagesForRemoveTest(typeName1, providerId1, imageIdBase, 1, size1, size2);

        imageDiskCache.removeDefaultImages(typeName1, providerId1);
        Assert.assertNull(imageDiskCache.getDefaultImage(typeName1, providerId1, size1));
        Assert.assertNull(imageDiskCache.getDefaultImage(typeName1, providerId1, size2));
        Assert.assertNotNull(imageDiskCache.getDefaultImage(typeName1, providerId2, size1));
        Assert.assertNotNull(imageDiskCache.getDefaultImage(typeName1, providerId2, size2));
        Assert.assertNotNull(imageDiskCache.getDefaultImage(typeName2, providerId1, size1));
        Assert.assertNotNull(imageDiskCache.getDefaultImage(typeName2, providerId1, size2));
        assertImagesExist(typeName1, providerId1, imageIdBase, 1, 1, size1, size2);
    }

    /**
     * Test for removing stored scaled and unscaled images
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testRemoveImages() throws Exception {
        String typeName1 = UUID.randomUUID().toString();
        String typeName2 = typeName1 + "2";
        String providerId1 = PROVIDER_ID + "Remove";
        String providerId2 = providerId1 + "2";
        String imageIdBase = UUID.randomUUID().toString() + "_";
        ImageSize size1 = new ImageSize(RandomUtils.nextInt(100) + 1, RandomUtils.nextInt(100) + 1);
        ImageSize size2 = new ImageSize(size1.getWidth() + 1, size1.getHeight() + 1);
        int numberOfImages = 10 + RandomUtils.nextInt(100);
        createImagesForRemoveTest(typeName1, providerId1, imageIdBase, numberOfImages, size1, size2);
        // add images for other type
        createImagesForRemoveTest(typeName2, providerId1, imageIdBase, 1, size1, size2);
        // add images for other provider
        createImagesForRemoveTest(typeName1, providerId2, imageIdBase, 1, size1, size2);
        // add 2 default images
        byte[] payload = RandomStringUtils.random(1000).getBytes();
        Image image = new ByteArrayImage(payload, "image/unknown", new Date(), providerId1, true);
        imageDiskCache.storeDefaultImage(typeName1, providerId1, image, size1);
        imageDiskCache.storeDefaultImage(typeName1, providerId1, image, size2);

        // test removal with concrete ID
        imageDiskCache.removeImages(typeName1, providerId1, imageIdBase + 1);
        assertImagesDoNotExist(typeName1, providerId1, imageIdBase, 1, 1, size1, size2);
        assertImagesExist(typeName1, providerId1, imageIdBase, 2, numberOfImages, size1, size2);
        assertImagesExist(typeName2, providerId1, imageIdBase, 1, 1, size1, size2);
        assertImagesExist(typeName1, providerId2, imageIdBase, 1, 1, size1, size2);
        Assert.assertNotNull(imageDiskCache.getDefaultImage(typeName1, providerId1, size1));
        Assert.assertNotNull(imageDiskCache.getDefaultImage(typeName1, providerId1, size2));

        // test removal of all images of type and provider
        imageDiskCache.removeImages(typeName1, providerId1, null);
        assertImagesDoNotExist(typeName1, providerId1, imageIdBase, 1, numberOfImages, size1, size2);
        assertImagesExist(typeName2, providerId1, imageIdBase, 1, 1, size1, size2);
        assertImagesExist(typeName1, providerId2, imageIdBase, 1, 1, size1, size2);
        Assert.assertNotNull(imageDiskCache.getDefaultImage(typeName1, providerId1, size1));
        Assert.assertNotNull(imageDiskCache.getDefaultImage(typeName1, providerId1, size2));

        // add some images again and test remove of all images of a type
        createImagesForRemoveTest(typeName1, providerId1, imageIdBase, 11, size1, size2);
        imageDiskCache.removeScaledImages(typeName1);
        assertScaledImagesDoNotExist(typeName1, providerId1, imageIdBase, 1, 11, size1, size2);
        assertUnscaledImagesExist(typeName1, providerId1, imageIdBase, 1, 11, size1, size2);
        assertScaledImagesDoNotExist(typeName1, providerId2, imageIdBase, 1, 1, size1, size2);
        assertUnscaledImagesExist(typeName1, providerId2, imageIdBase, 1, 1, size1, size2);
        assertImagesExist(typeName2, providerId1, imageIdBase, 1, 1, size1, size2);
        Assert.assertNull(imageDiskCache.getDefaultImage(typeName1, providerId1, size1));
        Assert.assertNull(imageDiskCache.getDefaultImage(typeName1, providerId1, size2));
    }

    private void testSeparateDefaultFileIsStored(String typeName, String providerId,
            ImageSize size, Image image, File referenceFile) throws IOException {
        File imageFile = imageDiskCache.getDefaultImage(typeName, providerId, size);
        Assert.assertNull(imageFile);
        imageFile = imageDiskCache.storeDefaultImage(typeName, providerId, image, size);
        Assert.assertNotNull(imageFile);
        Assert.assertTrue(imageFile.exists());
        Assert.assertNotEquals(imageFile.getAbsolutePath(), referenceFile.getAbsolutePath());
        Assert.assertTrue(referenceFile.exists());
    }

    private void testSeparateScaledFileIsStored(String typeName, String providerId,
            String imageIdentifier, ImageSize size, Image image, File referenceFile)
                    throws IOException {
        File imageFile = imageDiskCache.getScaledImage(typeName, providerId, imageIdentifier, size);
        Assert.assertNull(imageFile);
        imageFile = imageDiskCache.storeScaledImage(typeName, providerId, imageIdentifier, image,
                size);
        Assert.assertNotNull(imageFile);
        Assert.assertTrue(imageFile.exists());
        Assert.assertNotEquals(imageFile.getAbsolutePath(), referenceFile.getAbsolutePath());
        Assert.assertTrue(referenceFile.exists());
    }

    private void testSeparateUnscaledFileIsStored(String typeName, String providerId,
            String imageIdentifier, Image image, File referenceFile) throws IOException {
        File imageFile = imageDiskCache.storeUnscaledImage(typeName, providerId, imageIdentifier,
                image);
        Assert.assertNotNull(imageFile);
        Assert.assertTrue(imageFile.exists());
        Assert.assertNotEquals(imageFile.getAbsolutePath(), referenceFile.getAbsolutePath());
        Assert.assertTrue(referenceFile.exists());
    }

    /**
     * Test for storing default images on disk
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testStoreDefaultImages() throws Exception {
        byte[] payload = RandomStringUtils.random(1000).getBytes();
        Image image = new ByteArrayImage(payload, "image/unknown", new Date(), PROVIDER_ID, true);
        ImageSize size = new ImageSize(RandomUtils.nextInt(100) + 1, RandomUtils.nextInt(100) + 1);
        Assert.assertNull(imageDiskCache.getDefaultImage(IMAGE_TYPE, PROVIDER_ID, size));
        File imageFile = imageDiskCache.storeDefaultImage(IMAGE_TYPE, PROVIDER_ID, image, size);
        assertFileContent(imageFile, payload);
        Assert.assertEquals(imageDiskCache.getDefaultImage(IMAGE_TYPE, PROVIDER_ID, size)
                .getAbsolutePath(), imageFile.getAbsolutePath());

        // test that image is updated
        modifyPayload(payload);
        image = new ByteArrayImage(payload, "image/unknown", new Date(), PROVIDER_ID, true);
        imageFile = imageDiskCache.storeDefaultImage(IMAGE_TYPE, PROVIDER_ID, image, size);
        assertFileContent(imageFile, payload);

        // test that images of other size but same provider and type won't conflict
        testSeparateDefaultFileIsStored(IMAGE_TYPE, PROVIDER_ID, new ImageSize(size.getWidth() + 1,
                size.getHeight() + 1), image, imageFile);
        // test that images of other provider but same type and size won't conflict
        testSeparateDefaultFileIsStored(IMAGE_TYPE, PROVIDER_ID + "2", size, image, imageFile);
        // test that images of other type but same provider and size won't conflict
        testSeparateDefaultFileIsStored(IMAGE_TYPE + "2", PROVIDER_ID, size, image, imageFile);

        // test no conflict with scaled images of same provider, type and size
        String imageIdentifier = UUID.randomUUID().toString();
        testSeparateScaledFileIsStored(IMAGE_TYPE, PROVIDER_ID, imageIdentifier, size, image,
                imageFile);

        // test no conflict with unscaled images of same provider and type
        testSeparateUnscaledFileIsStored(IMAGE_TYPE, imageIdentifier, imageIdentifier, image,
                imageFile);

        // test that disk cache is client-aware
        try {
            setClient(clientId + "2");
            Assert.assertNull(imageDiskCache.getDefaultImage(IMAGE_TYPE, imageIdentifier, size));
            testSeparateDefaultFileIsStored(IMAGE_TYPE, PROVIDER_ID, size, image, imageFile);
        } finally {
            setClient(clientId);
        }
    }

    /**
     * Test for storing scaled images on disk
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testStoreScaledImages() throws Exception {
        byte[] payload = RandomStringUtils.random(1000).getBytes();
        Image image1 = new ByteArrayImage(payload, "image/unknown", new Date(), PROVIDER_ID, false);
        ImageSize size1 = new ImageSize(RandomUtils.nextInt(100) + 1, RandomUtils.nextInt(100) + 1);
        String imageIdentifier1 = UUID.randomUUID().toString();
        File imageFile1 = imageDiskCache.storeScaledImage(IMAGE_TYPE, PROVIDER_ID,
                imageIdentifier1, image1, size1);
        assertFileContent(imageFile1, payload);
        File imageFile2 = imageDiskCache.getScaledImage(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1,
                size1);
        Assert.assertNotNull(imageFile2);
        Assert.assertTrue(imageFile2.exists());
        Assert.assertEquals(imageFile2.getAbsolutePath(), imageFile1.getAbsolutePath());

        // test that storing with other size will not override images with same ID and other size
        ImageSize size2 = new ImageSize(size1.getWidth() + 1, size1.getHeight() + 1);
        testSeparateScaledFileIsStored(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1, size2, image1,
                imageFile1);

        // test that images with other ID but but same provider, type and size won't conflict
        String imageIdentifier2 = imageIdentifier1 + "2";
        testSeparateScaledFileIsStored(IMAGE_TYPE, PROVIDER_ID, imageIdentifier2, size1, image1,
                imageFile1);

        // test that images of other provider but same ID, type and size won't conflict
        Image image2 = new ByteArrayImage(payload, "image/unknown", new Date(), PROVIDER_ID + "2",
                false);
        testSeparateScaledFileIsStored(IMAGE_TYPE, PROVIDER_ID + "2", imageIdentifier1, size1,
                image2, imageFile1);

        // test that images of other type but same ID, provider and size won't conflict
        testSeparateScaledFileIsStored(IMAGE_TYPE + "2", PROVIDER_ID, imageIdentifier1, size1,
                image1, imageFile1);

        // test that disk cache is client-aware
        try {
            setClient(clientId + "2");
            Assert.assertNull(imageDiskCache.getScaledImage(IMAGE_TYPE, imageIdentifier1,
                    imageIdentifier1, size1));
            testSeparateScaledFileIsStored(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1, size1,
                    image1, imageFile1);
        } finally {
            setClient(clientId);
        }

        // test that existing scaled images are not updated
        Assert.assertTrue(imageFile1.exists());
        byte orgValue = payload[payload.length - 1];
        modifyPayload(payload);
        image1 = new ByteArrayImage(payload, "image/unknown", new Date(), PROVIDER_ID, false);
        imageFile2 = imageDiskCache.storeScaledImage(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1,
                image1, size1);
        payload[payload.length - 1] = orgValue;
        assertFileContent(imageFile2, payload);
        Assert.assertEquals(imageFile2.getAbsolutePath(), imageFile1.getAbsolutePath());
    }

    /**
     * Test for storing unscaled images on disk
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testStoreUnscaledImages() throws Exception {
        byte[] payload = RandomStringUtils.random(1000).getBytes();
        Image image1 = new ByteArrayImage(payload, "image/unknown", new Date(), PROVIDER_ID, false);
        String imageIdentifier1 = UUID.randomUUID().toString();
        File imageFile1 = imageDiskCache.storeUnscaledImage(IMAGE_TYPE, PROVIDER_ID,
                imageIdentifier1, image1);
        assertFileContent(imageFile1, payload);
        File imageFile2 = imageDiskCache
                .getUnscaledImage(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1);
        Assert.assertNotNull(imageFile2);
        Assert.assertTrue(imageFile2.exists());
        Assert.assertEquals(imageFile2.getAbsolutePath(), imageFile1.getAbsolutePath());

        // test that images with other ID but but same provider and type won't conflict
        testSeparateUnscaledFileIsStored(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1 + "2", image1,
                imageFile1);

        // test that images of other provider but same ID, type and size won't conflict
        Image image2 = new ByteArrayImage(payload, "image/unknown", new Date(), PROVIDER_ID + "2",
                false);
        testSeparateUnscaledFileIsStored(IMAGE_TYPE, PROVIDER_ID + "2", imageIdentifier1, image2,
                imageFile1);

        // test that images of other type but same ID and provider won't conflict
        testSeparateUnscaledFileIsStored(IMAGE_TYPE + "2", PROVIDER_ID, imageIdentifier1, image1,
                imageFile1);

        // test that there are no conflicts with scaled images
        ImageSize size1 = new ImageSize(RandomUtils.nextInt(100) + 1, RandomUtils.nextInt(100) + 1);
        testSeparateScaledFileIsStored(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1, size1, image1,
                imageFile1);
        ImageSize size2 = new ImageSize(size1.getWidth() + 1, size1.getHeight() + 1);
        testSeparateScaledFileIsStored(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1, size2, image1,
                imageFile1);

        // test that disk cache is client-aware
        try {
            setClient(clientId + "2");
            testSeparateUnscaledFileIsStored(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1, image1,
                    imageFile1);
        } finally {
            setClient(clientId);
        }
        // scaled images of other client must still exist
        Assert.assertTrue(imageDiskCache.getScaledImage(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1,
                size1).exists());
        Assert.assertTrue(imageDiskCache.getScaledImage(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1,
                size2).exists());

        // test that the scaled files are removed when unscaled is updated and that unscaled changes
        modifyPayload(payload);
        image1 = new ByteArrayImage(payload, "image/unknown", new Date(), PROVIDER_ID, false);
        imageFile1 = imageDiskCache.storeUnscaledImage(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1,
                image1);
        assertFileContent(imageFile1, payload);
        Assert.assertNull(imageDiskCache.getScaledImage(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1,
                size1));
        Assert.assertNull(imageDiskCache.getScaledImage(IMAGE_TYPE, PROVIDER_ID, imageIdentifier1,
                size2));
    }
}
