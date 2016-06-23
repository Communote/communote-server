package com.communote.common.matcher;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Abstract common class for entity matchers with helper methods.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            Type of the entity to match.
 */
public abstract class Matcher<T> {

    /**
     * Method to filter a given collection for matching elements. Only the matching elements will be
     * returned.
     * 
     * @param entities
     *            The entities to filter.
     * @return A collection of entities, which matches this matcher.
     */
    public Collection<T> filter(Collection<T> entities) {
        Collection<T> result = new ArrayList<T>();
        for (T entity : entities) {
            if (matches(entity)) {
                if (entity instanceof Matchable) {
                    ((Matchable) entity).setMatched(true);
                }
                result.add(entity);
            }
        }
        return result;
    }

    /**
     * Marks all matched entities as matched.
     * <p>
     * <b>Note:</b> The entities must implement the {@link Matchable} interface, else this method
     * will immediately return.
     * </p>
     * 
     * @param entities
     *            Entities to mark.
     */
    public void markMatching(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        if (!(entities.iterator().next() instanceof Matchable)) {
            return; // Fast exit, if the entities don't implement Matched.
        }
        for (T entity : entities) {
            ((Matchable) entity).setMatched(matches(entity));
        }
    }

    /**
     * Method to check an entity.
     * 
     * @param entity
     *            The entity to match.
     * @return <code>True</code>, if the entity matches, else false.
     */
    public abstract boolean matches(T entity);

}
