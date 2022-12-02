package com.alibaba.fastjson2.adapter.jackson.datatype.jdk8;

import com.alibaba.fastjson2.adapter.jackson.databind.Module;

public class Jdk8Module
        extends Module {
    private boolean cfgHandleAbsentAsNull;

    public Jdk8Module configureAbsentsAsNulls(boolean state) {
        cfgHandleAbsentAsNull = state;
        return this;
    }
}
