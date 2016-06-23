package com.communote.server.api.core.common;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.common.IdentifiableEntityData;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class IdentifiableEntityDataTest {
    /**
     * Tests {@link IdentifiableEntityData#setProperty(String, Object)} and {@link IdentifiableEntityData#getProperty(String)}
     */
    @Test
    public void testSetAndGetProperty() {
        IdentifiableEntityData listItem = new IdentifiableEntityData();
        String property1 = new String(UUID.randomUUID().toString());
        Long property2 = new Long(2);
        Object property3 = new Object();
        listItem.setProperty(property1, property1);
        listItem.setProperty(property2.toString(), property2);
        listItem.setProperty(property3.toString(), property3);

        Assert.assertTrue(listItem.<String> getProperty(property1) instanceof String);
        Assert.assertTrue(listItem.<Long> getProperty(property2.toString()) instanceof Long);
        Assert.assertTrue(listItem.<Object> getProperty(property3.toString()) instanceof Object);
        Assert.assertNull(listItem.getProperty(UUID.randomUUID().toString()));
        Assert.assertFalse(listItem.<String> getProperty(property2.toString()) instanceof String);
        Assert.assertFalse(listItem.<Long> getProperty(property1.toString()) instanceof Long);
    }
}
