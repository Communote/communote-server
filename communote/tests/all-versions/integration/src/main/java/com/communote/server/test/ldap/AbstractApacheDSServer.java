/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package com.communote.server.test.ldap;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.CoreSession;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.DefaultServerEntry;
import org.apache.directory.server.core.jndi.CoreContextFactory;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.ldap.handlers.bind.MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.cramMD5.CramMd5MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.digestMD5.DigestMd5MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.gssapi.GssapiMechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.ntlm.NtlmMechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.plain.PlainMechanismHandler;
import org.apache.directory.server.ldap.handlers.extended.StartTlsHandler;
import org.apache.directory.server.ldap.handlers.extended.StoredProcedureExtendedOperationHandler;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.shared.ldap.constants.SupportedSaslMechanisms;
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.entry.Value;
import org.apache.directory.shared.ldap.exception.LdapConfigurationException;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.util.AvailablePortFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just a copy of {@link org.apache.directory.server.unit.AbstractServerTest} without JUnit stuff.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractApacheDSServer {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(AbstractApacheDSServer.class);

    private static final List<LdifEntry> EMPTY_LIST = Collections
            .unmodifiableList(new ArrayList<LdifEntry>(0));
    private static final String CTX_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    private static int START;

    private static int NB_TESTS = 10000;

    /**
     * @return the nbTests
     */
    public static int getNbTests() {
        return NB_TESTS;
    }

    /**
     * @return the start
     */
    public static int getStart() {
        return START;
    }

    /**
     * @param nbTests
     *            the nbTests to set
     */
    public static void setNbTests(int nbTests) {
        AbstractApacheDSServer.NB_TESTS = nbTests;
    }

    /**
     * @param start
     *            the start to set
     */
    public static void setStart(int start) {
        AbstractApacheDSServer.START = start;
    }

    /** the context root for the system partition */
    private LdapContext sysRoot;
    /** the context root for the rootDSE */
    private CoreSession rootDSE;
    /** the context root for the schema */
    private LdapContext schemaRoot;
    /** flag whether to delete database files for each test or not */
    private boolean doDelete = true;

    private int port = -1;

    private DirectoryService directoryService;

    private NioSocketAcceptor socketAcceptor;

    private LdapServer ldapServer;

    /**
     * Does nothing.
     *
     * @throws Exception
     *             Exception.
     */
    protected void configureDirectoryService() throws Exception {
    }

    /**
     * Does nothing.
     */
    protected void configureLdapServer() {
    }

    /**
     * Deletes the Eve working directory.
     *
     * @param wkdir
     *            the directory to delete
     * @throws Exception
     *             if the directory cannot be deleted
     */
    protected void doDelete(File wkdir) throws Exception {
        if (isDoDelete()) {
            if (wkdir.exists()) {
                FileUtils.deleteDirectory(wkdir);
            }

            if (wkdir.exists()) {
                throw new Exception("Failed to delete: " + wkdir);
            }
        }
    }

    /**
     * @return the directoryService
     */
    public DirectoryService getDirectoryService() {
        return directoryService;
    }

    /**
     * @return the ldapServer
     */
    public LdapServer getLdapServer() {
        return ldapServer;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the rootDSE
     */
    public CoreSession getRootDSE() {
        return rootDSE;
    }

    /**
     * @return the schemaRoot
     */
    public LdapContext getSchemaRoot() {
        return schemaRoot;
    }

    /**
     * @return the socketAcceptor
     */
    public NioSocketAcceptor getSocketAcceptor() {
        return socketAcceptor;
    }

    /**
     * @return the sysRoot
     */
    public LdapContext getSysRoot() {
        return sysRoot;
    }

    /**
     * Common code to get an initial context via a simple bind to the server over the wire using the
     * SUN JNDI LDAP provider. Do not use this method until after the setUp() method is called to
     * start the server otherwise it will fail.
     *
     * @return an LDAP context as the the administrator to the rootDSE
     * @throws Exception
     *             if the server cannot be contacted
     */
    protected LdapContext getWiredContext() throws Exception {
        return getWiredContext(ServerDNConstants.ADMIN_SYSTEM_DN, "secret");
    }

    /**
     * Common code to get an initial context via a simple bind to the server over the wire using the
     * SUN JNDI LDAP provider. Do not use this method until after the setUp() method is called to
     * start the server otherwise it will fail.
     *
     * @param bindPrincipalDn
     *            the DN of the principal to bind as
     * @param password
     *            the password of the bind principal
     * @return an LDAP context as the the administrator to the rootDSE
     * @throws Exception
     *             if the server cannot be contacted
     */
    protected LdapContext getWiredContext(String bindPrincipalDn, String password) throws Exception {
        // if ( ! apacheDS.isStarted() )
        // {
        // throw new ConfigurationException( "The server is not online! Cannot connect to it." );
        // }

        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, CTX_FACTORY);
        env.put(Context.PROVIDER_URL, "ldap://localhost:" + getPort());
        env.put(Context.SECURITY_PRINCIPAL, bindPrincipalDn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        return new InitialLdapContext(env, null);
    }

    /**
     * Imports the LDIF entries packaged with the Eve JNDI provider jar into the newly created
     * system partition to prime it up for operation. Note that only ou=system entries will be added
     * - entries for other partitions cannot be imported and will blow chunks.
     *
     * @throws Exception
     *             if there are problems reading the ldif file and adding those entries to the
     *             system partition
     * @param in
     *            the input stream with the ldif
     */
    protected void importLdif(InputStream in) throws Exception {
        try {
            for (LdifEntry ldifEntry : new LdifReader(in)) {
                getRootDSE().add(
                        new DefaultServerEntry(getRootDSE().getDirectoryService().getRegistries(),
                                ldifEntry.getEntry()));
            }
        } catch (Exception e) {
            String msg = "failed while trying to parse system ldif file";
            Exception ne = new LdapConfigurationException(msg, e);
            throw ne;
        }
    }

    /**
     * Inject an ldif String into the server. DN must be relative to the root.
     *
     * @param ldif
     *            the entries to inject
     * @throws Exception
     *             if the entries cannot be added
     */
    protected void injectEntries(String ldif) throws Exception {
        LdifReader reader = new LdifReader();
        List<LdifEntry> entries = reader.parseLdif(ldif);

        for (LdifEntry entry : entries) {
            getRootDSE().add(
                    new DefaultServerEntry(getRootDSE().getDirectoryService().getRegistries(),
                            entry.getEntry()));
        }
    }

    /**
     * @return the doDelete
     */
    public boolean isDoDelete() {
        return doDelete;
    }

    /**
     * Loads an LDIF from an input stream and adds the entries it contains to the server. It appears
     * as though the administrator added these entries to the server.
     *
     * @param in
     *            the input stream containing the LDIF entries to load
     * @param verifyEntries
     *            whether or not all entry additions are checked to see if they were in fact
     *            correctly added to the server
     * @return a list of entries added to the server in the order they were added
     * @throws Exception
     *             of the load fails
     */
    protected List<LdifEntry> loadLdif(InputStream in, boolean verifyEntries) throws Exception {
        if (in == null) {
            return EMPTY_LIST;
        }

        LdifReader ldifReader = new LdifReader(in);
        ArrayList<LdifEntry> entries = new ArrayList<LdifEntry>();

        for (LdifEntry entry : ldifReader) {
            getRootDSE()
                    .add(new DefaultServerEntry(getDirectoryService().getRegistries(), entry
                    .getEntry()));

            if (verifyEntries) {
                verify(entry);
                LOG.info("Successfully verified addition of entry " + entry.getDn());
            } else {
                LOG.info("Added entry " + entry.getDn() + " without verification");
            }

            entries.add(entry);
        }

        return entries;
    }

    /**
     * If there is an LDIF file with the same name as the test class but with the .ldif extension
     * then it is read and the entries it contains are added to the server. It appears as though the
     * administor adds these entries to the server.
     *
     * @param verifyEntries
     *            whether or not all entry additions are checked to see if they were in fact
     *            correctly added to the server
     * @return a list of entries added to the server in the order they were added
     * @throws Exception
     *             of the load fails
     */
    protected List<LdifEntry> loadTestLdif(boolean verifyEntries) throws Exception {
        return loadLdif(getClass().getResourceAsStream(getClass().getSimpleName() + ".ldif"),
                verifyEntries);
    }

    /**
     * Sets the contexts of this class taking into account the extras and overrides properties.
     *
     * @param env
     *            an environment to use while setting up the system root.
     * @throws Exception
     *             if there is a failure of any kind
     */
    protected void setContexts(Hashtable<String, Object> env) throws Exception {
        Hashtable<String, Object> envFinal = new Hashtable<String, Object>(env);
        envFinal.put(Context.PROVIDER_URL, ServerDNConstants.SYSTEM_DN);
        setSysRoot(new InitialLdapContext(envFinal, null));

        envFinal.put(Context.PROVIDER_URL, "");
        setRootDSE(getDirectoryService().getAdminSession());

        envFinal.put(Context.PROVIDER_URL, ServerDNConstants.OU_SCHEMA_DN);
        setSchemaRoot(new InitialLdapContext(envFinal, null));
    }

    /**
     * Sets the contexts for this base class. Values of user and password used to set the respective
     * JNDI properties. These values can be overriden by the overrides properties.
     *
     * @param user
     *            the username for authenticating as this user
     * @param passwd
     *            the password of the user
     * @throws Exception
     *             if there is a failure of any kind
     */
    protected void setContexts(String user, String passwd) throws Exception {
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(DirectoryService.JNDI_KEY, getDirectoryService());
        env.put(Context.SECURITY_PRINCIPAL, user);
        env.put(Context.SECURITY_CREDENTIALS, passwd);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName());
        setContexts(env);
    }

    /**
     * @param directoryService
     *            the directoryService to set
     */
    public void setDirectoryService(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    /**
     * @param doDelete
     *            the doDelete to set
     */
    public void setDoDelete(boolean doDelete) {
        this.doDelete = doDelete;
    }

    /**
     * @param ldapServer
     *            the ldapServer to set
     */
    public void setLdapServer(LdapServer ldapServer) {
        this.ldapServer = ldapServer;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @param rootDSE
     *            the rootDSE to set
     */
    public void setRootDSE(CoreSession rootDSE) {
        this.rootDSE = rootDSE;
    }

    /**
     * @param schemaRoot
     *            the schemaRoot to set
     */
    public void setSchemaRoot(LdapContext schemaRoot) {
        this.schemaRoot = schemaRoot;
    }

    /**
     * @param socketAcceptor
     *            the socketAcceptor to set
     */
    public void setSocketAcceptor(NioSocketAcceptor socketAcceptor) {
        this.socketAcceptor = socketAcceptor;
    }

    /**
     * @param sysRoot
     *            the sysRoot to set
     */
    public void setSysRoot(LdapContext sysRoot) {
        this.sysRoot = sysRoot;
    }

    /**
     * Get's the initial context factory for the provider's ou=system context root.
     *
     * @throws Exception
     *             if there is a failure of any kind
     */
    protected void setUp() throws Exception {
        setStart(getStart() + 1);
        setDirectoryService(new DefaultDirectoryService());
        getDirectoryService().setShutdownHookEnabled(false);
        setPort(AvailablePortFinder.getNextAvailable(1024));
        setLdapServer(new LdapServer());
        getLdapServer().setTransports(new TcpTransport(getPort()));
        getLdapServer().setDirectoryService(getDirectoryService());

        setupSaslMechanisms(getLdapServer());
        getDirectoryService().setWorkingDirectory(
                new File("target" + File.separator + "server-work"));
        doDelete(getDirectoryService().getWorkingDirectory());
        configureDirectoryService();
        getDirectoryService().startup();

        configureLdapServer();

        // TODO shouldn't this be before calling configureLdapServer() ???
        getLdapServer().addExtendedOperationHandler(new StartTlsHandler());
        getLdapServer().addExtendedOperationHandler(new StoredProcedureExtendedOperationHandler());

        getLdapServer().start();
        setContexts(ServerDNConstants.ADMIN_SYSTEM_DN, "secret");
    }

    /**
     * @param server
     *            The server
     */
    private void setupSaslMechanisms(LdapServer server) {
        HashMap<String, MechanismHandler> mechanismHandlerMap = new HashMap<String, MechanismHandler>();

        mechanismHandlerMap.put(SupportedSaslMechanisms.PLAIN, new PlainMechanismHandler());

        CramMd5MechanismHandler cramMd5MechanismHandler = new CramMd5MechanismHandler();
        mechanismHandlerMap.put(SupportedSaslMechanisms.CRAM_MD5, cramMd5MechanismHandler);

        DigestMd5MechanismHandler digestMd5MechanismHandler = new DigestMd5MechanismHandler();
        mechanismHandlerMap.put(SupportedSaslMechanisms.DIGEST_MD5, digestMd5MechanismHandler);

        GssapiMechanismHandler gssapiMechanismHandler = new GssapiMechanismHandler();
        mechanismHandlerMap.put(SupportedSaslMechanisms.GSSAPI, gssapiMechanismHandler);

        NtlmMechanismHandler ntlmMechanismHandler = new NtlmMechanismHandler();
        // TODO - set some sort of default NtlmProvider implementation here
        // ntlmMechanismHandler.setNtlmProvider( provider );
        // TODO - or set FQCN of some sort of default NtlmProvider implementation here
        // ntlmMechanismHandler.setNtlmProviderFqcn( "com.foo.BarNtlmProvider" );
        mechanismHandlerMap.put(SupportedSaslMechanisms.NTLM, ntlmMechanismHandler);
        mechanismHandlerMap.put(SupportedSaslMechanisms.GSS_SPNEGO, ntlmMechanismHandler);

        getLdapServer().setSaslMechanismHandlers(mechanismHandlerMap);
    }

    /**
     * Sets the system context root to null.
     *
     * @throws Exception
     *             Exception.
     */
    protected void tearDown() throws Exception {
        getLdapServer().stop();
        try {
            getDirectoryService().shutdown();
        } catch (Exception e) {
        }

        setSysRoot(null);
    }

    /**
     * Verifies that an entry exists in the directory with the specified attributes.
     *
     * @param entry
     *            the entry to verify
     * @throws Exception
     *             if there are problems accessing the entry
     */
    protected void verify(LdifEntry entry) throws Exception {
        Entry readEntry = getRootDSE().lookup(entry.getDn());

        for (EntryAttribute readAttribute : readEntry) {
            String id = readAttribute.getId();
            EntryAttribute origAttribute = entry.getEntry().get(id);

            for (Value<?> value : origAttribute) {
                if (!readAttribute.contains(value)) {
                    LOG.error("Failed to verify entry addition of " + entry.getDn() + ". " + id
                            + " attribute in original entry missing from read entry.");
                    throw new AssertionError("Failed to verify entry addition of " + entry.getDn());
                }
            }
        }
    }
}
