package com.communote.server.api.core.bootstrap;

import com.communote.common.i18n.LocalizedMessage;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class InitializationStatus {
    public enum Type {
        /**
         * The initialization is still in progress.
         */
        IN_PROGRESS,
        /**
         * The initialization completed successfully.
         */
        SUCCESS,
        /**
         * The initialization failed.
         */
        FAILURE;
    }

    private final Type status;

    private final LocalizedMessage message;

    public InitializationStatus(InitializationStatus.Type status, LocalizedMessage message) {
        this.status = status;
        this.message = message;
    }

    public InitializationStatus(Type status) {
        this(status, null);
    }

    /**
     * @return a message with additional details explaining the status. Can be null;
     */
    public LocalizedMessage getMessage() {
        return message;
    }

    /**
     * @return the status type
     */
    public Type getStatus() {
        return status;
    }

}
