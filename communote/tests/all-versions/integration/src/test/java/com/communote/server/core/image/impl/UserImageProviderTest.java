package com.communote.server.core.image.impl;

import java.awt.Color;
import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.core.image.type.UserImageProvider;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * Test for {@link UserImageProvider}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserImageProviderTest extends CommunoteIntegrationTest {

    /**
     * Test loading an image with the {@link UserImageProvider}
     * 
     * 
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testLoadImage() throws Exception {
        User user = TestUtils.createRandomUser(false);
        String imageIdentifier = user.getId().toString();
        // should not get image or version string
        UserImageProvider provider = new UserImageProvider(null);
        try {
            provider.getVersionString(imageIdentifier);
            Assert.fail("There should be no version string for a user who did not upload an image");
        } catch (ImageNotFoundException e) {
            // expected
        }
        try {
            provider.loadImage(imageIdentifier);
            Assert.fail("There should be no image for a user who did not upload an image");
        } catch (ImageNotFoundException e) {
            // expected
        }
        byte[] userImage = TestUtils.createPngImage(80, 80, Color.RED);
        UserProfileManagement userProfileManagement = ServiceLocator
                .findService(UserProfileManagement.class);
        Authentication currentAuth = AuthenticationHelper.setAsAuthenticatedUser(user);
        try {
            userProfileManagement.updateImage(user.getId(), userImage);
        } finally {
            AuthenticationHelper.setAuthentication(currentAuth);
        }
        Image loadedUserImage = provider.loadImage(imageIdentifier);
        Assert.assertNotNull(loadedUserImage);
        Assert.assertTrue(Arrays.equals(loadedUserImage.getBytes(), userImage));
        Assert.assertEquals(userProfileManagement.findUserProfileByUserId(user.getId())
                .getLastPhotoModificationDate(),
                loadedUserImage.getLastModificationDate());
        Assert.assertNotNull(provider.getVersionString(imageIdentifier));
        // remove image again and check version string
        currentAuth = AuthenticationHelper.setAsAuthenticatedUser(user);
        try {
            userProfileManagement.removeUserImage(user.getId());
        } finally {
            AuthenticationHelper.setAuthentication(currentAuth);
        }
        try {
            provider.getVersionString(imageIdentifier);
            Assert.fail("There should be no version string for a user who did not upload an image");
        } catch (ImageNotFoundException e) {
            // expected
        }
        try {
            provider.loadImage(imageIdentifier);
            Assert.fail("There should be no image for a user who did not upload an image");
        } catch (ImageNotFoundException e) {
            // expected
        }
    }
}
