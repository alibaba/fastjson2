package com.alibaba.fastjson2.support.springdoc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;

/**
 * OpenApiJsonWriter: SpringDoc的Json处理，解决json对象转成了json字符串，导致接口文档无法显示
 *
 * @author Victor.Zxy
 * @since 2.0.6
 */
public class OpenApiJsonWriter
        implements ObjectWriter<Object> {
    public static final OpenApiJsonWriter INSTANCE = new OpenApiJsonWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
        } else {
            String value = object.toString();
            jsonWriter.writeAny(JSON.parse(value));
        }
    }
}
