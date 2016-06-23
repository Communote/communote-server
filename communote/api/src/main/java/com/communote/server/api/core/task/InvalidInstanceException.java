package com.communote.server.api.core.task;

/**
 * Thrown to indicate that a task management operation targets a task that is not running on the
 * current instance.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class InvalidInstanceException extends TaskManagementException {

    private static final long serialVersionUID = -4324829745512283399L;
    private final String expectedInstanceName;

    private final String actualInstanceName;

    /**
     * Constructor.
     *
     * @param actualInstanceName
     *            Name of the actual instance.
     * @param expectedInstanceName
     *            Name of the expected instance.
     */
    public InvalidInstanceException(String actualInstanceName, String expectedInstanceName) {
        super("Current instance is " + actualInstanceName + " but instance of task is "
                + expectedInstanceName);
        this.actualInstanceName = actualInstanceName;
        this.expectedInstanceName = expectedInstanceName;
    }

    /**
     * @return the actualInstanceName
     */
    public String getActualInstanceName() {
        return actualInstanceName;
    }

    /**
     * @return the expectedInstanceName
     */
    public String getExpectedInstanceName() {
        return expectedInstanceName;
    }

}
