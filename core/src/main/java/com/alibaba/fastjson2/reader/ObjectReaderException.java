package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class ObjectReaderException
        extends ObjectReaderBean {
    static final long HASH_MESSAGE = Fnv.hashCode64("message");
    static final long HASH_DETAIL_MESSAGE = Fnv.hashCode64("detailMessage");
    static final long HASH_CAUSE = Fnv.hashCode64("cause");
    static final long HASH_STACKTRACE = Fnv.hashCode64("stackTrace");
    static final long HASH_SUPPRESSED_EXCEPTIONS = Fnv.hashCode64("suppressedExceptions");

    protected ObjectReaderException(Class objectClass, String typeName, JSONSchema schema) {
        super(objectClass, typeName, schema);
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        throw new JSONException("not support : " + objectClass.getName());
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        boolean start = jsonReader.nextIfObjectStart();

        String message = null;
        Throwable cause = null;
        StackTraceElement[] stackTrace = null;
        List<Throwable> suppressedExceptions = null;

        String suppressedExceptionsReference = null;
        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }
            long hash = jsonReader.readFieldNameHashCode();
            if (hash == HASH_MESSAGE || hash == HASH_DETAIL_MESSAGE) {
                message = jsonReader.readString();
            } else if (hash == HASH_CAUSE) {
                cause = jsonReader.read(Throwable.class);
            } else if (hash == HASH_STACKTRACE) {
                stackTrace = jsonReader.read(StackTraceElement[].class);
            } else if (hash == HASH_SUPPRESSED_EXCEPTIONS) {
                if (jsonReader.isReference()) {
                    suppressedExceptionsReference = jsonReader.readReference();
                } else {
                    suppressedExceptions = jsonReader.readArray(Throwable.class);
                }
            } else {
                jsonReader.skipValue();
            }
        }

        Throwable object = null;
        if (objectClass == UncheckedIOException.class) {
            if (message != null && cause != null) {
                object = new UncheckedIOException(message, (IOException) cause);
            } else if (cause != null) {
                object = new UncheckedIOException((IOException) cause);
            }
        }

        if (object == null) {
            throw new JSONException("not support : " + objectClass.getName());
        }

        if (stackTrace != null) {
            object.setStackTrace(stackTrace);
        }

        return object;
    }
}
