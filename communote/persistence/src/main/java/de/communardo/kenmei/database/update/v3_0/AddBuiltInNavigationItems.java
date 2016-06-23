package de.communardo.kenmei.database.update.v3_0;

import java.util.Collection;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.service.NavigationItemService;

/**
 * Custom task to add the built-in navigation items for the existing users
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AddBuiltInNavigationItems implements CustomTaskChange {

    @Override
    public void execute(Database arg0) throws CustomChangeException, UnsupportedChangeException {
        SecurityContext currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            Collection<User> users = ServiceLocator
                    .findService(UserManagement.class)
                    .findUsersByRole(UserRole.ROLE_KENMEI_USER, UserStatus.ACTIVE);
            NavigationItemService navigationItemService = ServiceLocator
                    .findService(NavigationItemService.class);
            for (User user : users) {
                navigationItemService.createBuiltInNavigationItems(user.getId());
            }
        } catch (AuthorizationException e) {
            throw new CustomChangeException("Creating built-in navigation items failed", e);
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return null;
    }

    @Override
    public void setFileOpener(FileOpener arg0) {
        // nothing

    }

    @Override
    public void setUp() throws SetupException {
        // nothing

    }

    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // nothing

    }

}
