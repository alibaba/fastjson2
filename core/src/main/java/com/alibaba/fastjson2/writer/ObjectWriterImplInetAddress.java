package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.net.Inet4Address;

final class ObjectWriterImplInetAddress extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInetAddress INSTANCE = new ObjectWriterImplInetAddress();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        Inet4Address address = (Inet4Address) object;
        jsonWriter.writeString(
                address.getHostName());
    }
}
