package com.communote.server.core.common.velocity.tools;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RenderTool {

    /**
     * MIME types for Word files
     */
    private final Set<String> wordTypes = new HashSet<String>();

    /**
     * MIME types for Excel files
     */
    private final Set<String> excelTypes = new HashSet<String>();

    /**
     * MIME types for PowerPoint files
     */
    private final Set<String> powerPointTypes = new HashSet<>();

    /**
     * MIME types for coding files
     */
    private final Set<String> codeTypes = new HashSet<>();

    /**
     * Create a new renderer.
     */
    public RenderTool() {
        wordTypes.add("application/msword");
        wordTypes
                .add("application/vnd.ms-officetheme docm=application/vnd.ms-word.document.macroEnabled.12");
        wordTypes.add("application/vnd.ms-word.template.macroEnabled.12");
        wordTypes.add("application/vnd.oasis.opendocument.text");
        wordTypes.add("application/vnd.oasis.opendocument.text-master");
        wordTypes.add("application/vnd.oasis.opendocument.text-template");
        wordTypes.add("application/vnd.oasis.opendocument.text-web");
        wordTypes.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        wordTypes.add("application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        wordTypes.add("application/rtf");
        wordTypes.add("text/rtf");

        excelTypes.add("application/excel");
        excelTypes.add("application/msexcel");
        excelTypes.add("application/vnd.ms-excel");
        excelTypes.add("application/vnd.ms-excel.addin.macroEnabled.12");
        excelTypes.add("application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        excelTypes.add("application/vnd.ms-excel.sheet.macroEnabled.12");
        excelTypes.add("application/vnd.ms-excel.template.macroEnabled.12");
        excelTypes.add("application/vnd.oasis.opendocument.presentation");
        excelTypes.add("application/vnd.oasis.opendocument.presentation-template");
        excelTypes.add("application/vnd.oasis.opendocument.spreadsheet");
        excelTypes.add("application/vnd.oasis.opendocument.spreadsheet-template");
        excelTypes.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        excelTypes.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        excelTypes.add("application/vnd.openxmlformats-officedocument.spreadsheetml.template");

        powerPointTypes.add("application/mspowerpoint");
        powerPointTypes.add("application/vnd.ms-powerpoint");
        powerPointTypes.add("application/vnd.ms-powerpoint.addin.macroEnabled.12");
        powerPointTypes.add("application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        powerPointTypes.add("application/vnd.ms-powerpoint.slide.macroEnabled.12");
        powerPointTypes.add("application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
        powerPointTypes.add("application/vnd.oasis.opendocument.presentation");
        powerPointTypes.add("application/vnd.oasis.opendocument.presentation-template");
        powerPointTypes
                .add("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        powerPointTypes.add("application/vnd.openxmlformats-officedocument.presentationml.slide");
        powerPointTypes
                .add("application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        powerPointTypes
                .add("application/vnd.openxmlformats-officedocument.presentationml.template");

        codeTypes.add("application/xhtml+xml");
        codeTypes.add("application/xml");
        codeTypes.add("text/xml");
        codeTypes.add("text/xml-external-parsed-entity");
        codeTypes.add("application/x-httpd-php");
        codeTypes.add("application/x-javascript");
        codeTypes.add("application/javascript");
        codeTypes.add("text/css");
        codeTypes.add("text/html");
    }

    /**
     * @return The CSS class for the requested mimeType.
     */
    public String getAttachmentClass(String mimeType) {
        if (mimeType != null) {
            if (wordTypes.contains(mimeType)) {
                return "cn-type-word";
            } else if (excelTypes.contains(mimeType)) {
                return "cn-type-excel";
            } else if (powerPointTypes.contains(mimeType)) {
                return "cn-type-pp";
            } else if (codeTypes.contains(mimeType)) {
                return "cn-type-code";
            } else if ("application/pdf".contains(mimeType)) {
                return "cn-type-pdf";
            } else if (mimeType.startsWith("image/")) {
                return "cn-type-image";
            } else if (mimeType.equals("text/plain")) {
                return "cn-type-text";
            }
        }
        return "cn-type-undefined";
    }

    /**
     * Returns a human-readable version of the file size.
     *
     * @param size
     *            The number of bytes.
     * @return a human-readable file size including units.
     */
    public String getHumanReadableFileSize(Long size) {
        return FileUtils.byteCountToDisplaySize(size);
    }

    /**
     * Returns a human-readable version of the file size.
     *
     * @param size
     *            The number of bytes.
     * @return a human-readable file size including units.
     */
    public String getHumanReadableFileSize(Number size) {
        return FileUtils.byteCountToDisplaySize(size.longValue());
    }
}
