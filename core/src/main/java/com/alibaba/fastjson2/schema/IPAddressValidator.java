package com.alibaba.fastjson2.schema;

import java.nio.ByteBuffer;

final class IPAddressValidator
        implements FormatValidator {
    static final IPAddressValidator IPV6 = new IPAddressValidator(false);
    static final IPAddressValidator IPV4 = new IPAddressValidator(true);

    final boolean v4;

    public IPAddressValidator(boolean v4) {
        this.v4 = v4;
    }

    @Override
    public boolean isValid(String address) {
        if (address == null) {
            return false;
        }
        if (!isInetAddress(address)) {
            return false;
        }

        if (v4) {
            return address.indexOf('.') != -1;
        } else {
            return address.indexOf(':') != -1;
        }
    }

    static boolean isInetAddress(String ip) {
        String ipString = ip;
        // Make a first pass to categorize the characters in this string.
        boolean hasColon = false;
        boolean hasDot = false;
        int percentIndex = -1;
        for (int i = 0; i < ipString.length(); i++) {
            char c = ipString.charAt(i);
            if (c == '.') {
                hasDot = true;
            } else if (c == ':') {
                if (hasDot) {
                    return false; // Colons must not appear after dots.
                }
                hasColon = true;
            } else if (c == '%') {
                percentIndex = i;
                break; // everything after a '%' is ignored (it's a Scope ID): http://superuser.com/a/99753
            } else if (Character.digit(c, 16) == -1) {
                return false; // Everything else must be a decimal or hex digit.
            }
        }

        // Now decide which address family to parse.
        if (hasColon) {
            if (hasDot) {
                ipString = convertDottedQuadToHex(ipString);
                if (ipString == null) {
                    return false;
                }
            }
            if (percentIndex != -1) {
                ipString = ipString.substring(0, percentIndex);
            }
            return numericFormatV6(ipString);
        } else if (hasDot) {
            if (percentIndex != -1) {
                return false; // Scope IDs are not supported for IPV4
            }
            return textToNumericFormatV4(ipString) != null;
        }
        return false;
    }

    static String convertDottedQuadToHex(String ipString) {
        int lastColon = ipString.lastIndexOf(':');
        String initialPart = ipString.substring(0, lastColon + 1);
        String dottedQuad = ipString.substring(lastColon + 1);
        byte[] quad = textToNumericFormatV4(dottedQuad);
        if (quad == null) {
            return null;
        }
        String penultimate = Integer.toHexString(((quad[0] & 0xff) << 8) | (quad[1] & 0xff));
        String ultimate = Integer.toHexString(((quad[2] & 0xff) << 8) | (quad[3] & 0xff));
        return initialPart + penultimate + ":" + ultimate;
    }

    private static byte[] textToNumericFormatV4(String ipString) {
        int dotCount = 0;
        for (int i = 0; i < ipString.length(); i++) {
            if (ipString.charAt(i) == '.') {
                dotCount++;
            }
        }
        if (dotCount + 1 != 4) {
            return null; // Wrong number of parts
        }

        byte[] bytes = new byte[4];
        int start = 0;
        // Iterate through the parts of the ip string.
        // Invariant: start is always the beginning of an octet.
        for (int i = 0; i < 4; i++) {
            int end = ipString.indexOf('.', start);
            if (end == -1) {
                end = ipString.length();
            }
            try {
                bytes[i] = parseOctet(ipString, start, end);
            } catch (NumberFormatException ex) {
                return null;
            }
            start = end + 1;
        }

        return bytes;
    }

    private static boolean numericFormatV6(String ipString) {
        final int IPV6_PART_COUNT = 8;

        // An address can have [2..8] colons.
        int delimiterCount = 0;
        for (int i = 0; i < ipString.length(); i++) {
            if (ipString.charAt(i) == ':') {
                delimiterCount++;
            }
        }

        if (delimiterCount < 2 || delimiterCount > IPV6_PART_COUNT) {
            return false;
        }

        int partsSkipped = IPV6_PART_COUNT - (delimiterCount + 1); // estimate; may be modified later
        boolean hasSkip = false;
        // Scan for the appearance of ::, to mark a skip-format IPV6 string and adjust the partsSkipped
        // estimate.
        for (int i = 0; i < ipString.length() - 1; i++) {
            if (ipString.charAt(i) == ':' && ipString.charAt(i + 1) == ':') {
                if (hasSkip) {
                    return false; // Can't have more than one ::
                }
                hasSkip = true;
                partsSkipped++; // :: means we skipped an extra part in between the two delimiters.
                if (i == 0) {
                    partsSkipped++; // Begins with ::, so we skipped the part preceding the first :
                }
                if (i == ipString.length() - 2) {
                    partsSkipped++; // Ends with ::, so we skipped the part after the last :
                }
            }
        }
        if (ipString.charAt(0) == ':' && ipString.charAt(1) != ':') {
            return false; // ^: requires ^::
        }
        if (ipString.charAt(ipString.length() - 1) == ':'
                && ipString.charAt(ipString.length() - 2) != ':') {
            return false; // :$ requires ::$
        }
        if (hasSkip && partsSkipped <= 0) {
            return false; // :: must expand to at least one '0'
        }
        if (!hasSkip && delimiterCount + 1 != IPV6_PART_COUNT) {
            return false; // Incorrect number of parts
        }

        ByteBuffer rawBytes = ByteBuffer.allocate(2 * IPV6_PART_COUNT);
        try {
            // Iterate through the parts of the ip string.
            // Invariant: start is always the beginning of a hextet, or the second ':' of the skip
            // sequence "::"
            int start = 0;
            if (ipString.charAt(0) == ':') {
                start = 1;
            }
            while (start < ipString.length()) {
                int end = ipString.indexOf(':', start);
                if (end == -1) {
                    end = ipString.length();
                }
                if (ipString.charAt(start) == ':') {
                    // expand zeroes
                    for (int i = 0; i < partsSkipped; i++) {
                        rawBytes.putShort((short) 0);
                    }
                } else {
                    rawBytes.putShort(parseHextet(ipString, start, end));
                }
                start = end + 1;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    private static byte parseOctet(String ipString, int start, int end) {
        // Note: we already verified that this string contains only hex digits, but the string may still
        // contain non-decimal characters.
        int length = end - start;
        if (length <= 0 || length > 3) {
            throw new NumberFormatException();
        }
        // Disallow leading zeroes, because no clear standard exists on
        // whether these should be interpreted as decimal or octal.
        if (length > 1 && ipString.charAt(start) == '0') {
            throw new NumberFormatException();
        }
        int octet = 0;
        for (int i = start; i < end; i++) {
            octet *= 10;
            int digit = Character.digit(ipString.charAt(i), 10);
            if (digit < 0) {
                throw new NumberFormatException();
            }
            octet += digit;
        }
        if (octet > 255) {
            throw new NumberFormatException();
        }
        return (byte) octet;
    }

    private static short parseHextet(String ipString, int start, int end) {
        // Note: we already verified that this string contains only hex digits.
        int length = end - start;
        if (length <= 0 || length > 4) {
            throw new NumberFormatException();
        }
        int hextet = 0;
        for (int i = start; i < end; i++) {
            hextet = hextet << 4;
            hextet |= Character.digit(ipString.charAt(i), 16);
        }
        return (short) hextet;
    }
}
