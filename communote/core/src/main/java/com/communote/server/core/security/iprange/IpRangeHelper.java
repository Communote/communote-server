package com.communote.server.core.security.iprange;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.communote.server.model.security.IpRange;

/**
 * Helper class for IpV6 address calculations.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeHelper {

    /** 128 bit bitmask. */
    private static BigInteger BITMASK_IPV6 = new BigInteger(new byte[] { 0x1, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff });

    /**
     * Transform byte array to ipv6 notation.
     * 
     * @param value
     *            the byte array
     * @return the string in ipv6 notation
     */
    public static String byteToString(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("value can not be null");
        }
        LinkedList<String> ipStr = new LinkedList<String>();
        String result = null;
        for (int i = value.length - 1; i >= 0; i--, i--) {
            if (i - 1 >= 0) {
                ipStr.addFirst(Integer
                        .toHexString(((value[i - 1] & 0xFF) << 8) | (value[i] & 0xFF)));
            } else {
                ipStr.addFirst(Integer.toHexString((value[i] & 0xFF)));
            }
        }
        result = StringUtils.join(ipStr, ":");

        return compressIPV6(result);
    }

    /**
     * Compress ip v6, add ::.
     * 
     * @param ip
     *            the ip
     * @return the string
     */
    private static String compressIPV6(String ip) {
        int index = ip.indexOf(":0:");
        if (index >= 0) {
            ip = ip.replaceFirst(":0:", "::");
            while ((index = ip.indexOf("::0:")) >= 0) {
                ip = ip.replaceFirst("::0:", "::");
            }
            if ((index = ip.indexOf("0::")) == 0) {
                ip = ip.substring(1, ip.length());
            }
            if ((index = ip.indexOf("::0")) == ip.length() - 3) {
                ip = ip.substring(0, ip.length() - 1);
            }
        }
        return ip;
    }

    /**
     * Removes :: from ipv6 address.
     * 
     * @param ip
     *            the ip as ipv6 or ipv4.
     * @return the string
     */
    private static String expandIpAddress(String ip) {
        // ipv4 adress
        if (!ip.contains(":")) {
            ip = "::" + ip;
        }
        // removes ::
        if (ip.startsWith(":")) {
            ip = "0" + ip;
        }
        if (ip.endsWith(":")) {
            ip = ip + "0";
        }
        if (ip.contains("::")) {
            int expected = 7;
            if (ip.contains(".")) {
                expected = 6;
            }
            while (StringUtils.countMatches(ip, ":") < expected) {
                ip = ip.replace("::", ":0::");
            }
            ip = ip.replace("::", ":0:");
        }
        return ip;
    }

    /**
     * Gets the broadcast address.
     * 
     * @param ip
     *            the ip
     * @param netmask
     *            the netmask
     * @return the broadcast address
     */
    public static BigInteger getBroadcastAddress(BigInteger ip, BigInteger netmask) {
        return ip.or(netmask.not()).and(BITMASK_IPV6);
    }

    /**
     * Gets the net address.
     * 
     * @param ip
     *            the ip
     * @param netmask
     *            the netmask
     * @return the net address
     */
    public static BigInteger getNetAddress(BigInteger ip, BigInteger netmask) {
        return ip.and(netmask).and(BITMASK_IPV6);
    }

    /**
     * Gets the bit mask.
     * 
     * @param bits
     *            the bits to set
     * @return the net mask
     */
    public static BigInteger getSubNetMask(int bits) {
        int maxBits = 128;
        if (bits > 128) {
            throw new IllegalArgumentException("can not create more than 128bit mask");
        }
        return BigInteger.ONE.shiftLeft(maxBits + 1).subtract(BigInteger.ONE)
                .shiftLeft(maxBits - bits).and(BITMASK_IPV6);
    }

    /**
     * Gets the sub net mask for the given ip.
     * 
     * @param ip
     *            the ip
     * @return the sub net mask
     */
    public static BigInteger getSubNetMask(String ip) {
        return getSubNetMask(getSubNetMaskBits(ip));
    }

    /**
     * Gets the sub net mask value from the ip as string, default is 128bit.
     * 
     * @param ip
     *            the ip as string
     * @return the sub net mask value, default 128bit
     */
    private static int getSubNetMaskBits(String ip) {
        int result = 128;
        int index = 0;
        if ((index = ip.indexOf("/")) >= 0) {
            result = Integer.valueOf(ip.substring(index + 1));
            // ipv4, add 96 bits
            if (!ip.contains(":")) {
                result = result + 96;
            }
        }
        return result;
    }

    /**
     * Checks if if the ip is in range.
     * 
     * @param ip
     *            the ip
     * @param start
     *            the start of the range
     * @param end
     *            the end of the range
     * @return true, if ip is in range
     */
    public static boolean ipInRange(BigInteger ip, BigInteger start, BigInteger end) {
        return ip.compareTo(start) >= 0 && ip.compareTo(end) <= 0;
    }

    /**
     * Checks if if the ip is in range.
     * 
     * @param ip
     *            the ip
     * @param start
     *            the start of the range
     * @param end
     *            the end of the range
     * @return true, if ip is in range
     * @throws InvalidIpRangeException
     *             the invalid ip range exception
     * @throws InvalidIpAddressException
     *             the invalid ip address exception
     */
    public static boolean ipInRange(String ip, String start, String end)
            throws InvalidIpRangeException, InvalidIpAddressException {
        BigInteger ipValue = ipToInt(ip);
        BigInteger startValue = getNetAddress(ipToInt(start), getSubNetMask(start));
        BigInteger endValue = getBroadcastAddress(ipToInt(end), getSubNetMask(end));
        if (start.compareTo(end) > 0) {
            throw new InvalidIpRangeException("start can not be greater then end", ip + "-" + end);
        }
        return ipInRange(ipValue, startValue, endValue);
    }

    /**
     * Transform the given ipv6 or ipv4 address to 16 byte array.
     * 
     * @param ip
     *            the ip as ipv6 or ipv4
     * @return 16 byte value
     */
    private static byte[] ipToByte(String ip) {
        // remove net mask
        int index = 0;
        if ((index = ip.indexOf("/")) >= 0) {
            ip = ip.substring(0, index);
        }
        ip = expandIpAddress(ip);

        // if ipv6 has %.. at the end
        String ipV6 = ip;
        String[] seperatedIpV6AndZoneId = ip.split("%");
        if (seperatedIpV6AndZoneId.length == 2) {
            ipV6 = seperatedIpV6AndZoneId[0];
        }

        String[] ipV6Token = ipV6.split(":");

        byte[] result = new byte[16];
        Integer value = 0;
        for (int i = 0; i < ipV6Token.length; i++) {
            // werte ipV4 Adresse aus ( 4 bytes)
            if (ipV6Token.length == 7 && i == 6) {
                String[] ipV4Token = ipV6Token[i].split("\\.");
                for (int j = 0; j < ipV4Token.length; j++) {
                    value = Integer.valueOf(ipV4Token[j], 10);
                    result[i * 2 + j] = (byte) (value & 0xff);
                }
            } else {
                value = Integer.valueOf(ipV6Token[i], 16);
                result[i * 2] = (byte) ((value >> 8) & 0xff);
                result[i * 2 + 1] = (byte) (value & 0xff);
            }
        }
        return result;
    }

    /**
     * Transform the given ipv6 address to 128 bit integer.
     * 
     * @param ip
     *            the ip
     * @return the ip as 128 bit integer
     * @throws InvalidIpAddressException
     *             the invalid ip address exception
     */
    public static BigInteger ipToInt(String ip) throws InvalidIpAddressException {
        if (ip == null) {
            throw new IllegalArgumentException("ip can not be null");
        }
        if (validateIP(ip)) {
            return new BigInteger(ipToByte(ip));
        } else {
            throw new InvalidIpAddressException("invalid ip address", ip);
        }
    }

    /**
     * Transform the given 128 bit value to ipv6 notation.
     * 
     * @param ip
     *            the ip
     * @return the string in ipv6 notation
     */
    public static String ipToString(BigInteger ip) {
        if (ip == null) {
            throw new IllegalArgumentException("ip can not be null");
        }
        return byteToString(trimTo16Bytes(ip));
    }

    /**
     * Transform ranges to string. The method will first look for the stringRepresentation member of
     * the IpRange and take it if it is not null. Otherwise the members start and end will be
     * converted to an IPv6 IP address range.
     * 
     * @param ranges
     *            the ranges
     * @param delimiter
     *            the delimiter
     * @return the string
     */
    public static String rangesToString(Collection<IpRange> ranges, String delimiter) {
        if (ranges == null) {
            throw new IllegalArgumentException("ranges can not be null");
        }
        Collection<String> tokens = new ArrayList<String>();
        for (IpRange range : ranges) {
            if (range.getStringRepresentation() != null) {
                tokens.add(range.getStringRepresentation());
            } else {
                if (range.getStart().compareTo(range.getEnd()) == 0) {
                    tokens.add(ipToString(range.getStart()));
                } else {
                    tokens.add(ipToString(range.getStart()) + "-" + ipToString(range.getEnd()));
                }
            }
        }
        return StringUtils.join(tokens, delimiter);
    }

    /**
     * Parse an iP range.
     * 
     * @param iprange
     *            the iprange
     * @return the iP range
     * @throws InvalidIpRangeException
     *             the invalid ip range exception
     * @throws InvalidIpAddressException
     *             the invalid ip address exception
     */
    public static IpRange stringToRange(String iprange) throws InvalidIpRangeException,
            InvalidIpAddressException {
        iprange = StringUtils.trimToEmpty(iprange);
        if (StringUtils.isEmpty(iprange)) {
            throw new InvalidIpAddressException("got empty ip adress", iprange);
        }
        String startIp;
        String endIp;
        if (iprange.contains("-")) {
            String[] ips = iprange.split("-");
            startIp = ips[0].trim();
            endIp = ips[1].trim();
        } else {
            startIp = iprange;
            endIp = iprange;
        }
        BigInteger start = getNetAddress(ipToInt(startIp), getSubNetMask(startIp));
        BigInteger end = getBroadcastAddress(ipToInt(endIp), getSubNetMask(endIp));
        if (start.compareTo(end) > 0) {
            throw new InvalidIpRangeException("start address can not be greater then end address",
                    iprange);
        }
        IpRange result = IpRange.Factory.newInstance();
        result.setStringRepresentation(iprange);
        result.setStart(start);
        result.setEnd(end);
        return result;
    }

    /**
     * Parses multiple ip ranges.
     * 
     * @param text
     *            the text
     * @param delimiter
     *            the delimiter
     * @return the list< ip range>
     * @throws InvalidIpRangeException
     *             the invalid ip range exception
     * @throws InvalidIpAddressException
     *             the invalid ip address exception
     */
    public static List<IpRange> stringToRanges(String text, String delimiter)
            throws InvalidIpRangeException, InvalidIpAddressException {
        List<IpRange> result = new ArrayList<IpRange>();
        String[] token = StringUtils.split(text, delimiter);
        if (token != null) {
            for (String range : token) {
                if (StringUtils.isNotBlank(range)) {
                    result.add(stringToRange(range));
                }
            }
        }
        return result;
    }

    /**
     * Trim to16 bytes.
     * 
     * @param ip
     *            the ip
     * @return the byte[]
     */
    private static byte[] trimTo16Bytes(BigInteger ip) {
        byte[] result = ip.toByteArray();
        if (result.length > 16) {
            result = ArrayUtils.subarray(result, result.length - 16, result.length);
        } else if (result.length < 16) {
            result = ArrayUtils.addAll(new byte[16 - result.length], result);
        }
        return result;
    }

    /**
     * Validate ip. Accepts ipv4 and ipv6 notation.
     * 
     * @param ip
     *            the ip
     * @return true, if successful
     */
    public static boolean validateIP(String ip) {
        return IpValidationUtils.isIPAddress(ip);
    }

    /**
     * Instantiates a new iP range helper.
     */
    private IpRangeHelper() {

    }

}
