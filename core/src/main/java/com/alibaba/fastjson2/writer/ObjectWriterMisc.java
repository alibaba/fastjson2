package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterMisc
        implements ObjectWriter {
    static final ObjectWriterMisc INSTANCE = new ObjectWriterMisc();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Class<?> objectClass = object.getClass();
        String objectClassName = objectClass.getName();

        String str;
        switch (objectClassName) {
            case "net.sf.json.JSONNull":
                jsonWriter.writeNull();
                return;
            case "java.net.Inet4Address":
            case "java.net.Inet6Address":
                str = ((java.net.InetAddress) object).getHostName();
                break;
            case "java.text.SimpleDateFormat":
                str = ((java.text.SimpleDateFormat) object).toPattern();
                break;
            case "java.util.regex.Pattern":
                str = ((java.util.regex.Pattern) object).pattern();
                break;
            case "java.net.InetSocketAddress": {
                java.net.InetSocketAddress address = (java.net.InetSocketAddress) object;

                jsonWriter.startObject();

                jsonWriter.writeName("address");
                jsonWriter.writeColon();
                jsonWriter.writeAny(address.getAddress());

                jsonWriter.writeName("port");
                jsonWriter.writeColon();
                jsonWriter.writeInt32(address.getPort());

                jsonWriter.endObject();
                return;
            }
            default:
                throw new JSONException("not support class : " + objectClassName);
        }

        jsonWriter.writeString(str);
    }
}
