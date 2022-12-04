package com.alibaba.fastjson2.adapter.jackson.dataformat.csv;

import com.alibaba.fastjson2.adapter.jackson.databind.MappingIterator;
import com.alibaba.fastjson2.support.csv.CSVParser;

import java.io.IOException;

public class CSVMappingIterator<T>
        extends MappingIterator<T> {
    final CSVParser parser;
    final Class<T> objectClass;
    T object;

    public CSVMappingIterator(CSVParser parser, Class<T> objectClass) {
        this.parser = parser;
        this.objectClass = objectClass;
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }

    @Override
    public boolean hasNext() {
        if (parser.isEnd()) {
            return false;
        }
        object = parser.readLoneObject();
        return true;
    }

    @Override
    public T next() {
        return object;
    }
}
