package com.communote.server.web.commons.view.velocity;

import org.apache.velocity.tools.ToolInfo;
import org.apache.velocity.tools.config.Configuration;
import org.apache.velocity.tools.config.ConfigurationException;
import org.apache.velocity.tools.config.NullKeyException;
import org.apache.velocity.tools.config.ToolConfiguration;

/**
 * ToolConfiguration which works directly on classes and not on class names for better OSGI support.
 * For simplicity some features of the parent class are not supported: checking whether this is an
 * old tool and validating whether initialization works.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClassBasedToolConfiguration extends ToolConfiguration {

    private Class<?> toolClass;
    private String defaultKey;

    public ClassBasedToolConfiguration(Class<?> toolClass) {
        this.toolClass = toolClass;
    }

    @Override
    public void addConfiguration(Configuration config) {
        super.addConfiguration(config);
        // add class info
        if (config instanceof ToolConfiguration) {
            ToolConfiguration that = (ToolConfiguration) config;
            if (that.getToolClass() != null) {
                setClass(that.getToolClass());
            }
        }
    }

    @Override
    public ToolInfo createInfo() {
        ToolInfo info = new ToolInfo(getKey(), getToolClass());
        // following is just copied from parent
        info.restrictTo(getRestrictTo());
        if (getSkipSetters() != null) {
            info.setSkipSetters(getSkipSetters());
        }
        // it's ok to use this here, because we know it's the
        // first time properties have been added to this ToolInfo
        info.addProperties(getPropertyMap());
        return info;
    }

    @Override
    public String getClassname() {
        if (toolClass == null) {
            return null;
        }
        return toolClass.getName();
    }

    @Override
    public String getDefaultKey() {
        // little optimization
        if (this.defaultKey == null) {
            this.defaultKey = super.getDefaultKey();
        }
        return defaultKey;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class getToolClass() {
        return toolClass;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void setClass(Class clazz) {
        this.toolClass = clazz;
    }

    @Override
    public void setClassname(String classname) {
        // ignore
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Tool '");
        out.append(getKey());
        out.append("' ");
        out.append("=> ");
        out.append(getClassname());
        if (getRestrictTo() != null)
        {
            out.append(" only for '");
            out.append(getRestrictTo());
            out.append('\'');
        }
        out.append(" ");
        appendProperties(out);
        return out.toString();
    }

    @Override
    public void validate() {
        super.validate();
        // make sure class and are not null
        if (getToolClass() == null) {
            throw new ConfigurationException(this, "Tool class is null");
        }
        if (getKey() == null) {
            throw new NullKeyException(this);
        }
    }
}
