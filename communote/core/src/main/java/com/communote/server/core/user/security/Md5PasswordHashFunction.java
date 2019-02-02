package com.communote.server.core.user.security;

import com.communote.common.encryption.HashCodeGenerator;

/**
 * Legacy implementation which uses an unsalted MD5 hash and is intended for backwards
 * compatibility.
 *
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 * @since 3.5
 */
public class Md5PasswordHashFunction implements PasswordHashFunction {

    private static String ID = "md5";

    @Override
    public boolean canHandle(String passwordHash) {
        if (passwordHash != null) {
            return !passwordHash.startsWith("$") && passwordHash.length() == 32;
        }
        return false;
    }

    @Override
    public boolean check(String passwordHash, String password) {
        return passwordHash.equals(generate(password));
    }

    @Override
    public String generate(String password) {
        return HashCodeGenerator.generateMD5HashCode(password);
    }

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public boolean needsUpdate(String passwordHash) {
        return false;
    }

}
