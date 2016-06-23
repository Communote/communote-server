package com.communote.server.test.external;

import com.communote.server.core.external.ExternalObjectSource;
import com.communote.server.core.external.ExternalObjectSourceConfiguration;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MockExternalObjectSource implements ExternalObjectSource {

    public static class MockExternalObjectSourceConfiguration implements
    ExternalObjectSourceConfiguration {

        private int numberOfMaximumExternalObjectsPerTopic = 0;

        @Override
        public int getNumberOfMaximumExternalObjectsPerTopic() {
            return numberOfMaximumExternalObjectsPerTopic;
        }

        public void setNumberOfMaximumExternalObjectsPerTopic(
                int numberOfMaximumExternalObjectsPerTopic) {
            this.numberOfMaximumExternalObjectsPerTopic = numberOfMaximumExternalObjectsPerTopic;
        }
    }

    private MockExternalObjectSourceConfiguration config;
    private String identifier;

    public MockExternalObjectSource(String identifier, MockExternalObjectSourceConfiguration config) {
        this.identifier = identifier;
        this.config = config;
    }

    @Override
    public MockExternalObjectSourceConfiguration getConfiguration() {
        return config;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public void setConfiguration(MockExternalObjectSourceConfiguration config) {
        this.config = config;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
