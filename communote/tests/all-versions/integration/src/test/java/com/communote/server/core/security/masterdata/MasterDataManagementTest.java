package com.communote.server.core.security.masterdata;

import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.user.MasterDataManagement;
import com.communote.server.model.user.Language;
import com.communote.server.test.CommunoteIntegrationTest;

/**
 * Testing the master data management functionality
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MasterDataManagementTest extends CommunoteIntegrationTest {

    private final String languageCode = "ab_cd";
    private final String languageName = UUID.randomUUID().toString();

    /**
     * Test for {@link MasterDataManagement#addLanguage(String, String)}
     */
    @Test
    public void testAddLanguage() {
        MasterDataManagement masterDataManagement = ServiceLocator
                .findService(MasterDataManagement.class);
        List<Language> originalLanguages = masterDataManagement.getLanguages();
        masterDataManagement.addLanguage(languageCode, languageName);
        List<Language> existingLanguages = masterDataManagement.getLanguages();
        Language language = masterDataManagement.findLanguageByCode(languageCode);
        Assert.assertEquals(existingLanguages.size(), originalLanguages.size() + 1);
        Assert.assertEquals(language.getName(), languageName);
    }

    /**
     * Test for {@link MasterDataManagement#removeLanguage(String)}
     */
    @Test(dependsOnMethods = "testAddLanguage")
    public void testRemoveLanguage() {
        MasterDataManagement masterDataManagement = ServiceLocator
                .findService(MasterDataManagement.class);
        List<Language> originalLanguages = masterDataManagement.getLanguages();
        masterDataManagement.removeLanguage(languageCode);
        List<Language> existingLanguages = masterDataManagement.getLanguages();
        Assert.assertEquals(existingLanguages.size(), originalLanguages.size() - 1);
        Assert.assertNull(masterDataManagement.findLanguageByCode(languageCode));

    }
}
