package com.communote.server.core.vo.content;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.communote.server.model.attachment.AttachmentStatus;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentRedirectTO extends AttachmentTO {

    private final String redirectUrl;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AttachmentRedirectTO(String redirectUrl) {
        this(redirectUrl, null);
    }

    public AttachmentRedirectTO(String redirectUrl, AttachmentStatus status) {
        super(status);
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    @Override
    protected InputStream open() throws IOException {
        return null;
    }

    @Override
    public void sendInResponse(HttpServletRequest request, HttpServletResponse response,
            String characterEncoding, boolean dipositionTypeAsAttachment) throws IOException {

        response.sendRedirect(redirectUrl);
    }

}