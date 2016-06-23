package com.communote.server.persistence.user.group;

import java.util.UUID;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.test.CommunoteIntegrationTest;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalUserGroupDaoTest extends CommunoteIntegrationTest {

    /**
     * Test that the setting of the lowercase matching is respected when searching for external
     * groups.
     */
    @Test
    public void testGroupIdCaseMatching() {
        ExternalUserGroupDao dao = ServiceLocator.findService(ExternalUserGroupDao.class);
        String externalId = UUID.randomUUID().toString().toLowerCase();
        ExternalUserGroup group = ExternalUserGroup.Factory.newInstance();
        group.setExternalId(externalId);
        group.setExternalSystemId(externalId);
        group.setAlias(externalId);
        group.setName(externalId);
        dao.create(group);

        // Disable lower case comparison.
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(
                ClientProperty.COMPARE_EXTERNAL_GROUP_IDS_LOWERCASE,
                Boolean.FALSE.toString());

        // Find, upper case must fail
        Assert.assertNotNull(dao.findByExternalId(externalId, externalId));
        Assert.assertNull(dao.findByExternalId(externalId.toUpperCase(), externalId));

        // Enable lower case comparison.
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(
                ClientProperty.COMPARE_EXTERNAL_GROUP_IDS_LOWERCASE,
                Boolean.TRUE.toString());

        // Find, upper case may not fail
        Assert.assertNotNull(dao.findByExternalId(externalId, externalId));
        Assert.assertNotNull(dao.findByExternalId(externalId.toUpperCase(), externalId));
    }
}
