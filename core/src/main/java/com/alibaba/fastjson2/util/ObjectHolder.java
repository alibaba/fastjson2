package com.alibaba.fastjson2.util;

import java.io.Serializable;

public final class ObjectHolder
        implements Serializable {
    public Object object;

    public ObjectHolder() {
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
