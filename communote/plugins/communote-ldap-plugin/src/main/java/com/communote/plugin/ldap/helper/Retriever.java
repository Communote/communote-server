package com.communote.plugin.ldap.helper;

import java.util.Collection;
import java.util.List;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.directory.SearchControls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AggregateDirContextProcessor;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.GreaterThanOrEqualsFilter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.communote.server.model.config.LdapSearchBaseDefinition;


/**
 * Abstract class for retrieving entities from LDAP/AD with common functionality.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <T>
 *            The type of the results.
 * @deprecated Use {@link VisitingRetriever} and visitor pattern instead.
 */
@Deprecated
public abstract class Retriever<T> extends VisitingRetriever<T> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Retriever.class);

    private boolean noEntitiesLeft = false;
    private final int pagingSize;
    private final ContextSourceTransactionManager contextSourceTransactionManager;
    private PagedResultsDirContextProcessor pagedResultsControl;
    private final LdapTemplate ldapTemplate;
    private int currentSearchBaseIndex = 0;

    private TransactionDefinition transactionDefinition = new DefaultTransactionDefinition(
            TransactionDefinition.PROPAGATION_REQUIRED);

    private final long internalHighestCommittedUSN;

    private final long externalHighestCommittedUSN;

    private final boolean isPagingAllowed;

    /**
     * Constructor.
     * 
     * @param ldapTemplate
     *            The template for LDAP.
     * @param internalHighestCommittedUSN
     *            The usn to use.
     * @param externalHighestCommittedUSN
     *            The usn from the external system.
     * @param pagingSize
     *            The maximal size of requestet pages.
     * @param timeout
     *            The timeout for connections.
     * @param isPagingAllowed
     *            True, if paging is allowed.
     * 
     */
    public Retriever(LdapTemplate ldapTemplate, int timeout, int pagingSize,
            long internalHighestCommittedUSN, long externalHighestCommittedUSN,
            boolean isPagingAllowed) {
        super(ldapTemplate, timeout, pagingSize, internalHighestCommittedUSN, isPagingAllowed);
        this.ldapTemplate = ldapTemplate;
        this.pagingSize = pagingSize;
        this.internalHighestCommittedUSN = internalHighestCommittedUSN;
        this.externalHighestCommittedUSN = externalHighestCommittedUSN;
        this.isPagingAllowed = isPagingAllowed;
        this.contextSourceTransactionManager = new ContextSourceTransactionManager();
        this.contextSourceTransactionManager.setContextSource(ldapTemplate.getContextSource());
        this.pagedResultsControl = new PagedResultsDirContextProcessor(pagingSize);
    }

    /**
     * @return List of next entities for this connection or none if there are no one left.
     * @throws Exception
     *             Exception.
     * */
    public Collection<T> getNextEntities() throws Exception {
        if (externalHighestCommittedUSN <= internalHighestCommittedUSN) {
            return null;
        }
        LdapSearchBaseDefinition searchBase = getSearchBaseDefinitions().get(
                currentSearchBaseIndex);
        Collection<T> entities = null;
        while ((entities = getNextEntities(searchBase)) == null || entities.size() == 0) {
            currentSearchBaseIndex++;
            if (currentSearchBaseIndex >= getSearchBaseDefinitions().size()) {
                return null;
            }
            transactionDefinition = new DefaultTransactionDefinition(
                    TransactionDefinition.PROPAGATION_REQUIRED);
            pagedResultsControl = new PagedResultsDirContextProcessor(pagingSize);
            noEntitiesLeft = false;
            searchBase = getSearchBaseDefinitions().get(currentSearchBaseIndex);
        }
        return entities;
    }

    /**
     * 
     * @param searchBase
     *            The current search base.
     * @return The next entities or null, if this search base hasn't anymore entities.
     * @throws Exception
     *             Exception.
     */
    @SuppressWarnings("unchecked")
    private Collection<T> getNextEntities(LdapSearchBaseDefinition searchBase)
            throws Exception {
        final Name baseDN = new CompositeName(searchBase.getSearchBase());
        SearchControls searchControls = getSearchControl(searchBase.isSearchSubtree());
        AndFilter filter = new AndFilter();
        filter.and(new HardcodedFilter(getSearchFilter()));
        filter.and(new GreaterThanOrEqualsFilter(USN_ATTRIBUTE_KEY, Long
                .toString(internalHighestCommittedUSN + 1)));
        if (isPagingAllowed) {
            return pageSearchResults(baseDN, filter.encode(), searchControls);
        }
        if (noEntitiesLeft) {
            return null;
        }
        List<T> result = ldapTemplate.search(baseDN, filter.encode(), searchControls,
                getContextMapper());
        noEntitiesLeft = true;
        return result;
    }

    /**
     * Paged search.
     * 
     * @param baseDN
     *            The baseDN.
     * @param filter
     *            The search filter.
     * @param searchControls
     *            The searchControls.
     * 
     * @throws Exception
     *             Exception.
     * @return List of entities or null, if there are no more entities.
     */
    private Collection<T> pageSearchResults(Name baseDN, String filter,
            SearchControls searchControls) throws Exception {
        if (noEntitiesLeft) {
            return null;
        }
        TransactionStatus transactionStatus = contextSourceTransactionManager
                .getTransaction(transactionDefinition);
        try {
            CollectingNameClassPairCallbackHandler handler = getCollectingNameClassPairCallbackHandler();

            // setup the LDAP request control(s)
            AggregateDirContextProcessor aggregateDirContextProcessor = new AggregateDirContextProcessor();
            aggregateDirContextProcessor.addDirContextProcessor(pagedResultsControl);

            ldapTemplate.search(baseDN, filter, searchControls,
                    handler, aggregateDirContextProcessor);
            LOGGER.debug("Paging Result size: {}", pagedResultsControl.getPageSize());

            pagedResultsControl = new PagedResultsDirContextProcessor(pagingSize,
                    pagedResultsControl.getCookie());

            List<T> result = handler.getList();
            if (pagedResultsControl.getCookie() != null) {
                byte[] cookie = pagedResultsControl.getCookie().getCookie();
                noEntitiesLeft = cookie == null || cookie.length == 0;
            }
            return result;
        } finally {
            contextSourceTransactionManager.commit(transactionStatus);
        }
    }
}
