package com.communote.server.api.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.client.ClientTO;
import com.communote.server.model.security.ChannelType;

/**
 * Holder which associates the client and channel execution contexts with the current thread.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ClientAndChannelContextHolder {

    private static class ClientAndChannelContext {

        private ClientTO client;

        private ChannelType channelType;

        public ChannelType getChannel() {
            return channelType;
        }

        public ClientTO getClient() {
            return client;
        }

        public void setChannel(ChannelType channelType) {
            this.channelType = channelType;
        }

        public void setClient(ClientTO client) {
            this.client = client;
        }

    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ClientAndChannelContextHolder.class);

    private static final ThreadLocal<ClientAndChannelContext> CONTEXT = new ThreadLocal<>();

    /**
     * Remove the stored information for the current thread.
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * @return the channel associated with the current thread. Can be null.
     */
    public static ChannelType getChannel() {
        return getChannel(null);
    }

    /**
     * Return the channel associated with the current thread or the given fallback if no channel was
     * set.
     *
     * @param fallback
     *            the fallback to return if no channel is set
     * @return the current channel or the fallback
     */
    public static ChannelType getChannel(ChannelType fallback) {
        if (CONTEXT.get() != null && CONTEXT.get().getChannel() != null) {
            return CONTEXT.get().getChannel();
        } else {
            String message = "No channel set! Using fallback=" + fallback;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(message, new Exception());
            } else {
                LOGGER.warn(message);
            }
        }
        return fallback;
    }

    /**
     * @return the client on which all operations of the current thread are run. A return value of
     *         <code>null</code> has to be treated as the global client execution context.
     */
    public static ClientTO getClient() {
        ClientTO result = null;
        if (CONTEXT.get() != null) {
            result = CONTEXT.get().getClient();
        }
        return result;
    }

    /**
     * Associate the channel with the current thread
     *
     * @param channelType
     *            the channel execution CONTEXT
     */
    public static void setChannel(ChannelType channelType) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new ClientAndChannelContext());
        }
        CONTEXT.get().setChannel(channelType);
    }

    /**
     * Set the client on which the operations of the current thread should be run
     *
     * @param client
     *            the client execution CONTEXT
     */
    public static void setClient(ClientTO client) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new ClientAndChannelContext());
        }
        CONTEXT.get().setClient(client);
    }

}
