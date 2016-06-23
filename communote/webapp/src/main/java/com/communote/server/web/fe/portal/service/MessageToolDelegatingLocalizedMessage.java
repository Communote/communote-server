package com.communote.server.web.fe.portal.service;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.core.common.velocity.tools.MessageTool;

/**
 * Localized message which delegates to the {@link MessageTool} and can thus be used to render
 * imprint or terms of use.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MessageToolDelegatingLocalizedMessage implements LocalizedMessage {

    private final boolean imprint;

    /**
     * Create the localized message that delegates to the appropriate MessageTool method
     * 
     * @param imprint
     *            whether the imprint or the terms of use should be rendered
     */
    public MessageToolDelegatingLocalizedMessage(boolean imprint) {
        this.imprint = imprint;
    }

    @Override
    public String toString(Locale locale, Object... arguments) {
        MessageTool messageTool = new MessageTool();
        HttpServletRequest request = null;
        if (arguments != null && arguments.length > 0 && arguments[0] instanceof HttpServletRequest) {
            request = (HttpServletRequest) arguments[0];
        }
        String message;
        if (request != null) {
            if (imprint) {
                message = messageTool.getImprint(request);
            } else {
                message = messageTool.getTermsOfUse(request);
            }
        } else {
            if (imprint) {
                message = messageTool.getImprint(locale);
            } else {
                message = messageTool.getTermsOfUse(locale);
            }
        }
        return message;
    }

}
