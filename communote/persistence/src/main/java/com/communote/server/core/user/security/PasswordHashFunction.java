package com.communote.server.core.user.security;

/**
 *
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 * @since 3.5
 */
public interface PasswordHashFunction {

    /**
     * Test whether the provided hash was generated by {@link #generate(String)} of this hash
     * function.
     *
     * @param passwordHash
     *            a password hash
     * @return true if the hash was generated by this function, false otherwise
     */
    boolean canHandle(String passwordHash);

    /**
     * Test a clear text password against a password hash generated by this hash function. This
     * method will only be called if {@link #canHandle(String)} for the hash returns true.
     *
     * @param passwordHash
     *            a hash generated by this function
     * @param password
     *            the clear text password to validate
     * @return true if the hash was created from the given password, false otherwise
     */
    boolean check(String passwordHash, String password);

    /**
     * Generate the hash of a clear text password. The resulting string has to contain information
     * to determine whether the hash was generated by this function
     * ({@link PasswordHashFunction#canHandle(String)}) and to check the hash against a clear-text
     * password in {@link #check(String, String)}. The latter requires to include the salt
     * and any parameters like cost factors or iterations in the created string. A recommended way
     * to encode this information is the
     * <a href="https://github.com/P-H-C/phc-string-format/blob/master/phc-sf-spec.md">PHC string
     * format</a>.
     *
     * @param password
     *            the clear text password to hash
     * @return the generated hash with all relevant information
     */
    String generate(String password);

    /**
     * @return a unique identifier of the hash function (key derivation function) or function
     *         family.
     */
    String getIdentifier();

    /**
     * Test whether a password hash needs to be updated. If the function for instance has a
     * configurable cost factor this method could check whether the cost factor used to generate the
     * hash is up-to-date and return false if not. In case false is returned a new hash will be
     * generated with a call to {@link #generate(String)}. This method will only be called if
     * {@link #canHandle(String)} for the hash returns true.
     *
     * @param passwordHash
     *            a hash generated by this function
     * @return true if the hash should be updated to adhere to newer/stronger security requirements
     */
    boolean needsUpdate(String passwordHash);

}
