package com.communote.server.web.commons.resource.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter match processor that corrects the relative path of an <code>url( ... )</code> element in a
 * CSS file so that referenced resources (mainly images) can be resolved when the concatenated file
 * is delivered to the client
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CssUrlRewriter extends ResourceFilterMatchProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CssUrlRewriter.class);
    private final Pattern cssRelativeUrlPattern;

    /**
     * Create a new normalizer
     */
    public CssUrlRewriter() {
        cssRelativeUrlPattern = Pattern
                .compile("url\\(\\s*['\"]?(\\.\\./[^\\s'\"\\)]+)\\s*['\"]?\\)");
    }

    @Override
    public String processMatch(String match, String startDelimiter, String endDelimiter) {
        LOGGER.trace("Checking URL element {}", match);
        Matcher urlMatcher = cssRelativeUrlPattern.matcher(match);
        if (urlMatcher.matches()) {
            String resourceLocation = getProcessedResource().getResourceLocation();
            String resourcePath = resourceLocation.substring(0,
                    resourceLocation.lastIndexOf('/') + 1);
            URI uri;
            try {
                // TODO make prefixes configurable?
                if (getProcessedResource().isCoreResource()) {
                    // core resources can only reference other core resources (mainly images) which
                    // are currently not served by dispatcher servlet (no microblog/global), thus
                    // prepend 3 '..' to navigate out from microblog/global/styles/packed.css where
                    // the packed CSS download controller is registered
                    uri = new URI("../../../" + resourcePath + urlMatcher.group(1));
                } else {
                    // resourcePath starts with bundle name and resource download path is
                    // microblog/client-id/plugins/resourcePath , '..' is required because
                    // packed.css is registered at microblog/client-id/styles/
                    uri = new URI("../plugins/" + resourcePath + urlMatcher.group(1));
                }
            } catch (URISyntaxException e) {
                LOGGER.error("Normalizing URL element " + match + " found in resource "
                        + resourceLocation + " failed", e);
                return null;
            }
            return match.substring(0, urlMatcher.start(1)) + uri.normalize().toString()
                    + match.substring(urlMatcher.end(1));
        } else {
            LOGGER.trace("URL rewriting not required");
            return match;
        }

    }
}
