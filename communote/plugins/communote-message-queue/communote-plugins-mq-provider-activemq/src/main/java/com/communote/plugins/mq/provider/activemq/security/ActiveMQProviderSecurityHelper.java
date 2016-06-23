package com.communote.plugins.mq.provider.activemq.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.activemq.filter.DestinationMapEntry;
import org.apache.activemq.security.AuthorizationEntry;
import org.apache.activemq.security.AuthorizationPlugin;
import org.apache.activemq.security.DefaultAuthorizationMap;

/**
 * Security Helper for outsourcing some logic
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public final class ActiveMQProviderSecurityHelper {

    /**
     * Wildcard for a queue name match. .> matches all (like a '*')
     */
    private static final String QUEUE_NAME_MATCH_ALL = ".>";

    /**
     * Creates the authorization pluging
     * 
     * @param queueName
     *            the name of the queue
     * @param replyQueueName
     *            the name of the reply queue
     * @return the plugin
     * @throws Exception
     *             exception
     */
    public static AuthorizationPlugin createAuthorizationPlugin(String queueName,
            String replyQueueName)
            throws Exception {
        AuthorizationEntry cntQueueEntry = new AuthorizationEntry();
        cntQueueEntry.setQueue(queueName);
        cntQueueEntry.setAdmin("users");
        cntQueueEntry.setRead("users");
        cntQueueEntry.setWrite("users");

        AuthorizationEntry cntQueuereplyEntry = new AuthorizationEntry();
        cntQueuereplyEntry.setQueue(replyQueueName + QUEUE_NAME_MATCH_ALL);
        cntQueuereplyEntry.setAdmin("users");
        cntQueuereplyEntry.setRead("users");
        cntQueuereplyEntry.setWrite("users");

        AuthorizationEntry advisoryEntry = new AuthorizationEntry();
        advisoryEntry.setTopic("ActiveMQ.Advisory" + QUEUE_NAME_MATCH_ALL);
        advisoryEntry.setAdmin("admins, users");
        advisoryEntry.setRead("admins, users");
        advisoryEntry.setWrite("admins, users");

        @SuppressWarnings("rawtypes")
        List<DestinationMapEntry> entries = new ArrayList<DestinationMapEntry>();

        entries.add(cntQueueEntry);
        entries.add(cntQueuereplyEntry);
        entries.add(advisoryEntry);

        DefaultAuthorizationMap defaultAuthorizationMap = new DefaultAuthorizationMap();
        defaultAuthorizationMap.setAuthorizationEntries(entries);

        AuthorizationPlugin authorizationPlugin = new AuthorizationPlugin();
        authorizationPlugin.setMap(defaultAuthorizationMap);

        return authorizationPlugin;
    }

    /**
     * Helper class
     */
    private ActiveMQProviderSecurityHelper() {

    }
}
