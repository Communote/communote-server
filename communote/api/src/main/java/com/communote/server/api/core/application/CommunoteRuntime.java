package com.communote.server.api.core.application;

/**
 * Holder of the singleton runtime instance.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CommunoteRuntime {

    private static Runtime INSTANCE;

    /**
     * @return the runtime or null if init wasn't called
     */
    public static Runtime getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the runtime with the given builder. A BootstrapException will be called if
     * initialization fails.
     *
     * @param runtimeBuilder
     *            the runtime builder to create the runtime
     */
    public static void init(RuntimeBuilder runtimeBuilder) {
        if (INSTANCE == null) {
            INSTANCE = runtimeBuilder.build();
        }
    }

    private CommunoteRuntime() {

    }
}
