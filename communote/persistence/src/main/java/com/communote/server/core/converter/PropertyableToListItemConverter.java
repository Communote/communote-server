package com.communote.server.core.converter;

import com.communote.common.converter.PojoTargetConverter;
import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.model.property.Propertyable;
import com.communote.server.model.property.StringProperty;


/**
 * Converter that extracts the properties of an entity and adds them to the to the list item. The
 * group and the key are joined by a '.'
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <S>
 *            the source type of the conversion
 * @param <T>
 *            the target type of the conversion
 */
public class PropertyableToListItemConverter<S extends Propertyable, T extends IdentifiableEntityData> extends
        PojoTargetConverter<S, T> {

    private final boolean includeProperties;

    /**
     * Create a new converter
     * 
     * @param clazz
     *            the class of the target type
     * @param includeProperties
     *            whether to add the properties to the result. The property key will be constructed
     *            as follows &lt;groupKey&gt;.&lt;propertyKey&gt; If false this converter will do
     *            nothing.
     */
    public PropertyableToListItemConverter(Class<T> clazz, boolean includeProperties) {
        super(clazz);
        this.includeProperties = includeProperties;
    }

    @Override
    public void convert(S source, T target) {
        // TODO shouldn't the properties be StringPropertyTOs or something which is aware of the
        // group? Should we use the property management? Is it correct to add the '.' (well some
        // calling code already uses it that way)?
        if (this.includeProperties && source.getProperties() != null) {
            for (StringProperty property : source.getProperties()) {
                target.getProperties().put(
                        property.getKeyGroup() + "." + property.getPropertyKey(),
                        property.getPropertyValue());
            }
        }
    }

}
