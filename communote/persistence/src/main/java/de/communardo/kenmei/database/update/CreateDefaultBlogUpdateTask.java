/**
 *
 */
package de.communardo.kenmei.database.update;

import java.util.List;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Creates the default blog for clients where there is none yet.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CreateDefaultBlogUpdateTask implements CustomTaskChange {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
        // on new clients where the schema was just created do nothing, because a client manager is
        // required to create the public blog. The default blog will be created in the final
        // initialization step of a new client
        List<User> users = ServiceLocator.findService(UserManagement.class)
                .findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                        UserStatus.ACTIVE);
        if (users.isEmpty()) {
            // stop here because the client creation was not completed
            return;
        }

        try {
            ServiceLocator.findService(BlogManagement.class).createDefaultBlog(
                    ClientHelper.getCurrentClient().getName());
        } catch (NonUniqueBlogIdentifierException e) {
            throw new CustomChangeException("Creation of the default blog failed", e);
        } catch (BlogIdentifierValidationException e) {
            throw new CustomChangeException("Creation of the default blog failed", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        // nothing
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
