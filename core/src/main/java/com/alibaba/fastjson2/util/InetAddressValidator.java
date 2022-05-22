package com.alibaba.fastjson2.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InetAddressValidator {
    private static final String IPV4_REGEX =
            "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";

    /**
     * Singleton instance of this class.
     */
    private static final InetAddressValidator VALIDATOR = new InetAddressValidator();

    /** IPv4 RegexValidator */
    private final RegexValidator ipv4Validator = new RegexValidator(IPV4_REGEX);

    /**
     * Returns the singleton instance of this validator.
     * @return the singleton instance of this validator
     */
    public static InetAddressValidator getInstance() {
        return VALIDATOR;
    }

    /**
     * Checks if the specified string is a valid IP address.
     * @param inetAddress the string to validate
     * @return true if the string validates as an IP address
     */
    public boolean isValid(String inetAddress) {
        return isValidInet4Address(inetAddress) || isValidInet6Address(inetAddress);
    }

    /**
     * Validates an IPv4 address. Returns true if valid.
     * @param inet4Address the IPv4 address to validate
     * @return true if the argument contains a valid IPv4 address
     */
    public boolean isValidInet4Address(String inet4Address) {
        // verify that address conforms to generic IPv4 format
        String[] groups = ipv4Validator.match(inet4Address);

        if (groups == null) {
            return false;
        }

        // verify that address subgroups are legal
        for (int i = 0; i <= 3; i++) {
            String ipSegment = groups[i];
            if (ipSegment == null || ipSegment.length() == 0) {
                return false;
            }

            int iIpSegment = 0;

            try {
                iIpSegment = Integer.parseInt(ipSegment);
            } catch (NumberFormatException e) {
                return false;
            }

            if (iIpSegment > 255) {
                return false;
            }

            if (ipSegment.length() > 1 && ipSegment.startsWith("0")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates an IPv6 address. Returns true if valid.
     * @param inet6Address the IPv6 address to validate
     * @return true if the argument contains a valid IPv6 address
     *
     * @since 1.4.1
     */
    public boolean isValidInet6Address(String inet6Address) {
        boolean containsCompressedZeroes = inet6Address.contains("::");
        if (containsCompressedZeroes && (inet6Address.indexOf("::") != inet6Address.lastIndexOf("::"))) {
            return false;
        }
        if ((inet6Address.startsWith(":") && !inet6Address.startsWith("::"))
                || (inet6Address.endsWith(":") && !inet6Address.endsWith("::"))) {
            return false;
        }
        String[] octets = inet6Address.split(":");
        if (containsCompressedZeroes) {
            List<String> octetList = new ArrayList<String>(Arrays.asList(octets));
            if (inet6Address.endsWith("::")) {
                // String.split() drops ending empty segments
                octetList.add("");
            } else if (inet6Address.startsWith("::") && !octetList.isEmpty()) {
                octetList.remove(0);
            }
            octets = octetList.toArray(new String[octetList.size()]);
        }
        if (octets.length > 8) {
            return false;
        }
        int validOctets = 0;
        int emptyOctets = 0;
        for (int index = 0; index < octets.length; index++) {
            String octet = (String) octets[index];
            if (octet.length() == 0) {
                emptyOctets++;
                if (emptyOctets > 1) {
                    return false;
                }
            } else {
                emptyOctets = 0;
                if (octet.contains(".")) { // contains is Java 1.5+
                    if (!inet6Address.endsWith(octet)) {
                        return false;
                    }
                    if (index > octets.length - 1 || index > 6) {
                        // IPV4 occupies last two octets
                        return false;
                    }
                    if (!isValidInet4Address(octet)) {
                        return false;
                    }
                    validOctets += 2;
                    continue;
                }
                if (octet.length() > 4) {
                    return false;
                }
                int octetInt = 0;
                try {
                    octetInt = Integer.valueOf(octet, 16).intValue();
                } catch (NumberFormatException e) {
                    return false;
                }
                if (octetInt < 0 || octetInt > 0xffff) {
                    return false;
                }
            }
            validOctets++;
        }
        if (validOctets < 8 && !containsCompressedZeroes) {
            return false;
        }
        return true;
    }
}
