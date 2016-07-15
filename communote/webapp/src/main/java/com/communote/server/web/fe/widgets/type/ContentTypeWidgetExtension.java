package com.communote.server.web.fe.widgets.type;

import java.util.HashSet;
import java.util.Set;

import com.communote.server.web.fe.widgets.extension.WidgetExtension;

/**
 * This class allows to add new content types to Communote, for instance "Video", "Image" or
 * "Document". Each content type can be part of one ore more categories. Content types can be used
 * in conjunction with the {@link ContentTypeWidget} to allow filtering for content types.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ContentTypeWidgetExtension implements
        WidgetExtension<ContentTypeWidgetExtension, ContentTypeWidgetExtensionManagement> {

    /** Default category. */
    public final static String CATEGORY_DEFAULT = "default";

    /** Category for the CPL, the feed. */
    public final static String CATEGORY_NOTE_LIST = "notes";

    private final String alias;
    private final String titleKey;
    private final String propertyFilter;
    private final int order;
    private final Set<String> categories = new HashSet<String>();

    /**
     * Constructor.
     *
     * @param alias
     *            Alias of this content type.
     * @param titleKey
     *            Key for the title.
     * @param propertyFilter
     *            Property filter. See {@link #getPropertyFilter()} for details.
     * @param categories
     *            Categories, this content type can be used in.
     * @param order
     *            Value indicating the order of this content type.
     */
    public ContentTypeWidgetExtension(String alias, String titleKey, String propertyFilter,
            int order, String... categories) {
        this.alias = alias;
        this.titleKey = titleKey;
        this.propertyFilter = propertyFilter;
        this.order = order;
        if (categories == null || categories.length == 0) {
            this.getCategories().add(CATEGORY_DEFAULT);
        } else {
            for (String category : categories) {
                this.getCategories().add(category);
            }
        }
    }

    /**
     * Constructor with default order.
     *
     * @param alias
     *            Alias of this content type.
     * @param titleKey
     *            Key for the title.
     * @param categories
     *            Categories, this content type can be used in.
     * @param propertyFilter
     *            Property filter. See getPropertyFilter for details.
     */
    public ContentTypeWidgetExtension(String alias, String titleKey, String propertyFilter,
            String... categories) {
        this(alias, titleKey, propertyFilter, DEFAULT_ORDER_VALUE, categories);
    }

    /**
     * @return Alias of this content type.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return Categories, this content type can be used in.
     */
    public Set<String> getCategories() {
        return categories;
    }

    /**
     * @return ContentTypeWidgetExtensionManagement.class
     */
    @Override
    public Class<ContentTypeWidgetExtensionManagement> getManagementType() {
        return ContentTypeWidgetExtensionManagement.class;
    }

    /**
     * @return Value indicating the order of this content type.
     */
    @Override
    public int getOrder() {
        return order;
    }

    /**
     * The filter has to have the following format:
     * <b>['Property','Group','Key','Value','MatchMode','Negate (Optional)']</b> <br>
     * The elements are
     * <ul>
     * <li><b>Property</b>: Class, the property is for, this can be one of "Note", "Entity" or
     * "Dummy".</li>
     * <li><b>Group</b>: The key group of this property. See
     * {@link com.communote.server.api.core.property.property.PropertyManagement}.</li>
     * <li><b>Key</b>: The key of the property. See
     * {@link com.communote.server.api.core.property.property.PropertyManagement}.</li>
     * <li><b>Value</b>: The value to compare of the property. See
     * {@link com.communote.server.api.core.property.property.PropertyManagement}.</li>
     * <li><b>MatchMode</b>: The name of one of
     * {@link com.communote.server.core.vo.query.filter.PropertyFilter.MatchMode}</li>
     * <li><b>Negate</b>: If true, the filter will be negated. Default is false.</li>
     * </ul>
     *
     * @see {@link com.communote.server.web.fe.widgets.type.TimelineQueryParametersConfigurator} for
     *      more information.
     *
     * @return The property filter for this content type.
     */
    public String getPropertyFilter() {
        return propertyFilter;
    }

    /**
     * @return Key for the title of the content type..
     */
    public String getTitleKey() {
        return titleKey;
    }

}
