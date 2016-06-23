package com.communote.common.util;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class HTMLHelperTest {
    /**
     * Regression test for the HTML to plaintext conversion with some special content from HTML
     * e-mails which caused NoSuchElementExceptions while converting OL or UL elements.
     *
     * @throws Exception
     *             In case the test failed
     */
    @Test
    public void testHtmlToPlaintext() throws Exception {

        try (InputStream inputStream = HTMLHelperTest.class
                .getResourceAsStream("/HTMLHelperTest/NoteFromEMail.html")) {
            Assert.assertNotNull(inputStream);
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, "UTF-8");
            String htmlContent = writer.toString();
            HTMLHelper.htmlToPlaintext(htmlContent);
            HTMLHelper.htmlToPlaintextExt(htmlContent, false);
            HTMLHelper.htmlToPlaintextExt(htmlContent, true);
        }

    }

    /**
     * Test whether the HTML helper method convertXmlSerializedHtmlToLegalHtml correctly removes
     * elements that do not have a body.
     */
    @Test
    public void testRemoveEmptyNodesByConvertXmlSerializedHtmlToLegalHtml() {
        // test removal of certain tags that do not have a body
        String[] tags = new String[] { "b", "strong", "em", "i", "u", "li", "ul", "ol", "a", "div",
        "blockquote" };
        for (String tag : tags) {
            String out = HTMLHelper.convertXmlSerializedHtmlToLegalHtml("lorem ipsum <" + tag
                    + "/>test");
            Assert.assertEquals(out, "lorem ipsum test");
            out = HTMLHelper.convertXmlSerializedHtmlToLegalHtml("lorem ipsum <" + tag + "></"
                    + tag + ">test");
            Assert.assertEquals(out, "lorem ipsum test");
        }
        // whitespace robustness
        String out = HTMLHelper.convertXmlSerializedHtmlToLegalHtml("lorem ipsum <b  />test");
        Assert.assertEquals(out, "lorem ipsum test");
        out = HTMLHelper.convertXmlSerializedHtmlToLegalHtml("lorem ipsum <b ></b>test");
        Assert.assertEquals(out, "lorem ipsum test");
        // test stacked empty elements
        out = HTMLHelper.convertXmlSerializedHtmlToLegalHtml("lorem ipsum <ol><li/></ol>test");
        Assert.assertEquals(out, "lorem ipsum test");
        out = HTMLHelper.convertXmlSerializedHtmlToLegalHtml("lorem ipsum <ul><li></li></ul>test");
        Assert.assertEquals(out, "lorem ipsum test");
        out = HTMLHelper
                .convertXmlSerializedHtmlToLegalHtml("lorem ipsum <div><div><div/></div></div>test");
        Assert.assertEquals(out, "lorem ipsum test");
        // attribute robustness
        out = HTMLHelper
                .convertXmlSerializedHtmlToLegalHtml("lorem ipsum <a href=\"example.com/test/\"/>test");
        Assert.assertEquals(out, "lorem ipsum test");
        out = HTMLHelper
                .convertXmlSerializedHtmlToLegalHtml("lorem ipsum <a href=\"example.com/test/\" target=\"blank\"></a>test");
        Assert.assertEquals(out, "lorem ipsum test");
        // negative test
        out = HTMLHelper.convertXmlSerializedHtmlToLegalHtml("lorem ipsum <div>content</div>test");
        Assert.assertEquals(out, "lorem ipsum <div>content</div>test");
    }
}
