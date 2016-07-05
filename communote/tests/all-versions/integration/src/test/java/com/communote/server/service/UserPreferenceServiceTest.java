package com.communote.server.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserPreferenceServiceTest extends CommunoteIntegrationTest {

    @Autowired
    private UserPreferenceService userPreferenceService;

    /**
     * Simple test case for getting and saving properties.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testReadAndStore() throws Exception {
        userPreferenceService.register(TestUserPreference.class);
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(false));
        TestUserPreference preferences = userPreferenceService
                .getPreferences(TestUserPreference.class);
        Assert.assertNotNull(preferences);
        Assert.assertEquals(preferences.getSomeValue(), TestUserPreference.SOME_DEFAULT_VALUE);
        String newSomeValue = random();
        preferences.setSomeValue(newSomeValue);
        userPreferenceService.storePreferences(preferences);
        preferences = userPreferenceService.getPreferences(TestUserPreference.class);
        Assert.assertEquals(preferences.getSomeValue(), newSomeValue);
        userPreferenceService.removePreferences(TestUserPreference.class);
        preferences = userPreferenceService.getPreferences(TestUserPreference.class);
        Assert.assertEquals(preferences.getSomeValue(), TestUserPreference.SOME_DEFAULT_VALUE);

        Map<String, String> preferencesAsMap = new HashMap<String, String>();
        preferencesAsMap.put(TestUserPreference.KEY_SOME_VALUE, newSomeValue);

        userPreferenceService
        .mergePreferences(TestUserPreference.class.getName(), preferencesAsMap);
        preferences = userPreferenceService.getPreferences(TestUserPreference.class);
        Assert.assertEquals(preferences.getSomeValue(), newSomeValue);
    }
}
