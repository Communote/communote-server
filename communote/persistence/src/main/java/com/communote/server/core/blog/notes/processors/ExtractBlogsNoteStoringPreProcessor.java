package com.communote.server.core.blog.notes.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.communote.common.util.HTMLHelper;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringEditableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.core.blog.OnlyCrosspostMarkupException;
import com.communote.server.model.note.Note;

/**
 * Preprocessor which searches the content for strings that are syntactically correct blog aliases
 * and start with an '&amp;' character. The found aliases are added to the storing TO and the
 * strings are removed from the note content.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ExtractBlogsNoteStoringPreProcessor implements NoteStoringEditableContentPreProcessor {

    private static final String BLOG_PREFIX = "(?:&|(?:&amp;))";

    // matches words without whitespaces (including non-breaking space)
    private final static Pattern BLOG_PATTERN = Pattern.compile("(^|[\\s\\u00A0;,>])" + BLOG_PREFIX
            + "([\\w-]+)(?=([\\s\\u00A0.,<]|$))");

    /**
     * Extract the blog aliases from the content of the note.
     *
     * @param noteStoringTO
     *            the TO describing the note
     * @return whether blog aliases were found
     */
    private boolean extractBlogsFromNote(NoteStoringTO noteStoringTO) {
        boolean blogsFound = false;
        Matcher matcher = BLOG_PATTERN.matcher(noteStoringTO.getContent());
        while (matcher.find()) {
            noteStoringTO.getAdditionalBlogs().add(matcher.group(2));
            blogsFound = true;
        }
        return blogsFound;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Override
    public boolean isProcessAutosave() {
        // crosspost creation should only happen when publishing
        return false;
    }

    @Override
    public NoteStoringTO process(NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        if (noteStoringTO.getAdditionalBlogs() != null) {
            if (extractBlogsFromNote(noteStoringTO)) {
                // remove the crosspost syntax
                String newContent = BLOG_PATTERN.matcher(noteStoringTO.getContent()).replaceAll(
                        "$1");
                // test if the new content is empty
                if (!HTMLHelper.containsNonEmptyTextNodes(newContent)) {
                    throw new OnlyCrosspostMarkupException(
                            "Note contains only crosspost markup and no real content");
                }
                noteStoringTO.setContent(newContent);
            }
        }
        return noteStoringTO;
    }

    @Override
    public NoteStoringTO processEdit(Note noteToEdit, NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        return process(noteStoringTO);
    }
}
