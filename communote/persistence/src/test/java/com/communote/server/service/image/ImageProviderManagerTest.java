package com.communote.server.service.image;

import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.image.ImageSize;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.ImageProvider;
import com.communote.server.api.core.image.ImageProviderManagerException;
import com.communote.server.api.core.image.ImageTypeDescriptor;
import com.communote.server.api.core.image.ImageTypeNotFoundException;
import com.communote.server.api.core.image.type.AttachmentImageDescriptor;
import com.communote.server.api.core.image.type.DefaultImageTypeDescriptor;
import com.communote.server.api.core.image.type.EntityBannerImageDescriptor;
import com.communote.server.api.core.image.type.EntityProfileImageDescriptor;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.image.ImageProviderManager;
import com.communote.server.core.image.type.AttachmentImageProvider;
import com.communote.server.core.image.type.ClientImageDescriptor;
import com.communote.server.core.image.type.ClientImageProvider;
import com.communote.server.core.image.type.EntityImageProvider;
import com.communote.server.core.image.type.UserImageProvider;
import com.communote.server.model.user.ImageSizeType;

/**
 * Tests for the {@link ImageProviderManager}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ImageProviderManagerTest {

    private static class TestImageProvider extends ImageProvider {

        public TestImageProvider(String identifier) {
            super(identifier, null);
        }

        @Override
        public boolean canLoad(String imageIdentifier) {
            return false;
        }

        @Override
        public String getVersionString(String imageIdentifier) throws AuthorizationException,
                ImageNotFoundException {
            throw new ImageNotFoundException();
        }

        @Override
        public boolean isAuthorized(String imageIdentifier) {
            return true;
        }

        @Override
        public boolean isExternalProvider() {
            return false;
        }

        @Override
        public Image loadImage(String imageIdentifier) throws ImageNotFoundException,
                AuthorizationException {
            throw new ImageNotFoundException();
        }

    }

    private static class TestImageTypeDescriptor extends DefaultImageTypeDescriptor {
        private final ImageSize size;
        private final String name;

        public TestImageTypeDescriptor(String name, ImageSize size) {
            this.size = size;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ImageSize getSizeForImageSizeType(ImageSizeType sizeType) {
            return size;
        }

    }

    private ImageProviderManager providerManager;

    private void assertBuiltInProvider(String typeName, String providerId) {
        List<ImageProvider> providers = providerManager.getProviders(typeName);
        Assert.assertNotNull(providers);
        Assert.assertEquals(providers.size(), 1);
        Assert.assertEquals(providers.get(0).getIdentifier(), providerId);
        Assert.assertEquals(providerManager.getBuiltInProvider(typeName), providers.get(0));
    }

    private void assertImageTypeExists(String typeName) {
        ImageTypeDescriptor descriptor = providerManager.getTypeDescriptor(typeName);
        Assert.assertNotNull(descriptor);
        Assert.assertEquals(descriptor.getName(), typeName);
    }

    /**
     * preparations
     */
    @BeforeClass
    public void setup() {
        providerManager = new ImageProviderManager();
    }

    /**
     * Test that adding a provider for an image type which was not registered fails.
     * 
     * @throws ImageProviderManagerException
     *             in case the test failed
     */
    @Test(dependsOnMethods = { "testBuiltInTypesAndProvidersExist" })
    public void testAddProviderForNotExistingType() throws ImageProviderManagerException {
        String typeName = UUID.randomUUID().toString();
        Assert.assertNull(providerManager.getTypeDescriptor(typeName));
        try {
            providerManager.registerProvider(typeName, new UserImageProvider(null));
            Assert.fail("Adding provider for not existing provider should fail");
        } catch (ImageTypeNotFoundException e) {
            // expected
        }
    }

    /**
     * Test that adding a provider with an ID which already exists for a type fails.
     * 
     * @throws ImageTypeNotFoundException
     *             in case the test failed
     */
    @Test(dependsOnMethods = { "testBuiltInTypesAndProvidersExist" })
    public void testAddProviderWithIdOfExistingProvider() throws ImageTypeNotFoundException {
        ImageProvider provider = new TestImageProvider(UserImageProvider.PROVIDER_IDENTIFIER);
        try {
            providerManager.registerProvider(UserImageDescriptor.IMAGE_TYPE_NAME, provider);
            Assert.fail("Adding provider with ID of already existing provider for type should fail");
        } catch (ImageProviderManagerException e) {
            // expected
        }
    }

    /**
     * Test for adding and removing providers for a buit-in image type.
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = { "testBuiltInTypesAndProvidersExist" })
    public void testAddRemoveProviderForBuiltInImageType() throws Exception {
        String providerId1 = UUID.randomUUID().toString();
        ImageProvider provider1 = new TestImageProvider(providerId1);
        providerManager.registerProvider(UserImageDescriptor.IMAGE_TYPE_NAME, provider1);
        List<ImageProvider> providers = providerManager
                .getProviders(UserImageDescriptor.IMAGE_TYPE_NAME);
        Assert.assertEquals(providers.size(), 2);
        Assert.assertEquals(providers.get(0).getIdentifier(), providerId1);
        Assert.assertEquals(providers.get(1).getIdentifier(), UserImageProvider.PROVIDER_IDENTIFIER);

        String providerId2 = UUID.randomUUID().toString();
        ImageProvider provider2 = new TestImageProvider(providerId2);
        providerManager.registerProvider(UserImageDescriptor.IMAGE_TYPE_NAME, provider2);
        providers = providerManager.getProviders(UserImageDescriptor.IMAGE_TYPE_NAME);
        Assert.assertEquals(providers.size(), 3);
        Assert.assertEquals(providers.get(0).getIdentifier(), providerId2);
        Assert.assertEquals(providers.get(1).getIdentifier(), providerId1);
        Assert.assertEquals(providers.get(2).getIdentifier(), UserImageProvider.PROVIDER_IDENTIFIER);

        providerManager.unregisterProvider(UserImageDescriptor.IMAGE_TYPE_NAME, provider1);
        providers = providerManager.getProviders(UserImageDescriptor.IMAGE_TYPE_NAME);
        Assert.assertEquals(providers.size(), 2);
        Assert.assertEquals(providers.get(0).getIdentifier(), providerId2);
        Assert.assertEquals(providers.get(1).getIdentifier(), UserImageProvider.PROVIDER_IDENTIFIER);

        providerManager.unregisterProvider(UserImageDescriptor.IMAGE_TYPE_NAME, provider2);
        providers = providerManager.getProviders(UserImageDescriptor.IMAGE_TYPE_NAME);
        Assert.assertEquals(providers.size(), 1);
        Assert.assertEquals(providers.get(0).getIdentifier(), UserImageProvider.PROVIDER_IDENTIFIER);
    }

    /**
     * Test adding and removing new image type and providers for hat type.
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = { "testBuiltInTypesAndProvidersExist" })
    public void testAddRemoveProviderForNewImageType() throws Exception {
        String imageTypeName = "testImages";

        ImageTypeDescriptor imageType = new TestImageTypeDescriptor(imageTypeName, new ImageSize(
                10, 10));

        providerManager.registerTypeDescriptor(imageType);
        assertImageTypeExists(imageTypeName);
        Assert.assertNull(providerManager.getProviders(imageTypeName));
        Assert.assertNull(providerManager.getBuiltInProvider(imageTypeName));

        String providerId1 = UUID.randomUUID().toString();
        ImageProvider provider1 = new TestImageProvider(providerId1);
        providerManager.registerProvider(imageTypeName, provider1);
        List<ImageProvider> providers = providerManager.getProviders(imageTypeName);
        Assert.assertEquals(providers.size(), 1);
        Assert.assertEquals(providers.get(0).getIdentifier(), providerId1);

        String providerId2 = UUID.randomUUID().toString();
        ImageProvider provider2 = new TestImageProvider(providerId2);
        providerManager.registerProvider(imageTypeName, provider2);
        providers = providerManager.getProviders(imageTypeName);
        Assert.assertEquals(providers.size(), 2);
        Assert.assertEquals(providers.get(0).getIdentifier(), providerId2);
        Assert.assertEquals(providers.get(1).getIdentifier(), providerId1);

        // remove type, providers must not be accessible anymore
        providerManager.unregisterTypeDescriptor(imageType);
        Assert.assertNull(providerManager.getTypeDescriptor(imageTypeName));
        Assert.assertNull(providerManager.getProviders(imageTypeName));
        // removing non-existing type should have no effect
        providerManager.unregisterTypeDescriptor(imageType);

        // removing should still work. After re-adding type, only one provider must be left
        providerManager.unregisterProvider(imageTypeName, provider1);
        providerManager.registerTypeDescriptor(imageType);
        Assert.assertNotNull(providerManager.getTypeDescriptor(imageTypeName));
        providers = providerManager.getProviders(imageTypeName);
        Assert.assertEquals(providers.size(), 1);
        Assert.assertEquals(providers.get(0).getIdentifier(), providerId2);

        // remove remaining
        providerManager.unregisterProvider(imageTypeName, provider2);
        Assert.assertNotNull(providerManager.getTypeDescriptor(imageTypeName));
        Assert.assertNull(providerManager.getProviders(imageTypeName));

        // remove non-existing providers should have no effect
        providerManager.unregisterProvider(imageTypeName, provider2);
        providerManager.unregisterProvider(imageTypeName + "2", provider2);

        providerManager.unregisterTypeDescriptor(imageType);
        Assert.assertNull(providerManager.getTypeDescriptor(imageTypeName));
    }

    /**
     * Test that the built-in types and providers are added
     */
    @Test
    public void testBuiltInTypesAndProvidersExist() {
        assertImageTypeExists(UserImageDescriptor.IMAGE_TYPE_NAME);
        assertImageTypeExists(ClientImageDescriptor.IMAGE_TYPE_NAME);
        assertImageTypeExists(AttachmentImageDescriptor.IMAGE_TYPE_NAME);
        assertImageTypeExists(EntityBannerImageDescriptor.IMAGE_TYPE_NAME);
        assertImageTypeExists(EntityProfileImageDescriptor.IMAGE_TYPE_NAME);

        assertBuiltInProvider(UserImageDescriptor.IMAGE_TYPE_NAME,
                UserImageProvider.PROVIDER_IDENTIFIER);
        assertBuiltInProvider(ClientImageDescriptor.IMAGE_TYPE_NAME,
                ClientImageProvider.PROVIDER_IDENTIFIER);
        assertBuiltInProvider(AttachmentImageDescriptor.IMAGE_TYPE_NAME,
                AttachmentImageProvider.PROVIDER_IDENTIFIER);
        assertBuiltInProvider(EntityBannerImageDescriptor.IMAGE_TYPE_NAME,
                EntityImageProvider.PROVIDER_IDENTIFIER);
        assertBuiltInProvider(EntityProfileImageDescriptor.IMAGE_TYPE_NAME,
                EntityImageProvider.PROVIDER_IDENTIFIER);
    }

    /**
     * Test the overlaying of image types to change the mapping of ImageSizeType constants to actual
     * sizes
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = { "testBuiltInTypesAndProvidersExist" })
    public void testImageTypeOverlay() throws Exception {
        // test overlay for built-in type
        ImageSize userImageSize = providerManager.getTypeDescriptor(
                UserImageDescriptor.IMAGE_TYPE_NAME).getSizeForImageSizeType(ImageSizeType.LARGE);
        ImageSize overlaySize = new ImageSize(userImageSize.getWidth() + 1,
                userImageSize.getHeight() + 1);
        ImageTypeDescriptor overlayType = new TestImageTypeDescriptor(
                UserImageDescriptor.IMAGE_TYPE_NAME, overlaySize);
        providerManager.registerTypeDescriptor(overlayType);
        ImageSize curSize = providerManager.getTypeDescriptor(UserImageDescriptor.IMAGE_TYPE_NAME)
                .getSizeForImageSizeType(ImageSizeType.LARGE);
        Assert.assertEquals(curSize.getHeight(), overlaySize.getHeight());
        Assert.assertEquals(curSize.getWidth(), overlaySize.getWidth());
        assertBuiltInProvider(UserImageDescriptor.IMAGE_TYPE_NAME,
                UserImageProvider.PROVIDER_IDENTIFIER);
        // remove overlay
        providerManager.unregisterTypeDescriptor(overlayType);
        curSize = providerManager.getTypeDescriptor(UserImageDescriptor.IMAGE_TYPE_NAME)
                .getSizeForImageSizeType(ImageSizeType.LARGE);
        Assert.assertEquals(curSize.getHeight(), userImageSize.getHeight());
        Assert.assertEquals(curSize.getWidth(), userImageSize.getWidth());
        assertBuiltInProvider(UserImageDescriptor.IMAGE_TYPE_NAME,
                UserImageProvider.PROVIDER_IDENTIFIER);

        // test for new type
        String imageTypeName = "overlayTestImageType";
        ImageTypeDescriptor imageType = new TestImageTypeDescriptor(imageTypeName, new ImageSize(
                10, 10));
        providerManager.registerTypeDescriptor(imageType);
        assertImageTypeExists(imageTypeName);
        String providerId = UUID.randomUUID().toString();
        ImageProvider provider = new TestImageProvider(providerId);
        providerManager.registerProvider(imageTypeName, provider);

        // add overlay, providers must be untouched
        overlayType = new TestImageTypeDescriptor(imageTypeName, new ImageSize(100, 100));
        providerManager.registerTypeDescriptor(overlayType);
        assertImageTypeExists(imageTypeName);
        curSize = providerManager.getTypeDescriptor(imageTypeName).getSizeForImageSizeType(
                ImageSizeType.LARGE);
        Assert.assertEquals(curSize.getHeight(), 100);
        Assert.assertEquals(curSize.getWidth(), 100);
        List<ImageProvider> providers = providerManager.getProviders(imageTypeName);
        Assert.assertEquals(providers.size(), 1);
        Assert.assertEquals(providers.get(0).getIdentifier(), providerId);

        // remove overlay
        providerManager.unregisterTypeDescriptor(overlayType);
        assertImageTypeExists(imageTypeName);
        curSize = providerManager.getTypeDescriptor(imageTypeName).getSizeForImageSizeType(
                ImageSizeType.LARGE);
        Assert.assertEquals(curSize.getHeight(), 10);
        Assert.assertEquals(curSize.getWidth(), 10);
        providers = providerManager.getProviders(imageTypeName);
        Assert.assertEquals(providers.size(), 1);
        Assert.assertEquals(providers.get(0).getIdentifier(), providerId);
    }

    /**
     * Test that removing a built-in provider fails.
     */
    @Test(dependsOnMethods = { "testBuiltInTypesAndProvidersExist" })
    public void testRemoveBuiltInProvider() {
        try {
            providerManager.unregisterProvider(UserImageDescriptor.IMAGE_TYPE_NAME,
                    providerManager.getBuiltInProvider(UserImageDescriptor.IMAGE_TYPE_NAME));
            Assert.fail("Removing built-in provider should fail");
        } catch (ImageProviderManagerException e) {
            // expected
        }
        assertBuiltInProvider(UserImageDescriptor.IMAGE_TYPE_NAME,
                UserImageProvider.PROVIDER_IDENTIFIER);
    }
}
