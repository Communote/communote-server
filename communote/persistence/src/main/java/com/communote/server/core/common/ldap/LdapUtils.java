package com.communote.server.core.common.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.communote.common.util.Pair;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.persistence.user.ExternalUserVO;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class LdapUtils {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtils.class);

    /**
     * Converts the bytes to a string in LDAP bytes format. This string will be a backslash
     * separated concatenation of the hexadecimal value of each bytes, e.g. '\0a\0b\99\ff'.
     *
     * @param bytes
     *            the bytes to convert
     * @return the string representation
     */
    public static String convertToByteFormatString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append("\\");
            int intValue = bytes[i] & 0xFF;
            if (intValue <= 0xF) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(intValue));
        }
        return sb.toString();
    }

    /**
     * Converts the value to a string. If the value is null an empty string will be returned.
     *
     * @param value
     *            the value to convert
     * @return the string representation
     */
    private static String convertValueToString(Object value) {
        String result = StringUtils.EMPTY;
        if (value != null) {
            if (value instanceof byte[]) {
                result = LdapUtils.convertToByteFormatString((byte[]) value);
            } else {
                result = value.toString();
            }
        }
        return result;
    }

    /**
     * Tests whether a DN conforms to at least on of the search bases. This ignores the case for the
     * attribute names.
     *
     * @param dn
     *            the DN to test
     * @param searchBaseDefs
     *            the search base definition
     * @return true if the DN conforms to at least on of the search bases, false otherwise
     */
    public static boolean dnConformsToSearchBaseDefinitions(String dn,
            Collection<LdapSearchBaseDefinition> searchBaseDefs) {
        boolean conforms = false;
        try {
            Name name = new LdapName(dn);
            for (LdapSearchBaseDefinition searchBaseDefinition : searchBaseDefs) {
                Name searchBaseName = new LdapName(searchBaseDefinition.getSearchBase());
                if (name.equals(searchBaseName)) {
                    conforms = true;
                    break;
                }
                if (searchBaseDefinition.isSearchSubtree()) {
                    // DN must end with search base. We have to use start, as it is reversed here.
                    if (name.startsWith(searchBaseName)) {
                        conforms = true;
                        break;
                    }
                } else {
                    // Base of DN must match search base. We have to use start, as it is reversed
                    // here.
                    name.remove(name.size() - 1);
                    if (name.equals(searchBaseName)) {
                        conforms = true;
                        break;
                    }
                }
            }
        } catch (InvalidNameException e) {
            LOGGER.debug(e.getMessage(), e);
            return false;
        }
        return conforms;
    }

    /**
     * Escapes an LDAP search filter value according to RFC 2254 (Section 4).
     *
     * @param value
     *            the value to be escaped
     * @return the escaped value
     */
    public static String escapeLdapSearchFilterValue(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
            // TODO Doesn't work within search, if replaced.
            // case '*':
            // sb.append("\\2a");
            // break;
            case '(':
                sb.append("\\28");
                break;
            case ')':
                sb.append("\\29");
                break;
            case '\\':
                sb.append("\\5c");
                break;
            case '\u0000':
                sb.append("\\00");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Returns the values for the named LDAP attribute. If the LDAP attribute is not contained null
     * is returned. If the attribute has no values the list will be empty. If one of the values is
     * null an empty string will be added to the list.
     *
     * @param attributes
     *            the attributes from which the value should be extracted
     * @param ldapAttributeName
     *            the name of the attribute in LDAP
     * @return the list of values converted to strings
     * @throws LdapAttributeMappingException
     *             if there was an error while retrieving the value
     */
    public static List<String> getAttributeValues(Attributes attributes, String ldapAttributeName)
            throws LdapAttributeMappingException {
        Attribute attribute = attributes.get(ldapAttributeName);
        if (attribute == null) {
            return null;
        }
        try {
            NamingEnumeration<?> values = attribute.getAll();
            List<String> valueList = new ArrayList<String>();
            while (values.hasMore()) {
                Object value = values.next();
                valueList.add(convertValueToString(value));
            }
            return valueList;
        } catch (NamingException e) {
            throw new LdapAttributeMappingException("Error while retriving attribute value", e);
        }
    }

    /**
     * Do an LDAP query for a user by email or alias.
     *
     * @param emailOrAlias
     *            email or alias of LDAP user
     * @return the VO or null if not found
     */
    public static ExternalUserVO queryUserByName(String emailOrAlias) {
        try {
            LdapUserAttribute userNameAttribute;
            // TODO this pretty naive to believe that an alias does not contain an @
            if (emailOrAlias.contains("@")) {
                userNameAttribute = LdapUserAttribute.EMAIL;
            } else {
                userNameAttribute = LdapUserAttribute.ALIAS;
            }
            CommunoteLdapUserSearch ldapSearch = new CommunoteLdapUserSearch(CommunoteRuntime
                    .getInstance().getConfigurationManager().getClientConfigurationProperties()
                    .getLdapConfiguration(), userNameAttribute);
            ExternalUserVO userVO = ldapSearch.searchForUserTransformed(emailOrAlias);

            // set fields to be synchronized
            setSynchronizationFields(userVO);
            return userVO;
        } catch (RequiredAttributeNotContainedException e) {
            LOGGER.warn("Problem during context mapping: {}: {} for {}",
                    new Object[] { e.getMessage(), e.getLdapAttributeName(), emailOrAlias });
            throw new AuthenticationServiceException(
                    "LDAP authentication failed due to attribute mapping exception", e);
        } catch (LdapAttributeMappingException e) {
            LOGGER.error("Error during context mapping: {} for {}", e.getMessage(), emailOrAlias);
            throw new AuthenticationServiceException(
                    "LDAP authentication failed due to attribute mapping exception", e);
        } catch (UsernameNotFoundException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("User with name " + emailOrAlias + " not found in LDAP directory");
            }
        }
        return null;
    }

    /**
     * Sets the fields of the user to be updated.
     *
     * @param userVO
     *            the VO
     */
    public static void setSynchronizationFields(ExternalUserVO userVO) {
        userVO.setUpdateEmail(true);
        userVO.setUpdateFirstName(true);
        userVO.setUpdateLanguage(true);
        userVO.setUpdateLastName(true);
        userVO.setUpdatePassword(false);
    }

    /**
     * Strips the first RDN from a DN and returns a pair where the left element is the stripped of
     * RDN and the right element holds the rest. In case there is no remainder the right element
     * will be null. Example DN 'cn=abc,O=u,OU=dc' will be split into 'cn=abc' and 'O=u,OU=dc'
     *
     * @param dn
     *            the DN to split
     * @return A pair where the left element is the stripped of RDN and the right element holds the
     *         rest.
     */
    public static Pair<String, String> splitAfterFirstRdn(String dn) {
        Pair<String, String> result = new Pair<String, String>(dn, null);
        boolean lastCharWasEscapeChar = false;
        for (int i = 0; i < dn.length(); i++) {
            char c = dn.charAt(i);
            if (c == '\\') {
                lastCharWasEscapeChar = true;
            } else if (c == ',' && !lastCharWasEscapeChar) {
                if (i == 0) {
                    result.setLeft(dn.substring(1).trim());
                } else {
                    result.setLeft(dn.substring(0, i).trim());
                    result.setRight(StringUtils.trimToNull(dn.substring(i + 1)));
                }
                break;
            } else {
                lastCharWasEscapeChar = false;
            }
        }
        return result;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private LdapUtils() {
        // Do nothing
    }
}
