package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.util.InetAddresses;

final class IPV6AddressValidator implements FormatValidator {
    final static IPV6AddressValidator INSTANCE = new IPV6AddressValidator();

    @Override
    public boolean isValid(String address) {
        if (address == null) {
            return false;
        }
        return InetAddresses.isInetAddress(address) && address.indexOf(':') != -1;
    }
}
