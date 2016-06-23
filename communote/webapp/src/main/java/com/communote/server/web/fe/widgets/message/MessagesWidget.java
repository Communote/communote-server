package com.communote.server.web.fe.widgets.message;

import com.communote.server.widgets.AbstractWidget;

/**
 * The Messages Widget takes care of showing error and success messages stored in the session.
 * Messages can be stored using {@link com.communote.server.web.commons.MessageHelper}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MessagesWidget extends AbstractWidget {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "widget.message.messages." + outputType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }
}
