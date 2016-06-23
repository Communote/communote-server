package com.communote.server.core.messaging.connectors.xmpp;

import org.jivesoftware.smack.packet.PacketExtension;

/**
 * This bean class represents the alias in a XMPP packet.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AliasPacketExtension implements PacketExtension {

    /** The alias elements name. */
    public static final String ALIAS_ELEMENT_NAME = "communardo_alias";
    /** The alias elements namespace. */
    public static final String ALIAS_NAMESPACE = "http://communardo.de/protocol/xmpp/alias";
    /** The alias elements attributes (child element) name. */
    public static final String ALIAS_ATTRIBUTE_NAME = "value";
    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = 1L;
    private String value = null;

    /**
     * Empty constructor needed for introspection.
     */
    public AliasPacketExtension() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    public String getElementName() {
        return ALIAS_ELEMENT_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public String getNamespace() {
        return ALIAS_NAMESPACE;
    }

    /**
     * Returns the alias.
     * 
     * @return as {@link String}
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the alias.
     * 
     * @param value
     *            as {@link String}
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public String toXML() {
        return "<" + ALIAS_ELEMENT_NAME + " xmlns=\"" + ALIAS_NAMESPACE + "\">"
                + value + "</" + ALIAS_ELEMENT_NAME + ">";
    }
}
