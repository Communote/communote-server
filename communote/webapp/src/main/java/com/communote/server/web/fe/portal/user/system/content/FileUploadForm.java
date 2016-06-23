package com.communote.server.web.fe.portal.user.system.content;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class FileUploadForm {

    private String attachmentSize;
    private String imageSize;

    /**
     * Does nothing.
     */
    public FileUploadForm() {
        // Do nothing.
    }

    /**
     * @param attachmentSize
     *            attachment size.
     * @param imageSize
     *            image size
     */
    public FileUploadForm(String attachmentSize, String imageSize) {
        this.attachmentSize = attachmentSize == null ? null : attachmentSize.trim();
        this.imageSize = imageSize == null ? null : imageSize.trim();
    }

    /**
     * @return the path
     */
    public String getAttachmentSize() {
        return attachmentSize;
    }

    /**
     * @return the imageSize
     */
    public String getImageSize() {
        return imageSize;
    }

    /**
     * @param attachmentSize
     *            the attachmentSize to set
     */
    public void setAttachmentSize(String attachmentSize) {
        this.attachmentSize = attachmentSize == null ? null : attachmentSize.trim();
    }

    /**
     * @param imageSize
     *            the imageSize to set
     */
    public void setImageSize(String imageSize) {
        this.imageSize = imageSize == null ? null : imageSize.trim();
    }
}
