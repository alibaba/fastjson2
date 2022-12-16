package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public abstract class MappingIterator<T>
        implements Iterator<T>, Closeable {
    public T nextValue() throws IOException {
        throw new JSONException("TODO");
    }
}
