package com.communote.server.widgets;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.widgets.util.WidgetFactoryUtils;

/**
 * Factory that uses reflection to create widget instances. The FQN of the class of the widget is
 * derived from group and widget name by interpreting the group as a package name and the widget
 * type name as the class name. Any slashes in the group name will be replaced with dots. The class
 * is than loaded with the current class loader.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SimpleWidgetFactory implements WidgetFactory {

    private String widgetPackagePrefix = StringUtils.EMPTY;

    private final Map<String, Class<Widget>> widgetRepository = new HashMap<String, Class<Widget>>();

    @Override
    public Widget createWidget(String widgetGroupName, String widgetName,
            Map<String, String[]> requestParameters) throws WidgetCreationException {
        try {
            Class<Widget> widgetClass = resolveWithCurrentClassLoader(widgetGroupName, widgetName);
            Widget widgetInstance = widgetClass.newInstance();
            return widgetInstance;
        } catch (InstantiationException e) {
            throw new WidgetCreationException("Error instantiating widget " + widgetName
                    + " of group " + widgetGroupName, e);
        } catch (IllegalAccessException e) {
            throw new WidgetCreationException("Illegal access during widget instantiation", e);
        } catch (ClassNotFoundException e) {
            throw new WidgetCreationException("Class not found for widget " + widgetName
                    + " of group " + widgetGroupName, e);
        }
    }

    /**
     * Resolve the widget class by creating the FQCN from the widget group and the widget name. The
     * widget group will be converted into a package name by replacing slashes with dots and the
     * widget name will be used as class name. If a widgetPackagePrefix is defined it will be
     * prepended to the package. The class is loaded using the current class loader (the one that
     * loaded this class).
     *
     * @param widgetGroup
     *            the group of the widget
     * @param widgetName
     *            the name of the widget class without the Widget suffix
     * @return The class for the widget
     * @throws ClassNotFoundException
     *             In case the class has not been found
     */
    private Class<Widget> resolveWithCurrentClassLoader(String widgetGroup, String widgetName)
            throws ClassNotFoundException {
        String className = WidgetFactoryUtils.getClassNameFromGroupAndWidgetName(
                widgetGroup, widgetName, widgetPackagePrefix);
        Class<Widget> widgetClass = widgetRepository.get(className);
        if (widgetClass == null) {
            synchronized (this) {
                if (widgetRepository.get(className) == null) {
                    widgetClass = (Class<Widget>) Class.forName(className);
                    widgetRepository.put(className, widgetClass);
                }
                widgetClass = widgetRepository.get(className);
            }
        }
        return widgetClass;
    }

    /**
     * Set an optional package prefix to interpret the widget group as a relative package and thus
     * only resolve widgets which are in a sub-package of that package.
     *
     * @param widgetPackagePrefix
     *            the package prefix used by this instance to resolve the widget classes
     */
    public void setWidgetPackagePrefix(String widgetPackagePrefix) {
        if (StringUtils.isBlank(widgetPackagePrefix)) {
            widgetPackagePrefix = StringUtils.EMPTY;
        } else {
            widgetPackagePrefix = widgetPackagePrefix.trim();

            if (!widgetPackagePrefix.endsWith(".")) {
                widgetPackagePrefix = widgetPackagePrefix + ".";
            }
        }
        this.widgetPackagePrefix = widgetPackagePrefix;
    }

}
