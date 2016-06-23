package com.communote.common.converter;

/**
 * Convenience base class for converters which convert into target types with a no-args constructor
 * and setters for the members. This class also helps in building a hierarchy of converters that
 * transform one source type into a hierarchy of target types where each sub class of the target
 * types holds more data than its parent.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <S>
 *            the source type of the conversion
 * @param <T>
 *            the target type of the conversion
 */
public abstract class PojoTargetConverter<S, T> implements Converter<S, T> {
    private final Class<T> clazz;

    /**
     * Create a new converter
     * 
     * @param clazz
     *            the class of the target type
     */
    public PojoTargetConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T convert(S source) {
        T target = create();
        convert(source, target);
        return target;
    }

    /**
     * Convert the source into the provided target object
     * 
     * @param source
     *            the source object
     * @param target
     *            the target object
     */
    public abstract void convert(S source, T target);

    /**
     * Create an instance of the target type
     * 
     * @return an instance of the target class
     */
    protected T create() {
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
