package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson2.JSONWriter;

public class SerialContext {
    public final SerialContext parent;
    public final Object object;
    public final Object fieldName;
    public final int features;
    JSONWriter jsonWriter;

    SerialContext(JSONWriter jsonWriter, SerialContext parent, Object object, Object fieldName, int features, int fieldFeatures) {
        this.parent = parent;
        this.jsonWriter = jsonWriter;
        this.object = object;
        this.fieldName = fieldName;
        this.features = features;
    }

    public SerialContext(SerialContext parent, Object object, Object fieldName, int features, int fieldFeatures) {
        this.parent = parent;
        this.object = object;
        this.fieldName = fieldName;
        this.features = features;
    }

    /**
     * @deprecated
     */
    public String getPath() {
        return toString();
    }

    public String toString() {
        String path = null;
        if (jsonWriter != null) {
            path = jsonWriter.getPath();
        }

        return path;
    }
}
