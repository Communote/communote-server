package com.communote.server.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable that sets or gets a value of a {@link ClientValue}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SetCheckValue implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(SetCheckValue.class);

    private final ClientValue<Integer> value;
    private final int id;
    private final boolean check;
    private boolean success;
    private Exception exception;

    public SetCheckValue(ClientValue<Integer> value, int id, boolean check) {
        super();
        this.value = value;
        this.id = id;
        this.check = check;
    }

    public Exception getException() {
        return exception;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public void run() {
        try {
            ClientValueTest.setClientAsCurrent("" + id);
            if (check) {
                Integer val = value.getValue();
                if (val != null && val.intValue() == id) {
                    success = true;
                }
            } else {
                value.setValue(id);
                success = true;
            }
        } catch (Exception exception) {
            LOGGER.error("Error running for client id=" + id + " " + exception.getMessage());
            this.exception = exception;
        }
    }

}