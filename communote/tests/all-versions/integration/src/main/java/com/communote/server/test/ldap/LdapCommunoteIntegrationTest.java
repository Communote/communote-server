package com.communote.server.test.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;

import org.apache.directory.server.constants.ServerDNConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.common.ldap.LdapGroupAttribute;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapGroupSyncConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.model.config.LdapSearchConfiguration;
import com.communote.server.test.CommunoteIntegrationTest;

/**
 * Abstract LdapTest, which could be used to run tests against a test ldap.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public abstract class LdapCommunoteIntegrationTest extends CommunoteIntegrationTest {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(LdapCommunoteIntegrationTest.class);

    private final static String LDAP_MANAGER_PASSWORD = "secret";

    private final static String LDAP_SEARCH_BASE = "ou=Users,dc=communote,dc=com";

    private final static String LDAP_SEARCH_FILTER = "(objectClass=person)";

    private final static boolean LDAP_SEARCH_SUBTREE = true;

    private final static String LDAP_PROPERTY_MAPPING = "email=mail,alias=uid,firstName=givenName,lastName=sn,uid=uid";
    public static final String EXTERNAL_SYSTEM_ID = ConfigurationManagement.DEFAULT_LDAP_SYSTEM_ID;

    private final ApacheDSServer server = new ApacheDSServer();

    /**
     * Create the group configuration of ldap.
     *
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
    public LdapConfiguration createGroupConfiguration(boolean memberMode,
            boolean binaryGroupIdentifier, String synchronizationAttribute) throws Exception {
        LdapSearchBaseDefinition searchBase = LdapSearchBaseDefinition.Factory.newInstance();
        searchBase.setSearchBase("ou=Groups,dc=communote,dc=com");
        List<LdapSearchBaseDefinition> searchBases = new ArrayList<LdapSearchBaseDefinition>();
        searchBases.add(searchBase);
        LdapSearchConfiguration groupSearchConfiguration = LdapSearchConfiguration.Factory
                .newInstance();
        groupSearchConfiguration.setPropertyMapping(LdapGroupAttribute.NAME + "=cn,"
                + LdapGroupAttribute.ALIAS + "=cn," + LdapGroupAttribute.DESCRIPTION
                + "=description," + LdapGroupAttribute.UID + "=cn," + LdapGroupAttribute.MEMBERSHIP
                + "=" + synchronizationAttribute);
        groupSearchConfiguration.setSearchBases(searchBases);
        groupSearchConfiguration.setSearchFilter("objectClass=*");
        LdapGroupSyncConfiguration groupConfiguration = LdapGroupSyncConfiguration.Factory
                .newInstance();
        groupConfiguration.setGroupIdentifierIsBinary(binaryGroupIdentifier);
        groupConfiguration.setMemberMode(memberMode);
        groupConfiguration.setGroupSearch(groupSearchConfiguration);
        LdapConfiguration configuration = LdapConfiguration.Factory.newInstance();
        configuration.setManagerDN(ServerDNConstants.ADMIN_SYSTEM_DN);
        configuration.setManagerPassword("secret");
        configuration.setSystemId(EXTERNAL_SYSTEM_ID);
        configuration.setGroupSyncConfig(groupConfiguration);
        configuration.setUrl(getEnvironment().get(Context.PROVIDER_URL).toString());
        configuration.setUserSearch(createLdapConfiguration().getUserSearch());
        return configuration;
    }

    /**
     * Creates the ldap configuration.
     *
     * @return the authentication configuration
     * @throws Exception
     *             Exception.
     */
    public LdapConfiguration createLdapConfiguration() throws Exception {
        LdapConfiguration config = LdapConfiguration.Factory.newInstance();
        config.setAllowExternalAuthentication(false);
        config.setPrimaryAuthentication(false);
        config.setUrl(getEnvironment().get(Context.PROVIDER_URL).toString());
        config.setManagerDN(ServerDNConstants.ADMIN_SYSTEM_DN);
        config.setManagerPassword(LDAP_MANAGER_PASSWORD);
        config.setUserSearch(LdapSearchConfiguration.Factory.newInstance());
        config.getUserSearch().setPropertyMapping(LDAP_PROPERTY_MAPPING);
        config.getUserSearch().setSearchFilter(LDAP_SEARCH_FILTER);
        LdapSearchBaseDefinition searchBaseDef = LdapSearchBaseDefinition.Factory.newInstance();
        searchBaseDef.setSearchBase(LDAP_SEARCH_BASE);
        searchBaseDef.setSearchSubtree(LDAP_SEARCH_SUBTREE);
        config.getUserSearch().getSearchBases().add(searchBaseDef);
        config.setSystemId(EXTERNAL_SYSTEM_ID);
        return config;
    }

    /**
     * @return Hashtable with environment parameters.
     * @throws Exception
     *             Exception.
     */
    protected Hashtable<?, ?> getEnvironment() throws Exception {
        return server.getEnvironment();
    }

    /**
     * Returns the next free port between 5000 and 65000.
     *
     * @return The port.
     */
    private int getNextFreePort() {
        for (int i = 5000; i <= 65000; i++) {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(i);
            } catch (IOException ex) {
                continue; // try next port
            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        continue;
                    }
                }
            }
            return i;
        }
        return 0;
    }

    /**
     * Setup.
     *
     * @param ldifFile
     *            ldif file as classpath or file URL. Default is to load from classpath.
     * @throws Exception
     *             Exception.
     */
    @Parameters({ "ldifFile" })
    @BeforeClass(groups = "ldap-test-setup")
    public void setup(
            @Optional("classpath:/com/communote/server/test/ldap/test_ldap.ldif") String ldifFile)
            throws Exception {
        server.setPort(getNextFreePort());
        server.start();
        LOG.info("Load ldif from: " + ldifFile);
        URL url;
        if (ldifFile.startsWith("classpath:")) {
            ldifFile = ldifFile.substring(10);
            url = getClass().getResource(ldifFile);
        } else {
            url = new URL(ldifFile);
        }
        try (InputStream in = url.openStream()) {
            server.importLdifFromStream(in);
        }
    }

    /**
     * Stop.
     *
     * @throws Exception
     *             Exception.
     */
    @AfterClass
    public void stop() throws Exception {
        server.stop();
    }
}
