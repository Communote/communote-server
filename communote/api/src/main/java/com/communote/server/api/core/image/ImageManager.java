package com.communote.server.api.core.image;

import java.io.IOException;
import java.util.Map;

import com.communote.common.image.ImageSize;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.user.ImageSizeType;

/**
 * Service for accessing different kinds of images in different sizes (width-height dimension). To
 * support a certain image type like a user profile picture, an {@link ImageTypeDescriptor} can be
 * registered along with an {@link ImageProvider} which is responsible for retrieving the full-size
 * (= unscaled) image.
 * <p>
 * On startup a set of built-in image types and providers are added automatically.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ImageManager {

    /**
     * Inform the image manager that the default image of the given provider changed. The image
     * manager will clear caches and notify the provider about the change. To avoid unnecessary
     * cache invalidations the caller should provide as much details as possible.
     *
     * @param typeName
     *            the type of the image that changed. If there is no provider for the image type,
     *            this call has no effect.
     * @param providerIdentifier
     *            the ID of the {@link ImageProvider} that loaded the changed image. If null, all
     *            providers that are registered for that type are considered.
     */
    public void defaultImageChanged(String typeName, String providerIdentifier);

    /**
     * Get an image
     *
     * @param typeName
     *            Name of the image type
     * @param descriptor
     *            the descriptor with image ID and details about scaling
     * @return The image or the default image, if there is one.
     * @throws AuthorizationException
     *             in case the current user has no access to the image
     * @throws IOException
     *             in case retrieving or scaling the image failed
     * @throws ImageNotFoundException
     *             in case the image type is not known or there is no provider for the image type or
     *             the provider has no image for the descriptor and a default image doesn't exist
     *             neither
     */
    public Image getImage(String typeName, ImageDescriptor descriptor)
            throws AuthorizationException, IOException, ImageNotFoundException;

    /**
     * Get an image
     *
     * @param typeName
     *            the name of the type of the image
     * @param parameters
     *            the parameters as a key to value mapping which can contain the image identifier.
     *            The ImageTypeDescriptor identified by the typeName will be used to extract the
     *            identifier.
     * @param size
     *            the size type constant which will be mapped to the actual size
     * @return the image
     * @throws AuthorizationException
     *             in case the current user has no access to the image
     * @throws IOException
     *             in case retrieving or scaling the image failed
     * @throws ImageNotFoundException
     *             in case the image type is not known or there is no provider for the image type or
     *             the provider has no image for the extracted identifier and a default image
     *             doesn't exist neither
     */
    public Image getImage(String typeName, Map<String, ? extends Object> parameters,
            ImageSizeType size) throws AuthorizationException, IOException, ImageNotFoundException;

    /**
     * Get an image
     *
     * @param typeName
     *            the name of the type of the image
     * @param imageIdentifier
     *            the identifier of the image
     * @param size
     *            the size type constant which will be mapped to the actual size
     * @return the image
     * @throws AuthorizationException
     *             in case the current user has no access to the image
     * @throws IOException
     *             in case retrieving or scaling the image failed
     * @throws ImageNotFoundException
     *             in case the image type is not known or there is no provider for the image type or
     *             the provider has no image for the identifier and a default image doesn't exist
     *             neither
     */
    public Image getImage(String typeName, String imageIdentifier, ImageSizeType size)
            throws AuthorizationException, IOException, ImageNotFoundException;

    /**
     * Map the size type constant to the actual size.
     *
     * @param typeName
     *            the name of the image type
     * @param sizeType
     *            the size type constant to map
     * @return the image size or null if there if no image type descriptor for the type was
     *         registered
     */
    public ImageSize getImageSize(String typeName, ImageSizeType sizeType);

    /**
     * Return a string which represents the version of the image. Whenever an image is modified the
     * version string will usually change. If the image does not exist but the provider has a
     * default image, the version string of that image is returned<br>
     *
     * This result of this method can be used to create a unique URL to image to bypass caches of
     * clients like browsers.
     *
     * @param typeName
     *            Name of the image type.
     * @param imageIdentifier
     *            identifier of the image
     * @return The version string
     * @throws ImageNotFoundException
     *             in case there is no provider for that image type or the image with the identifier
     *             does not exist the provider has no default image
     * @throws AuthorizationException
     *             in case the current user is not allowed to access the image
     */
    public String getImageVersionString(String typeName, String imageIdentifier)
            throws ImageNotFoundException, AuthorizationException;

    /**
     * Inform the image manager that an image was changed. The image manager will clear caches and
     * notify the provider about the change. To avoid unnecessary cache invalidations the caller
     * should provide as much details as possible.
     *
     * @param typeName
     *            the type of the image that changed. If there is no provider for the image type,
     *            this call has no effect.
     * @param providerIdentifier
     *            the ID of the {@link ImageProvider} that loaded the changed image. If null, all
     *            providers that are registered for that type are considered.
     * @param imageIdentifier
     *            the ID of the image that changed. If null, all images of the provider are treated
     *            as having changed.
     */
    public void imageChanged(String typeName, String providerIdentifier, String imageIdentifier);

    /**
     * Register an image provider which can load images for the given image type. There can be
     * several providers for a type.
     *
     * @param imageTypeName
     *            the name of the type which must have been registered before
     * @param provider
     *            the provider to add
     * @throws ImageProviderManagerException
     *             in case there is already a provider with the same identifier
     * @throws ImageTypeNotFoundException
     *             in case the image type does not exist
     */
    public void registerImageProvider(String imageTypeName, ImageProvider provider)
            throws ImageProviderManagerException, ImageTypeNotFoundException;

    /**
     * Register an image type. If there is already a type with the same name the new one will be an
     * overlay for the existing, that is, it will become the active descriptor for that type. The
     * existing descriptor will become the active one as soon as the overlay is unregistered. Such
     * an overlay is for instance useful for changing the supported image sizes.
     *
     * After adding a type, providers for that type can be registered.
     *
     * @param imageType
     *            the descriptor of the type
     * @throws IllegalArgumentException
     *             in case the type has no name
     */
    public void registerImageType(ImageTypeDescriptor imageType);

    /**
     * Remove an image provider which was added for a given image type. If the type or the provider
     * do not exist the call is ignored.
     *
     * @param imageTypeName
     *            the name of the type
     * @param provider
     *            the provider to remove
     * @throws ImageProviderManagerException
     *             in case the provider to remove is a built-in provider
     */
    public void unregisterImageProvider(String imageTypeName, ImageProvider provider)
            throws ImageProviderManagerException;

    /**
     * Remove a previously registered image type. If there are several types with the same name and
     * the type to remove is the active one the next type which was registered before this type will
     * become the active one. <br/>
     * Providers will not be removed by this method, even if there are no more types with the same
     * name. However, accessing images of the type is not possible anymore. <br/>
     * If the type to remove does not exist the call is ignored.
     *
     * @param imageType
     *            the type to remove
     */
    public void unregisterImageType(ImageTypeDescriptor imageType);

}