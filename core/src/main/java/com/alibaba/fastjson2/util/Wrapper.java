package com.alibaba.fastjson2.util;

public interface Wrapper {
    <T> T unwrap(java.lang.Class<T> iface);
}
