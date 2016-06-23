package com.communote.server.core.vo.query.converters;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import com.communote.common.util.PageableList;
import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentToAttachmentDataQueryResultConverter extends
        DirectQueryResultConverter<Attachment, AttachmentData> {

    // TODO this class should be in same package as AttachmentData, as soon as we cleaned the
    // package layout
    /**
     * Comparator to sort the attachments alphabetically in a locale-sensitive way by filename.
     * 
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     * 
     */
    private class AttachmentDataFileNameComparator implements Comparator<AttachmentData> {
        final Collator collator;

        /**
         * Create a new comparator
         * 
         * @param locale
         *            the locale to use. If null the default locale of client will be used.
         */
        public AttachmentDataFileNameComparator(Locale locale) {
            if (locale == null) {
                locale = ClientHelper.getDefaultLanguage();
            }
            this.collator = Collator.getInstance(locale);
            // ignore case
            this.collator.setStrength(Collator.SECONDARY);
        }

        @Override
        public int compare(AttachmentData o1, AttachmentData o2) {
            return this.collator.compare(o1.getFileName(), o2.getFileName());
        }
    }

    private final Locale locale;

    /**
     * Create a new converter which will return the converted list sorted by filename.
     * 
     * @param locale
     *            the locale to use for sorting
     */
    public AttachmentToAttachmentDataQueryResultConverter(Locale locale) {
        this.locale = locale;
    }

    @Override
    public boolean convert(Attachment source, AttachmentData target) {
        target.setId(source.getId());
        target.setContentId(source.getContentIdentifier());
        target.setFileName(source.getName());
        target.setMimeTyp(source.getContentType());
        target.setRepositoryId(source.getRepositoryIdentifier());
        target.setSize(source.getSize());
        if (source.getNote() != null) {
            target.setNoteId(source.getNote().getId());
        }
        return true;
    }

    @Override
    public PageableList<AttachmentData> convert(PageableList<Attachment> queryResult) {
        PageableList<AttachmentData> result = super.convert(queryResult);
        sortByName(result);
        return result;
    }

    @Override
    public AttachmentData create() {
        return new AttachmentData();
    }

    /**
     * Sort the attachments alphabetically by filename using the locale passed to the constructor.
     * 
     * @param attachments
     *            the attachments to sort
     */
    public void sortByName(PageableList<AttachmentData> attachments) {
        if (attachments.size() > 1) {
            Comparator<AttachmentData> fileNameComparator = new AttachmentDataFileNameComparator(
                    locale);
            Collections.sort(attachments, fileNameComparator);
        }
    }
}
