package com.communote.server.widgets.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.widgets.Widget;

/**
 * Could be used for static import, because multiple inheritance is not possible.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AnnotatedWidgetProcesser {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatedWidgetProcesser.class);

    private static Map<String, Method> METHOD_MAPPING = new HashMap<String, Method>();

    /**
     * Searches for the given method.
     * 
     * @param widgetAction
     *            The action
     * @param <W>
     *            class of type widget.
     * @param widget
     *            The calling widget
     * @return The method or null if non was found.
     */
    private static <W extends Widget> Method findMethod(String widgetAction, W widget) {
        String identifier = widget.getClass().getName() + "#" + widgetAction;
        Method foundMethod = METHOD_MAPPING.get(identifier);
        if (foundMethod != null) {
            return foundMethod;
        }
        for (Method method : widget.getClass().getMethods()) {
            WidgetAction action = method.getAnnotation(WidgetAction.class);
            if (action != null && action.value().equals(widgetAction)) {
                METHOD_MAPPING.put(identifier, method);
                foundMethod = method;
                break;
            }
        }
        return foundMethod;
    }

    /**
     * Processes the selected method. Calls only the first method found with the selected name.
     * 
     * @param <W>
     *            class of type widget.
     * @param widget
     *            The widget.
     */
    public static <W extends Widget> void processActions(W widget) {
        String widgetAction = widget.getParameter(WidgetAction.ACTION);
        if (StringUtils.isEmpty(widgetAction)) {
            return;
        }
        Method foundMethod = findMethod(widgetAction, widget);
        if (foundMethod != null) {
            try {
                foundMethod.invoke(widget, ArrayUtils.EMPTY_OBJECT_ARRAY);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Error invoking 'WidgetAction' method: "
                        + widget.getClass().getCanonicalName() + "#" + widgetAction, e);
            } catch (IllegalAccessException e) {
                LOGGER.error("Error invoking 'WidgetAction' method: "
                        + widget.getClass().getCanonicalName() + "#" + widgetAction, e);
            } catch (InvocationTargetException e) {
                LOGGER.error("Error invoking 'WidgetAction' method: "
                        + widget.getClass().getCanonicalName() + "#" + widgetAction, e);
            }
        } else {
            LOGGER.warn("The is no method for called action {} available on widget {}",
                    widgetAction, widget.getClass().getName());
        }
    }

    /**
     * Returns the identifier specified through the {@link ViewIdentifier} annotation and combines
     * it with the specified output type.
     * 
     * @param <W>
     *            class of type widget.
     * @param widget
     *            The widget.
     * @return the identifier with trailing "."
     */
    public static <W extends Widget> String processGetViewIdentifier(W widget) {
        ViewIdentifier identifier = widget.getClass().getAnnotation(ViewIdentifier.class);
        if (identifier == null) {
            throw new IllegalArgumentException("Needed ViewIdentifier annotation not found in "
                    + widget.getClass().getCanonicalName());
        }
        return identifier.value();
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private AnnotatedWidgetProcesser() {
        // Do nothing.
    }
}
