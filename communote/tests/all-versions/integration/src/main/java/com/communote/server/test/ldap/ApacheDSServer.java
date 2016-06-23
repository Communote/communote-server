package com.communote.server.test.ldap;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.xdbm.Index;
import org.testng.Assert;

/**
 * Wrapper server for {@link AbstractApacheDSServer}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ApacheDSServer extends AbstractApacheDSServer {

    /**
     * Add a new partition to the server
     *
     * @param partitionId
     *            The partition Id
     * @param partitionDn
     *            The partition DN
     * @param attributes
     *            Attributes.
     * @return The newly added partition
     * @throws Exception
     *             If the partition can't be added
     */
    private Partition addPartition(String partitionId, String partitionDn, String... attributes)
            throws Exception {
        // Create a new partition named 'foo'.
        Partition partition = new JdbmPartition();
        partition.setId(partitionId);
        partition.setSuffix(partitionDn);
        getDirectoryService().addPartition(partition);
        HashSet<Index<?, ServerEntry>> indexedAttributes = new HashSet<Index<?, ServerEntry>>();
        for (String attribute : attributes) {
            indexedAttributes.add(new JdbmIndex<String, ServerEntry>(attribute));
        }
        ((JdbmPartition) partition).setIndexedAttributes(indexedAttributes);
        return partition;
    }

    /**
     *
     * @return Hashtable with environment parameters.
     * @throws Exception
     *             Exception.
     */
    public Hashtable<?, ?> getEnvironment() throws Exception {
        return getWiredContext().getEnvironment();
    }

    /**
     * {@inheritDoc}
     */
    public void importLdifFromStream(InputStream inputStream) {
        try {
            super.importLdif(inputStream);
        } catch (Exception e) {
            Assert.fail("Failure on import.", e);
        }
    }

    /**
     * super.setUp.
     *
     * @throws Exception
     *             Exception.
     */
    public void start() throws Exception {
        super.setUp();
        addPartition("communote", "dc=communote,dc=com", "objectClass", "ou", "uid");
    }

    /**
     * super.tearDown
     *
     * @throws Exception
     *             Exception.
     */
    public void stop() throws Exception {
        super.tearDown();
    }
}
