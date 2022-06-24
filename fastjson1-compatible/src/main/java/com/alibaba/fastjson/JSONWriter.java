package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.io.Writer;

public class JSONWriter {
    private final Writer out;
    final com.alibaba.fastjson2.JSONWriter raw;

    public JSONWriter(Writer out) {
        this.out = out;
        raw = com.alibaba.fastjson2.JSONWriter.ofUTF8();
    }

    public void config(SerializerFeature feature, boolean state) {
        com.alibaba.fastjson2.JSONWriter.Context ctx = raw.getContext();
        switch (feature) {
            case UseISO8601DateFormat:
                if (state) {
                    ctx.setDateFormat("iso8601");
                }
                break;
            case WriteMapNullValue:
                ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.WriteNulls, state);
                break;
            case WriteNullListAsEmpty:
                ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.WriteNullListAsEmpty, state);
                break;
            case WriteNullStringAsEmpty:
                ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.WriteNullStringAsEmpty, state);
                break;
            case WriteNullNumberAsZero:
                ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.WriteNullNumberAsZero, state);
                break;
            case WriteNullBooleanAsFalse:
                ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.WriteNullBooleanAsFalse, state);
                break;
            case BrowserCompatible:
                ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.BrowserCompatible, state);
                break;
            case WriteClassName:
                ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.WriteClassName, state);
                break;
            case WriteNonStringValueAsString:
                ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString, state);
                break;
            case WriteEnumUsingToString:
                ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.WriteEnumUsingToString, state);
                break;
            case NotWriteRootClassName:
                ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.NotWriteRootClassName, state);
                break;
            case IgnoreErrorGetter:
                ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.IgnoreErrorGetter, state);
                break;
            case WriteDateUseDateFormat:
                if (state) {
                    ctx.setDateFormat(JSON.DEFFAULT_DATE_FORMAT);
                }
                break;
            case BeanToArray:
                if (state) {
                    ctx.config(com.alibaba.fastjson2.JSONWriter.Feature.BeanToArray);
                }
                break;
            default:
                break;
        }
    }

    public void writeObject(Object object) {
        raw.writeAny(object);
    }

    public void flush() throws IOException {
        raw.flushTo(out);
        out.flush();
    }

    public void close() {
        raw.close();
        try {
            out.close();
        } catch (IOException ignored) {
            //
        }
    }
}
