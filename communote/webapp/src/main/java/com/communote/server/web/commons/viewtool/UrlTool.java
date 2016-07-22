package com.communote.server.web.commons.viewtool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

import com.communote.common.util.UrlHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.blog.export.PermalinkGenerationManagement;
import com.communote.server.core.blog.export.PermalinkGenerator;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.image.CoreImageType;
import com.communote.server.core.image.type.EntityImageManagement;
import com.communote.server.core.retrieval.helper.AttachmentHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.security.ssl.ChannelManagement;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.commons.helper.ImageUrlHelper;
import com.communote.server.web.commons.resource.ConcatenatedResourceStore;
import com.communote.server.web.commons.resource.FaviconProviderManager;

/**
 * Tool for rendering application URLs.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@DefaultKey("urlTool")
@ValidScope(Scope.REQUEST)
public class UrlTool extends RequestAwareTool {

    private PermalinkGenerator permalinkGenerator;
    private ConcatenatedResourceStore jsConcatenatedResourceStore;
    private ConcatenatedResourceStore cssConcatenatedResourceStore;
    private FaviconProviderManager faviconProviderManager;

    /**
     * Constructor.
     */
    public UrlTool() {

    }

    /**
     * Renders a permanent link to the named blog
     *
     * @param alias
     *            the alias of the blog
     * @return the link
     */
    public String blog(String alias) {
        return getPermalinkGenerator().getBlogLink(alias, getRequest().isSecure());
    }

    /**
     * Renders a permanent link to the named blog
     *
     * @param blogId
     *            The blogs id as String.
     * @return the link
     */
    public String blogFromId(String blogId) {
        try {
            return blog(ServiceLocator.instance().getService(BlogManagement.class)
                    .getBlogById(Long.parseLong(blogId), false).getNameIdentifier());
        } catch (BlogNotFoundException e) {
            throw BlogManagementHelper.convertException(e);
        } catch (BlogAccessException e) {
            throw BlogManagementHelper.convertException(e);
        }
    }

    /**
     * Creates a URL pointing to the logo of the current client.
     *
     * @param size
     *            the size of the logo
     * @return the URL to the client logo
     */
    public String clientImage(ImageSizeType size) {
        String url = ImageUrlHelper.buildImageUrl("", CoreImageType.clientlogo, size);
        return this.render(url);
    }

    /**
     * Creates a request parameter holding the build timestamp.
     *
     * @return the parameter string
     */
    public String createBuildTimestampParam() {
        return "t="
                + CommunoteRuntime.getInstance().getApplicationInformation().getBuildTimestamp();
    }

    /**
     * Renders a permanent link to the current user
     *
     * @return the link
     */
    public String currentUser() {
        return user(SecurityHelper.getCurrentUserAlias());
    }

    /**
     * @return the lazily initialized ConcatenatedResourceStore for CSS
     */
    private ConcatenatedResourceStore getCssConcatenatedResourceStore() {
        if (cssConcatenatedResourceStore == null) {
            cssConcatenatedResourceStore = WebServiceLocator.instance().getService(
                    "cssConcatenatedResourceStore", ConcatenatedResourceStore.class);
        }
        return cssConcatenatedResourceStore;
    }

    /**
     * Returns a link to the given external object within the external system.
     *
     * @param externalSystemId
     *            The external system id.
     * @param externalObjectId
     *            The external object id.
     * @return The link or null, if there is no generator for the given external system.
     */
    public String getExternalObjectLink(String externalSystemId, String externalObjectId) {
        return ServiceLocator.instance().getService(PermalinkGenerationManagement.class)
                .getExternalObjectLink(externalSystemId, externalObjectId);
    }

    private FaviconProviderManager getFaviconProviderManager() {
        if (faviconProviderManager == null) {
            faviconProviderManager = WebServiceLocator.findService(FaviconProviderManager.class);
        }
        return faviconProviderManager;
    }

    /**
     * @return the URL of the favicon icon
     */
    public String getFaviconUrl() {
        return getFaviconProviderManager().getFaviconUrl(getRequest());
    }

    /**
     * @return the lazily initialized ConcatenatedResourceStore for JavaScript
     */
    private ConcatenatedResourceStore getJsConcatenatedResourceStore() {
        if (jsConcatenatedResourceStore == null) {
            jsConcatenatedResourceStore = WebServiceLocator.instance().getService(
                    "jsConcatenatedResourceStore", ConcatenatedResourceStore.class);
        }
        return jsConcatenatedResourceStore;
    }

    /**
     * @return the lazily initialized permalink generator
     */
    private PermalinkGenerator getPermalinkGenerator() {
        if (permalinkGenerator == null) {
            permalinkGenerator = ServiceLocator.instance().getService(
                    PermalinkGenerationManagement.class);
        }
        return permalinkGenerator;
    }

    /**
     * Creates a URL pointing to an imag for the given entity.
     *
     * @param entityId
     *            Id of the entity to get the link for.
     * @param imageType
     *            Type of the image.
     * @param size
     *            Size of the requested image.
     * @return the URL to the image
     */
    public String image(String entityId, CoreImageType imageType, ImageSizeType size) {
        String url = ImageUrlHelper.buildImageUrl(entityId, imageType, size);
        return this.render(url);
    }

    /**
     * @param entityId
     *            Id of the entity to get the link for.
     * @return The url for the large banner image of the given entity.
     */
    public String imageForBanner(String entityId) {
        return image(entityId, CoreImageType.entityBanner, ImageSizeType.LARGE);
    }

    /**
     * @param entityId
     *            Id of the entity to get the link for.
     * @return The url for the large profile image of the given entity.
     */
    public String imageForProfile(String entityId) {
        return image(entityId, CoreImageType.entityProfile, ImageSizeType.LARGE);
    }

    /**
     * This method inserts the session id into the url. This should only be used for internal urls.
     *
     * @param url
     *            The url.
     * @return The url with session id inserted.
     */
    public String insertSessionId(String url) {
        return UrlHelper.insertSessionIdInUrl(url, getRequest().getSession().getId());
    }

    /**
     *
     * @param relativeAttachmentUrl
     *            the relative attachment URL.
     * @param useAbsoluteUrl
     *            {@code true} to render absolute URL
     * @param staticResource
     *            true if the url should be rendered as a resource not to be delivered by the
     *            dispatcher servlet
     *
     * @return URL for the attachment.
     */
    private String internalRenderAttachmentUrl(String relativeAttachmentUrl,
            boolean useAbsoluteUrl, boolean staticResource) {
        if (useAbsoluteUrl) {
            return ControllerHelper.renderAbsoluteUrl(getRequest(), null, relativeAttachmentUrl,
                    false, staticResource, false);
        } else {
            return ControllerHelper.renderRelativeUrl(getRequest(), relativeAttachmentUrl,
                    staticResource, false);
        }
    }

    /**
     * Renders a permanent link to the note
     *
     * @param blogAlias
     *            the alias of the blog
     * @param noteId
     *            the id of the note
     * @return the link
     */
    public String note(String blogAlias, Long noteId) {
        return getPermalinkGenerator().getNoteLink(blogAlias, noteId, getRequest().isSecure());
    }

    /**
     * Renders the URL for the specified urlPath. Delegates to
     * {@link #render(String, boolean, boolean)} with parameters absolute and secure set to false.
     *
     * @param urlPath
     *            the URL path
     * @return the created URL
     */
    public String render(String urlPath) {
        return render(urlPath, null, false, false, null, null, false);
    }

    /**
     * Renders the URL for the specified urlPath. Delegates to
     * {@link #render(String, String, boolean, boolean, Boolean, String)} with parameters baseUrl,
     * renderSessionId and clientId set to null.
     *
     * @param urlPath
     *            the URL path
     * @param absolute
     *            whether the URL should be rendered as an absolute URL
     * @param secure
     *            whether the URL should be rendered with the HTTPS protocol. Will be ignored if
     *            absolute is false.
     * @param staticResource
     *            {@code true} if the URL should be rendered as a resource not to be delivered by
     *            the dispatcher servlet
     * @return the rendered URL
     */
    public String render(String urlPath, boolean absolute, boolean secure, boolean staticResource) {
        return render(urlPath, null, absolute, secure, null, null, staticResource);
    }

    /**
     * Renders the URL for the specified urlPath. Delegates to
     * {@link #render(String, String, boolean, boolean, Boolean, String)} with parameters baseUrl,
     * renderSessionId and clientId set to null.
     *
     * @param urlPath
     *            the URL path
     * @param absolute
     *            whether the URL should be rendered as an absolute URL
     * @param channelTypeAsString
     *            The channel type as string to check against.
     * @param staticResource
     *            {@code true} if the URL should be rendered as a resource not to be delivered by
     *            the dispatcher servlet
     * @return the rendered URL
     * @throws IllegalArgumentException
     *             Thrown, when there is no such channel type.
     */
    public String render(String urlPath, boolean absolute, String channelTypeAsString,
            boolean staticResource) throws IllegalArgumentException {
        ChannelType channelType = ChannelType.fromString(channelTypeAsString);
        boolean secure = ServiceLocator.findService(ChannelManagement.class)
                .isForceSsl(channelType);
        return render(urlPath, null, absolute, secure, null, null, staticResource);
    }

    /**
     * Renders the URL for the specified urlPath.
     *
     * @param urlPath
     *            the URL path
     * @param baseUrl
     *            optional string with a URL that should be prefixed to the urlPath. If set, the
     *            parameters absolute, secure and clientId will be ignored.
     * @param absolute
     *            whether the URL should be rendered as an absolute URL. Will be ignored if baseUrl
     *            is not null.
     * @param secure
     *            whether the URL should be rendered with the HTTPS protocol. Will be ignored if
     *            absolute is false or baseUrl is not null.
     * @param renderSessionId
     *            optional parameter to force inclusion of the current session ID. If null the
     *            session ID will be included if the request asked for an existing session and the
     *            session ID was not received via a cookie. If not null and true the ID of the
     *            current session will be included. In case there is no session the session will be
     *            created. If baseUrl is not null the session ID won't be included.
     * @param clientId
     *            the ID of the client to be included in the URL. If null the ID of the current
     *            client will be used. Will be ignored if baseUrl is not {@code null}.
     * @param staticResource
     *            {@code true} if the URL should be rendered as a resource which will not be handled
     *            by the dispatcher servlet
     * @return the rendered URL
     */
    public String render(String urlPath, String baseUrl, boolean absolute, boolean secure,
            Boolean renderSessionId, String clientId, boolean staticResource) {
        String url = ControllerHelper.renderUrl(getRequest(), urlPath, baseUrl, absolute, secure,
                renderSessionId, clientId, staticResource, false);
        if (staticResource) {
            url = ControllerHelper.appendTimestamp(url, urlPath);
        }
        return url;
    }

    /**
     * Determine the relative attachment URL path for a resource
     *
     * @param attachment
     *            The attachment.
     * @return the relative URL path for the attachment.
     */
    public String renderAttachmentUrl(Attachment attachment) {
        return renderAttachmentUrl(attachment, false, false);
    }

    /**
     * Determine the resource URL for download/access for the given item
     *
     * @param attachment
     *            the attachment.
     * @param useAbsoluteUrl
     *            {@code true} to render absolute URL
     * @param staticResource
     *            true if the URL should be rendered as a resource which will not be handled by the
     *            dispatcher servlet
     *
     * @return URL for the attachment.
     */
    public String renderAttachmentUrl(Attachment attachment, boolean useAbsoluteUrl,
            boolean staticResource) {
        return internalRenderAttachmentUrl(
                AttachmentHelper.determineRelativeAttachmentUrl(attachment), useAbsoluteUrl,
                staticResource);
    }

    /**
     * Determine the relative attachment URL path for a resource
     *
     * @param attachment
     *            the attachment list item.
     * @return the relative URL path for the attachment.
     */
    public String renderAttachmentUrl(AttachmentData attachment) {
        return renderAttachmentUrl(attachment, false, false);
    }

    /**
     * Determine the resource URL for download/access for the given item
     *
     * @param attachment
     *            the attachment list item.
     * @param useAbsoluteUrl
     *            {@code true} to render absolute URL
     * @param staticResource
     *            true if the URL should be rendered as a resource which will not be handled by the
     *            dispatcher servlet
     *
     * @return URL for the attachment.
     */
    public String renderAttachmentUrl(AttachmentData attachment, boolean useAbsoluteUrl,
            boolean staticResource) {
        return internalRenderAttachmentUrl(
                AttachmentHelper.determineRelativeAttachmentUrl(attachment), useAbsoluteUrl,
                staticResource);
    }

    /**
     * Render the URL to the concatenated CSS of a given category.
     *
     * @param categoryName
     *            the name of the category
     * @param minimized
     *            whether to return the minimized version of the concatenated CSS
     * @return the URL to retrieve the concatenated CSS or empty string if the resource does not
     *         exist
     */
    public String renderConcatenatedCssUrl(String categoryName, boolean minimized) {
        return renderResourceStoreDownloadUrl("styles/packed.css",
                getCssConcatenatedResourceStore(), categoryName, minimized, true);
    }

    /**
     * Render the URL to the concatenated JavaScript of a given category.
     *
     * @param categoryName
     *            the name of the category
     * @param minimized
     *            whether to return the minimized version of the concatenated JavaScript
     * @return the URL to retrieve the concatenated JavaScript or empty string if the resource does
     *         not exist
     */
    public String renderConcatenatedJsUrl(String categoryName, boolean minimized) {
        return renderResourceStoreDownloadUrl("javascripts/packed.js",
                getJsConcatenatedResourceStore(), categoryName, minimized, true);
    }

    /**
     * Render the URLs to the CSS resources of the given category.
     *
     * @param categoryName
     *            the name of the category
     * @param minimized
     *            whether to render the URLs to the minimized versions
     * @return the URLs to the CSS resources. The list will be empty if the category is not known.
     */
    public List<String> renderCssUrls(String categoryName, boolean minimized) {
        List<String> urls = renderResourceUrls(getCssConcatenatedResourceStore(), categoryName,
                minimized);
        String propertyResourceUrl = renderResourceStoreDownloadUrl("styles/propertyResource.css",
                getCssConcatenatedResourceStore(), categoryName, minimized, false);
        if (propertyResourceUrl != StringUtils.EMPTY) {
            urls.add(propertyResourceUrl);
        }
        return urls;
    }

    /**
     * Uses {@link #render(String)} to render the URL if it is relative, otherwise the URL is
     * returned unchanged.
     * <p>
     * Example:
     * <ul>
     * <li>https://github.com/communote remains https://github.com/communote</li>
     * <li>/portal/home becomes /microblog/global/portal/home</li>
     * </ul>
     * </p>
     *
     * @param urlPath
     *            the url to render
     * @return the rendered or unmodified URL
     */
    public String renderIfRelative(String urlPath) {
        if (urlPath == null) {
            urlPath = "";
        }
        if (UrlHelper.isAbsoluteHttpUrl(urlPath)) {
            return urlPath;
        }
        return render(urlPath);
    }

    /**
     * Render the URLs to the JavaScript resources of the given category.
     *
     * @param categoryName
     *            the name of the category
     * @param minimized
     *            whether to render the URLs to the minimized versions
     * @return the URLs to the JavaScript resources. The list will be empty if the category is not
     *         known.
     */
    public List<String> renderJsUrls(String categoryName, boolean minimized) {
        return renderResourceUrls(getJsConcatenatedResourceStore(), categoryName, minimized);
    }

    /**
     * Render the URL to download one of the resources managed by the
     * {@link ConcatenatedResourceStore} of a given category
     *
     * @param urlBase
     *            the base URL
     * @param fileStore
     *            the file store managing the categories
     * @param categoryName
     *            the name of the category
     * @param minimized
     *            whether to return the minimized version of the concatenated resource
     * @param concatenated
     *            whether to get the concatenated or the property resource URL
     * @return the URL to retrieve the resource or empty string if the resource does not exist,
     */
    private String renderResourceStoreDownloadUrl(String urlBase,
            ConcatenatedResourceStore fileStore, String categoryName, boolean minimized,
            boolean concatenated) {
        long timestamp;
        if (concatenated) {
            timestamp = fileStore.getConcatenatedFileLastModified(categoryName);
        } else {
            timestamp = fileStore.getPropertyResourceLastModified(categoryName);
        }
        // category does not exist
        if (timestamp < 0) {
            return StringUtils.EMPTY;
        }
        String url = render(urlBase + "?category=" + categoryName + "&t=" + timestamp);
        if (minimized) {
            url += "&suffix=" + fileStore.getMinimizedSuffix();
        }
        return url;
    }

    /**
     * Render the URLs to the resources of the given category.
     *
     * @param fileStore
     *            the file store managing the categories
     * @param categoryName
     *            the name of the category
     * @param minimized
     *            whether to render the URLs to the minimized versions
     * @return the URLs to the resources. The list will be empty if the category is not known.
     */
    private List<String> renderResourceUrls(ConcatenatedResourceStore fileStore,
            String categoryName, boolean minimized) {
        List<String> urls;
        List<String> resources = fileStore.getCoreResources(categoryName, minimized, true);
        // if the category is not known there are also no plugin resources
        if (resources == null) {
            urls = Collections.emptyList();
        } else {
            urls = new ArrayList<String>();
            for (String resourceLocation : resources) {
                // use controller helper directly to avoid adding another timestamp
                urls.add(ControllerHelper.renderUrl(getRequest(), resourceLocation, null, false,
                        false, null, null, true, false));
            }
            resources = fileStore.getPluginResources(categoryName, minimized, true);
            if (resources != null) {
                for (String resourceLocation : resources) {
                    // prepend 'plugins' as the controller is listening on that path
                    urls.add(render("plugins" + resourceLocation));
                }
            }
        }
        return urls;
    }

    /**
     * Renders the startpage url.
     *
     * @return the startpage url
     */
    public String renderStartpage() {
        return render(WebServiceLocator.instance().getStartpageRegistry().getStartpage());
    }

    /**
     * Renders the URL of a static resource which is not delivered by the dispatcher servlet. This
     * is a convenience function for the render method.
     *
     * @param urlPath
     *            The URL path.
     * @return the rendered URL
     */
    public String renderStatic(String urlPath) {
        return render(urlPath, false, false, true);
    }

    /**
     * Renders the base URL of all static resources. This function won't add the sessionId or a
     * timestamp request parameter.
     *
     * @param absolute
     *            whether the URL should be rendered as an absolute URL.
     * @param secure
     *            whether the URL should be rendered with the HTTPS protocol
     * @param clientId
     *            the ID of the client to be included in the URL. If null the ID of the current
     *            client will be used.
     * @return the rendered URL
     */
    public String renderStaticBase(boolean absolute, boolean secure, String clientId) {
        return ControllerHelper.renderUrl(getRequest(), StringUtils.EMPTY, null, absolute, secure,
                Boolean.FALSE, clientId, true, false);
    }

    /**
     * Renders a permanent link to the given tag.
     *
     * @param tagId
     *            The tag.
     * @return the link
     */
    public String tag(long tagId) {
        return getPermalinkGenerator().getTagLink(tagId, getRequest().isSecure());
    }

    /**
     * Renders a permanent link to the given tag.
     *
     * @param tag
     *            The tag.
     * @return the link
     * @deprecated Use {@link #tag(long)} instead.
     */
    @Deprecated
    public String tag(String tag) {
        return getPermalinkGenerator().getTagLink(tag, getRequest().isSecure());
    }

    /**
     * Set the image url to the default topic image.
     *
     * @return the url to the default profile image
     */
    public String topicDefaultProfileImage() {
        return imageForProfile(EntityImageManagement.DEFAULT_IMAGE_ENTITY_ID);
    }

    /**
     * Renders a permanent link to the named user
     *
     * @param alias
     *            the alias of the user
     * @return the link
     */
    public String user(String alias) {
        return getPermalinkGenerator().getUserLink(alias, getRequest().isSecure());
    }

    /**
     * Creates a URL pointing to a user image.
     *
     * @param userId
     *            the ID of the user whose image should be rendered
     * @param size
     *            the size of the image
     * @return the URL to the image
     */
    public String userImage(Long userId, ImageSizeType size) {
        return this.render(ImageUrlHelper.buildUserImageUrl(userId, size));
    }

    /**
     * Creates a URL pointing to a user image.
     *
     * @param user
     *            the user whose image should be rendered
     * @param size
     *            the size of the image
     * @return the URL to the image
     */
    public String userImage(UserData user, ImageSizeType size) {
        return userImage(user != null ? user.getId() : null, size);
    }
}
