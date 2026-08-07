package com.alibaba.fastjson.serializer.filters.canal;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class AddressSerializer
        implements ObjectSerializer {
    public static AddressSerializer instance = new AddressSerializer();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
            throws IOException {
        if (object == null) {
            serializer.writeNull();
            return;
        }

        Address address = (Address) object;
        // 优先使用name
        serializer.write(address.getHostName());
    }
}
