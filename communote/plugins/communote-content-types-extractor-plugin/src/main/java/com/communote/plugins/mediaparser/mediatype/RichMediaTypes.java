package com.communote.plugins.mediaparser.mediatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.communote.plugins.mediaparser.RichMediaDescription;

/**
 * Property constants that represents types of rich media the plugin supports.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum RichMediaTypes implements RichMediaType {
    /** YouTube videos */
    YOUTUBE("youtube.com.+(?:v=|embed/)([^?&]+)", "youtu.be/([a-zA-Z0-9_-]+)"),

    /** vimeo videos */
    VIMEO("vimeo.com/?([0-9]+)",
            "player.vimeo.com/video/?([0-9]+)", "vimeo.com/groups/(?:\\w+)/videos/(\\d+)",
            "vimeo.com/channels/(?:\\w+)/(\\d+)");

    private final Collection<Pattern> patterns = new ArrayList<Pattern>();

    /**
     * Constructor.
     *
     * @param patterns
     *            The patterns this media type works for. The pattern needs to have one capturing
     *            group which extracts the ID of the media
     */
    RichMediaTypes(String... patterns) {
        for (String pattern : patterns) {
            this.patterns.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RichMediaDescription extractRichMediaDescription(String link) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(link);
            while (matcher.find()) {
                String mediaId = matcher.group(1);
                return new RichMediaDescription(mediaId, this.toString());
            }
        }
        return null;
    }

    /**
     * @return the patterns
     */
    public Collection<Pattern> getPatterns() {
        return patterns;
    }

    /**
     * {@inheritDoc}
     *
     * @return this.
     */
    @Override
    public String getTypeId() {
        return this.toString();
    }
}