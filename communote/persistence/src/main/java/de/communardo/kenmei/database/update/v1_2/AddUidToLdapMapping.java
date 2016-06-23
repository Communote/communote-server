package de.communardo.kenmei.database.update.v1_2;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import com.communote.server.core.common.ldap.LdapUserAttribute;

/**
 * Update task that adds the {@link LdapUserAttribute#UID} to an existing LDAP propertyMapping. The
 * value will be initialized with the mapped attribute of {@link LdapUserAttribute#ALIAS}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AddUidToLdapMapping implements CustomTaskChange {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database arg0) throws CustomChangeException, UnsupportedChangeException {

        // The ldap configuration has moved (partial) to a plugin, hence the change cannot happen
        // here anymore.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileOpener(FileOpener arg0) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
        // nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // nothing

    }

}
