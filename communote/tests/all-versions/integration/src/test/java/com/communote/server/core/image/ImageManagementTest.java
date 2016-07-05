package com.communote.server.core.image;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.image.ImageFormatType;
import com.communote.common.image.ImageScaler;
import com.communote.common.image.ImageSize;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.common.virusscan.exception.VirusScannerException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.ByteArrayImage;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageDescriptor;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.ImageProvider;
import com.communote.server.api.core.image.ImageTypeDescriptor;
import com.communote.server.api.core.image.type.DefaultImageTypeDescriptor;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.image.type.UserImageProvider;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.model.user.User;
import com.communote.server.service.UserProfileService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageManagementTest extends CommunoteIntegrationTest {

    private class TestImageProvider extends ImageProvider {

        boolean canLoad;
        boolean isAuthorized;
        boolean isExternal;
        byte[] defaultImage;
        String defaultImageVersionString;
        String handledImageId;
        byte[] handledImage;
        String handledImageVersionString;
        Date handledImageLastModified;

        public TestImageProvider(String identifier, byte[] defaultImage,
                String defaultImageVersionString) {
            super(identifier, null);
            this.canLoad = true;
            isAuthorized = true;
            isExternal = false;
            this.defaultImage = defaultImage;
            this.defaultImageVersionString = defaultImageVersionString;
        }

        @Override
        public boolean canLoad(String imageIdentifier) {
            return canLoad;
        }

        @Override
        public String getDefaultImageMimeType() throws ImageNotFoundException {
            if (defaultImage == null) {
                throw new ImageNotFoundException();
            }
            return "image/png";
        }

        @Override
        public String getDefaultImageVersionString() throws ImageNotFoundException {
            if (defaultImage == null) {
                throw new ImageNotFoundException();
            }
            return defaultImageVersionString;
        }

        @Override
        public String getVersionString(String imageIdentifier) throws AuthorizationException,
                ImageNotFoundException {
            if (imageIdentifier.equals(handledImageId)) {
                return handledImageVersionString;
            }
            throw new ImageNotFoundException("Image not handled");
        }

        @Override
        public boolean hasDefaultImage(String imageIdentifier) {
            return defaultImage != null;
        }

        @Override
        public boolean isAuthorized(String imageIdentifier) {
            return isAuthorized;
        }

        @Override
        public boolean isExternalProvider() {
            return isExternal;
        }

        @Override
        public Image loadDefaultImage() throws ImageNotFoundException {
            if (defaultImage == null) {
                throw new ImageNotFoundException();
            }
            return new ByteArrayImage(defaultImage, "image/png", DEFAULT_IMAGE_LAST_MODIFIED,
                    getIdentifier(), true, isExternal);
        }

        @Override
        public Image loadImage(String imageIdentifier) throws ImageNotFoundException,
                AuthorizationException {
            if (imageIdentifier.equals(handledImageId)) {
                return new ByteArrayImage(handledImage, "image/png", handledImageLastModified,
                        getIdentifier(), false, isExternal);
            }

            throw new ImageNotFoundException("Image not handled");
        }

        public void setImage(String imageIdentifier, byte[] imageData, String versionString,
                Date lastModified) {
            handledImage = imageData;
            handledImageId = imageIdentifier;
            handledImageLastModified = lastModified;
            handledImageVersionString = versionString;
        }

        public void unsetImage() {
            handledImage = null;
            handledImageId = null;
            handledImageVersionString = null;
            handledImageLastModified = null;
        }

    }

    private class TestImageType extends DefaultImageTypeDescriptor {

        private final String name;
        private final Map<ImageSizeType, ImageSize> sizeMapping;

        public TestImageType(String name, Map<ImageSizeType, ImageSize> imageSizeMapping) {
            this.name = name;
            this.sizeMapping = imageSizeMapping;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ImageSize getSizeForImageSizeType(ImageSizeType sizeType) {
            return sizeMapping.get(sizeType);
        }
    }

    private static final Date DEFAULT_IMAGE_LAST_MODIFIED = new Date(1000);
    private static final ImageSize DEFAULT_IMAGE_SIZE = new ImageSize(30, 30);
    private static final ImageSize TEST_IMAGE_SIZE_LARGE = new ImageSize(20, 20);
    private static final ImageSize TEST_IMAGE_SIZE_MEDIUM = new ImageSize(10, 10);
    private static final ImageSize TEST_IMAGE_SIZE_SMALL = new ImageSize(5, 5);

    private ImageManager imageManagement;
    private ImageTypeDescriptor testImageType;
    private ImageManagementTest.TestImageProvider testImageProvider;
    private int defaultLargeImageHashCode;
    private int defaultMediumImageHashCode;
    private int defaultSmallImageHashCode;
    private String imageId1;
    private String imageId2;

    private void assertCorrectImage(Image image, String expectedProviderId, int expectedHashCode,
            boolean expectedIsDefault, boolean expectedIsExternal) throws IOException {
        Assert.assertNotNull(image);
        Assert.assertEquals(image.getProviderId(), expectedProviderId);
        Assert.assertEquals(image.isDefaultImage(), expectedIsDefault);
        Assert.assertEquals(image.isExternal(), expectedIsExternal);
        byte[] imageData = image.getBytes();
        Assert.assertNotNull(imageData);
        Assert.assertEquals(Arrays.hashCode(imageData), expectedHashCode);
    }

    private String createVersionString(String imageVersionString, ImageTypeDescriptor imageType) {
        // TODO uses internal knowledge of implementation. But is there a better way to achieve
        // this?
        return imageVersionString + "_t" + imageType.getVersionString();
    }

    private int getHashCodeOfScaledImage(byte[] data, ImageSize size, ImageTypeDescriptor type) {
        ImageScaler scaler = new ImageScaler(size, ImageFormatType.png);
        scaler.setDrawBackground(type.isDrawBackground());
        scaler.setBackgroundColor(type.getBackgroundColor());
        scaler.setSameAspectRatio(type.isPreserveAspectRation());
        scaler.setBackgroundColor(type.getBackgroundColor());
        scaler.setHorizontalAlignment(type.getHorizontalAlignment());
        scaler.setVerticalAlignment(type.getVerticalAlignment());
        byte[] resizedImage = scaler.resizeImage(data);
        return Arrays.hashCode(resizedImage);
    }

    @BeforeClass
    public void setup() throws Exception {
        String imageTypeName = UUID.randomUUID().toString();
        imageManagement = ServiceLocator.findService(ImageManager.class);
        Map<ImageSizeType, ImageSize> sizeMapping = new HashMap<>();
        sizeMapping.put(ImageSizeType.LARGE, TEST_IMAGE_SIZE_LARGE);
        sizeMapping.put(ImageSizeType.MEDIUM, TEST_IMAGE_SIZE_MEDIUM);
        sizeMapping.put(ImageSizeType.SMALL, TEST_IMAGE_SIZE_SMALL);
        testImageType = new TestImageType(imageTypeName, sizeMapping);
        imageManagement.registerImageType(testImageType);
        testImageProvider = new TestImageProvider(UUID.randomUUID().toString(),
                TestUtils.createPngImage(DEFAULT_IMAGE_SIZE.getWidth(),
                        DEFAULT_IMAGE_SIZE.getHeight(), Color.BLUE), UUID.randomUUID().toString());
        imageManagement.registerImageProvider(imageTypeName, testImageProvider);
        defaultLargeImageHashCode = getHashCodeOfScaledImage(testImageProvider.defaultImage,
                TEST_IMAGE_SIZE_LARGE, testImageType);
        defaultSmallImageHashCode = getHashCodeOfScaledImage(testImageProvider.defaultImage,
                TEST_IMAGE_SIZE_SMALL, testImageType);
        defaultMediumImageHashCode = getHashCodeOfScaledImage(testImageProvider.defaultImage,
                TEST_IMAGE_SIZE_MEDIUM, testImageType);
        String imageIdBase = UUID.randomUUID().toString();
        imageId1 = imageIdBase + "_1";
        imageId2 = imageIdBase + "_2";
    }

    /**
     * Test that the authorization is checked
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testAuthorizationIsChecked() throws Exception {
        // force auth exception
        testImageProvider.isAuthorized = false;
        try {
            imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.LARGE);
            Assert.fail("Authorization is not checked");
        } catch (AuthorizationException e) {
            // expected
        } finally {
            testImageProvider.isAuthorized = true;
        }
    }

    /**
     * Test that there is a fallback to a built-in provider if an external provider does not return
     * an image
     */
    @Test
    public void testBuiltInProviderFallback() throws Exception {
        User user = TestUtils.createRandomUser(false);
        byte[] userImage = TestUtils.createPngImage(100, 100, Color.GREEN);
        uploadUserImage(user, userImage);
        ImageSize largeImageSize = imageManagement.getImageSize(
                UserImageDescriptor.IMAGE_TYPE_NAME, ImageSizeType.LARGE);
        int builtInProviderLargeImageHashCode = getHashCodeOfScaledImage(userImage, largeImageSize,
                new UserImageDescriptor());
        Image image = imageManagement.getImage(UserImageDescriptor.IMAGE_TYPE_NAME, user.getId()
                .toString(), ImageSizeType.LARGE);
        assertCorrectImage(image, UserImageProvider.PROVIDER_IDENTIFIER,
                builtInProviderLargeImageHashCode, false, false);
        // register an another user image provider
        TestImageProvider externalUserProvider = new TestImageProvider("testUserImageProvider",
                TestUtils.createPngImage(50, 50, Color.BLUE), UUID.randomUUID().toString());
        imageManagement.registerImageProvider(UserImageDescriptor.IMAGE_TYPE_NAME,
                externalUserProvider);
        byte[] externalUserImage = TestUtils.createPngImage(100, 100, Color.ORANGE);
        String versionString = UUID.randomUUID().toString();
        externalUserProvider.setImage(user.getId().toString(), externalUserImage, versionString,
                new Date());
        // check that the image of the other and not the builtin provider is returned
        int externalProviderLargeImageHashCode = getHashCodeOfScaledImage(externalUserImage,
                largeImageSize, new UserImageDescriptor());
        image = imageManagement.getImage(UserImageDescriptor.IMAGE_TYPE_NAME, user.getId()
                .toString(), ImageSizeType.LARGE);
        assertCorrectImage(image, externalUserProvider.getIdentifier(),
                externalProviderLargeImageHashCode, false, false);
        // simplest case: disable other image provider, another (which is the built-in) should be
        // used
        externalUserProvider.canLoad = false;
        image = imageManagement.getImage(UserImageDescriptor.IMAGE_TYPE_NAME, user.getId()
                .toString(), ImageSizeType.LARGE);
        assertCorrectImage(image, UserImageProvider.PROVIDER_IDENTIFIER,
                builtInProviderLargeImageHashCode, false, false);
        // this should also work if other provider is external
        externalUserProvider.isExternal = true;
        image = imageManagement.getImage(UserImageDescriptor.IMAGE_TYPE_NAME, user.getId()
                .toString(), ImageSizeType.LARGE);
        assertCorrectImage(image, UserImageProvider.PROVIDER_IDENTIFIER,
                builtInProviderLargeImageHashCode, false, false);
        // allow loading again, but remove custom image. Default of external should be returned
        externalUserProvider.canLoad = true;
        externalUserProvider.unsetImage();
        imageManagement.imageChanged(UserImageDescriptor.IMAGE_TYPE_NAME,
                externalUserProvider.getIdentifier(), user.getId().toString());
        int externalDefaultImageLargeHashCode = getHashCodeOfScaledImage(
                externalUserProvider.defaultImage, largeImageSize, new UserImageDescriptor());
        image = imageManagement.getImage(UserImageDescriptor.IMAGE_TYPE_NAME, user.getId()
                .toString(), ImageSizeType.LARGE);
        assertCorrectImage(image, externalUserProvider.getIdentifier(),
                externalDefaultImageLargeHashCode, true, true);
        // remove default image, built-in provider should return the image
        externalUserProvider.defaultImage = null;
        imageManagement.defaultImageChanged(UserImageDescriptor.IMAGE_TYPE_NAME,
                externalUserProvider.getIdentifier());
        image = imageManagement.getImage(UserImageDescriptor.IMAGE_TYPE_NAME, user.getId()
                .toString(), ImageSizeType.LARGE);
        assertCorrectImage(image, UserImageProvider.PROVIDER_IDENTIFIER,
                builtInProviderLargeImageHashCode, false, false);
        // remove image of user default image of built-in should be returned
        uploadUserImage(user, null);
        imageManagement.imageChanged(UserImageDescriptor.IMAGE_TYPE_NAME,
                UserImageProvider.PROVIDER_IDENTIFIER, user.getId().toString());
        image = imageManagement.getImage(UserImageDescriptor.IMAGE_TYPE_NAME, user.getId()
                .toString(), ImageSizeType.LARGE);
        // have no access to the default image of that provider, thus just check that hash-code
        // changed
        Assert.assertEquals(image.getProviderId(), UserImageProvider.PROVIDER_IDENTIFIER);
        Assert.assertTrue(image.isDefaultImage());
        Assert.assertFalse(image.isExternal());
        Assert.assertTrue(image.getBytes().length > 0);
        int returnedImageHashCode = Arrays.hashCode(image.getBytes());
        Assert.assertFalse(returnedImageHashCode == externalDefaultImageLargeHashCode);
        Assert.assertFalse(returnedImageHashCode == builtInProviderLargeImageHashCode);
        Assert.assertFalse(returnedImageHashCode == externalProviderLargeImageHashCode);
        // fallback is intended for external providers only, should get NotFoundException otherwise
        externalUserProvider.isExternal = false;
        try {
            image = imageManagement.getImage(UserImageDescriptor.IMAGE_TYPE_NAME, user.getId()
                    .toString(), ImageSizeType.LARGE);
            Assert.fail("The fallback should not be used for non-external providers");
        } catch (ImageNotFoundException e) {
            // expected
        }
        imageManagement.unregisterImageProvider(UserImageDescriptor.IMAGE_TYPE_NAME,
                externalUserProvider);
    }

    /**
     * Test that the canLoad method of a provider is used before requesting an image.
     *
     * @throws in
     *             case the test failed
     */
    @Test
    public void testCanLoadIsChecked() throws Exception {
        try {
            testImageProvider.canLoad = false;
            imageManagement.getImage(testImageType.getName(), imageId1, ImageSizeType.LARGE);
            Assert.fail("Can load is not checked!");
        } catch (ImageNotFoundException e) {
            // expected
        } finally {
            testImageProvider.canLoad = true;
        }
    }

    /**
     * Check that the custom image instead of the default image is returned
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testCustomImageReturned() throws Exception {
        byte[] customImage = TestUtils.createPngImage(40, 40, Color.RED);
        String versionString = UUID.randomUUID().toString();
        Date lastModified = new Date(DEFAULT_IMAGE_LAST_MODIFIED.getTime() + 360000);
        testImageProvider.setImage(imageId2, customImage, versionString, lastModified);
        // check version string of default image
        Assert.assertEquals(createVersionString(versionString, testImageType),
                imageManagement.getImageVersionString(testImageType.getName(), imageId2));

        int largeImageHashCode = getHashCodeOfScaledImage(customImage, TEST_IMAGE_SIZE_LARGE,
                testImageType);
        int mediumImageHashCode = getHashCodeOfScaledImage(customImage, TEST_IMAGE_SIZE_MEDIUM,
                testImageType);
        int smallImageHashCode = getHashCodeOfScaledImage(customImage, TEST_IMAGE_SIZE_SMALL,
                testImageType);
        // large size image
        Image image = imageManagement.getImage(testImageType.getName(), imageId2,
                ImageSizeType.LARGE);
        assertCorrectImage(image, testImageProvider.getIdentifier(), largeImageHashCode, false,
                false);
        // check getImage with descriptor
        ImageDescriptor descriptor = new ImageDescriptor(TEST_IMAGE_SIZE_LARGE, imageId2, true,
                testImageType.isDrawBackground(), testImageType.getBackgroundColor(),
                testImageType.isPreserveAspectRation());
        image = imageManagement.getImage(testImageType.getName(), descriptor);
        assertCorrectImage(image, testImageProvider.getIdentifier(), largeImageHashCode, false,
                false);
        // medium size image
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.MEDIUM);
        assertCorrectImage(image, testImageProvider.getIdentifier(), mediumImageHashCode, false,
                false);
        // check getImage with parameter map using default "id" parameter name
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("id", imageId2);
        image = imageManagement.getImage(testImageType.getName(), parameters, ImageSizeType.MEDIUM);
        assertCorrectImage(image, testImageProvider.getIdentifier(), mediumImageHashCode, false,
                false);
        // small size image
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.SMALL);
        assertCorrectImage(image, testImageProvider.getIdentifier(), smallImageHashCode, false,
                false);
    }

    /**
     * Test that caches are reset correctly if the default image of a provider was changed.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testDefaultImageChanged() throws Exception {
        byte[] originalDefaultImage = testImageProvider.defaultImage;
        // get image at least once so it is cached
        imageManagement.getImage(testImageType.getName(), imageId1, ImageSizeType.LARGE);
        try {
            // change default image
            testImageProvider.defaultImage = TestUtils.createPngImage(
                    DEFAULT_IMAGE_SIZE.getWidth(), DEFAULT_IMAGE_SIZE.getHeight(), Color.CYAN);
            int newDefaultLargeImageHashCode = getHashCodeOfScaledImage(
                    testImageProvider.defaultImage,
                    imageManagement.getImageSize(testImageType.getName(), ImageSizeType.LARGE),
                    testImageType);
            // avoid false positive
            Assert.assertFalse(defaultLargeImageHashCode == newDefaultLargeImageHashCode);
            imageManagement.defaultImageChanged(testImageType.getName(),
                    testImageProvider.getIdentifier());
            Image image = imageManagement.getImage(testImageType.getName(), imageId1,
                    ImageSizeType.LARGE);
            assertCorrectImage(image, testImageProvider.getIdentifier(),
                    newDefaultLargeImageHashCode, true, false);
        } finally {
            testImageProvider.defaultImage = originalDefaultImage;
            imageManagement.defaultImageChanged(testImageType.getName(),
                    testImageProvider.getIdentifier());
        }
    }

    /**
     * Check that default image is returned, scaled according to requested size.
     *
     * @throws Exception
     *             in case test failed
     */
    @Test
    public void testDefaultImageReturned() throws Exception {
        // check version string of default image
        Assert.assertEquals(
                createVersionString(testImageProvider.defaultImageVersionString, testImageType),
                imageManagement.getImageVersionString(testImageType.getName(), imageId1));
        // large size image
        Image image = imageManagement.getImage(testImageType.getName(), imageId1,
                ImageSizeType.LARGE);
        assertCorrectImage(image, testImageProvider.getIdentifier(), defaultLargeImageHashCode,
                true, false);
        // medium size image
        image = imageManagement.getImage(testImageType.getName(), imageId1, ImageSizeType.MEDIUM);
        assertCorrectImage(image, testImageProvider.getIdentifier(), defaultMediumImageHashCode,
                true, false);
        // small size image
        image = imageManagement.getImage(testImageType.getName(), imageId1, ImageSizeType.SMALL);
        assertCorrectImage(image, testImageProvider.getIdentifier(), defaultSmallImageHashCode,
                true, false);
    }

    /**
     * Test that caches are correctly cleared by imageChanged
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = { "testImageTypeOverlay" })
    public void testImageChanged() throws Exception {
        testImageProvider.unsetImage();
        imageManagement.imageChanged(testImageType.getName(), testImageProvider.getIdentifier(),
                imageId2);
        // should return default image now
        Assert.assertEquals(
                createVersionString(testImageProvider.defaultImageVersionString, testImageType),
                imageManagement.getImageVersionString(testImageType.getName(), imageId2));
        // large size image
        Image image = imageManagement.getImage(testImageType.getName(), imageId2,
                ImageSizeType.LARGE);
        assertCorrectImage(image, testImageProvider.getIdentifier(), defaultLargeImageHashCode,
                true, false);
        // medium size image
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.MEDIUM);
        assertCorrectImage(image, testImageProvider.getIdentifier(), defaultMediumImageHashCode,
                true, false);
        // small size image
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.SMALL);
        assertCorrectImage(image, testImageProvider.getIdentifier(), defaultSmallImageHashCode,
                true, false);
    }

    /**
     * Test that adding and removing providers for a type that overlay another provider for the same
     * type work correctly. Overlay in this context means that the providers handle the same images.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testCustomImageReturned")
    public void testImageProviderOverlay() throws Exception {
        String identifier = testImageProvider.getIdentifier() + "_overlay";
        String versionString = testImageProvider.defaultImageVersionString + "_overlay";
        TestImageProvider overlayProvider = new TestImageProvider(identifier,
                TestUtils.createPngImage(DEFAULT_IMAGE_SIZE.getWidth(),
                        DEFAULT_IMAGE_SIZE.getHeight(), Color.YELLOW), versionString);
        int overlayDefaultLargeImageHashCode = getHashCodeOfScaledImage(
                overlayProvider.defaultImage,
                imageManagement.getImageSize(testImageType.getName(), ImageSizeType.LARGE),
                testImageType);
        int overlayDefaultMediumImageHashCode = getHashCodeOfScaledImage(
                overlayProvider.defaultImage,
                imageManagement.getImageSize(testImageType.getName(), ImageSizeType.MEDIUM),
                testImageType);
        int overlayDefaultSmallImageHashCode = getHashCodeOfScaledImage(
                overlayProvider.defaultImage,
                imageManagement.getImageSize(testImageType.getName(), ImageSizeType.SMALL),
                testImageType);
        // avoid false positives
        Assert.assertFalse(defaultLargeImageHashCode == overlayDefaultLargeImageHashCode);
        Assert.assertFalse(defaultMediumImageHashCode == overlayDefaultMediumImageHashCode);
        Assert.assertFalse(defaultSmallImageHashCode == overlayDefaultSmallImageHashCode);
        imageManagement.registerImageProvider(testImageType.getName(), overlayProvider);
        // using imageId2 which has custom image that was set by testCustomImageReturned
        Assert.assertEquals(createVersionString(versionString, testImageType),
                imageManagement.getImageVersionString(testImageType.getName(), imageId2));
        Image image = imageManagement.getImage(testImageType.getName(), imageId2,
                ImageSizeType.LARGE);
        assertCorrectImage(image, identifier, overlayDefaultLargeImageHashCode, true, false);
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.MEDIUM);
        assertCorrectImage(image, identifier, overlayDefaultMediumImageHashCode, true, false);
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.SMALL);
        assertCorrectImage(image, identifier, overlayDefaultSmallImageHashCode, true, false);
        // add a custom image to overlay provider
        String customVersionString = versionString + "_custom";
        overlayProvider.setImage(imageId2, TestUtils.createPngImage(30, 30, Color.PINK),
                customVersionString, new Date());
        imageManagement.imageChanged(testImageType.getName(), identifier, imageId2);
        Assert.assertEquals(createVersionString(customVersionString, testImageType),
                imageManagement.getImageVersionString(testImageType.getName(), imageId2));
        int overlayLargeImageHashCode = getHashCodeOfScaledImage(overlayProvider.handledImage,
                imageManagement.getImageSize(testImageType.getName(), ImageSizeType.LARGE),
                testImageType);
        int overlayMediumImageHashCode = getHashCodeOfScaledImage(overlayProvider.handledImage,
                imageManagement.getImageSize(testImageType.getName(), ImageSizeType.MEDIUM),
                testImageType);
        int overlaySmallImageHashCode = getHashCodeOfScaledImage(overlayProvider.handledImage,
                imageManagement.getImageSize(testImageType.getName(), ImageSizeType.SMALL),
                testImageType);
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.LARGE);
        assertCorrectImage(image, identifier, overlayLargeImageHashCode, false, false);
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.MEDIUM);
        assertCorrectImage(image, identifier, overlayMediumImageHashCode, false, false);
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.SMALL);
        assertCorrectImage(image, identifier, overlaySmallImageHashCode, false, false);
        // imageId1 should still return the default image of overlay provider
        image = imageManagement.getImage(testImageType.getName(), imageId1, ImageSizeType.SMALL);
        assertCorrectImage(image, identifier, overlayDefaultSmallImageHashCode, true, false);
        // remove overlay again
        imageManagement.unregisterImageProvider(testImageType.getName(), overlayProvider);
        // just test control sample
        Assert.assertEquals(
                createVersionString(testImageProvider.handledImageVersionString, testImageType),
                imageManagement.getImageVersionString(testImageType.getName(), imageId2));
        int largeImageHashCode = getHashCodeOfScaledImage(testImageProvider.handledImage,
                imageManagement.getImageSize(testImageType.getName(), ImageSizeType.LARGE),
                testImageType);
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.LARGE);
        assertCorrectImage(image, testImageProvider.getIdentifier(), largeImageHashCode, false,
                false);
        Assert.assertEquals(
                createVersionString(testImageProvider.defaultImageVersionString, testImageType),
                imageManagement.getImageVersionString(testImageType.getName(), imageId1));
        image = imageManagement.getImage(testImageType.getName(), imageId1, ImageSizeType.LARGE);
        assertCorrectImage(image, testImageProvider.getIdentifier(), defaultLargeImageHashCode,
                true, false);

    }

    @Test
    public void testImageSizeMapping() {
        ImageSize size = imageManagement.getImageSize(testImageType.getName(), ImageSizeType.LARGE);
        Assert.assertEquals(size.getHeight(), TEST_IMAGE_SIZE_LARGE.getHeight());
        Assert.assertEquals(size.getWidth(), TEST_IMAGE_SIZE_LARGE.getWidth());
        size = imageManagement.getImageSize(testImageType.getName(), ImageSizeType.MEDIUM);
        Assert.assertEquals(size.getHeight(), TEST_IMAGE_SIZE_MEDIUM.getHeight());
        Assert.assertEquals(size.getWidth(), TEST_IMAGE_SIZE_MEDIUM.getWidth());
        size = imageManagement.getImageSize(testImageType.getName(), ImageSizeType.SMALL);
        Assert.assertEquals(size.getHeight(), TEST_IMAGE_SIZE_SMALL.getHeight());
        Assert.assertEquals(size.getWidth(), TEST_IMAGE_SIZE_SMALL.getWidth());

        // should be null for unregistered type
        Assert.assertNull(imageManagement.getImageSize(testImageType.getName() + "1",
                ImageSizeType.LARGE));
        Assert.assertNull(imageManagement.getImageSize(testImageType.getName() + "1",
                ImageSizeType.MEDIUM));
        Assert.assertNull(imageManagement.getImageSize(testImageType.getName() + "1",
                ImageSizeType.SMALL));
    }

    /**
     * Test that after adding an overlay image type the images are scaled correctly
     *
     * @throws Exception
     *             in case the test failed
     *
     */
    @Test(dependsOnMethods = { "testImageProviderOverlay" })
    public void testImageTypeOverlay() throws Exception {
        Map<ImageSizeType, ImageSize> sizeMapping = new HashMap<>();
        // define new size mapping
        sizeMapping.put(ImageSizeType.LARGE, DEFAULT_IMAGE_SIZE);
        sizeMapping.put(ImageSizeType.MEDIUM, TEST_IMAGE_SIZE_LARGE);
        sizeMapping.put(ImageSizeType.SMALL, TEST_IMAGE_SIZE_MEDIUM);
        ImageTypeDescriptor typeOverlay = new TestImageType(testImageType.getName(), sizeMapping);
        imageManagement.registerImageType(typeOverlay);
        // check correct size is returned
        ImageSize size = imageManagement.getImageSize(testImageType.getName(), ImageSizeType.LARGE);
        Assert.assertEquals(size.getHeight(), DEFAULT_IMAGE_SIZE.getHeight());
        Assert.assertEquals(size.getWidth(), DEFAULT_IMAGE_SIZE.getWidth());
        size = imageManagement.getImageSize(testImageType.getName(), ImageSizeType.MEDIUM);
        Assert.assertEquals(size.getHeight(), TEST_IMAGE_SIZE_LARGE.getHeight());
        Assert.assertEquals(size.getWidth(), TEST_IMAGE_SIZE_LARGE.getWidth());
        size = imageManagement.getImageSize(testImageType.getName(), ImageSizeType.SMALL);
        Assert.assertEquals(size.getHeight(), TEST_IMAGE_SIZE_MEDIUM.getHeight());
        Assert.assertEquals(size.getWidth(), TEST_IMAGE_SIZE_MEDIUM.getWidth());
        // check default image
        int overlayDefaultLargeImageHashCode = getHashCodeOfScaledImage(
                testImageProvider.defaultImage, DEFAULT_IMAGE_SIZE, testImageType);
        // large size default image
        Image image = imageManagement.getImage(testImageType.getName(), imageId1,
                ImageSizeType.LARGE);
        assertCorrectImage(image, testImageProvider.getIdentifier(),
                overlayDefaultLargeImageHashCode, true, false);
        // medium size default image
        image = imageManagement.getImage(testImageType.getName(), imageId1, ImageSizeType.MEDIUM);
        assertCorrectImage(image, testImageProvider.getIdentifier(), defaultLargeImageHashCode,
                true, false);
        // small size default image
        image = imageManagement.getImage(testImageType.getName(), imageId1, ImageSizeType.SMALL);
        assertCorrectImage(image, testImageProvider.getIdentifier(), defaultMediumImageHashCode,
                true, false);
        // check custom image set by testCustomImageReturned
        int largeImageHashCode = getHashCodeOfScaledImage(testImageProvider.handledImage,
                DEFAULT_IMAGE_SIZE, testImageType);
        int mediumImageHashCode = getHashCodeOfScaledImage(testImageProvider.handledImage,
                TEST_IMAGE_SIZE_LARGE, testImageType);
        int smallImageHashCode = getHashCodeOfScaledImage(testImageProvider.handledImage,
                TEST_IMAGE_SIZE_MEDIUM, testImageType);
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.LARGE);
        assertCorrectImage(image, testImageProvider.getIdentifier(), largeImageHashCode, false,
                false);
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.MEDIUM);
        assertCorrectImage(image, testImageProvider.getIdentifier(), mediumImageHashCode, false,
                false);
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.SMALL);
        assertCorrectImage(image, testImageProvider.getIdentifier(), smallImageHashCode, false,
                false);
        // remove image type overlay and check that everything is normal again
        imageManagement.unregisterImageType(typeOverlay);
        size = imageManagement.getImageSize(testImageType.getName(), ImageSizeType.LARGE);
        Assert.assertEquals(size.getHeight(), TEST_IMAGE_SIZE_LARGE.getHeight());
        Assert.assertEquals(size.getWidth(), TEST_IMAGE_SIZE_LARGE.getWidth());
        size = imageManagement.getImageSize(testImageType.getName(), ImageSizeType.MEDIUM);
        Assert.assertEquals(size.getHeight(), TEST_IMAGE_SIZE_MEDIUM.getHeight());
        Assert.assertEquals(size.getWidth(), TEST_IMAGE_SIZE_MEDIUM.getWidth());
        size = imageManagement.getImageSize(testImageType.getName(), ImageSizeType.SMALL);
        Assert.assertEquals(size.getHeight(), TEST_IMAGE_SIZE_SMALL.getHeight());
        Assert.assertEquals(size.getWidth(), TEST_IMAGE_SIZE_SMALL.getWidth());
        // just test a control sample
        image = imageManagement.getImage(testImageType.getName(), imageId1, ImageSizeType.MEDIUM);
        assertCorrectImage(image, testImageProvider.getIdentifier(), defaultMediumImageHashCode,
                true, false);
        image = imageManagement.getImage(testImageType.getName(), imageId2, ImageSizeType.LARGE);
        // need to check with mediumImageHashCode as it was created from image scaled to LARGE size
        assertCorrectImage(image, testImageProvider.getIdentifier(), mediumImageHashCode, false,
                false);
    }

    /**
     * Test that a missing image provider leads to the correct exception
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testMissingProviderHandledCorrectly() throws Exception {
        try {
            imageManagement.getImage(testImageType.getName() + "unknown", imageId1,
                    ImageSizeType.LARGE);
            Assert.fail("There should be no provider for the type!");
        } catch (ImageNotFoundException e) {
            // expected
        }
    }

    private void uploadUserImage(User user, byte[] userImage) throws UserNotFoundException,
            AuthorizationException, VirusFoundException, VirusScannerException, IOException {
        Authentication currentAuth = AuthenticationHelper.setAsAuthenticatedUser(user);
        try {
            if (userImage == null) {
                ServiceLocator.findService(UserProfileService.class).removeUserImage(user.getId());
            } else {
                ServiceLocator.findService(UserProfileService.class).storeOrUpdateUserImage(
                        user.getId(), userImage);
            }
        } finally {
            AuthenticationHelper.setAuthentication(currentAuth);
        }
    }
}
