package com.communote.plugins.core.views;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface PluginViewController {
    /**
     * Name that resolves to the template of the main page.
     */
    public static final String DEFAULT_MAIN_TEMPLATE = "core.layout.main.wrapper";
    /**
     * Name that resolves to the template of the administration main page.
     */
    public static final String DEFAULT_MAIN_TEMPLATE_ADMINISTRATION = "core.layout.administration.wrapper";

    /**
     * @return Path to Velocity template for the content.
     */
    public abstract String getContentTemplate();

    /**
     * The key for the main template.
     *
     * @return "main.wrapper"
     */
    public abstract String getMainTemplate();

    /**
     * @return the symbolicName
     */
    public abstract String getSymbolicName();

}