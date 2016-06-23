package com.communote.server.core.blog.notes.processors;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RemoveUnsupportedMarkupNotePreProcessorTest {

    /**
     * Regression for KENMEI-5902: some attributes are not correctly removed from OL or UL elements
     * which later caused a NoSuchElementException in plaintext conversion
     *
     * @throws NoteStoringPreProcessorException
     *             in case the test failed
     * @throws IOException
     *             in case the test failed
     */
    @Test
    public void testForKENMEI5902() throws NoteStoringPreProcessorException, IOException {
        String content = "<body><p><ol type=\"1\"><li style=\"disply: block;\">Hello</li></ol></p></body>";
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent(content);
        new RemoveUnsupportedMarkupNotePreProcessor().process(noteStoringTO);
        Assert.assertEquals(noteStoringTO.getContent(), "<p><ol><li>Hello</li></ol></p>");

        content = "<body><p><ul type=\"1\"><li style=\"mso-margin-top-alt:auto;mso-margin-bottom-alt:auto;mso-list:l0 level1 lfo1\">Hello</li></ul></p></body>";
        noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent(content);
        new RemoveUnsupportedMarkupNotePreProcessor().process(noteStoringTO);
        Assert.assertEquals(noteStoringTO.getContent(), "<p><ul><li>Hello</li></ul></p>");
    }

    /**
     * Test that style tag is correctly removed
     * 
     * @throws NoteStoringPreProcessorException
     * @throws IOException
     */
    @Test
    public void testStyleTagRemoval() throws NoteStoringPreProcessorException, IOException {
        String content = "<body><style>div {color:red;}</style><p>Hello</p></body>";
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent(content);
        noteStoringTO.setContentType(NoteContentType.HTML);
        new RemoveUnsupportedMarkupNotePreProcessor().process(noteStoringTO);
        Assert.assertEquals(noteStoringTO.getContent(), "<p>Hello</p>");
    }
}
