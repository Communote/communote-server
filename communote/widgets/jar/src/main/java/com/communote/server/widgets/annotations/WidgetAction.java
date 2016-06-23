package com.communote.server.widgets.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to easier call widget actions. Annotated elements may not have an arguments.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WidgetAction {

    /**
     * Use this as the forms action in a hidden input to define the action.
     */
    public static final String ACTION = "widgetAction";

    /**
     * Name of the method to call.
     */
    String value();
}
