package com.communote.server.external.log4j;

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;

/**
 * Overridden Pattern Parser for the new option %i. It will be rendered the client name and id
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PatternClientParser extends PatternParser {

    /**
     * New Converter for pattern rendering
     */
    private static class ClientPatternConverter extends PatternConverter {

        /**
         * Constructor
         */
        public ClientPatternConverter() {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String convert(LoggingEvent event) {
            ClientTO client = ClientAndChannelContextHolder.getClient();
            return client != null ? client.getClientId() : "No Client";
        }

    }

    private static final char CLIENT_ID_PATTERN = 'i';

    /**
     * Constructor
     *
     * @param pattern
     *            The given output log pattern
     */
    public PatternClientParser(String pattern) {
        super(pattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalizeConverter(char c) {
        if (c == CLIENT_ID_PATTERN) {
            addConverter(new ClientPatternConverter());
        } else {
            super.finalizeConverter(c);
        }
    }

}
