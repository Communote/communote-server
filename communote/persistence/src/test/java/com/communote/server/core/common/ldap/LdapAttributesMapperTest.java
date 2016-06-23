package com.communote.server.core.common.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.string.StringHelper;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.common.ldap.LdapAttributeMappingException;
import com.communote.server.core.common.ldap.LdapGroupAttribute;
import com.communote.server.core.common.ldap.LdapGroupAttributesMapper;
import com.communote.server.core.common.ldap.LdapUserAttribute;
import com.communote.server.core.common.ldap.LdapUserAttributesMapper;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.model.config.LdapSearchConfiguration;
import com.communote.server.persistence.user.ExternalUserVO;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class LdapAttributesMapperTest {

    /**
     * Creates an LAP Configuration for testing
     * 
     * @param aliasAttribute
     *            the alias attribute name
     * @param emailAttribute
     *            the email attribute name
     * @param fnAttribute
     *            the first name attribute name
     * @param lnAttribute
     *            the last name attribute name
     * @param uidAttribute
     *            the uid attribute name
     * @param upnAttribute
     *            the upn attribute name
     * @return the configuration
     */
    private LdapConfiguration createTestLdapConfig(String aliasAttribute, String emailAttribute,
            String fnAttribute, String lnAttribute, String uidAttribute, String upnAttribute) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(LdapUserAttribute.ALIAS.getName(), aliasAttribute);
        map.put(LdapUserAttribute.EMAIL.getName(), emailAttribute);
        map.put(LdapUserAttribute.FIRSTNAME.getName(), fnAttribute);
        map.put(LdapUserAttribute.LASTNAME.getName(), lnAttribute);
        map.put(LdapUserAttribute.UID.getName(), uidAttribute);
        map.put(LdapUserAttribute.UPN.getName(), upnAttribute);

        LdapConfiguration ldapConfiguration = LdapConfiguration.Factory.newInstance();
        ldapConfiguration.setUrl("ldap://test.domain");
        ldapConfiguration.setSystemId("testLdap");
        ldapConfiguration.setPrimaryAuthentication(true);
        ldapConfiguration.setUserIdentifierIsBinary(false);
        ldapConfiguration.setAllowExternalAuthentication(true);
        ldapConfiguration.setSynchronizeUserGroups(true);
        ldapConfiguration.setUserSearch(LdapSearchConfiguration.Factory.
                newInstance(StringHelper.toString(map), new ArrayList<LdapSearchBaseDefinition>()));
        return ldapConfiguration;
    }

    /**
     * Test mapping of group attributes.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testGroupAttributesMapper() throws Exception {
        String name = UUID.randomUUID().toString();
        String alias = UUID.randomUUID().toString().replace("-", "");
        String description = UUID.randomUUID().toString();
        String entry = UUID.randomUUID().toString();
        String nameAttributeId = UUID.randomUUID().toString();
        String aliasAttributeId = UUID.randomUUID().toString();
        String descriptionAttributeId = UUID.randomUUID().toString();
        String entryAttributeId = UUID.randomUUID().toString();
        String memberAttributeId = UUID.randomUUID().toString();
        Attribute nameAttribute = new BasicAttribute(nameAttributeId, name);
        nameAttribute.add(name);
        Attribute aliasAttribute = new BasicAttribute(aliasAttributeId, alias);
        aliasAttribute.add(alias);
        Attribute descriptionAttribute = new BasicAttribute(descriptionAttributeId, description);
        descriptionAttribute.add(description);
        Attribute entryAttribute = new BasicAttribute(entryAttributeId, entry);
        descriptionAttribute.add(entry);
        Attribute memberAttribute = new BasicAttribute(entryAttributeId, entry);
        Attributes attributes = new BasicAttributes();
        attributes.put(nameAttribute);
        attributes.put(aliasAttribute);
        attributes.put(descriptionAttribute);
        attributes.put(entryAttribute);
        attributes.put(memberAttribute);
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(LdapGroupAttribute.NAME.getName(), nameAttributeId);
        mapping.put(LdapGroupAttribute.ALIAS.getName(), aliasAttributeId);
        mapping.put(LdapGroupAttribute.DESCRIPTION.getName(), descriptionAttributeId);
        mapping.put(LdapGroupAttribute.UID.getName(), entryAttributeId);
        mapping.put(LdapGroupAttribute.MEMBERSHIP.getName(), memberAttributeId);
        String groupDummyDN = "uid=group1,ou=test,dc=communote,dc=com";
        LdapGroupAttributesMapper mapper = new LdapGroupAttributesMapper(mapping, "TestLdap", false);
        ExternalGroupVO groupVo = mapper.mapAttributes(groupDummyDN, attributes);
        Assert.assertNotNull(groupVo);
        Assert.assertEquals(alias, groupVo.getAlias());
        Assert.assertEquals(name, groupVo.getName());
        Assert.assertEquals(description, groupVo.getDescription());
        Assert.assertEquals(groupDummyDN, groupVo.getAdditionalProperty());
    }

    /**
     * Test the user attributes mapping
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testUserAttributesMapper() throws Exception {
        LdapUserAttributesMapper mapper = new LdapUserAttributesMapper(createTestLdapConfig(
                "alias", "mail", "fn", "ln", "guid", "userPrincipalName"));
        String emailValue = "test1@test.domain";
        String fnValue = UUID.randomUUID().toString();
        String lnValue = UUID.randomUUID().toString();
        String uidValue = UUID.randomUUID().toString();
        String aliasValue1 = "peter_test";
        String aliasValue2 = "petertest";
        String upnValue = emailValue;
        Attributes attributes = new BasicAttributes();

        BasicAttribute aliasAttribute = new BasicAttribute("alias", aliasValue1);
        aliasAttribute.add(aliasValue2);
        attributes.put(aliasAttribute);
        attributes.put(new BasicAttribute("mail", emailValue));
        attributes.put(new BasicAttribute("fn", fnValue));
        attributes.put(new BasicAttribute("ln", lnValue));
        attributes.put(new BasicAttribute("guid", uidValue));
        attributes.put(new BasicAttribute("userPrincipalName", upnValue));
        String userDN = "uid=user1,ou=test,dc=communote,dc=com";
        ExternalUserVO userVo = mapper.mapAttributes(userDN, attributes);
        Assert.assertNotNull(userVo);
        // ensure the first is taken
        Assert.assertEquals(aliasValue1, userVo.getExternalUserName());
        Assert.assertEquals(emailValue, userVo.getEmail());
        Assert.assertEquals(fnValue, userVo.getFirstName());
        Assert.assertEquals(lnValue, userVo.getLastName());
        Assert.assertEquals(uidValue, userVo.getPermanentId());
        Assert.assertEquals(userDN, userVo.getAdditionalProperty());
        StringPropertyTO[] properties = userVo.getProperties().toArray(
                new StringPropertyTO[userVo.getProperties().size()]);
        Assert.assertEquals(upnValue, properties[0].getPropertyValue());
    }

    /**
     * Test the user attributes mapping that extracts the alias from the DN
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testUserAttributesMapperDN() throws Exception {
        LdapUserAttributesMapper mapper = new LdapUserAttributesMapper(createTestLdapConfig(
                "DN:alias", "mail", "fn", "ln", "guid", "userPrincipalName"));
        String aliasValue1 = "peter_test";
        String aliasValue2 = "petertest";
        String aliasValue3 = "peter.test";
        String emailValue = "test1@test.domain";
        String fnValue = UUID.randomUUID().toString();
        String lnValue = UUID.randomUUID().toString();
        String uidValue = UUID.randomUUID().toString();
        String upnValue = emailValue;
        BasicAttribute aliasAttribute = new BasicAttribute("alias", aliasValue1);
        aliasAttribute.add(aliasValue2);
        aliasAttribute.add(aliasValue3);
        Attributes attributes = new BasicAttributes();
        attributes.put(aliasAttribute);
        attributes.put(new BasicAttribute("mail", emailValue));
        attributes.put(new BasicAttribute("fn", fnValue));
        attributes.put(new BasicAttribute("ln", lnValue));
        attributes.put(new BasicAttribute("guid", uidValue));
        attributes.put(new BasicAttribute("userPrincipalName", upnValue));
        String userDN = "alias=" + aliasValue3 + ",ou=test,dc=communote,dc=com";
        ExternalUserVO userVo = mapper.mapAttributes(userDN, attributes);
        Assert.assertNotNull(userVo);
        // ensure the value from the DN is taken
        Assert.assertEquals(aliasValue3, userVo.getExternalUserName());
        Assert.assertEquals(emailValue, userVo.getEmail());
        Assert.assertEquals(fnValue, userVo.getFirstName());
        Assert.assertEquals(lnValue, userVo.getLastName());
        Assert.assertEquals(uidValue, userVo.getPermanentId());
        Assert.assertEquals(userDN, userVo.getAdditionalProperty());
        // test case of DN
        userDN = "ALIAS=" + aliasValue3 + " , ou=test,dc=communote,dc=com";
        userVo = mapper.mapAttributes(userDN, attributes);
        Assert.assertNotNull(userVo);
        // ensure the value from the DN is taken
        Assert.assertEquals(aliasValue3, userVo.getExternalUserName());
        Assert.assertEquals(emailValue, userVo.getEmail());
        Assert.assertEquals(fnValue, userVo.getFirstName());
        Assert.assertEquals(lnValue, userVo.getLastName());
        Assert.assertEquals(uidValue, userVo.getPermanentId());
        Assert.assertEquals(userDN, userVo.getAdditionalProperty());
        // test exception is thrown if not in DN
        boolean mappingFailed = false;
        userDN = "ALIASAttribute=" + aliasValue3 + ",ou=test,dc=communote,dc=com";
        try {
            userVo = mapper.mapAttributes(userDN, attributes);
        } catch (LdapAttributeMappingException e) {
            mappingFailed = true;
        }
        Assert.assertTrue(mappingFailed, "AttributesMappingException was not thrown.");
        StringPropertyTO[] properties = userVo.getProperties().toArray(
                new StringPropertyTO[userVo.getProperties().size()]);
        Assert.assertEquals(upnValue, properties[0].getPropertyValue());
    }
}
