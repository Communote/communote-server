package com.communote.server.api.core.config;

/**
 * Interface marking a configuration property constant.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface ConfigurationPropertyConstant {

    /**
     * String representation of the constant to be used as key in Properties objects.
     *
     * @return the constant as string
     */
    String getKeyString();

}
