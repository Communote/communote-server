package com.communote.server.core.vo.query;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Repository of query definitions containing all queries
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class QueryDefinitionRepository {

    private final static Logger LOG = Logger.getLogger(QueryDefinitionRepository.class);

    /**
     * the one and only instance
     */
    private final static QueryDefinitionRepository INSTANCE = new QueryDefinitionRepository();

    /**
     * The one and only repository instance
     * 
     * @return the instance
     */
    public static QueryDefinitionRepository instance() {
        return INSTANCE;
    }

    private final Map<Class<? extends Query>, Query> repository;

    /**
     * Setup the repository
     */
    private QueryDefinitionRepository() {
        repository = new HashMap<Class<? extends Query>, Query>();
    }

    /**
     * Get the query definition. If it does not exists it will be tried to create it
     * 
     * @param <E>
     *            The type of the definition to get
     * @param clazz
     *            The class of the definition to get
     * @return The definition
     */
    public <E extends Query> E getQueryDefinition(Class<E> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null!");
        }
        E definition = (E) repository.get(clazz);
        if (definition == null) {
            try {
                Constructor<E> constructor = clazz.getConstructor();
                definition = constructor.newInstance();
            } catch (Exception e) {
                LOG.error("Error constructing QueryDefinition for class: " + clazz.getName());
            }
            if (definition == null) {
                throw new IllegalArgumentException(
                        "Was not able to instantiate query definition for " + clazz.getName());
            } else {
                register(definition);
            }
        }
        return definition;
    }

    /**
     * Register a {@link Query}
     * 
     * @param queryDefinition
     *            the one to register
     */
    private synchronized void register(Query queryDefinition) {
        repository.put(queryDefinition.getClass(), queryDefinition);
    }
}
