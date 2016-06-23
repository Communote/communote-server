package com.communote.plugins.mq.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.test.CommunoteIntegrationTest;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO Still not finished, just PoC for deploying plugins within CommunoteIntegrationTest
public class MessageQueueCommunoteIntegrationTest extends CommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getBundlePathsWithinMavenRepository() {
        return new String[] {
                "org/apache/felix/org.apache.felix.ipojo.annotations"
                        + "/1.8.2/org.apache.felix.ipojo.annotations-1.8.2.jar",
                "org/apache/felix/org.apache.felix.ipojo/1.8.2/org.apache.felix.ipojo-1.8.2.jar",
                "com/communote/plugins/communote-plugins-core/3.0.1-SNAPSHOT/communote-plugins-core-3.0.1-SNAPSHOT.jar",
                "com/communote/plugins/communote-plugin-activity-base"
                        + "/1.0-SNAPSHOT/communote-plugin-activity-base-1.0-SNAPSHOT.jar",
                "com/communote/plugins/communote-plugin-activity-core"
                        + "/1.0-SNAPSHOT/communote-plugin-activity-core-1.0-SNAPSHOT.jar",
                "com/communote/plugins/mq/com-communote-plugins-mq-message"
                        + "/1.0-SNAPSHOT/com-communote-plugins-mq-message-1.0-SNAPSHOT.jar",
                "com/communote/plugins/mq/communote-plugins-mq-service"
                        + "/1.0-SNAPSHOT/communote-plugins-mq-service-1.0-SNAPSHOT.jar",
                "com/communote/plugins/mq/com-communote-plugins-mq-message-core"
                        + "/1.0-SNAPSHOT/com-communote-plugins-mq-message-core-1.0-SNAPSHOT.jar"
        };
    }

    /**
     * Dummy test.
     */
    @Test
    public void test() {
        Assert.assertTrue(true);
    }
}
