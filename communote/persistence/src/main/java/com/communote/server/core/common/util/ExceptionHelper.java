package com.communote.server.core.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.core.security.ldap.LdapUserException;
import com.communote.server.core.user.AliasAlreadyExistsException;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.persistence.common.security.SecurityCodeNotFoundException;

/**
 * Utility class to check and throw exceptions.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExceptionHelper {

    /**
     * Name of the exception of a MySQL Constraint violation. Cannot check by the Class since it is
     * not always available.
     */
    private static final String MY_SQL_INTEGRITY_CONSTRAINT_VIOLATION_EXCEPTION_NAME = "MySQLIntegrityConstraintViolationException";

    /**
     * @param th
     *            the exception
     * @return the exceptions stacktrace and messages as string
     */
    public static String buildExceptionString(Throwable th) {
        StringBuilder errorMessage = new StringBuilder();
        if (th != null) {
            errorMessage.append(th.getMessage());
            errorMessage.append("\n");

            if (th.getCause() instanceof Exception) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(new StringWriter());
                th.printStackTrace(pw);
                pw.flush();
                errorMessage.append(sw.toString());
                pw.close();
            }

            if (th.getCause() != null && th.getCause() != th) {
                errorMessage.append("\n");
                errorMessage.append("Caused by: ");

                errorMessage.append(buildExceptionString(th.getCause()));
            }
        }
        return errorMessage.toString();
    }

    /**
     *
     * @param exception
     *            The exception object.
     * @throws AliasAlreadyExistsException
     *             The alias already exists exception.
     */
    public static void checkAliasAlreadyExists(Object exception) throws AliasAlreadyExistsException {
        if (exception instanceof AliasAlreadyExistsException) {
            throw (AliasAlreadyExistsException) exception;
        }
    }

    /**
     * Check email already exists.
     *
     * @param exception
     *            the exception
     * @throws EmailAlreadyExistsException
     *             the email already exists exception
     */
    public static void checkEmailAlreadyExists(Object exception) throws EmailAlreadyExistsException {
        if (exception instanceof EmailAlreadyExistsException) {
            throw (EmailAlreadyExistsException) exception;
        }
    }

    /**
     * Check email validation.
     *
     * @param exception
     *            the exception
     * @throws EmailValidationException
     *             the email validation exception
     */
    public static void checkEmailValidation(Object exception) throws EmailValidationException {
        if (exception instanceof EmailValidationException) {
            throw (EmailValidationException) exception;
        }
    }

    public static void checkLdapUserException(Object exception) throws LdapUserException {
        if (exception instanceof LdapUserException) {
            throw (LdapUserException) exception;
        }
    }

    /**
     * Check security code not found.
     *
     * @param exception
     *            the exception
     * @throws SecurityCodeNotFoundException
     *             the security code not found exception
     */
    public static void checkSecurityCodeNotFound(Object exception)
            throws SecurityCodeNotFoundException {
        if (exception instanceof SecurityCodeNotFoundException) {
            throw (SecurityCodeNotFoundException) exception;
        }
    }

    /**
     * Checks if the given exception has its cause in a database constraint violation, that can be a
     * hint of concurrent access (e.g. creating the same user parallel). so maybe a rerun of the
     * causing function helps.
     *
     * This method will check the provided Throwable and the causes up the chain.
     *
     * @param throwable
     *            the exception to check
     * @return true if it is a constraint violation exception and a recall of the function may help
     */
    public static boolean isConstraintViolationException(Throwable throwable) {
        // this method is especially useful for the silly RuntimeException wrapping of
        // RuntimeExceptions in the XyzDaoBase classes
        boolean isConstraintException = isConstraintViolationExceptionWithoutCause(throwable);

        if (!isConstraintException) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                isConstraintException = isConstraintViolationException(cause);
            }

        }
        return isConstraintException;
    }

    /**
     * Checks if the given exception has is a database constraint violation, that can be a hint of
     * concurrent access (e.g. creating the same user parallel). so maybe a rerun of the causing
     * function helps.
     *
     * Remark: The determination of the violation exception is considering only the given one, not
     * the cause. See {@link #isConstraintViolationException(Throwable)}
     *
     * @param th
     *            the exception to check
     * @return true if it is a constraint violation exception and a recall of the function may help
     */
    public static boolean isConstraintViolationExceptionWithoutCause(Throwable th) {
        return th instanceof DataIntegrityViolationException
                || th instanceof ConstraintViolationException
                || th.getClass().getName()
                        .equals(MY_SQL_INTEGRITY_CONSTRAINT_VIOLATION_EXCEPTION_NAME);
    }

    /**
     * Instantiates a new exception util.
     */
    private ExceptionHelper() {

    }

}
