package com.communote.server.core.blog;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.util.HTMLHelper;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * Tests for the note shortener
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class NoteShortenerTest {

    /**
     * test shortening a provided content. It is expected that the content is long enough so that it
     * must be shortened
     *
     * @param shortener
     *            the shortener instance to use
     * @param source
     *            the content to shorten
     * @param expected
     *            the expected content including the rml tag
     * @throws NoteStoringPreProcessorException
     *             in case the test failed
     */
    private void testContent(NoteShortener shortener, String source, String expected)
            throws NoteStoringPreProcessorException {
        // do conversion as shortener is doing them too
        expected = HTMLHelper.convertXmlSerializedHtmlToLegalHtml(expected);
        Assert.assertEquals(shortener.processNoteContent(source), expected);
    }

    /**
     * Test note shortening that is caused by linebreaks resulting from long texts.
     *
     * @throws Exception
     *             in case the test failed
     */
    public void testLineLengthBasedNoteShortening() throws Exception {
        NoteShortener shortener = new NoteShortener(4, 75, 47, 6);
        // line length caused linebreaks
        testContent(shortener,
                "<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod</p>"
                        + "<p>Line 3</p><p>Line 4</p><p>Line 5</p>",
                        "<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod</p>"
                                + "<p>Line 3</p><p>Line 4<rml/></p>");
        // line length caused linebreaks, starting with text node
        testContent(shortener,
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod"
                        + "<p>Line 3</p><p>Line 4</p><p>Line 5</p>",
                        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod"
                                + "<p>Line 3</p><p>Line 4<rml/></p>");
        // line length caused linebreaks, assert breaking after word respecting the
        // maxLengthOnLastLine setting
        testContent(
                shortener,
                "<p>Line 1</p><p>Line 2</p><p>Line 3</p>"
                        + "<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eir</p>",
                        "<p>Line 1</p><p>Line 2</p><p>Line 3</p>"
                                + "<p>Lorem ipsum dolor sit amet, consetetur<rml/></p>");
        // assert no cut if line is shorter
        Assert.assertNull(shortener
                .processNoteContent("<p>Line 1</p><p>Line 2</p><p>Line 3</p>"
                        + "<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy</p>"));
        // assert no cut if line is at limit
        Assert.assertNull(shortener
                .processNoteContent("<p>Line 1</p><p>Line 2</p><p>Line 3</p><p>"
                        + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy ei</p>"));
        // assert cut with one long line
        testContent(
                shortener,
                "<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                        + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                        + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                        + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod</p>",
                        "<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                                + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                                + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                                + "Lorem ipsum dolor sit amet,<rml/></p>");
        // assert no cut with one long line, but not exceeding limit (75 chars on last line!)
        Assert.assertNull(shortener
                .processNoteContent("<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                        + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                        + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                        + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,</p>"));
        // assert don't let whitespace sequences fool the shortener as they are not rendered
        Assert.assertNull(shortener
                .processNoteContent("<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                        + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                        + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                        + "Lorem  ipsum   dolor sit amet, consetetur   sadipscing elitr,</p>"));
        // KENMEI-6677
        // assert cut with one long line
        testContent(
                shortener,
                "<p><b><a href=\"http://www.abcde.de/portal/ShowPage.do?pid=ipd&amp;nid=865382&amp;"
                        + "stat_Mparam=int_rss_abcde-en_kommrtewten-bluntel\" target=\"_blank\">"
                        + "Vorsorgeaufwendungen - Aufteilung eines einheitlichen Sozialversicherungsbeitrags"
                        + " (Globalbeitrag)</a></b></p><br/>Ned HZT erd fertigelte, wie zur "
                        + "Streuunges dei feuerblich ber\u00FCcksichtigungsf\u00E4higen "
                        + "Abselgolweltanfungen die vom Bauermaseratigen mitelstaten ausleihzulken"
                        + " Bikliazurtersoiligungetr\u00E4ge (Modalevertr\u00E4ge) zulitengezogen "
                        + "abzulenmien sind (Tz. XV A 6 - S-3171 / 02 / 13453 :045).<br/>",
                "<p><b><a href=\"http://www.abcde.de/portal/ShowPage.do?pid=ipd&amp;nid=865382&amp;"
                        + "stat_Mparam=int_rss_abcde-en_kommrtewten-bluntel\" target=\"_blank\">"
                        + "Vorsorgeaufwendungen - Aufteilung eines einheitlichen Sozialversicherungsbeitrags"
                        + " (Globalbeitrag)</a></b></p><br/>Ned HZT erd fertigelte, wie zur "
                        + "Streuunges dei feuerblich ber\u00FCcksichtigungsf\u00E4higen "
                        + "Abselgolweltanfungen<rml/>");
        testContent(
                shortener,
                "<div><p>In diesem Beitrag sind die drei Punkte falsch gesetzt, oder?"
                        + " </p><p></p><div><div><div>Lorem ipsum dolor sit amet, consetetur"
                        + " sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et"
                        + " dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam"
                        + " et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea "
                        + "takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit "
                        + "amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor "
                        + "invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. "
                        + "At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd"
                        + " gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
                        + "</div></div></div></div>",
                        "<div><p>In diesem Beitrag sind die drei Punkte falsch gesetzt, oder?"
                                + " </p><p></p><div><div><div>Lorem ipsum dolor sit amet, consetetur"
                                + " sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et"
                                + " dolore<rml/></div></div></div></div>");
    }

    /**
     * Test that the NoteShortener correctly cuts the content and produces valid XML containing the
     * marker
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testNoteShortening() throws Exception {
        NoteShortener shortener = new NoteShortener(4, 75, 47, 6);
        Assert.assertNull(shortener.processNoteContent("<p>Line1</p>"),
                "There must be no shortening if the text is short enough");
        // cut after 4th line
        Assert.assertNull(shortener.processNoteContent("<p>Line 1</p><p>Line 2</p>"
                + "<p>Line 3</p><p>Line 4</p>"),
                "There must be no shortening if the text is short enough");
        // P caused linebreaks
        testContent(shortener, "<p>Line 1</p><p>Line 2</p><p>Line 3</p><p>Line 4</p><p>Line 5</p>",
                "<p>Line 1</p><p>Line 2</p><p>Line 3</p><p>Line 4<rml/></p>");
        // P caused linebreaks, with empty lines
        testContent(shortener, "<p>Line 1</p><p></p><p></p><p></p><p>Line 5</p>",
                "<p>Line 1</p><p></p><p></p><p><rml/></p>");
        // UL caused linebreaks
        testContent(shortener, "<p>Line 1</p><ul><li>Line 2</li>"
                + "<li>Line 3</li><li>Line 4</li><li>Line 5</li></ul>",
                "<p>Line 1</p><ul><li>Line 2</li><li>Line 3</li><li>Line 4<rml/></li></ul>");
        // OL caused linebreaks
        testContent(shortener, "<p>Line 1</p><ol><li>Line 2</li>"
                + "<li>Line 3</li><li>Line 4</li><li>Line 5</li></ol>",
                "<p>Line 1</p><ol><li>Line 2</li><li>Line 3</li><li>Line 4<rml/></li></ol>");
        // stacked UL and OL
        testContent(shortener, "<p>Line 1</p><ul><li>Line 2</li>"
                + "<ol><li>Line 3</li><li>Line 4</li></ol><li>Line 5</li></ul>",
                "<p>Line 1</p><ul><li>Line 2</li><ol><li>Line 3</li><li>Line 4<rml/></li></ol></ul>");
        // BR caused linebreaks
        testContent(shortener, "<p>Line 1</p><p>Line 2<br/>Line 3<br/>Line 4<br/>Line 5</p>",
                "<p>Line 1</p><p>Line 2<br/>Line 3<br/>Line 4<rml/></p>");
        testContent(shortener, "Line1<br/>Line2<br/>Line3<br/>Line4<br/>Line5",
                "Line1<br/>Line2<br/>Line3<br/>Line4<rml/>");
        testContent(shortener, "<br/>Line2<br/>Line3<br/>Line4<br/>Line5",
                "<br/>Line2<br/>Line3<br/>Line4<rml/>");
        // index out of range on last line bug
        testContent(shortener,
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod"
                        + " tempor invidunt ut labore et Lorem ipsum dolor sit amet, consetetur "
                        + "sadipscing elitr, sed diam nonumy Lorem ipsum dolor sit amet, "
                        + "consetetur sadipscing elitr, sed diam nonumy Lorem ipsum dolor <b>sit"
                        + " amet, consetetur sadipscing elitr, sed diam nonumy</b>",
                        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod"
                                + " tempor invidunt ut labore et Lorem ipsum dolor sit amet, consetetur "
                                + "sadipscing elitr, sed diam nonumy Lorem ipsum dolor sit amet, "
                                + "consetetur sadipscing elitr, sed diam nonumy Lorem ipsum dolor <rml/>");
    }
}
