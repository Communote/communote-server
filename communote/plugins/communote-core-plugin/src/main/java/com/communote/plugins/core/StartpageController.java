package com.communote.plugins.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which can be used to mark a Controller as a start page providing controller. For this
 * annotation to work the {@link com.communote.plugins.core.views.annotations.UrlMapping} or
 * {@link com.communote.plugins.core.views.annotations.UrlMappings} annotation must be added too.
 * The start page URL will be the first URL retrieved from the other annotations,
 * {@link com.communote.plugins.core.views.annotations.UrlMapping} takes precedence.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StartpageController {

}
