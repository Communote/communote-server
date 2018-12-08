package com.communote.plugins.mediaparser.mediatype;

import static com.communote.plugins.mediaparser.mediatype.RichMediaTypes.VIMEO;
import static com.communote.plugins.mediaparser.mediatype.RichMediaTypes.YOUTUBE;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link RichMediaTypes}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RichMediaTypesTest {

    /**
     * Test for {@link RichMediaTypes#VIMEO}
     */
    @Test
    public void testVimeo() {
        Assert.assertEquals(VIMEO.extractRichMediaDescription("http://vimeo.com/12345678")
                .getMediaId(), "12345678");
        Assert.assertEquals(VIMEO.extractRichMediaDescription(
                "http://player.vimeo.com/video/12345678?title=0&amp;")
                .getMediaId(), "12345678");
        Assert.assertEquals(
                VIMEO.extractRichMediaDescription(
                        "http://vimeo.com/groups/electronika/videos/12345678")
                        .getMediaId(), "12345678");
        Assert.assertNotNull(VIMEO
                .extractRichMediaDescription("http://vimeo.com/channels/quinzaine/23916731"));
        Assert.assertNull(VIMEO.extractRichMediaDescription("http://vimeo.com/video/12345678"));
        Assert.assertNull(VIMEO.extractRichMediaDescription("http://vimeo.com/mohazima"));
        Assert.assertNull(VIMEO.extractRichMediaDescription("anything"));
    }

    /**
     * Test for {@link RichMediaTypes#YOUTUBE}
     */
    @Test
    public void testYouTube() {
        Assert.assertEquals(YOUTUBE
                .extractRichMediaDescription("http://www.youtube.com/watch?v=SeJymXp1r3I")
                .getMediaId(), "SeJymXp1r3I");
        Assert.assertEquals(YOUTUBE
                .extractRichMediaDescription("http://youtu.be/SeJymXp1r3I").getMediaId(),
                "SeJymXp1r3I");
        Assert.assertEquals(YOUTUBE
                .extractRichMediaDescription("http://www.youtube.com/embed/SeJymXp1r3I")
                .getMediaId(), "SeJymXp1r3I");
        Assert.assertNull(YOUTUBE.extractRichMediaDescription("anything"));
        Assert.assertNull(YOUTUBE
                .extractRichMediaDescription("http://www.youtube.com/?gl=DE&hl=de"));
    }
}
