package com.communote.server.web.fe.portal.user.system.content;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class FileStorageForm {

    private String path;

    /**
     * Does nothing.
     */
    public FileStorageForm() {
        // Do nothing.
    }

    /**
     * @param path
     *            The path.
     */
    public FileStorageForm(String path) {
        this.path = path == null ? null : path.trim();
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }
}
