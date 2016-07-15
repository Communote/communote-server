package com.communote.server.core.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.image.ImageProvider;
import com.communote.server.api.core.image.ImageProviderManagerException;
import com.communote.server.api.core.image.ImageTypeDescriptor;
import com.communote.server.api.core.image.ImageTypeNotFoundException;
import com.communote.server.api.core.image.type.AttachmentImageDescriptor;
import com.communote.server.api.core.image.type.EntityBannerImageDescriptor;
import com.communote.server.api.core.image.type.EntityProfileImageDescriptor;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.core.image.type.AttachmentImageProvider;
import com.communote.server.core.image.type.ClientImageDescriptor;
import com.communote.server.core.image.type.ClientImageProvider;
import com.communote.server.core.image.type.EntityImageManagement.ImageType;
import com.communote.server.core.image.type.EntityImageProvider;
import com.communote.server.core.image.type.UserImageProvider;

/**
 * Manages image types and image providers for these types. Some built-in types and providers are
 * added automatically during initialization.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageProviderManager {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(ImageProviderManager.class);

    // mapping from image type name to the registered image types
    private final Map<String, List<ImageTypeDescriptor>> typeDescriptors = new ConcurrentHashMap<>();
    // mapping from image type name to the registered providers
    private final Map<String, List<ImageProvider>> imageProviders = new ConcurrentHashMap<>();
    // mapping from image type name to the ID of the built-in provider
    private final Map<String, String> builtInProviders = new HashMap<>();

    /**
     * Constructor
     */
    public ImageProviderManager() {
        try {
            // register built-in types and providers
            registerTypeDescriptor(new UserImageDescriptor());
            ImageProvider provider = new UserImageProvider(
                    "/com/communote/images/default_userimage_large.jpg");
            registerProvider(UserImageDescriptor.IMAGE_TYPE_NAME, provider);
            builtInProviders.put(UserImageDescriptor.IMAGE_TYPE_NAME, provider.getIdentifier());

            registerTypeDescriptor(new ClientImageDescriptor());
            provider = new ClientImageProvider("/com/communote/images/default_client_logo.png");
            registerProvider(ClientImageDescriptor.IMAGE_TYPE_NAME, provider);
            builtInProviders.put(ClientImageDescriptor.IMAGE_TYPE_NAME, provider.getIdentifier());

            registerTypeDescriptor(new AttachmentImageDescriptor());
            provider = new AttachmentImageProvider(
                    "/com/communote/images/default_attachment_preview_image.png");
            registerProvider(AttachmentImageDescriptor.IMAGE_TYPE_NAME, provider);
            builtInProviders.put(AttachmentImageDescriptor.IMAGE_TYPE_NAME,
                    provider.getIdentifier());

            registerTypeDescriptor(new EntityBannerImageDescriptor());
            provider = new EntityImageProvider("/com/communote/images/default_entity_banner.png",
                    ImageType.BANNER);
            registerProvider(EntityBannerImageDescriptor.IMAGE_TYPE_NAME, provider);
            builtInProviders.put(EntityBannerImageDescriptor.IMAGE_TYPE_NAME,
                    provider.getIdentifier());

            registerTypeDescriptor(new EntityProfileImageDescriptor());
            provider = new EntityImageProvider("/com/communote/images/default_entity_profile.png",
                    ImageType.PROFILE);
            registerProvider(EntityProfileImageDescriptor.IMAGE_TYPE_NAME, provider);
            builtInProviders.put(EntityProfileImageDescriptor.IMAGE_TYPE_NAME,
                    provider.getIdentifier());
        } catch (ImageProviderManagerException | ImageTypeNotFoundException e) {
            throw new RuntimeException(
                    "Unexpected exception registering built-in image types and providers", e);
        }
    }

    /**
     * Return a built-in provider for the given image type.
     *
     * @param imageTypeName
     *            the name of the image type
     * @return the provider or null if there is no built-in provider for that type
     */
    public ImageProvider getBuiltInProvider(String imageTypeName) {
        String builtInProviderId = builtInProviders.get(imageTypeName);
        if (builtInProviderId != null) {
            List<ImageProvider> existingProviders = getProviders(imageTypeName);
            if (existingProviders != null) {
                for (int i = existingProviders.size() - 1; i >= 0; i--) {
                    ImageProvider provider = existingProviders.get(i);
                    if (builtInProviderId.equals(provider.getIdentifier())) {
                        return provider;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return the image providers that can handle the given image type
     *
     * @param imageTypeName
     *            the name of the image type
     * @return the providers or null if the type does not exist
     */
    public List<ImageProvider> getProviders(String imageTypeName) {
        List<ImageProvider> existingProviders = imageProviders.get(imageTypeName);
        // providers are not removed when last type is removed so check that type still exists
        if (existingProviders != null && typeDescriptors.containsKey(imageTypeName)) {
            return existingProviders;
        }
        return null;
    }

    public ImageTypeDescriptor getTypeDescriptor(String imageTypeName) {
        List<ImageTypeDescriptor> exitingDescriptors = typeDescriptors.get(imageTypeName);
        if (exitingDescriptors != null) {
            return exitingDescriptors.get(0);
        }
        return null;
    }

    /**
     * Register a provider which can load images for the given image type. There can be several
     * providers for a type.
     *
     * @param imageTypeName
     *            the name of the type which must have been registered before.
     * @param provider
     *            the provider to add
     * @throws ImageProviderManagerException
     *             in case there is already a provider with the same identifier
     * @throws ImageTypeNotFoundException
     *             in case the image type does not exist
     */
    public synchronized void registerProvider(String imageTypeName, ImageProvider provider)
            throws ImageProviderManagerException, ImageTypeNotFoundException {
        if (!typeDescriptors.containsKey(imageTypeName)) {
            throw new ImageTypeNotFoundException("The provider " + provider.getIdentifier()
                    + " cannot be added because there is no image type with name " + imageTypeName);
        }
        List<ImageProvider> providers = new ArrayList<>();
        providers.add(provider);
        List<ImageProvider> existingProviders = imageProviders.get(imageTypeName);
        if (existingProviders != null) {
            for (ImageProvider existingProvider : existingProviders) {
                if (existingProvider.getIdentifier().equals(provider.getIdentifier())) {
                    throw new ImageProviderManagerException("The provider "
                            + provider.getIdentifier()
                            + " cannot be added because there is already a provider with this id");
                }
                providers.add(existingProvider);
            }
        }
        LOG.debug("Added image provider {} for type {}", provider.getIdentifier(), imageTypeName);
        imageProviders.put(imageTypeName, Collections.unmodifiableList(providers));
    }

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
     * @return true if there was already a descriptor with the same name and the new type was added
     *         as an overlay
     * @throws IllegalArgumentException
     *             in case the type has no name
     */
    public synchronized boolean registerTypeDescriptor(ImageTypeDescriptor imageType)
            throws IllegalArgumentException {
        if (StringUtils.isBlank(imageType.getName())) {
            throw new IllegalArgumentException(
                    "The name of the image type must not be null or blank");
        }
        boolean overlayAdded = false;
        ArrayList<ImageTypeDescriptor> types = new ArrayList<>();
        types.add(imageType);
        if (typeDescriptors.containsKey(imageType.getName())) {
            List<ImageTypeDescriptor> existingTypes = typeDescriptors.get(imageType.getName());
            LOG.debug("Added overlay for image type with name {}", imageType.getName());
            types.addAll(existingTypes);
            overlayAdded = true;
        } else {
            LOG.debug("Added image type with name {}", imageType.getName());
        }
        typeDescriptors.put(imageType.getName(), Collections.unmodifiableList(types));
        return overlayAdded;
    }

    /**
     * Remove a provider that was added for a given image type. If the type or the provider do not
     * exist the call is ignored.
     *
     * @param imageTypeName
     *            the name of the type
     * @param provider
     *            the provider to remove
     * @throws ImageProviderManagerException
     *             in case the provider to remove is built-in provider
     */
    public synchronized void unregisterProvider(String imageTypeName, ImageProvider provider)
            throws ImageProviderManagerException {
        List<ImageProvider> existingProviders = imageProviders.get(imageTypeName);
        if (existingProviders == null) {
            LOG.warn("There are no providers for the image type {}", imageTypeName);
            return;
        }
        if (!existingProviders.contains(provider)) {
            LOG.warn("There is no matching provider for the image type {}", imageTypeName);
            return;
        }
        if (provider.getIdentifier().equals(builtInProviders.get(imageTypeName))) {
            throw new ImageProviderManagerException("The built-in provider "
                    + provider.getIdentifier() + " cannot be removed");
        }
        if (existingProviders.size() > 1) {
            List<ImageProvider> providers = new ArrayList<>();
            for (ImageProvider existingProvider : existingProviders) {
                if (!existingProvider.equals(provider)) {
                    providers.add(existingProvider);
                }
            }
            imageProviders.put(imageTypeName, Collections.unmodifiableList(providers));
        } else {
            imageProviders.remove(imageTypeName);
        }
        LOG.debug("Removed image provider {} for type {}", provider.getIdentifier(), imageTypeName);
    }

    /**
     * Remove a previously registered image type. If there are several types with the same name and
     * the type to remove is the active one the next type which was registered before this type will
     * become the active one. <br>
     * Providers will not be removed by this method, even if there are no more types with the same
     * name. However, a call to {@link #getProviders(String)} with the name of the removed type will
     * return null. <br>
     * If the type to remove does not exist the call is ignored.
     *
     * @param imageType
     *            the type to remove
     * @return true if an active overlay was removed, false otherwise
     */
    public synchronized boolean unregisterTypeDescriptor(ImageTypeDescriptor imageType) {
        List<ImageTypeDescriptor> existingTypes = typeDescriptors.get(imageType.getName());
        boolean removedActiveOverlay = false;
        if (existingTypes != null && existingTypes.contains(imageType)) {
            if (existingTypes.size() > 1) {
                removedActiveOverlay = existingTypes.get(0).equals(imageType);
                ArrayList<ImageTypeDescriptor> types = new ArrayList<>();
                for (ImageTypeDescriptor type : existingTypes) {
                    if (!type.equals(imageType)) {
                        types.add(type);
                    }
                }
                typeDescriptors.put(imageType.getName(), Collections.unmodifiableList(types));
                LOG.debug("Removed overlay for image type with name {}", imageType.getName());
            } else {
                typeDescriptors.remove(imageType.getName());
                LOG.debug("Removed image type with name {}", imageType.getName());
            }
        }
        return removedActiveOverlay;
    }
}
