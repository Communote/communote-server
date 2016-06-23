package com.communote.plugins.mediaparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.communote.plugins.mediaparser.mediatype.RichMediaType;
import com.communote.plugins.mediaparser.mediatype.RichMediaTypes;

/**
 * This class extracts media types from a given text.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class RichMediaExtractor {

    private static final Pattern URL_PATTERN = Pattern.compile("<a[^>]+href=\"([^\"]+)\"[^>]*>",
            Pattern.CASE_INSENSITIVE);

    private final Collection<RichMediaType> supportedRichMediaSources = new ArrayList<RichMediaType>();
    {
        supportedRichMediaSources.add(RichMediaTypes.YOUTUBE);
        supportedRichMediaSources.add(RichMediaTypes.VIMEO);
    }

    /**
     * @param text
     *            The text to check.
     * @return Found types.
     */
    public Collection<RichMediaDescription> getRichMediaDescriptions(String text) {
        Matcher matcher = URL_PATTERN.matcher(text);
        Collection<RichMediaDescription> types = new ArrayList<RichMediaDescription>();
        while (matcher.find()) {
            String link = matcher.group(1);
            innerLoop: for (RichMediaType richMediaType : supportedRichMediaSources) {
                RichMediaDescription result = richMediaType.extractRichMediaDescription(link);
                if (result != null) {
                    types.add(result);
                    break innerLoop;
                }
            }
        }
        return types;
    }
}
