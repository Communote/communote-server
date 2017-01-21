package com.communote.server.web.commons.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import com.communote.common.util.UriUtils;
import com.communote.common.util.UrlHelper;
import com.communote.server.core.image.CoreImageType;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.web.commons.helper.ImageUrlHelper;

/**
 * This class represents the custom tag for user or client images. To be used as: <ct:img userId="2"
 * size="large" /> or <ct:img type="client" />
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageKenmeiUrlTag extends KenmeiUrlTag {

    private static final long serialVersionUID = 1L;

    private Long userId = null;
    private String imageType = CoreImageType.clientlogo.toString();

    private boolean encodeQueryUrl = false;
    private ImageSizeType imageSize = ImageSizeType.MEDIUM;

    /**
     * This method sets specific values for rendering the image path. {@inheritDoc}
     */
    @Override
    public int doEndTag() throws JspException {
        String identifier;
        if (userId == null) {
            identifier = "";
        } else {
            identifier = userId.toString();
        }
        String value = ImageUrlHelper.buildImageUrl(identifier, CoreImageType.valueOf(imageType),
                imageSize);
        value = UrlHelper.insertSessionIdInUrl(value,
                ((HttpServletRequest) pageContext.getRequest()).getSession().getId());
        setValue(encodeQueryUrl ? UriUtils.encodeUriComponent(value) : value);
        return super.doEndTag();
    }

    /**
     * @return the imageSize
     */
    public ImageSizeType getSize() {
        return imageSize;
    }

    /**
     * @return the imageType
     */
    public String getType() {
        return imageType;
    }

    /**
     * @return the userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @return the encodeQueryUrl
     */
    public boolean isEncodeQueryUrl() {
        return encodeQueryUrl;
    }

    /**
     * @param encodeQueryUrl
     *            the encodeQueryUrl to set
     */
    public void setEncodeQueryUrl(boolean encodeQueryUrl) {
        this.encodeQueryUrl = encodeQueryUrl;
    }

    /**
     * @param size
     *            the imageSize to set
     */
    public void setSize(ImageSizeType size) {
        imageSize = size;
    }

    /**
     * @param type
     *            the imageType to set
     */
    public void setType(String type) {
        CoreImageType.valueOf(type);
        this.imageType = type;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
