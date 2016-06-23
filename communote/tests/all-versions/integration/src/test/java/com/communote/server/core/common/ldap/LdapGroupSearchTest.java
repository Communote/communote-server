package com.communote.server.core.common.ldap;

import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.plugin.ldap.helper.MemberAndNonMemberModeVisitingRetriever;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.plugins.api.externals.ExternalEntityVisitor;
import com.communote.server.test.ldap.LdapCommunoteIntegrationTest;

/**
 * Tests for {@link LdapGroupSearch} and partially {@link MemberAndNonMemberModeVisitingRetriever}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class LdapGroupSearchTest extends LdapCommunoteIntegrationTest {

    /**
     * Visitor which counts the visited entities
     */
    private class Visitor implements ExternalEntityVisitor<ExternalGroupVO> {
        private int counter = 0;

        @Override
        public void visit(ExternalGroupVO entity) throws Exception {
            if (entity != null) {
                counter++;
            }
        }
    }

    /**
     * @param memberMode
     *            Set to true for memberMode.
     * @param binaryGroupIdentifier
     *            Set to true if the group identifier is binary.
     * @param synchronizationAttribute
     *            The synchronization attribute.
     * @return {@link LdapConfiguration}.
     * @throws Exception
     *             Exception.
     */
    public LdapConfiguration getConfiguration(boolean memberMode, boolean binaryGroupIdentifier,
            String synchronizationAttribute) throws Exception {
        return createGroupConfiguration(memberMode, binaryGroupIdentifier, synchronizationAttribute);
    }

    /**
     * Creates a mapper for the given configuration.
     *
     * @param ldapConfiguration
     *            Configuration.
     * @return A {@link LdapGroupAttributesMapper}
     * @throws LdapAttributeMappingException
     *             Exception.
     */
    private LdapGroupAttributesMapper getLdapGroupAttributesMapper(
            LdapConfiguration ldapConfiguration) throws LdapAttributeMappingException {
        LdapGroupAttributesMapper ldapGroupAttributesMapper = new LdapGroupAttributesMapper(
                ldapConfiguration.getGroupSyncConfig().getGroupSearch().getPropertyMapping(),
                ldapConfiguration.getSystemId(), ldapConfiguration.getGroupSyncConfig()
                .isGroupIdentifierIsBinary());
        return ldapGroupAttributesMapper;
    }

    /**
     * Tests the findGroups functionality.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testFindGroups() throws Exception {
        LdapGroupSearch groupSearch = new LdapGroupSearch(getConfiguration(true, false, "member"));
        Collection<ExternalGroupVO> groups = groupSearch.findGroups("*Plugin Development*");
        Assert.assertEquals(groups.size(), 1);
    }

    /**
     * Test the correct handling of getting the groups for the MemberMode.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testGetGroupsForUserForMemberMode() throws Exception {
        LdapConfiguration ldapConfiguration = getConfiguration(true, false, "member");
        MemberAndNonMemberModeVisitingRetriever retriever = new MemberAndNonMemberModeVisitingRetriever(
                null, ldapConfiguration, 100, 1000, -1, false,
                getLdapGroupAttributesMapper(ldapConfiguration));
        Visitor visitor = new Visitor();
        retriever
        .setFilter("cn=Vivi Strandlund,ou=Product Development,ou=Users,dc=communote, dc=com");
        retriever.accept(visitor, false);
        Assert.assertEquals(visitor.counter, 2);
        visitor.counter = 0;
        retriever
        .setFilter("cn=Mollie Whiting,ou=Product Development,ou=Users,dc=communote, dc=com");
        retriever.accept(visitor, false);
        Assert.assertEquals(visitor.counter, 1);
        visitor.counter = 0;
        retriever.setFilter("cn=Troy Parrilli,ou=Product Testing,ou=Users,dc=communote, dc=com");
        retriever.accept(visitor, false);
        Assert.assertEquals(visitor.counter, 0);
    }

    /**
     * Test the correct handling of getting the groups for the NonMemberMode.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testGetGroupsForUserForNonMemberMode() throws Exception {
        LdapConfiguration ldapConfiguration = getConfiguration(false, false, "seeAlso");
        MemberAndNonMemberModeVisitingRetriever retriever = new MemberAndNonMemberModeVisitingRetriever(
                null, ldapConfiguration, 100, 1000, -1, false,
                getLdapGroupAttributesMapper(ldapConfiguration));
        Visitor visitor = new Visitor();
        retriever.setFilter("cn=Mark Shahen,ou=Management,ou=Users,dc=communote, dc=com");
        retriever.accept(visitor, false);
        Assert.assertEquals(visitor.counter, 1);
        visitor.counter = 0;
        // only direct membership and no recursive search
        retriever.setFilter("cn=Carmencita Boutnikoff,ou=Consulting,ou=Users,dc=communote, dc=com");
        retriever.accept(visitor, false);
        Assert.assertEquals(visitor.counter, 2);
    }

    /**
     * Test the correct handling of getting the parent groups for the MemberMode.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testGetParentGroupsForMemberMode() throws Exception {
        LdapConfiguration ldapConfiguration = getConfiguration(true, false, "member");
        MemberAndNonMemberModeVisitingRetriever retriever = new MemberAndNonMemberModeVisitingRetriever(
                null, ldapConfiguration, 100, 1000, -1, false,
                getLdapGroupAttributesMapper(ldapConfiguration));
        Visitor visitor = new Visitor();
        retriever.setFilter("cn=NotExistingGroup,ou=Groups,dc=communote,dc=com");
        retriever.accept(visitor, false);
        Assert.assertEquals(visitor.counter, 0);
        visitor.counter = 0;
        retriever.setFilter("cn=Plugin Development,ou=Groups,dc=communote,dc=com");
        retriever.accept(visitor, false);
        Assert.assertEquals(visitor.counter, 1);
        visitor.counter = 0;
        retriever.setFilter("cn=Core Development,ou=Groups,dc=communote,dc=com");
        retriever.accept(visitor, false);
        Assert.assertEquals(visitor.counter, 1);
    }

    /**
     * Test the correct handling of getting the parent groups for the NonMemberMode.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testGetParentGroupsForNonMemberMode() throws Exception {
        LdapConfiguration ldapConfiguration = getConfiguration(false, false, "seeAlso");
        MemberAndNonMemberModeVisitingRetriever retriever = new MemberAndNonMemberModeVisitingRetriever(
                null, ldapConfiguration, 100, 1000, -1, false,
                getLdapGroupAttributesMapper(ldapConfiguration));
        Visitor visitor = new Visitor();
        retriever.setFilter("cn=Project1,ou=Groups,dc=communote,dc=com");
        retriever.accept(visitor, false);
        Assert.assertEquals(visitor.counter, 2);
        visitor.counter = 0;
        retriever.setFilter("cn=Project3,ou=Groups,dc=communote,dc=com");
        retriever.accept(visitor, false);
        Assert.assertEquals(visitor.counter, 0);
        visitor.counter = 0;
        retriever.setFilter("cn=Project2,ou=Groups,dc=communote,dc=com");
        retriever.accept(visitor, false);
        Assert.assertEquals(visitor.counter, 1);
    }

    /**
     * Tests, if the hasGroup method works.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testHasGroup() throws Exception {
        LdapGroupSearch groupSearch = new LdapGroupSearch(getConfiguration(true, false, "member"));
        Assert.assertTrue(groupSearch.hasGroup("cn=Development,ou=Groups,dc=communote,dc=com"));
        Assert.assertTrue(groupSearch
                .hasGroup("cn=Plugin Development,ou=Groups,dc=communote,dc=com"));
        Assert.assertTrue(groupSearch.hasGroup("cn=Core Development,ou=Groups,dc=communote,dc=com"));
        Assert.assertFalse(groupSearch
                .hasGroup("cn=notExistingGroup,ou=Groups,dc=communote,dc=com"));
    }
}
