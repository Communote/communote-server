package com.communote.server.core.user.validation;

import com.communote.common.util.Orderable;
import com.communote.server.model.user.User;

/**
 * The UserActivationValidator will be called before the status of a user is set to active.
 * Implementors can check additional conditions which should be met before a user can be activated.
 * If those conditions are not fulfilled an exception with a localizable message that describes the
 * reason can be thrown. An implementation must however not modify the user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface UserActivationValidator extends Orderable {

    /**
     * the default value for the order. This value should be used if the validator has no specific
     * requirements to the invocation order.
     */
    public static final int DEFAULT_ORDER = 1000;

    /**
     * @return the order value which is interpreted as the priority of the validator. The higher the
     *         priority, the earlier this extension will be called.
     */
    @Override
    public int getOrder();

    /**
     * Validate whether the status of the given user can be set to <code>ACTIVE</code>.
     *
     * @param user
     *            the user that should be activated. The user must not be modified.
     *
     * @throws UserActivationValidationException
     *             in case the validation fails and the user cannot be activated
     */
    public void validateUserActivation(User user) throws UserActivationValidationException;

}
