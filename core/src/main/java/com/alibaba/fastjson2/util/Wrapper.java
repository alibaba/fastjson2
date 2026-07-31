package com.alibaba.fastjson2.util;

@SuppressWarnings({"rawtypes", "unchecked", "sunapi"})
public interface Wrapper {
    <T> T unwrap(java.lang.Class<T> iface);
}
