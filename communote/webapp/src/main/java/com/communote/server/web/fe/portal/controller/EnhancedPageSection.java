package com.communote.server.web.fe.portal.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.communote.common.util.Pair;
import com.communote.common.util.UrlHelper;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.commons.controller.StaticPageSection;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Descriptor for sections which defines some additional content elements. These elements can for
 * instance be used to create a 2 column layout with an image on the left and an description
 * (provided by the content member) on the right side.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EnhancedPageSection extends StaticPageSection {

    private String imageUrl;
    private String imageLink;
    private List<Pair<String, String>> actionLinks;

    public EnhancedPageSection(String id) {
        super(id);
    }

    /**
     * Add an action link.
     *
     * @param labelMessageKey
     *            the message key of the label of the link
     * @param url
     *            the relative or absolute URL of the link
     */
    public void addActionLink(String labelMessageKey, String url) {
        if (!StringUtils.isBlank(url) && !StringUtils.isBlank(labelMessageKey)) {
            if (actionLinks == null) {
                actionLinks = new ArrayList<Pair<String, String>>();
            }
            actionLinks.add(new Pair<String, String>(labelMessageKey, url));
        }
    }

    /**
     * A list of pairs were each entry represents an action link. The left side of the pair contains
     * the localized label of the link and the right the rendered URL.
     *
     * @param locale
     *            the locale for rendering the link label
     * @param request
     *            the current request
     * @return the action links, can be empty
     */
    public List<Pair<String, String>> getActionLinks(Locale locale, HttpServletRequest request) {
        if (!hasActionLinks()) {
            return Collections.emptyList();
        }
        ArrayList<Pair<String, String>> renderedLinks = new ArrayList<>();
        for (Pair<String, String> link : actionLinks) {
            String label = ResourceBundleManager.instance().getText(link.getLeft(), locale);
            String url;
            // test for absolute URLs
            if (UrlHelper.getProtocolHostPort(link.getRight(), true) != null) {
                url = link.getRight();
            } else {
                url = ControllerHelper.renderUrl(request, link.getRight(), null, false, false,
                        null, null, false, false);
            }
            renderedLinks.add(new Pair<String, String>(label, url));
        }
        return renderedLinks;
    }

    /**
     * Render and return the URL that should be set as link behind the image.
     *
     * @param request
     *            the current request
     * @return the rendered URL or the empty string if no link was set
     */
    public String getImageLink(HttpServletRequest request) {
        if (imageLink == null) {
            return StringUtils.EMPTY;
        }
        return ControllerHelper.renderUrl(request, imageLink, null, false, false, null, null,
                false, false);
    }

    /**
     * Render and return the URL pointing to the image of the section.
     *
     * @param request
     *            the current request
     * @return the rendered URL of the image or the empty string if no imageUrl was set
     */
    public String getImageUrl(HttpServletRequest request) {
        if (imageUrl == null) {
            return StringUtils.EMPTY;
        }
        String url;
        if (UrlHelper.isAbsoluteHttpUrl(imageUrl)) {
            url = imageUrl;
        } else {
            if (imageUrl.startsWith("/plugins/")) {
                // plugin resources must be delivered by the dispatcher servlet
                url = ControllerHelper.renderUrl(request, imageUrl, null, false, false, null, null,
                        false, false);
            } else {
                url = ControllerHelper.renderUrl(request, imageUrl, null, false, false, null, null,
                        true, false);
            }
        }
        return url;
    }

    /**
     * @return whether action links were added
     */
    public boolean hasActionLinks() {
        return actionLinks != null && actionLinks.size() > 0;
    }

    /**
     * @return whether an image URL is set
     */
    public boolean hasImageUrl() {
        return imageUrl != null;
    }

    /**
     * Set a relative or absolute URL which should be added as a link behind the image.
     *
     * @param imageLink
     *            the URL of the link
     */
    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    /**
     * Set the URL of an image for the section.
     *
     * @param imageUrl
     *            relative or absolute URL pointing to an image
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
