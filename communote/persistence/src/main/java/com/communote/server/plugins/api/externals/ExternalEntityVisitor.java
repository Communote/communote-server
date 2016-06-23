package com.communote.server.plugins.api.externals;

/**
 * Interface for implementing the visitor pattern for visiting external entities.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a> <T>
 *         Type of the entity.
 * 
 * @param <T>
 *            Type of the entity to visit.
 */
public interface ExternalEntityVisitor<T> {
    /**
     * Method, which is called, whenever an entity is visited.
     * 
     * @param entity
     *            The entity.
     * @throws Exception
     *             Thrown, when something went wrong.
     */
    void visit(T entity) throws Exception;
}
