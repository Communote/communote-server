package com.communote.server.core.common.util;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SqlScriptException extends Exception {

    public SqlScriptException() {
        super();
    }

    public SqlScriptException(String message) {
        super(message);
    }

    public SqlScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlScriptException(Throwable cause) {
        super(cause);
    }

}
