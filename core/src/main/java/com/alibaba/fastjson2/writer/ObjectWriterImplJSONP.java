package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONPObject;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.List;

public class ObjectWriterImplJSONP
        extends ObjectWriterPrimitiveImpl<JSONPObject> {
    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        long features1 = jsonWriter.getFeatures(features);
        if ((features1 & JSONWriter.Feature.BrowserSecure.mask) != 0) {
            final String SECURITY_PREFIX = "/**/";
            jsonWriter.writeRaw(SECURITY_PREFIX);
        }

        JSONPObject jsonp = (JSONPObject) object;
        jsonWriter.writeRaw(jsonp.getFunction());
        jsonWriter.writeRaw('(');
        List<Object> parameters = jsonp.getParameters();
        for (int i = 0; i < parameters.size(); ++i) {
            if (i != 0) {
                jsonWriter.writeRaw(',');
            }
            jsonWriter.writeAny(parameters.get(i));
        }
        jsonWriter.writeRaw(')');
    }
}
