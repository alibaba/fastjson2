package com.alibaba.fastjson2.support.jdbc;

import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;

public class JdbcReaderModule implements ObjectWriterModule {

    @Override
    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        return null;
    }
}
