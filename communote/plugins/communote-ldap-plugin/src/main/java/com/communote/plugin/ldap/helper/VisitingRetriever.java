package com.communote.plugin.ldap.helper;

import java.util.List;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextMapperCallbackHandler;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AggregateDirContextProcessor;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.GreaterThanOrEqualsFilter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.communote.server.core.common.ldap.LdapSearchUtils;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.plugins.api.externals.ExternalEntityVisitor;

/**
 * Abstract class for retrieving entities from LDAP/AD with common functionality through the visitor
 * pattern.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <T>
 *            The type of the results.
 */
public abstract class VisitingRetriever<T> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(VisitingRetriever.class);

    /**
     * USN attribute name. The value can be set with a system property
     * "com.communote.ldap.attribute-name-usnchanged". Default is "uSNChanged"
     */
    protected final static String USN_ATTRIBUTE_KEY = System.getProperty(
            "com.communote.ldap.attribute-name-usnchanged", "uSNChanged");

    private final long internalHighestCommittedUSN;
    private final boolean isPagingAllowed;
    private LdapTemplate ldapTemplate;

    private final ContextSourceTransactionManager contextSourceTransactionManager =
            new ContextSourceTransactionManager();

    private final int timeout;

    private final int pagingSize;

    private final TransactionDefinition transactionDefinition = new DefaultTransactionDefinition(
            TransactionDefinition.PROPAGATION_REQUIRED);

    /**
     * Private constructor to set common variables.
     * 
     * @param internalHighestCommittedUSN
     *            The usn to use.
     * @param pagingSize
     *            The maximal size of requestet pages.
     * @param timeout
     *            The timeout for connections.
     * @param isPagingAllowed
     *            True, if paging is allowed.
     */
    private VisitingRetriever(int timeout, int pagingSize, long internalHighestCommittedUSN,
            boolean isPagingAllowed) {
        this.timeout = timeout;
        this.pagingSize = pagingSize;
        this.internalHighestCommittedUSN = internalHighestCommittedUSN;
        this.isPagingAllowed = isPagingAllowed;
    }

    /**
     * Constructor to set LDAP connection from configuration.
     * 
     * @param ldapConfiguration
     *            Ldap configuration.
     * @param internalHighestCommittedUSN
     *            The usn to use.
     * @param pagingSize
     *            The maximal size of requestet pages.
     * @param timeout
     *            The timeout for connections.
     * @param isPagingAllowed
     *            True, if paging is allowed.
     * @param binaryAttributes
     *            Array of binary attributes.
     * @throws NamingException
     *             Exception.
     */
    public VisitingRetriever(LdapConfiguration ldapConfiguration, int timeout, int pagingSize,
            long internalHighestCommittedUSN, boolean isPagingAllowed,
            String[] binaryAttributes) throws NamingException {
        this(timeout, pagingSize, internalHighestCommittedUSN, isPagingAllowed);
        LdapContextSource targetContextSource = LdapSearchUtils.createLdapContext(
                ldapConfiguration, binaryAttributes);
        ldapTemplate = new LdapTemplate(targetContextSource);
        contextSourceTransactionManager.setContextSource(ldapTemplate.getContextSource());
    }

    /**
     * Constructor.
     * 
     * @param ldapTemplate
     *            The template for LDAP.
     * @param internalHighestCommittedUSN
     *            The usn to use.
     * @param pagingSize
     *            The maximal size of requestet pages.
     * @param timeout
     *            The timeout for connections.
     * @param isPagingAllowed
     *            True, if paging is allowed.
     */
    public VisitingRetriever(LdapTemplate ldapTemplate, int timeout, int pagingSize,
            long internalHighestCommittedUSN, boolean isPagingAllowed) {
        this(timeout, pagingSize, internalHighestCommittedUSN, isPagingAllowed);
        this.ldapTemplate = ldapTemplate;
        contextSourceTransactionManager.setContextSource(ldapTemplate.getContextSource());
    }

    /**
     * This method retrieves all elements and let a visitor visit each of them.
     * 
     * @param visitor
     *            The visitor.
     * @param incremental
     *            If <code>true</code>, the only incremental changes will be considered, else all.
     * @throws RetrieverException
     *             Exception.
     */
    public void accept(ExternalEntityVisitor<T> visitor, boolean incremental)
            throws RetrieverException {
        for (LdapSearchBaseDefinition searchBaseDefinition : getSearchBaseDefinitions()) {
            Name baseDN;
            try {
                baseDN = new CompositeName(searchBaseDefinition.getSearchBase());
            } catch (InvalidNameException e) {
                throw new RetrieverException(e);
            }
            SearchControls searchControls = getSearchControl(searchBaseDefinition.isSearchSubtree());
            AndFilter filter = new AndFilter();
            filter.and(new HardcodedFilter(getSearchFilter()));
            if (incremental) {
                filter.and(new GreaterThanOrEqualsFilter(USN_ATTRIBUTE_KEY, Long
                        .toString(internalHighestCommittedUSN + 1)));
            }
            if (isPagingAllowed) {
                acceptPages(visitor, baseDN, filter.encode(), searchControls);
            } else {
                List<T> entities = ldapTemplate.search(baseDN, filter.encode(), searchControls,
                        getContextMapper());
                for (T entity : entities) {
                    try {
                        visitor.visit(entity);
                    } catch (Exception e) {
                        throw new RetrieverException(e);
                    }
                }
            }
        }
    }

    /**
     * Applies the visitor, in a paged environment.
     * 
     * @param visitor
     *            The visitor.
     * @param baseDN
     *            The base dn.
     * @param filter
     *            The filter.
     * @param searchControls
     *            The search controls to use.
     * @throws RetrieverException
     *             Thrown, when something went wrong.
     */
    private void acceptPages(ExternalEntityVisitor<T> visitor, Name baseDN, String filter,
            SearchControls searchControls) throws RetrieverException {
        TransactionStatus transactionStatus = contextSourceTransactionManager
                .getTransaction(transactionDefinition);
        try {
            PagedResultsCookie cookie = null;
            do {
                CollectingNameClassPairCallbackHandler handler = getCollectingNameClassPairCallbackHandler();
                AggregateDirContextProcessor aggregateDirContextProcessor = new AggregateDirContextProcessor();
                PagedResultsDirContextProcessor pagedResultsControl =
                        new PagedResultsDirContextProcessor(pagingSize, cookie);
                aggregateDirContextProcessor.addDirContextProcessor(pagedResultsControl);
                ldapTemplate.search(baseDN, filter, searchControls, handler,
                        aggregateDirContextProcessor);
                LOGGER.debug("Paging Result size: {}", pagedResultsControl.getPageSize());
                List<T> result = handler.getList();
                for (T entity : result) {
                    visitor.visit(entity);
                }
                cookie = pagedResultsControl.getCookie();
            } while (cookie != null && cookie.getCookie() != null && cookie.getCookie().length > 0);
        } catch (Exception e) {
            throw new RetrieverException(e);
        } finally {
            contextSourceTransactionManager.commit(transactionStatus);
        }
    }

    /**
     * Method to create a handler for converting a list of results as a callback.
     * 
     * @return A handler for converting the results.
     */
    protected CollectingNameClassPairCallbackHandler getCollectingNameClassPairCallbackHandler() {
        return new ContextMapperCallbackHandler(getContextMapper());
    }

    /**
     * Method to return a context mapper for mapping ldap results to the concrete result.
     * 
     * @return The mapper.
     */
    protected abstract ContextMapper getContextMapper();

    /**
     * Method to get the search base definitions.
     * 
     * @return List of definitions.
     */
    protected abstract List<LdapSearchBaseDefinition> getSearchBaseDefinitions();

    /**
     * SearchControl set the searchInSubtree flag, ReturningObj and timeout.
     * 
     * @param searchInSubtree
     *            True, if subtree search is enabled.
     * @return SearchControls Object.
     */
    protected SearchControls getSearchControl(boolean searchInSubtree) {
        SearchControls searchControls = new SearchControls();
        if (searchInSubtree) {
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        } else {
            searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        }
        searchControls.setReturningObjFlag(false);
        searchControls.setTimeLimit(timeout);
        return searchControls;
    }

    /**
     * Method to get the search filter for entities within the repository
     * 
     * @return The search filter as String.
     */
    protected abstract String getSearchFilter();

}
