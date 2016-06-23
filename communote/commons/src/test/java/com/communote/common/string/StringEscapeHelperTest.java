package com.communote.common.string;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link StringEscapeHelper}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class StringEscapeHelperTest {

    /**
     * test {@link StringEscapeHelper#escapeNonWordCharacters(String)}
     */
    @Test
    public void testEscapeNonWordCharacters() {
        Assert.assertEquals(
                StringEscapeHelper
                .escapeNonWordCharacters("abcdefghijklmnopqrstuvwxyz-012345.6789ABCDEFGHIJKLMN_OPQRSTUVWXYZ"),
                "abcdefghijklmnopqrstuvwxyz-012345.6789ABCDEFGHIJKLMN_OPQRSTUVWXYZ");
        Assert.assertEquals(StringEscapeHelper.escapeNonWordCharacters("?$ab!\"A;:"), "__ab__A__");
        Assert.assertEquals(StringEscapeHelper.escapeNonWordCharacters("({ab[@0*+"), "__ab__0__");
        Assert.assertEquals(
                StringEscapeHelper.escapeNonWordCharacters("^%Q&/P)]T=\\U~#Z'|F<>S`,R "),
                "__Q__P__T__U__Z__F__S__R_");
        Assert.assertEquals(StringEscapeHelper.escapeNonWordCharacters("L\u00f6we"), "L_we");
    }

    /**
     * Test for {@link StringEscapeHelper#escapeXml(String)}
     */
    @Test
    public void testEscapeXml() {
        Assert.assertEquals(StringEscapeHelper.escapeXml(""), "");
        Assert.assertEquals(StringEscapeHelper.escapeXml("Test string without XML."),
                "Test string without XML.");
        Assert.assertEquals(StringEscapeHelper.escapeXml("Testing & character"),
                "Testing &amp; character");
        Assert.assertEquals(StringEscapeHelper.escapeXml("Testing \" character"),
                "Testing &#034; character");
        Assert.assertEquals(StringEscapeHelper.escapeXml("Testing &,:'<;> characters"),
                "Testing &amp;,:&#039;&lt;;&gt; characters");
        Assert.assertEquals(StringEscapeHelper.escapeXml("double escape: &amp;,:&#039;&lt;;&gt;"),
                "double escape: &amp;amp;,:&amp;#039;&amp;lt;;&amp;gt;");
        Assert.assertEquals(
                StringEscapeHelper.escapeXml("<xml><inner attr=\"test\">content</inner></xml>"),
                "&lt;xml&gt;&lt;inner attr=&#034;test&#034;&gt;content&lt;/inner&gt;&lt;/xml&gt;");
    }
}
