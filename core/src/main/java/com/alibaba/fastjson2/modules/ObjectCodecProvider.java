package com.alibaba.fastjson2.modules;

public interface ObjectCodecProvider {
    Class getMixIn(Class target);
}
