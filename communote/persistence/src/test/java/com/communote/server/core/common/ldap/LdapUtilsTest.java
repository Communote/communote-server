package com.communote.server.core.common.ldap;

import java.util.Collection;
import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.core.common.ldap.LdapUtils;
import com.communote.server.model.config.LdapSearchBaseDefinition;


/**
 * Test for LdapUtils.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapUtilsTest {

    /**
     * Test for {@link LdapUtils#dnConformsToSearchBaseDefinitions(String, java.util.Collection)}
     */
    @Test
    public void testDnConformsToSearchBaseDefinitions() {
        // Format { DN, SearchBase, SearchSubtree, Matches }
        Object[][] datas = new Object[][] {
                { "cn=exampleuser1,ou=people,ou=example,dc=communote,dc=com",
                        "ou=people,ou=example,dc=communote,dc=com", true, true },
                { "cn=exampleuser1,ou=people,ou=example,dc=communote,dc=com",
                        "ou=people,ou=example,dc=communote,dc=com", false, true },
                { "cn=exampleuser1,ou=people,ou=example,dc=communote,dc=com",
                        "ou=people,ou=example,dc=communote,dc=com", true, true },
                { "cn=exampleuser1,oU=people,ou=example,dc=communote,Dc=com",
                        "ou=people,ou=example,dc=communote,dc=com", false, true },
                { "cn=exampleuser1,ou=people,ou=example,dc=communote,dc=com",
                        "ou=example,dc=communote,dc=com", true, true },
                { "cn=exampleuser1,ou=people,ou=example,dc=communote,dc=com",
                        "OU=example,DC=communote,dc=com", true, true },
                { "cn=exampleuser1,ou=people,ou=example,dc=communote,dc=com",
                        "OU=example,DC=communote,dc=com", false, false },
                { "cn=exampleuser1,ou=people,ou=example,dc=communote,dc=com",
                        "OU=example,DC=example,dc=com", false, false },
                { "cn=exampleuser1,ou=people,ou=example,dc=communote,dc=com",
                        "OU=example,DC=example,dc=com", true, false }
        };
        Collection<LdapSearchBaseDefinition> searchBaseDefs = new HashSet<LdapSearchBaseDefinition>();
        LdapSearchBaseDefinition ldapSearchBaseDefinition = LdapSearchBaseDefinition.Factory
                .newInstance("", true);
        searchBaseDefs.add(ldapSearchBaseDefinition);
        for (Object[] data : datas) {
            ldapSearchBaseDefinition.setSearchBase(data[1].toString());
            ldapSearchBaseDefinition.setSearchSubtree((Boolean) data[2]);
            boolean conforms = LdapUtils.dnConformsToSearchBaseDefinitions(data[0].toString(),
                    searchBaseDefs);
            Assert.assertEquals(conforms,
                    data[3], "Wrong result for " + data[0] + " and " + data[1]);

        }
    }

    /**
     * Tests the escape method.
     */
    @Test
    public void testSearchFilterEscape() {
        String testString = ",;hello*\"#+<>)\\=(world*)";
        testString = LdapUtils.escapeLdapSearchFilterValue(testString);
        Assert.assertEquals(",;hello*\"#+<>\\29\\5c=\\28world*\\29", testString);
    }
}
