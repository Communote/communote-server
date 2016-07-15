package com.communote.server.core.security.iprange;

import java.util.regex.Pattern;

/**
 * Helper class for ip address validation
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class IpValidationUtils {

    private static final String IPV4_PATTERN = "\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
            + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
            + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b";
    private static final Pattern IPV4_PATTERN_MATCHER = Pattern.compile(IPV4_PATTERN);

    private static final String IPV6_PATTERN_1 = "[0-9A-F]{1,4}:[0-9A-F]{1,4}:[0-9A-F]{1,4}:"
            + "[0-9A-F]{1,4}:[0-9A-F]{1,4}:[0-9A-F]{1,4}:[0-9A-F]{1,4}:[0-9A-F]{1,4}(%[0-9]+)?";
    private static final String IPV6_PATTERN_2 = "(?:[0-9A-F]{0,4}:){0,6}:"
            + "(?:[0-9A-F]{0,4}:){0,5}[0-9A-F]{0,4}";
    private static final String IPV6_PATTERN_3 = "[0-9A-F]{1,4}:[0-9A-F]{1,4}:[0-9A-F]{1,4}:"
            + "[0-9A-F]{1,4}:[0-9A-F]{1,4}:[0-9A-F]{1,4}";

    private static final Pattern IPV6_PATTERN_MATCHER_1 = Pattern.compile(IPV6_PATTERN_1,
            Pattern.CASE_INSENSITIVE);
    private static final Pattern IPV6_PATTERN_MATCHER_2 = Pattern.compile(IPV6_PATTERN_2,
            Pattern.CASE_INSENSITIVE);
    private static final Pattern IPV6_PATTERN_MATCHER_3 = Pattern.compile(IPV6_PATTERN_3,
            Pattern.CASE_INSENSITIVE);

    /**
     * Validate IP Address
     *
     * @param ip
     *            IP Address. Can be ipv4 ipv6
     * @return <code>true</code> if the ip address is valid
     */
    public static boolean isIPAddress(String ip) {
        return isIpV4Format(ip) || isIpV6Format(ip);
    }

    /**
     * Returns true if the given string is a correctly formed IPv4 address. <br>
     *
     * @param address
     *            IP address
     * @return <code>True</code> if the String passed is a valid IP address, <code>false</code>
     *         otherwise
     */
    public static boolean isIpV4Format(String address) {
        return IPV4_PATTERN_MATCHER.matcher(address).matches();
    }

    /**
     * Returns true if the given string is a correctly formed IPv6 address.
     *
     * @param address
     *            IP address
     * @return <code>True</code> if the String passed is a valid IP address, <code>false</code>
     *         otherwise
     */
    public static boolean isIpV6Format(String address) {
        boolean result = false;
        if (IPV6_PATTERN_MATCHER_1.matcher(address).matches()
                || IPV6_PATTERN_MATCHER_2.matcher(address).matches()) {
            result = true;
        } else {
            // Test variant 3
            int lastColon = address.lastIndexOf(':');
            String v4 = address.substring(lastColon + 1, address.length());
            String v6 = address.substring(0, lastColon + 1);

            if (v6.length() > 2) {
                // Test for compressed address
                if (v6.endsWith(":")) {
                    if (v6.charAt(v6.length() - 2) != ':') {
                        v6 = v6.substring(0, v6.length() - 1);
                    }
                }
            }

            if (isIpV4Format(v4)
                    && (IPV6_PATTERN_MATCHER_3.matcher(v6).matches() || IPV6_PATTERN_MATCHER_2
                            .matcher(v6).matches())) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Constructor
     */
    protected IpValidationUtils() {

    }

}
