package com.communote.server.core.vo.query.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.property.PropertyConstants;
import com.communote.server.model.property.Propertyable;
import com.communote.server.model.property.StringPropertyConstants;
import com.communote.server.model.user.UserNotePropertyConstants;
import com.communote.server.model.user.note.UserNoteEntityImpl;


/**
 * This filter can be used for filtering properties for a specific filter group and OR
 * concatenation.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PropertyFilter {
    /** Available match modes. */
    public enum MatchMode {
        /** Use on exact match */
        EQUALS("="),
        /** Use, if search property starts with the given value. */
        STARTS_WITH("LIKE", "", "%"),
        /** Use, if search property ends with the given value. */
        ENDS_WITH("LIKE", "%"),
        /** Use, if search property contains the given value. */
        CONTAINS("LIKE", "%", "%"),
        /** Use, if search property does not equals the given value. */
        NOT_EQUALS("<>"),
        /** Use, if the value does not matter, just the pure existence is sufficient */
        EXISTS("");

        private final String assignmentOperator;
        private final String prefix;
        private final String suffix;

        /**
         * @param assignmentOperator
         *            The assignment operator.
         */
        private MatchMode(String assignmentOperator) {
            this(assignmentOperator, StringUtils.EMPTY, StringUtils.EMPTY);
        }

        /**
         * @param assignmentOperator
         *            The assignment operator.
         * @param prefix
         *            The prefix to use.
         */
        private MatchMode(String assignmentOperator, String prefix) {
            this(assignmentOperator, prefix, StringUtils.EMPTY);
        }

        /**
         * @param assignmentOperator
         *            The assignment operator.
         * @param prefix
         *            The prefix to use.
         * @param suffix
         *            The suffix to use.
         */
        private MatchMode(String assignmentOperator, String prefix, String suffix) {
            this.assignmentOperator = assignmentOperator;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        /**
         * @param key
         *            Key of the property.
         * @param value
         *            Value of the property.
         * @param namePrefix
         *            Prefix for the parameters within the named query.
         * @param namedQueryNamesToValuesMapping
         *            Map for adding the named parameter names to their values.
         * @return The query.
         */
        public String renderForNamedQuery(String key, String value, String namePrefix,
                Map<String, String> namedQueryNamesToValuesMapping) {
            String keyNamedQueryName = namePrefix + namedQueryNamesToValuesMapping.size();
            namedQueryNamesToValuesMapping.put(keyNamedQueryName, key);

            String queryFragment =
                    "(" + PROPERTY_ALIAS + "." + PropertyConstants.PROPERTYKEY + " = :"
                            + keyNamedQueryName + " and " + PROPERTY_ALIAS + "."
                            + StringPropertyConstants.PROPERTYVALUE;
            if (EXISTS.equals(this)) {
                queryFragment += " is not null";
            }
            if (!EXISTS.equals(this)) {
                String valueNamedQueryName = namePrefix + namedQueryNamesToValuesMapping.size();
                namedQueryNamesToValuesMapping.put(valueNamedQueryName, prefix + value + suffix);
                queryFragment += " " + assignmentOperator + " :" + valueNamedQueryName;
            }
            queryFragment += ")";
            return queryFragment;
        }
    }

    /** Key for the default group */
    public static final String DEFAULT_KEY_GROUP = "defaultKeyGroup";

    /** Alias for the property */
    private static final String PROPERTY_ALIAS = "property";

    /**
     * Check if the value matches the property
     * 
     * @param matchMode
     *            the match mode to use
     * @param value
     *            the value to check
     * @param internalValue
     *            the internal value to check against
     * @return true if it matches
     */
    private static boolean match(MatchMode matchMode, String value, String internalValue) {
        boolean result;
        switch (matchMode) {
        case CONTAINS:
            result = value.contains(internalValue);
            break;
        case ENDS_WITH:
            result = value.endsWith(internalValue);
            break;
        case STARTS_WITH:
            result = value.startsWith(internalValue);
            break;
        case EQUALS:
            result = value.equals(internalValue);
            break;
        case NOT_EQUALS:
            result = !value.equals(internalValue);
            break;
        case EXISTS:
            // the value doesnt matter, only keygroup and key must match
            result = true;
            break;
        default:
            result = false;
            break;
        }
        return result;
    }

    private final String propertySQLNamePrefix = ("propertyFilter" + this.hashCode()).replace("-",
            "A");

    private final String keyGroupQueryName;
    private final Collection<String> renderedProperties = new ArrayList<String>();

    private final Collection<Object[]> properties = new ArrayList<Object[]>();

    private final Map<String, String> namedQueryNamesToValues = new HashMap<String, String>();

    private final Class<? extends Propertyable> propertyClass;

    private final boolean negate;

    private String keyGroup;

    /**
     * Constructor, which uses {@link #DEFAULT_KEY_GROUP} as key group.
     * 
     * @param propertyClass
     *            class this filter is used for.
     * @param <P>
     *            Type of the property this filter is for.
     */
    public <P extends Propertyable> PropertyFilter(Class<P> propertyClass) {
        this(DEFAULT_KEY_GROUP, propertyClass);
    }

    /**
     * Constructor.
     * 
     * @param keyGroup
     *            The key group this filter is used for.
     * @param propertyClass
     *            class this filter is used for.
     * @param <P>
     *            Type of the property this filter is for.
     */
    public <P extends Propertyable> PropertyFilter(String keyGroup, Class<P> propertyClass) {
        this(keyGroup, propertyClass, false);
    }

    /**
     * Constructor.
     * 
     * @param keyGroup
     *            The key group this filter is used for.
     * @param propertyClass
     *            class this filter is used for.
     * @param negate
     *            true if the property matching should be negated, e.g. to find an entity without a
     *            particular property
     * @param <P>
     *            Type of the property this filter is for.
     */
    public <P extends Propertyable> PropertyFilter(String keyGroup, Class<P> propertyClass,
            boolean negate) {
        this.keyGroup = keyGroup;
        this.propertyClass = propertyClass;
        this.keyGroupQueryName = this.propertySQLNamePrefix + "keyGroup";
        this.negate = negate;
        namedQueryNamesToValues.put(this.keyGroupQueryName, keyGroup);
    }

    /**
     * Adds a property for comparison.
     * 
     * @param key
     *            The key.
     * @param value
     *            The value.
     * @param matchMode
     *            The match mode.
     * @return The object itself.
     */
    public PropertyFilter addProperty(String key, String value, MatchMode matchMode) {
        renderedProperties.add(matchMode.renderForNamedQuery(key, value, propertySQLNamePrefix,
                namedQueryNamesToValues));
        properties.add(new Object[] { key, value, matchMode });
        return this;
    }

    /**
     * @return The map named query parameters and their values.
     */
    public Map<String, String> getNamedQueryNamesToValuesMap() {
        return namedQueryNamesToValues;
    }

    /**
     * Method to get the properties.
     * 
     * @return The properties where each element is an array of objects in the form [key:String,
     *         value:String, matchMode:MatchMode].
     */
    public Collection<Object[]> getProperties() {
        return properties;
    }

    /**
     * @return True, if this has properties, else false.
     */
    public boolean hasProperties() {
        return !renderedProperties.isEmpty();
    }

    /**
     * @return <code>True</code>, if this filter should be negated.
     */
    public boolean isNegate() {
        return negate;
    }

    /**
     * Method to match a given key group, key and value against the internal data.
     * 
     * @param keyGroup
     *            The key group to match.
     * @param key
     *            The key to match.
     * @param value
     *            The value to match.
     * @return <code>True</code>, if the given data matches this filter.
     */
    public boolean matches(String keyGroup, String key, String value) {
        if (!this.keyGroup.equals(keyGroup)) {
            return false;
        }
        // Format of property: [key, value, matchMode]
        // All properties are OR combined.
        boolean result = false;
        loop: for (Object[] property : properties) {
            String internalKey = property[0].toString();
            if (!internalKey.equals(key)) {
                continue loop;
            }
            String internalValue = null;
            if (property[1] != null) {
                internalValue = property[1].toString();
            }
            MatchMode matchMode = (MatchMode) property[2];
            result = match(matchMode, value, internalValue);
            if (result) {
                break loop;
            }
        }
        return negate ^ result;
    }

    /**
     * This method generates the query string for this property. To avoid errors you always should
     * use {@link #hasProperties()} before calling this method.
     * 
     * @param alias
     *            The alias of the outer class, which id should be matched.
     * @return The query string including the keys and values suitable for named queries. The
     *         mappings are available with {@link #getNamedQueryNamesToValuesMap()}
     */
    public String toQueryString(String alias) {
        if (!alias.endsWith(".")) {
            alias = alias + ".";
        }
        String queryFragment = isNegate() ? "not " : "";
        queryFragment += "exists (SELECT " + PROPERTY_ALIAS + " FROM ";
        if (UserNoteEntityImpl.class.equals(this.propertyClass)) {
            queryFragment += UserNotePropertyConstants.CLASS_NAME + " " + PROPERTY_ALIAS
                    + " WHERE " + PROPERTY_ALIAS + "." + UserNotePropertyConstants.NOTE + ".id = "
                    + alias + "id " + " AND " + PROPERTY_ALIAS + "."
                    + UserNotePropertyConstants.USER + ".id = "
                    + SecurityHelper.assertCurrentUserId();
        } else {
            queryFragment += propertyClass.getName()
                    + " propertyClass INNER JOIN propertyClass.properties " + PROPERTY_ALIAS
                    + " WHERE propertyClass.id = " + alias + "id ";
        }

        queryFragment += " AND " + PROPERTY_ALIAS
                + ".keyGroup = :" + this.keyGroupQueryName + " AND ("
                + StringUtils.join(renderedProperties, " OR ")
                + "))";
        return queryFragment;
    }
}
