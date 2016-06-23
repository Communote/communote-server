package com.communote.server.core.messaging.connector.xmpp;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ApplicationPropertyXmpp;
import com.communote.server.core.messaging.connectors.xmpp.XMPPConnector;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;

/**
 * Test for the {@link XMPPConnector}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class XMPPConnectorTest extends CommunoteIntegrationTest {

    /** map with test data. */
    private final Map<String, String> namesMap = new HashMap<String, String>();

    /**
     * Setups for tests.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(groups = "integration-test-setup")
    public final void setupGetUserAlias() throws Exception {
        namesMap.put("pete", "pete");
        namesMap.put("pete.communote", "pete");
        namesMap.put("pete.communote@somewhere", "pete");
        namesMap.put("pete.of.communote", "pete.of");
        namesMap.put("pete.of.communote@somewhere.org", "pete.of");
        namesMap.put("pete.of.communote@somewhere.org", "pete.of");
        namesMap.put("pete.communote@somewhere.org", "pete");
        namesMap.put("pete.one.two.three.communote@somewhere.org", "pete.one.two.three");
        Map<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationPropertyXmpp.USER_SUFFIX, "@somewhere.org");
        AuthenticationTestUtils.setManagerContext();
        CommunoteRuntime.getInstance().getConfigurationManager()
                .updateApplicationConfigurationProperties(settings);
    }

    /**
     * Test for {@link XMPPConnector#getUserAlias(String)}.
     */
    @Test
    public void testGetUserAlias() {
        XMPPConnector connector = new XMPPConnector("localhost", "1234", "communote", "123456",
                "test");
        for (String key : namesMap.keySet()) {
            Assert.assertEquals(connector.getUserAlias(key), namesMap.get(key));
        }
    }
}
