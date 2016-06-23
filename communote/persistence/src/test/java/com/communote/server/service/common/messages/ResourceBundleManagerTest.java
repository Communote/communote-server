package com.communote.server.service.common.messages;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.persistence.common.messages.ResourceBundleChangedEvent;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * Test for the {@link ResourceBundleManager}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ResourceBundleManagerTest {

    /* { {Key, Language, Pattern, Expected result, arguments ...} } */
    private static String[][] TEST_DATA;
    static {
        TEST_DATA = new String[][] { { "key1", "de", "Test", "Test" },
                { "key2", "en", "Test {0}", "Test 1", "1" },
                { "key3", "de", "Test {0} und {1}", "Test 1 und 2", "1", "2" },
                { "key4", "en", "Test {0} und {2}", "Test 1 und 3", "1", "2", "3" },
                { "key5", "en", "House", "House" }, { "key5", "de", "Haus", "Haus" } };
    }
    private ServiceLocator orgLocator;
    private Field instanceField;

    private ResourceBundleManager resourceBundleManager;

    /**
     * Clean up after tests.
     *
     * @throws Exception
     *             if cleanup failed
     */
    @AfterClass
    public void cleanup() throws Exception {
        // restore original service locator
        instanceField = ServiceLocator.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, orgLocator);
    }

    /**
     * @param testPattern
     *            The pattern.
     * @return The arguments for this.
     */
    private Object[] getArguments(String[] testPattern) {
        if (testPattern.length <= 4) {
            return null;
        }
        Object[] args = new Object[testPattern.length - 4];
        for (int i = 4; i < testPattern.length; i++) {
            args[i - 4] = testPattern[i];
        }
        return args;
    }

    /**
     * Setup.
     *
     * @throws Exception
     *             if setup failed
     */
    @BeforeClass
    public void setup() throws Exception {
        ServiceLocator mockServiceLocator = EasyMock.createMock(ServiceLocator.class);
        EventDispatcher mockEventDispatcher = EasyMock.createMock(EventDispatcher.class);
        mockEventDispatcher.fire(EasyMock.anyObject(ResourceBundleChangedEvent.class));
        EasyMock.expectLastCall().anyTimes();
        mockServiceLocator.getService(EventDispatcher.class);
        EasyMock.expectLastCall().andReturn(mockEventDispatcher).anyTimes();
        EasyMock.replay(mockServiceLocator, mockEventDispatcher);
        // replace service locator instance with mock object
        orgLocator = ServiceLocator.instance();
        instanceField = ServiceLocator.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, mockServiceLocator);
        resourceBundleManager = ResourceBundleManager.instance();
        for (String[] testPattern : TEST_DATA) {
            Map<String, String> localizations = new HashMap<String, String>();
            localizations.put(testPattern[0], testPattern[2]);
            resourceBundleManager.addLocalizations(testPattern[0], new Locale(testPattern[1]),
                    localizations);
        }
    }

    /**
     * Tests, that localizations are given back in the correct order (LAST IN FIRST OUT).
     */
    @Test
    public void testCorrectOrder() {
        String key = "testCorrectOrder";
        Map<String, String> localizations1 = new Hashtable<String, String>();
        localizations1.put(key, key + 1);
        Map<String, String> localizations2 = new Hashtable<String, String>();
        localizations2.put(key, key + 2);
        Map<String, String> localizations3 = new Hashtable<String, String>();
        localizations3.put(key, key + 3);
        Map<String, String> localizations4 = new Hashtable<String, String>();
        localizations4.put(key, key + 4);
        Map<String, String> localizations5 = new Hashtable<String, String>();
        localizations5.put(key, key + 5);
        resourceBundleManager.addLocalizations("test1", Locale.GERMAN, localizations1);
        Assert.assertEquals(resourceBundleManager.getText(key, Locale.GERMAN), key + 1);
        resourceBundleManager.addLocalizations("test2", Locale.GERMAN, localizations2);
        Assert.assertEquals(resourceBundleManager.getText(key, Locale.GERMAN), key + 2);
        resourceBundleManager.addLocalizations("test3", Locale.GERMAN, localizations3);
        Assert.assertEquals(resourceBundleManager.getText(key, Locale.GERMAN), key + 3);
        resourceBundleManager.addLocalizations("test4", new Locale("an"), localizations4);
        Assert.assertEquals(resourceBundleManager.getText(key, new Locale("an")), key + 4);
        resourceBundleManager.addLocalizations("test5", new Locale("an", "tje"), localizations5);
        Assert.assertEquals(resourceBundleManager.getText(key, new Locale("an", "tje")), key + 5);
        Assert.assertEquals(resourceBundleManager.getText(key, new Locale("an", "tja")), key + 4);
        resourceBundleManager.removeLocalizations("test5");
        resourceBundleManager.removeLocalizations("test4");
        resourceBundleManager.removeLocalizations("test3");
        Assert.assertEquals(resourceBundleManager.getText(key, Locale.GERMAN), key + 2);
        resourceBundleManager.removeLocalizations("test2");
        Assert.assertEquals(resourceBundleManager.getText(key, Locale.GERMAN), key + 1);
        resourceBundleManager.removeLocalizations("test1");
        Assert.assertEquals(resourceBundleManager.getText(key, Locale.GERMAN), "");
    }

    /**
     * Tests for fallback to English.
     */
    @Test
    public void testFallBack() {
        Map<String, String> localizations = new Hashtable<String, String>();
        String message = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        localizations.put(key, message);
        resourceBundleManager.addLocalizations("test2", Locale.ENGLISH, localizations);
        String foundMessage = resourceBundleManager.getText(key, Locale.GERMANY);
        Assert.assertEquals(foundMessage, message);
    }

    /**
     * Test.
     */
    @Test
    public void testGetText() {
        for (String[] testPattern : TEST_DATA) {
            Object[] arguments = getArguments(testPattern);
            String message = resourceBundleManager.getText(testPattern[0], new Locale(
                    testPattern[1]), arguments);
            Assert.assertEquals(message, testPattern[3]);
        }
    }

    /**
     * Tests, that the ResourceBundleManager gives back the correct list of used language codes.
     */
    @Test(priority = 0)
    public void testGetUsedLanguageCodes() {
        HashMap<String, String> localizations = new HashMap<String, String>();
        localizations.put("key", "value");
        Set<String> usedLanguageCodes = resourceBundleManager.getUsedLanguageCodes();
        Assert.assertEquals(usedLanguageCodes.size(), 2);
        Assert.assertTrue(usedLanguageCodes.contains("de"));
        Assert.assertTrue(usedLanguageCodes.contains("en"));
        resourceBundleManager.addLocalizations(Locale.CANADA.toString(), Locale.CANADA,
                localizations);
        usedLanguageCodes = resourceBundleManager.getUsedLanguageCodes();
        Assert.assertEquals(usedLanguageCodes.size(), 3);
        Assert.assertTrue(usedLanguageCodes.contains(Locale.CANADA.toString()));
        resourceBundleManager.removeLocalizations(Locale.CANADA.toString());
        usedLanguageCodes = resourceBundleManager.getUsedLanguageCodes();
        Assert.assertEquals(usedLanguageCodes.size(), 2);
        Assert.assertFalse(usedLanguageCodes.contains(Locale.CANADA.toString()));
    }

    /**
     * This tests that English is used only as fallback, when there is no localization in the
     * preferred language.
     */
    @Test
    public void testNoFallbackOnFirstIteration() {
        String key = "testNoFallbackOnFirstIteration_";
        Map<String, String> germanLocalization = new Hashtable<String, String>();
        germanLocalization.put(key, key + "de");
        Map<String, String> englishLocalizations = new Hashtable<String, String>();
        englishLocalizations.put(key, key + "en");
        resourceBundleManager.addLocalizations(key + 1, new Locale("de"), germanLocalization);
        resourceBundleManager.addLocalizations(key + 2, Locale.ENGLISH, englishLocalizations);
        Assert.assertEquals(resourceBundleManager.getText(key, Locale.GERMAN), key + "de");
        Assert.assertEquals(resourceBundleManager.getText(key, Locale.JAPAN), key + "en");
        resourceBundleManager.removeLocalizations(key + 1);
        Assert.assertEquals(resourceBundleManager.getText(key, Locale.GERMAN), key + "en");
    }

    /**
     * Test that single quotes in values are handled correctly (regression test for KENMEI-4779).
     */
    @Test
    public void testSingleQuoteValues() {
        String key = "key" + UUID.randomUUID();
        String value = "test'test'test" + UUID.randomUUID();
        HashMap<String, String> localizations = new HashMap<String, String>();
        localizations.put(key, value);
        Locale locale = new Locale("ad", "ri", "an");
        resourceBundleManager.addLocalizations(key, locale, localizations);
        Assert.assertEquals(resourceBundleManager.getText(key, locale), value);
        resourceBundleManager.removeLocalizations(key);
    }
}
