package com.communote.common.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstraction to offer functionality for converting whole collections.
 * 
 * @param <S>
 *            the source type of the conversion
 * @param <T>
 *            the target type of the conversion
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class CollectionConverter<S, T> implements Converter<S, T> {
    /**
     * Method to convert a collection of sources to a collection of targets.
     * 
     * @param sources
     *            The sources.
     * @return The result of {@link CollectionConverter#convert(List)
     */
    public Collection<T> convert(Collection<S> sources) {
        return convert(new ArrayList<S>(sources));
    }

    /**
     * Method to convert a list of sources to a collection of targets.
     * 
     * @param sources
     *            The sources.
     * @return List of targets.
     */
    public List<T> convert(List<S> sources) {
        List<T> targets = new ArrayList<T>();
        if (sources != null) {
            for (S source : sources) {
                T target = convert(source);
                if (target != null) {
                    targets.add(target);
                }
            }
        }
        return targets;
    }

    /**
     * Method to convert a set of sources to a set of targets.
     * 
     * @param sources
     *            The sources.
     * @return Set of targets.
     */
    public Set<T> convert(Set<S> sources) {
        Set<T> targets = new HashSet<T>();
        if (sources != null) {
            for (S source : sources) {
                T target = convert(source);
                if (target != null) {
                    targets.add(target);
                }
            }
        }
        return targets;
    }
}
