package com.communote.plugins.core.views.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for providing additional information to the page. The controller must be of type
 * {@link com.communote.plugins.core.views.ViewController}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Page {
    /**
     * List of names of the categories which group the CSS resources that should be included when
     * rendering the page. The categories are included in order.
     */
    String[] cssCategories() default { };

    /**
     * List of names of the categories which group the JavaScript resources that should be included
     * when rendering the page. The categories are included in order.
     */
    String[] jsCategories() default { };

    /** The category for which the localized JavaScript messages should be included */
    String jsMessagesCategory() default "";

    /** Key for the menu, this page is for. */
    String menu();

    /**
     * Defines the message key to be used for menu entries directing to this page.
     */
    String menuMessageKey() default "";

    /** Key for the submenu, this page is on */
    String submenu() default "";

    /**
     * Message key for the title to be shown on the page. The message can have a place holder which
     * will be passed the name of the current client. The ViewController will for example expose the
     * message under the variable pageTitle.
     */
    String titleKey() default "";
}
