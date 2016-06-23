package com.communote.server.web.commons.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.common.util.Orderable;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * Holds the details of a section of a static page.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StaticPageSection implements Orderable {

    /**
     * The default order value
     */
    public static final int DEFAULT_ORDER = 100;

    private static final Pattern ID_REGEX = Pattern.compile("[^\\w-]");
    private String shortTitleMessageKey;
    private String titleMessageKey;
    private LocalizedMessage shortTitle;
    private LocalizedMessage title;
    private LocalizedMessage content;
    private String contentMessageKey;
    private String contentTemplateName;
    private Map<String, Object> contentTemplateParameters;
    private int order = DEFAULT_ORDER;
    private final String id;

    private boolean contentNeedsRequest;

    private boolean shortTitleNeedsRequest;

    private boolean titleNeedsRequest;

    /**
     * Create a new section.
     *
     * @param id
     *            the ID of the section which can only contain word characters (see
     *            {@link java.util.regex.Pattern}) and the dash character
     */
    public StaticPageSection(String id) {
        if (id == null || id.length() == 0 || ID_REGEX.matcher(id).find()) {
            throw new IllegalArgumentException("ID " + id + " is not valid");
        }
        this.id = id;
    }

    /**
     * Add a parameter that is {@link Required} for rendering the content template
     *
     * @param key
     *            the key of the parameters
     * @param value
     *            the value
     */
    public void addContentTemplateParameter(String key, Object value) {
        getContentTemplateParameter().put(key, value);
    }

    /**
     * The localized content.
     *
     * @param locale
     *            the current locale
     * @param request
     *            the current request
     * @return the localized content
     */
    public String getContent(Locale locale, HttpServletRequest request) {
        if (content != null) {
            if (contentNeedsRequest) {
                return content.toString(locale, request);
            }
            return content.toString(locale);
        } else if (contentMessageKey != null) {
            return ResourceBundleManager.instance().getText(contentMessageKey, locale);
        }
        return StringUtils.EMPTY;
    }

    /**
     * The name of the template for rendering the content.
     *
     * @return the name of the template or null if not template is set
     */
    public String getContentTemplateName() {
        return contentTemplateName;
    }

    /**
     * @return parameters that are required for rendering the content template
     */
    public Map<String, Object> getContentTemplateParameter() {
        if (contentTemplateParameters == null) {
            contentTemplateParameters = new HashMap<>();
        }
        return contentTemplateParameters;
    }

    /**
     * @return the ID of the page
     */
    public String getId() {
        return id;
    }

    /**
     * @return the order value for sorting the sections. Returns by default {@value #DEFAULT_ORDER}
     */
    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Return the localized short title
     *
     * @param locale
     *            the locale to use for internationalization
     * @param request
     *            the current request
     * @return the localized short title or the value of {@link #getTitle(Locale)} if no short title
     *         is set
     */
    public String getShortTitle(Locale locale, HttpServletRequest request) {
        if (shortTitle != null) {
            if (shortTitleNeedsRequest) {
                return shortTitle.toString(locale, request);
            }
            return shortTitle.toString(locale);
        } else if (shortTitleMessageKey != null) {
            return ResourceBundleManager.instance().getText(shortTitleMessageKey, locale);
        }
        return getTitle(locale, request);
    }

    /**
     * Return the localized title
     *
     * @param locale
     *            the locale to use for internationalization
     * @param request
     *            the current request
     * @return the localized title or an empty string if not title is set
     */
    public String getTitle(Locale locale, HttpServletRequest request) {
        if (title != null) {
            if (titleNeedsRequest) {
                return title.toString(locale, request);
            }
            return title.toString(locale);
        } else if (titleMessageKey != null) {
            return ResourceBundleManager.instance().getText(titleMessageKey, locale);
        }
        return StringUtils.EMPTY;
    }

    /**
     * @return whether a content template was set.
     */
    public boolean isContentTemplate() {
        return contentTemplateName != null;
    }

    /**
     * @return whether a short title was set
     */
    public boolean isShortTitleAvailable() {
        return shortTitle != null || shortTitleMessageKey != null;
    }

    /**
     * Set the content. The content set with this method takes precedence over the one set with
     * {@link #setContentMessageKey(String)} but if also a content template is set, the template
     * will be used.
     *
     * @param content
     *            the localizable content
     * @param needsRequest
     *            whether the localized message needs the request object to create the message. If
     *            true the request will be passed as argument to the toString method of the
     *            localized message
     */
    public void setContent(LocalizedMessage content, boolean needsRequest) {
        this.content = content;
        this.contentNeedsRequest = needsRequest;
    }

    /**
     * Set the message key which should be used to resolve the localized content
     *
     * @param contentMessageKey
     *            the message key of the content
     */
    public void setContentMessageKey(String contentMessageKey) {
        this.contentMessageKey = contentMessageKey;
    }

    /**
     * Set the name/id of a template that should be rendered as the content of the section
     *
     * @param contentTemplateName
     *            the name of the template to render
     */
    public void setContentTemplateName(String contentTemplateName) {
        this.contentTemplateName = contentTemplateName;
    }

    /**
     * Set a custom order. The higher the value the earlier the section will be rendered.
     *
     * @param order
     *            the order to set
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Set a shorter version of the title which could for instance be shown in a menu. The title set
     * with this method takes precedence over the one set with
     * {@link #setShortTitleMessageKey(String)}.
     *
     * @param shortTitle
     *            the short title
     * @param needsRequest
     *            whether the localized message needs the request object to create the message. If
     *            true the request will be passed as argument to the toString method of the
     *            localized message
     */
    public void setShortTitle(LocalizedMessage shortTitle, boolean needsRequest) {
        this.shortTitle = shortTitle;
        this.shortTitleNeedsRequest = needsRequest;
    }

    /**
     * Set the message key for resolving a localized shorter version of the title which could for
     * instance be shown in a menu.
     *
     * @param shortTitleMessageKey
     *            the message key of the short title
     */
    public void setShortTitleMessageKey(String shortTitleMessageKey) {
        this.shortTitleMessageKey = shortTitleMessageKey;
    }

    /**
     * Set the title of the section. The title set with this method takes precedence over the one
     * set with {@link #setTitleMessageKey(String)}.
     *
     * @param title
     *            the title of the section
     * @param needsRequest
     *            whether the localized message needs the request object to create the message. If
     *            true the request will be passed as argument to the toString method of the
     *            localized message
     */
    public void setTitle(LocalizedMessage title, boolean needsRequest) {
        this.title = title;
        this.titleNeedsRequest = needsRequest;
    }

    /**
     * Set the message key for resolving a localized title of the section.
     *
     * @param titleMessageKey
     *            the message key of the title
     */
    public void setTitleMessageKey(String titleMessageKey) {
        this.titleMessageKey = titleMessageKey;
    }
}
