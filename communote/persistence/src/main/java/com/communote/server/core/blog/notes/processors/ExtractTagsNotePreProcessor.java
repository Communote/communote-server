package com.communote.server.core.blog.notes.processors;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.core.tag.TagParser;
import com.communote.server.core.tag.TagParserFactory;
import com.communote.server.model.note.Note;

/**
 * This processor extracts tags from the note content and adds them to the meta data of the note.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ExtractTagsNotePreProcessor implements NoteStoringImmutableContentPreProcessor {
    /** Pattern for extracting tags from plain content */
    public final static Pattern TAG_PATTERN = Pattern
            .compile("(^|[\\s\\u00A0;,(>])#((?:\"([^\"<>',]+)\")|(?:[[\\P{Punct}&&\\P{Space}&&\\P{Cntrl}]_.-]"
                    + "*[\\P{Punct}&&\\P{Space}&&\\P{Cntrl}]))");

    /**
     * @return {@link NoteStoringImmutableContentPreProcessor#DEFAULT_ORDER}
     */
    @Override
    public int getOrder() {
        return NoteStoringImmutableContentPreProcessor.DEFAULT_ORDER;
    }

    @Override
    public boolean isProcessAutosave() {
        // do not extract tags when doing an autosave to avoid creation of incomplete tags if user
        // is still typing
        return false;
    }

    @Override
    public NoteStoringTO process(NoteStoringTO note) {
        Set<String> extractedTags = new HashSet<String>();
        Matcher matcher = TAG_PATTERN.matcher(note.getContent());
        while (matcher.find()) {
            if (matcher.group(3) != null) {
                extractedTags.add(StringEscapeUtils.unescapeXml(matcher.group(3)));
            } else {
                extractedTags.add(StringEscapeUtils.unescapeXml(matcher.group(2)));
            }
        }
        if (extractedTags.size() > 0) {
            StringBuilder unparsedTags = new StringBuilder();
            TagParser tagParser = TagParserFactory.instance().getDefaultTagParser();
            int i = 1;
            for (String tag : extractedTags) {
                unparsedTags.append(tag);
                if (i < extractedTags.size()) {
                    unparsedTags.append(tagParser.getSeparator());
                }
                i++;
            }
            String finalUnparsedTags = tagParser.combineTags(note.getUnparsedTags(),
                    unparsedTags.toString());
            note.setUnparsedTags(finalUnparsedTags);
        }
        return note;
    }

    @Override
    public NoteStoringTO processEdit(Note noteToEdit, NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        return process(noteStoringTO);
    }
}
