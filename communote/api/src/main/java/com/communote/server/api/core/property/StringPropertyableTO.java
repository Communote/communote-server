package com.communote.server.api.core.property;

import java.util.Set;

/**
 * Value object which has properties with string vales.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface StringPropertyableTO {

    /**
     * This method return the properties of the object
     *
     * @return the properties of the object
     */
    public Set<StringPropertyTO> getObjectProperties();

}
