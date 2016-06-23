package com.communote.plugins.core.views.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation, which can be used to define the URL a specific Controller should listen to.
 * 
 * If the URL starts with /* /admin and the controller extends the AdministrationViewController it
 * will be rendered within the administration menu. If the url starts with /* /admin/application/ it
 * will be only visible and accessible in the global client. (See also
 * DynamicUrlHandlerMappingRegistry)
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UrlMapping {

    /** Path of the url. */
    String value();
}
