package com.communote.plugins.mq.provider.activemq.user;

import java.util.Set;

/**
 * User entity.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MQUser {
    private String name;

    private Set<String> roles;

    private char[] password;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the password
     */
    public char[] getPassword() {
        return password;
    }

    /**
     * @return the roles
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(char[] password) {
        this.password = password;
    }

    /**
     * @param roles
     *            the roles to set
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

}
