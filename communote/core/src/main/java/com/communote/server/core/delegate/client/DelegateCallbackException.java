package com.communote.server.core.delegate.client;

import com.communote.server.api.core.client.ClientDelegateCallback;

/**
 * Exception to wrap exceptions occurring within a {@link ClientDelegateCallback}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DelegateCallbackException extends Exception {

    private static final long serialVersionUID = 9220750251235515438L;

    /**
     * Constructor with a message only.
     *
     * @param message
     *            The message.
     */
    public DelegateCallbackException(String message) {
        super(message);
    }

    /**
     * Constructor with message and an original exception.
     *
     * @param message
     *            A message related to the Exception.
     * @param throwable
     *            The original exception.
     */
    public DelegateCallbackException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
