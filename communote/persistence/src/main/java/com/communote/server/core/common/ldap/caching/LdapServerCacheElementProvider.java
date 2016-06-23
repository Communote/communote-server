package com.communote.server.core.common.ldap.caching;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.communote.server.core.common.caching.CacheElementProvider;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapServerCacheElementProvider implements
        CacheElementProvider<LdapServerCacheKey, TreeMap<Integer, List<LdapServer>>> {

    /**
     * @return Object.
     */
    @Override
    public String getContentType() {
        return "Object";
    }

    /**
     * @return 3600
     */
    @Override
    public int getTimeToLive() {
        return 3600;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TreeMap<Integer, List<LdapServer>> load(LdapServerCacheKey key) {
        TreeMap<Integer, List<LdapServer>> serversMap = new TreeMap<Integer, List<LdapServer>>();
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns:");
            DirContext ctx = new InitialDirContext(env);
            Attributes attributes = ctx.getAttributes(key.getQueryPrefix() + key.getDomain(),
                    new String[] { "SRV" });
            Attribute attribute = attributes.get("SRV");
            int oldPriority = -1;
            int oldWeight = 0;
            for (int i = 0; i < attribute.size(); i++) {
                String srvRecord = attribute.get(i).toString();
                // each SRV record is in the format "PRIORITY WEIGHT PORT HOST.",
                // i.e. "0 100 389 dc1.company.com."
                String[] server = srvRecord.split(" ");
                String serverHost = server[3];
                serverHost = serverHost.endsWith(".") ? serverHost.substring(0,
                        serverHost.length() - 1) : serverHost;
                int currentPriority = Integer.parseInt(server[0]);
                List<LdapServer> serversList = serversMap.get(currentPriority);
                if (serversList == null) {
                    serversList = new ArrayList<LdapServer>();
                    serversMap.put(currentPriority, serversList);
                }
                if (currentPriority != oldPriority) {
                    oldPriority = currentPriority;
                    oldWeight = 0;
                }
                oldWeight = oldWeight + Integer.parseInt(server[1]);
                serversList
                        .add(new LdapServer("ldap://" + serverHost + ":" + server[2], oldWeight));
            }
            ctx.close();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return serversMap;
    }

}
