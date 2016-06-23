package com.communote.server.widgets.util;

import com.communote.common.util.Pair;
import com.communote.server.widgets.Widget;

/**
 * Utils for the widget factory
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class WidgetFactoryUtils {

    /**
     * Create the full qualified widget class name from widget group and widget name. The widget
     * group will be converted into a package name by replacing slashes with dots and the widget
     * name will be used as class name.
     * 
     * @param widgetGroup
     *            the name of the group.
     * @param widgetName
     *            the name of the widget
     * @param packagePrefix
     *            if provided the prefix is prepended to the class-name. Must end with a dot if not
     *            null.
     * @return the class name
     */
    public static String getClassNameFromGroupAndWidgetName(
            String widgetGroup, String widgetName, String packagePrefix) {
        String className = packagePrefix != null ? packagePrefix : "";
        className += widgetGroup.replace('/', '.') + "." + widgetName;
        return className;
    }

    /**
     * Returns the same as {@link #getGroupAndWidgetNameFromClass(Class, String)} with startAfter
     * set to ".widgets.".
     * 
     * @param widgetClass
     *            the class of the widget
     * @return a pair where the left part contains the group name and the right the widget name
     */
    public static Pair<String, String> getGroupAndWidgetNameFromClass(
            Class<? extends Widget> widgetClass) {
        return getGroupAndWidgetNameFromClass(widgetClass, ".widgets.");
    }

    /**
     * Get the widget group and widget name from the full qualified class name of a widget class.
     * The group name will be taken from the package with all dots replaced by slashes and the
     * widget name will be the class name.
     * 
     * Example: if called with the class com.communote.fe.widgets.example.test.TestWidget and
     * ".widgets." as startAfter the group name will be example/test and the widget name TestWidget
     * 
     * @param widgetClass
     *            the widget class
     * @param startAfter
     *            an optional string to start after when extracting the group and widget. The first
     *            occurrence of this string is searched in the package name of the class and if
     *            found only the substring following this string is used for extraction.
     * @return a pair where the left part contains the group name and the right the widget name
     */
    public static Pair<String, String> getGroupAndWidgetNameFromClass(
            Class<? extends Widget> widgetClass,
            String startAfter) {
        Pair<String, String> groupTypeName = new Pair<String, String>(null, null);
        String className = widgetClass.getName();
        String combinedWidgetGroupTypeName = className;
        if (startAfter != null) {
            int idx = className.indexOf(startAfter);
            if (idx > -1) {
                combinedWidgetGroupTypeName = className.substring(idx + startAfter.length());
            }
        }
        int idx = combinedWidgetGroupTypeName.lastIndexOf('.');
        if (idx == -1) {
            groupTypeName.setLeft("");
            groupTypeName.setRight(combinedWidgetGroupTypeName);
        } else {
            groupTypeName.setLeft(combinedWidgetGroupTypeName.substring(0, idx).replace('.', '/'));
            groupTypeName.setRight(combinedWidgetGroupTypeName.substring(idx + 1));
        }
        return groupTypeName;

    }

    private WidgetFactoryUtils() {
        // helper class
    }
}
