package com.communote.server.core.plugin;

import java.util.Map;
import java.util.Map.Entry;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.test.CommunoteIntegrationTest;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PluginPropertyManagementTest extends CommunoteIntegrationTest {

    private PluginPropertyManagement pluginPropertyManagement;

    private String symbolicName;

    /**
     * Test for getAllClientProperties
     */
    @Test
    public void getAllPropertiesTest() throws Exception {
        String symbolicName = random();
        pluginPropertyManagement.setClientProperty(symbolicName, "client" + random(), random());
        pluginPropertyManagement.setClientProperty(symbolicName, "client" + random(), random());
        pluginPropertyManagement.setClientProperty(symbolicName, "client" + random(), random());
        pluginPropertyManagement.setApplicationProperty(symbolicName, "application" + random(),
                random());
        pluginPropertyManagement.setApplicationProperty(symbolicName, "application" + random(),
                random());
        pluginPropertyManagement.setApplicationProperty(symbolicName, "application" + random(),
                random());

        Map<String, String> clientProperties = pluginPropertyManagement
                .getAllClientProperties(symbolicName);
        Assert.assertEquals(clientProperties.size(), 3);
        for (Entry<String, String> entry : clientProperties.entrySet()) {
            Assert.assertTrue(entry.getKey().startsWith("client"));
        }
        Map<String, String> applicationProperties = pluginPropertyManagement
                .getAllApplicationProperties(symbolicName);
        Assert.assertEquals(applicationProperties.size(), 3);
        for (Entry<String, String> entry : applicationProperties.entrySet()) {
            Assert.assertTrue(entry.getKey().startsWith("application"));
        }

    }

    /**
     * Setup.
     */
    @BeforeClass
    public void setup() {
        pluginPropertyManagement = ServiceLocator.instance().getService(
                PluginPropertyManagement.class);
        symbolicName = random();
    }

    /**
     * Test.
     *
     * @throws PluginPropertyManagementException
     *             Excpetion.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCantGetApplicationPropertyAsClientProperty()
            throws PluginPropertyManagementException {
        String key = random();
        String value = random();
        pluginPropertyManagement.setClientProperty(symbolicName, key, value);
        pluginPropertyManagement.getApplicationProperty(symbolicName, key);
        Assert.fail("This should never be reached.");
    }

    /**
     * Test.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCantGetClientPropertyAsApplicationProperty() throws Exception {
        String key = random();
        String value = random();
        pluginPropertyManagement.setApplicationProperty(symbolicName, key, value);
        pluginPropertyManagement.getClientProperty(symbolicName, key);
        Assert.fail("This should never be reached.");
    }

    /**
     * Test.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCantSetApplicationPropertyAsClientProperty() throws Exception {
        String key = random();
        String value = random();
        pluginPropertyManagement.setClientProperty(symbolicName, key, value);
        pluginPropertyManagement.setApplicationProperty(symbolicName, key, value + 1);
        Assert.fail("This should never be reached.");
    }

    /**
     * Test.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCantSetClientPropertyAsApplicationProperty() throws Exception {
        String key = random();
        String value = random();
        pluginPropertyManagement.setApplicationProperty(symbolicName, key, value);
        pluginPropertyManagement.setClientProperty(symbolicName, key, value + 1);
        Assert.fail("This should never be reached.");
    }

    /**
     * Test for application properties.
     *
     */
    @Test
    public void testSetGetApplicationProperty() throws Exception {
        String key = random();
        String value = random();
        Assert.assertNull(pluginPropertyManagement.getApplicationProperty(symbolicName, key));
        Assert.assertEquals(value + value,
                pluginPropertyManagement.getApplicationProperty(symbolicName, key, value + value));
        pluginPropertyManagement.setApplicationProperty(symbolicName, key, value);
        Assert.assertEquals(value,
                pluginPropertyManagement.getApplicationProperty(symbolicName, key));
        value = random();
        pluginPropertyManagement.setApplicationProperty(symbolicName, key, value);
        Assert.assertEquals(value,
                pluginPropertyManagement.getApplicationProperty(symbolicName, key));
        pluginPropertyManagement.setApplicationProperty(symbolicName, key, null);
        Assert.assertNull(pluginPropertyManagement.getApplicationProperty(symbolicName, key));

        // Default Value
        Assert.assertEquals(value,
                pluginPropertyManagement.getApplicationProperty(random(), random(), value));

    }

    /**
     * Test for client properties.
     */
    @Test
    public void testSetGetClientProperty() {
        String symbolicName = random();
        String key = random();
        String value = random();
        Assert.assertNull(pluginPropertyManagement.getClientProperty(symbolicName, key));
        Assert.assertEquals(value + value,
                pluginPropertyManagement.getClientProperty(symbolicName, key, value + value));
        pluginPropertyManagement.setClientProperty(symbolicName, key, value);
        Assert.assertEquals(value, pluginPropertyManagement.getClientProperty(symbolicName, key));
        value = random();
        pluginPropertyManagement.setClientProperty(symbolicName, key, value);
        Assert.assertEquals(value, pluginPropertyManagement.getClientProperty(symbolicName, key));
        pluginPropertyManagement.setClientProperty(symbolicName, key, null);
        Assert.assertNull(pluginPropertyManagement.getClientProperty(symbolicName, key));

        // Default Value
        Assert.assertEquals(value,
                pluginPropertyManagement.getClientProperty(random(), random(), value));
    }

    /**
     * Test for client properties.
     *
     * @throws PluginPropertyManagementException
     *             Exception
     */
    @Test
    public void testSetGetClientPropertyAsObject() throws PluginPropertyManagementException {
        String key = random();
        Property value = new Property(random());
        pluginPropertyManagement.setClientPropertyAsObject(symbolicName, key, value);
        Property value2 = pluginPropertyManagement.getClientPropertyAsObject(symbolicName, key,
                Property.class);
        Assert.assertEquals(value2.getValue(), value.getValue());
    }
}
