package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.util.InetAddresses;

final class IPV4AddressValidator
        implements FormatValidator {
    static final IPV4AddressValidator INSTANCE = new IPV4AddressValidator();

    @Override
    public boolean isValid(String address) {
        if (address == null) {
            return false;
        }
        return InetAddresses.isInetAddress(address) && address.indexOf('.') != -1;
    }
}
