package com.communote.server.core.image;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.image.ImageHelper;
import com.communote.server.api.core.image.ByteArrayImage;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.ImageProvider;
import com.communote.server.api.core.image.ImageTemporarilyNotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.image.exceptions.ExternalImageProviderServiceException;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Image provider which loads images from an external system using HTTP GET requests.
 * 
 * @param <C>
 *            the type of the configuration class
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class HttpExternalImageProvider<C> extends ImageProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpExternalImageProvider.class);

    private final int DEFAULT_HTTP_CLIENT_TIMEOUT = 30000;
    private CloseableHttpClient httpClient;
    private final Set<Integer> notFoundStatusCodes;
    private final long disablingDuration;
    // mapping from client-ID to disable timestamp
    private final Map<String, Long> disabledUntilTimestamp;

    private boolean provideDefaultImageIfDisabled = false;

    /**
     * Create a new external image provider
     * 
     * @param identifier
     *            The identifier of the provider. The identifier has to be unique among all
     *            providers that are registered for an image type.
     * @param pathToDefaultImage
     *            If the path starts with file: it is interpreted as a file URI otherwise it is
     *            interpreted as the name of a resource containing the default image. This resource
     *            will be loaded with the class loader of this class. If null, there will be no
     *            default image.
     * @param disablingDuration
     *            the amount of milliseconds to disable the image provider temporarily in certain
     *            situations e.g. if the external server is unreachable. If 0 or less the provider
     *            is never disabled.
     */
    public HttpExternalImageProvider(String identifier, String pathToDefaultImage,
            long disablingDuration) {
        super(identifier, pathToDefaultImage);
        this.notFoundStatusCodes = new HashSet<>();
        this.notFoundStatusCodes.add(HttpStatus.SC_NOT_FOUND);
        this.notFoundStatusCodes.add(HttpStatus.SC_MOVED_TEMPORARILY);
        this.notFoundStatusCodes.add(HttpStatus.SC_MOVED_PERMANENTLY);
        this.disablingDuration = disablingDuration;
        this.disabledUntilTimestamp = new HashMap<>();
    }

    /**
     * Test that the image provider was not disabled. This method is called from loadImage and
     * implementations should also invoke it from other methods that return image data or
     * information (default image, version string).
     * 
     * @param imageIdentifer
     *            the Id of the image that should be loaded. Only needed to create a useful
     *            exception.
     * @param configuration
     *            the current configuration of the image provider, can be null. Default
     *            implementation ignores this parameter.
     * @throws ImageTemporarilyNotFoundException
     *             in case the provider is disabled and the provided image can thus not be loaded
     */
    protected void assertNotDisabled(String imageIdentifier, C configuration)
            throws ImageNotFoundException {
        if (isDisabled(configuration)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Provider {} currently disabled until {}", getIdentifier(),
                        getDisabledUntilTimestamp());
            }
            throw new ImageTemporarilyNotFoundException("Cannot load immage " + imageIdentifier
                    + " because provider is disabled");
        }
    }

    /**
     * Cleanup held resources. Should be called when the provider is unregistered.
     */
    public void cleanup() {
        this.setHttpClient(null, true);
    }

    /**
     * Temporarily disable the image provider
     * 
     * @param reason
     *            The reason that caused the disabling of the provider
     * @param configuration
     *            the current configuration of the image provider, can be null. Default
     *            implementation ignores this parameter.
     */
    protected void disableTemporarily(String reason, C configuration) {
        if (disablingDuration > 0) {
            LOGGER.warn("Image provider {} temporarily disabled because of: {}", getIdentifier(),
                    reason);
            String clientId = ClientHelper.getCurrentClientId();
            disabledUntilTimestamp.put(clientId, (System.currentTimeMillis() + disablingDuration));
        }
    }

    /**
     * Re-enable the provider if it is currently temporarily disabled.
     */
    protected void enable() {
        String clientId = ClientHelper.getCurrentClientId();
        if (LOGGER.isDebugEnabled()) {
            if (this.disabledUntilTimestamp.containsKey(clientId)) {
                LOGGER.debug("Enabling temporarily disabled provider {}", getIdentifier());
            }
        }
        this.disabledUntilTimestamp.remove(clientId);
    }

    /**
     * Extract the image from the response entity. The caller has to take care of closing the
     * response or ensuring that the entity is consumed.
     * 
     * @param entity
     *            the entity to handle
     * @param imageLastModified
     *            the date of the last modification of the image contained in the entity. This could
     *            for instance be the value of the Last-Modified header.
     * @return the image
     * @throws ExternalImageProviderServiceException
     *             in case {@link #validateContentType(String)} threw this exception
     * @throws ImageNotFoundException
     *             in case the entity is not a valid image
     */
    protected ByteArrayImage extractImageFromEntity(HttpEntity entity,
            Date imageLastModified) throws ExternalImageProviderServiceException,
            ImageNotFoundException {
        if (entity == null) {
            throw new ImageNotFoundException("No image data contained in response");
        }
        Header contentType = entity.getContentType();
        validateContentType(contentType == null ? null : contentType.getValue());
        try {
            byte[] imageData = EntityUtils.toByteArray(entity);
            if (imageData != null) {
                // TODO virus scanning?
                // check for valid image
                String mimeType = ImageHelper.getMimeType(imageData);
                if (mimeType == null) {
                    throw new ImageNotFoundException(
                            "The response was not a valid or supported image");
                }
                return new ByteArrayImage(imageData, mimeType, imageLastModified,
                        getIdentifier(),
                        false, true);
            }
        } catch (IllegalArgumentException e) {
            // thrown by toByteArray if entity is too large
            throw new ImageNotFoundException("Response data is invalid", e);
        } catch (IOException e) {
            throw new ImageNotFoundException("Reading image data failed", e);
        }

        throw new ImageNotFoundException("The response was not a valid image");
    }

    /**
     * @return the current configuration of the external image provider. During loadImage the same
     *         configuration object is passed to all called methods.
     */
    protected abstract C getConfiguration();

    /**
     * @return timeout in milliseconds to wait for a connection from the connection manager of the
     *         HttpClient. Can be 0 for an infinite timeout. Defaults to 30s.
     */
    protected int getConnectionManagerTimeout() {
        return DEFAULT_HTTP_CLIENT_TIMEOUT;
    }

    /**
     * @return timeout in milliseconds to wait until a connection to the remote host must have been
     *         established. Can be 0 for an infinite timeout. Defaults to 30s.
     */
    protected int getConnectionTimeout() {
        return DEFAULT_HTTP_CLIENT_TIMEOUT;
    }

    @Override
    public String getDefaultImageVersionString() throws ImageNotFoundException {
        if (!provideDefaultImageIfDisabled) {
            assertNotDisabled("default-image", getConfiguration());
        }
        return super.getDefaultImageVersionString();
    }

    /**
     * Prepare and return a request configuration that is used as default for all requests started
     * by the shared HttpClient instance. The default implementation will set the socket, connection
     * and connection manager timeouts to the values returned by {@link #getSocketTimeout()},
     * {@link #getConnectionTimeout()} and {@link #getConnectionManagerTimeout()}.
     * 
     * @return the default request configuration
     */
    protected RequestConfig getDefaultRequestConfig() {
        return RequestConfig.custom().setConnectTimeout(getConnectionTimeout())
                .setSocketTimeout(getSocketTimeout())
                .setConnectionRequestTimeout(getConnectionManagerTimeout()).build();
    }

    /**
     * @return the epoch timestamp in milliseconds until the provider is currently disabled, or null
     *         if not disabled
     */
    protected Long getDisabledUntilTimestamp() {
        String clientId = ClientHelper.getCurrentClientId();
        return disabledUntilTimestamp.get(clientId);
    }

    /**
     * Get the HTTP client to be used for all requests. If there is no current HTTP client a new one
     * will be created and prepared with a call to {@link #prepareHttpClient(HttpClientBuilder)}.
     * 
     * @return the current HTTP client
     */
    protected CloseableHttpClient getHttpClient() {
        if (this.httpClient == null) {
            this.setHttpClient(prepareHttpClient(HttpClients.custom()), false);
        }
        return this.httpClient;
    }

    /**
     * Get the URL under which the image with the given identifier can be downloaded
     * 
     * @param imageIdentifier
     *            the ID of the image to download
     * @param configuration
     *            the configuration of the image provider, can be null
     * @return the URL to the image. If null is returned the load method will throw an
     *         ImageNotFoundException
     * @throws ImageNotFoundException
     *             can be thrown to indicate that image does not exist image
     */
    protected abstract String getImageURL(String imageIdentifier, C configuration)
            throws ImageNotFoundException;

    /**
     * Get last modified header value as date, will fall back to current time if header is not set
     * 
     * @param response
     *            the response of the image request
     * @return the last modification date
     */
    protected Date getLastModifiedHeaderValue(CloseableHttpResponse response) {
        Header lastModifiedHeader = response.getLastHeader(HttpHeaders.LAST_MODIFIED);
        Date lastModified = null;
        if (lastModifiedHeader != null && lastModifiedHeader.getValue() != null) {
            lastModified = DateUtils.parseDate(lastModifiedHeader.getValue());
        }
        if (lastModified == null) {
            // take current time rounded to seconds because Last-Modified has only second precision
            long now = (System.currentTimeMillis() / 1000) * 1000;
            lastModified = new Date(now);
        }
        return lastModified;
    }

    /**
     * @return the status codes which should not lead to disabling the image provider temporarily.
     *         The code 200 does not need to be in the result.
     */
    protected Set<Integer> getNotDisablingStatusCodes() {
        return notFoundStatusCodes;
    }

    /**
     * Get request context to be used when downloading the given image. Implementations could use
     * this method to provide authentication details. The default implementation returns null.
     * 
     * @param imageUrl
     *            the URL pointing to the image
     * @param imageIdentifier
     *            the ID of the image to get
     * @param configuration
     *            the configuration of the image provider, can be null
     * @return the request context
     */
    protected HttpContext getRequestContext(URI imageUrl, String imageIdentifier, C configuration) {
        return null;
    }

    /**
     * Get the HTTP request headers to add when downloading the given image. The default
     * implementation returns null.
     * 
     * @param imageUrl
     *            the URL pointing to the image
     * @param imageIdentifier
     *            the ID of the image to get
     * @param configuration
     *            the configuration of the image provider, can be null
     * @return the headers
     */
    protected List<Header> getRequestHeaders(URI imageUrl, String imageIdentifier, C configuration) {
        return null;
    }

    /**
     * @return timeout in milliseconds to wait for data after a connection was established. Can be 0
     *         for an infinite timeout. Defaults to 30s.
     */
    protected int getSocketTimeout() {
        return DEFAULT_HTTP_CLIENT_TIMEOUT;
    }

    @Override
    public boolean hasDefaultImage(String imageIdentifier) {
        if (provideDefaultImageIfDisabled || !isDisabled(getConfiguration())) {
            return super.hasDefaultImage(imageIdentifier);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Provider {} currently disabled until {}", getIdentifier(),
                    getDisabledUntilTimestamp());
        }
        return false;
    }

    /**
     * Invoked when the image URL was null or the image could not be downloaded and the cause was
     * not one that would disable the provider. Default implementation does nothing but subclasses
     * could use this method to update or reset the version string of the image if necessary.
     * 
     * @param imageIdentifier
     *            the ID of the image that failed
     */
    protected void imageNotDownloaded(String imageIdentifier) {
    }

    /**
     * Invoked by loadImage after successfully parsing and extracting the image. Default
     * implementation does nothing but subclasses could use this method to update the version string
     * of the image if necessary.
     * 
     * @param imageIdentifier
     *            the ID of the image
     * @param lastModificationDate
     *            the last modification date of the as returned by
     *            {@link #getLastModifiedHeaderValue(CloseableHttpResponse)}
     * @param response
     *            the response that contained the image
     */
    protected void imageSuccessfullyDownloaded(String imageIdentifier, Date lastModificationDate,
            CloseableHttpResponse response) {
    }

    /**
     * Test whether the image provider was temporarily disabled.
     * 
     * @param configuration
     *            the current configuration of the image provider, can be null. Default
     *            implementation ignores this parameter.
     * @return true if the provider was disabled
     */
    protected boolean isDisabled(C configuration) {
        Long timestamp = getDisabledUntilTimestamp();
        return timestamp != null && System.currentTimeMillis() < timestamp;
    }

    /**
     * Determines if a caught IO exception is one which should disable the image provider
     * temporarily. By default the exceptions {@link ConnectTimeoutException},
     * {@link SocketTimeoutException} and {@link SocketException} are considered as disabling
     * exceptions.
     * 
     * @param e
     *            the exception to check
     * @return true if the image provider should be disabled
     */
    protected boolean isDisablingException(IOException e) {
        return e instanceof ConnectTimeoutException || e instanceof SocketException
                || e instanceof SocketTimeoutException;
    }

    @Override
    public boolean isExternalProvider() {
        return true;
    }

    /**
     * @return whether to return the default image even if the provider is temporarily disabled.
     *         Defaults to false.
     */
    public boolean isProvideDefaultImageIfDisabled() {
        return provideDefaultImageIfDisabled;
    }

    @Override
    public Image loadDefaultImage() throws ImageNotFoundException {
        if (!provideDefaultImageIfDisabled) {
            assertNotDisabled("default-image", getConfiguration());
        }
        return super.loadDefaultImage();
    }

    @Override
    public Image loadImage(String imageIdentifier) throws ImageNotFoundException,
            AuthorizationException {
        C configuration = getConfiguration();
        assertNotDisabled(imageIdentifier, configuration);
        if (!this.isAuthorized(imageIdentifier)) {
            throw new AuthorizationException(
                    "Current user is not allowed to access the image with ID " + imageIdentifier);
        }
        CloseableHttpClient httpClient = getHttpClient();
        if (httpClient == null) {
            throw new ImageTemporarilyNotFoundException("HTTP Client was reset");
        }
        String imageUrl = getImageURL(imageIdentifier, configuration);
        if (StringUtils.isBlank(imageUrl)) {
            imageNotDownloaded(imageIdentifier);
            throw new ImageNotFoundException(
                    "The provider could not create a URL for the image with ID " + imageIdentifier);
        }
        HttpGet httpGet = new HttpGet(imageUrl);
        HttpContext context = getRequestContext(httpGet.getURI(), imageIdentifier, configuration);
        List<Header> headers = getRequestHeaders(httpGet.getURI(), imageIdentifier, configuration);
        if (headers != null) {
            for (Header header : headers) {
                httpGet.addHeader(header);
            }
        }
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        try {
            response = httpClient.execute(httpGet, context);
            validateStatusCode(response.getStatusLine());
            entity = response.getEntity();
            Date lastModificationDate = getLastModifiedHeaderValue(response);
            ByteArrayImage image = extractImageFromEntity(entity, lastModificationDate);
            imageSuccessfullyDownloaded(imageIdentifier, lastModificationDate, response);
            return image;
        } catch (IOException e) {
            String message = "Loading image " + imageIdentifier + " failed";
            LOGGER.error(message, e);
            if (isDisablingException(e)) {
                disableTemporarily(e.getMessage(), configuration);
                // avoid caching longer than it is temporarily disabled
                throw new ImageTemporarilyNotFoundException(message, e);
            }
            imageNotDownloaded(imageIdentifier);
            throw new ImageNotFoundException(message, e);
        } catch (ExternalImageProviderServiceException e) {
            String message = "Response for " + imageIdentifier + " is not a valid image";
            LOGGER.error(message, e);
            disableTemporarily(e.getMessage(), configuration);
            throw new ImageTemporarilyNotFoundException(message, e);
        } catch (ImageNotFoundException e) {
            String message = "Response for " + imageIdentifier + " is not a valid image: "
                    + e.getMessage();
            LOGGER.debug(message);
            imageNotDownloaded(imageIdentifier);
            throw new ImageNotFoundException(message, e.getCause());
        } finally {
            if (entity != null) {
                // only consume entity if available and keep connection open so it can be reused
                EntityUtils.consumeQuietly(entity);
            } else if (response != null) {
                IOUtils.closeQuietly(response);
            }
        }
    }

    /**
     * Prepare the HTTP client that should be used by all requests. The default implementations sets
     * the request config as returned by {@link #getDefaultRequestConfig()}.
     * 
     * @param builder
     *            the HTTP client builder
     * @return the HTTP client
     */
    protected CloseableHttpClient prepareHttpClient(HttpClientBuilder builder) {
        // no need to set socket timeout in default socket config, request config is enough
        builder.setDefaultRequestConfig(getDefaultRequestConfig());
        return builder.build();
    }

    /**
     * Set the HTTP client instance that should be used for all requests. This instance will be
     * returned by {@link #getHttpClient()}.
     * 
     * @param httpClient
     *            the HTTP client to set. Can be null, to reset the client.
     * @param replace
     *            if true the current HTTP client instance will be closed and replaced with the new
     *            one. If false and there is a current HTTP client the current one is not updated.
     */
    protected synchronized void setHttpClient(CloseableHttpClient httpClient, boolean replace) {
        if (replace) {
            if (this.httpClient != null) {
                try {
                    this.httpClient.close();
                } catch (IOException e) {
                    LOGGER.warn("Closing HttpClient failed: " + e.getMessage());
                }
            }
            this.httpClient = httpClient;
        } else if (this.httpClient == null) {
            this.httpClient = httpClient;
        }
    }

    /**
     * Set whether the default image can be returned even if the provider is temporarily disabled.
     * 
     * @param provideDefaultImageIfDisabled
     *            true to return the default image
     */
    public void setProvideDefaultImageIfDisabled(boolean provideDefaultImageIfDisabled) {
        this.provideDefaultImageIfDisabled = provideDefaultImageIfDisabled;
    }

    /**
     * Validate that the content type of the response is an image mime type.
     * 
     * @param contentType
     *            the content type
     * @throws ExternalImageProviderServiceException
     *             in case the content type is not an image/* mime type. This exception will lead to
     *             temporarily disabling the provider.
     */
    protected void validateContentType(String contentType)
            throws ExternalImageProviderServiceException {
        if (contentType != null) {
            contentType = contentType.trim().toLowerCase(Locale.ENGLISH);
            if (contentType.startsWith("image")) {
                return;
            }
        }
        throw new ExternalImageProviderServiceException(
                "Response did not have a valid image content type, but " + contentType);
    }

    /**
     * Checks the status code of a response for an image request.
     * 
     * @param statusLine
     *            the status line containing the status code
     * @throws ImageNotFoundException
     *             in case the status code is one of the codes in
     *             {@link #getNotDisablingStatusCodes()}
     * @throws ExternalImageProviderServiceException
     *             in case the status code is not 200 and not one of the codes returned by
     *             {@link #getNotDisablingStatusCodes()}. The image provider will be disabled
     *             temporarily.
     */
    protected void validateStatusCode(StatusLine statusLine)
            throws ImageNotFoundException, ExternalImageProviderServiceException {
        if (statusLine == null) {
            throw new ExternalImageProviderServiceException(
                    "Response did not contain a status code");
        }
        int statusCode = statusLine.getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            return;
        }
        if (getNotDisablingStatusCodes().contains(statusCode)) {
            throw new ImageNotFoundException("Image not found. Status code: " + statusCode);
        }
        throw new ExternalImageProviderServiceException("Loading image lead to status "
                + statusCode + " with message " + statusLine.getReasonPhrase());
    }

}
