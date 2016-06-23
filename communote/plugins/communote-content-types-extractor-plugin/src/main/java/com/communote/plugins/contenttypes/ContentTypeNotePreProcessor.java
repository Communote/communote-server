package com.communote.plugins.contenttypes;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.Orderable;
import com.communote.plugins.mediaparser.RichMediaExtractor;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.persistence.resource.AttachmentDao;

/**
 * NoteContentProcessor, which extracts the content type out of the note.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component
@Provides
@Instantiate(name = "ContentTypeNotePreProcessor")
public class ContentTypeNotePreProcessor implements NoteStoringImmutableContentPreProcessor,
        Orderable {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentTypeNotePreProcessor.class);

    private final static Map<ContentTypes, StringPropertyTO> DEFAULT_PROPERTIES =
            new HashMap<ContentTypes, StringPropertyTO>();

    private final static Set<String> VALID_DOCUMENT_TYPES = new HashSet<String>();

    private final static RichMediaExtractor RICH_MEDIA_EXTRACTOR = new RichMediaExtractor();

    private final static Map<ContentTypes, String[]> PROPERTIES_TO_TAGS = new HashMap<ContentTypes, String[]>();

    static {
        DEFAULT_PROPERTIES.put(ContentTypes.DOCUMENT,
                new StringPropertyTO(null, PropertyManagement.KEY_GROUP,
                        ContentTypes.DOCUMENT.getPropertyKey(), new Date()));
        DEFAULT_PROPERTIES.put(ContentTypes.IDEA,
                new StringPropertyTO(null, PropertyManagement.KEY_GROUP,
                        ContentTypes.IDEA.getPropertyKey(), new Date()));
        DEFAULT_PROPERTIES.put(ContentTypes.QUESTION,
                new StringPropertyTO(null, PropertyManagement.KEY_GROUP,
                        ContentTypes.QUESTION.getPropertyKey(), new Date()));
        DEFAULT_PROPERTIES.put(ContentTypes.IMAGE,
                new StringPropertyTO(null, PropertyManagement.KEY_GROUP,
                        ContentTypes.IMAGE.getPropertyKey(), new Date()));
        DEFAULT_PROPERTIES.put(ContentTypes.LINK,
                new StringPropertyTO(null, PropertyManagement.KEY_GROUP,
                        ContentTypes.LINK.getPropertyKey(), new Date()));
        DEFAULT_PROPERTIES.put(ContentTypes.RICH_MEDIA,
                new StringPropertyTO(null, PropertyManagement.KEY_GROUP,
                        ContentTypes.RICH_MEDIA.getPropertyKey(), new Date()));
        PROPERTIES_TO_TAGS.put(ContentTypes.IDEA, new String[] { "idea", "idee" });
        PROPERTIES_TO_TAGS.put(ContentTypes.QUESTION, new String[] { "question", "frage" });
        String[] validDocumentTypes = {
                "application/excel",
                "application/msexcel",
                "application/mspowerpoint",
                "application/msword",
                "application/pdf",
                "application/rtf",
                "application/vnd.ms-excel",
                "application/vnd.ms-excel.addin.macroEnabled.12",
                "application/vnd.ms-excel.sheet.binary.macroEnabled.12",
                "application/vnd.ms-excel.sheet.macroEnabled.12",
                "application/vnd.ms-excel.template.macroEnabled.12",
                "application/vnd.ms-officetheme docm=application/vnd.ms-word.document.macroEnabled.12",
                "application/vnd.ms-outlook",
                "application/vnd.ms-powerpoint",
                "application/vnd.ms-powerpoint.addin.macroEnabled.12",
                "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
                "application/vnd.ms-powerpoint.slide.macroEnabled.12",
                "application/vnd.ms-powerpoint.slideshow.macroEnabled.12",
                "application/vnd.ms-word.template.macroEnabled.12",
                "application/vnd.oasis.opendocument.chart",
                "application/vnd.oasis.opendocument.database",
                "application/vnd.oasis.opendocument.formula",
                "application/vnd.oasis.opendocument.graphics",
                "application/vnd.oasis.opendocument.graphics-template",
                "application/vnd.oasis.opendocument.image",
                "application/vnd.oasis.opendocument.presentation",
                "application/vnd.oasis.opendocument.presentation-template",
                "application/vnd.oasis.opendocument.spreadsheet",
                "application/vnd.oasis.opendocument.spreadsheet-template",
                "application/vnd.oasis.opendocument.text",
                "application/vnd.oasis.opendocument.text-master",
                "application/vnd.oasis.opendocument.text-template",
                "application/vnd.oasis.opendocument.text-web",
                "application/vnd.openxmlformats",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.openxmlformats-officedocument.presentationml.slide",
                "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
                "application/vnd.openxmlformats-officedocument.presentationml.template",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
                "application/x-msg",
                "image/vnd.microsoft.icon",
                "text/plain",
                "text/rtf"
        };
        for (String validDocumentType : validDocumentTypes) {
            VALID_DOCUMENT_TYPES.add(validDocumentType);
        }

    }

    /**
     * @return 0
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * Creates a property for the given type.
     *
     * @param type
     *            The type.
     * @return The property.
     */
    private StringPropertyTO getProperty(ContentTypes type) {
        StringPropertyTO property = new StringPropertyTO(type.getPropertyValue(),
                PropertyManagement.KEY_GROUP, type.getPropertyKey(), new Date());
        return property;
    }

    @Override
    public boolean isProcessAutosave() {
        // it's enough to extract the content type when publishing
        return false;
    }

    /**
     * This processes the notes attachments.
     *
     * @param note
     *            The note.
     * @param properties
     *            The map of properties.
     */
    private void processAttachments(NoteStoringTO note,
            Map<ContentTypes, StringPropertyTO> properties) {
        if (note.getAttachmentIds() == null || note.getAttachmentIds().length == 0) {
            return;
        }
        properties.put(ContentTypes.ATTACHMENT, getProperty(ContentTypes.ATTACHMENT));
        AttachmentDao attachmentDao = ServiceLocator.findService(AttachmentDao.class);
        for (Long attachmentId : note.getAttachmentIds()) {
            try {
                Attachment attachment = attachmentDao.load(attachmentId);
                String mimeType = attachment.getContentType();
                if (mimeType.startsWith("image/")
                        || attachment.getName().endsWith(".ai")) {
                    properties.put(ContentTypes.IMAGE,
                            getProperty(ContentTypes.IMAGE));
                } else if (mimeType.startsWith("video/")
                        || attachment.getName().endsWith(".ai")) {
                    properties.put(ContentTypes.RICH_MEDIA,
                            getProperty(ContentTypes.RICH_MEDIA));
                } else if (VALID_DOCUMENT_TYPES.contains(mimeType)) {
                    properties.put(ContentTypes.DOCUMENT,
                            getProperty(ContentTypes.DOCUMENT));
                }
            } catch (Exception e) {
                LOGGER.error("There was a problem with attachment {}.", attachmentId, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteStoringTO process(NoteStoringTO note) throws NoteStoringPreProcessorException {
        Map<ContentTypes, StringPropertyTO> properties = new HashMap<ContentTypes, StringPropertyTO>(
                DEFAULT_PROPERTIES);
        if (note.getContent().contains("<a href")) {
            properties.put(ContentTypes.LINK, getProperty(ContentTypes.LINK));
        }
        if (!RICH_MEDIA_EXTRACTOR.getRichMediaDescriptions(note.getContent()).isEmpty()) {
            properties.put(ContentTypes.RICH_MEDIA, getProperty(ContentTypes.RICH_MEDIA));
        }
        processAttachments(note, properties);
        processTags(note.getUnparsedTags(), properties);
        note.getProperties().addAll(properties.values());
        return note;
    }

    /**
     * Extracts content types out of the notes tags.
     *
     * @param tags
     *            The tags.
     * @param properties
     *            The properties.
     */
    private void processTags(String tags, Map<ContentTypes, StringPropertyTO> properties) {
        if (StringUtils.isBlank(tags)) {
            return;
        }
        Set<String> tagsSet = new HashSet<String>();
        tagsSet.addAll(Arrays.asList(StringUtils.split(tags.toLowerCase(), ',')));
        entryLoop: for (Entry<ContentTypes, String[]> entry : PROPERTIES_TO_TAGS.entrySet()) {
            for (String tag : entry.getValue()) {
                if (tagsSet.contains(tag.toLowerCase())) {
                    properties.put(entry.getKey(), getProperty(entry.getKey()));
                    continue entryLoop;
                }
            }
        }
    }
}
