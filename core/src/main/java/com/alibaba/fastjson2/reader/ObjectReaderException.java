package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.List;

public class ObjectReaderException<T>
        extends ObjectReaderBean<T> {
    static final long HASH_TYPE = Fnv.hashCode64("@type");
    static final long HASH_MESSAGE = Fnv.hashCode64("message");
    static final long HASH_DETAIL_MESSAGE = Fnv.hashCode64("detailMessage");
    static final long HASH_CAUSE = Fnv.hashCode64("cause");
    static final long HASH_STACKTRACE = Fnv.hashCode64("stackTrace");
    static final long HASH_SUPPRESSED_EXCEPTIONS = Fnv.hashCode64("suppressedExceptions");

    private FieldReader fieldReaderStackTrace;

    protected ObjectReaderException(Class<T> objectClass) {
        super(objectClass, null, objectClass.getName(), 0, null, null);
        fieldReaderStackTrace = ObjectReaders.fieldReader("stackTrace", StackTraceElement[].class, Throwable::setStackTrace);
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        jsonReader.nextIfObjectStart();

        String message = null;
        Throwable cause = null;
        StackTraceElement[] stackTrace = null;
        List<Throwable> suppressedExceptions = null;

        String stackTraceReference = null;
        String suppressedExceptionsReference = null;
        String causeReference = null;
        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }
            long hash = jsonReader.readFieldNameHashCode();

            if (i == 0 && hash == HASH_TYPE && jsonReader.isSupportAutoType(features)) {
                long typeHash = jsonReader.readTypeHashCode();
                JSONReader.Context context = jsonReader.getContext();
                ObjectReader reader = autoType(context, typeHash);
                String typeName = null;
                if (reader == null) {
                    typeName = jsonReader.getString();
                    reader = context.getObjectReaderAutoType(typeName, objectClass, features);

                    if (reader == null) {
                        throw new JSONException(jsonReader.info("No suitable ObjectReader found for" + typeName));
                    }
                }

                if (reader == this) {
                    continue;
                }

                return (T) reader.readObject(jsonReader);
            } else if (hash == HASH_MESSAGE || hash == HASH_DETAIL_MESSAGE) {
                message = jsonReader.readString();
            } else if (hash == HASH_CAUSE) {
                if (jsonReader.isReference()) {
                    causeReference = jsonReader.readReference();
                } else {
                    cause = jsonReader.read(Throwable.class);
                }
            } else if (hash == HASH_STACKTRACE) {
                if (jsonReader.isReference()) {
                    stackTraceReference = jsonReader.readReference();
                } else {
                    stackTrace = jsonReader.read(StackTraceElement[].class);
                }
            } else if (hash == HASH_SUPPRESSED_EXCEPTIONS) {
                if (jsonReader.isReference()) {
                    suppressedExceptionsReference = jsonReader.readReference();
                } else if (jsonReader.getType() == JSONB.Constants.BC_TYPED_ANY) {
                    suppressedExceptions = (List<Throwable>) jsonReader.readAny();
                } else {
                    suppressedExceptions = jsonReader.readArray(Throwable.class);
                }
            } else {
                jsonReader.skipValue();
            }
        }

        Throwable object = createObject(message, cause);

        if (object == null) {
            throw new JSONException(jsonReader.info(jsonReader.info("not support : " + objectClass.getName())));
        }

        if (stackTrace != null) {
            object.setStackTrace(stackTrace);
        }

        if (stackTraceReference != null) {
            jsonReader.addResolveTask(fieldReaderStackTrace, object, JSONPath.of(stackTraceReference));
        }

        return (T) object;
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.getType() == JSONB.Constants.BC_TYPED_ANY && jsonReader.isSupportAutoType(features)) {
            jsonReader.next();
            long typeHash = jsonReader.readTypeHashCode();

            JSONReader.Context context = jsonReader.getContext();

            ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
            if (autoTypeObjectReader == null) {
                String typeName = jsonReader.getString();
                autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);

                if (autoTypeObjectReader == null) {
                    throw new JSONException("auoType not support : " + typeName + ", offset " + jsonReader.getOffset());
                }
            }
            return (T) autoTypeObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, 0);
        }

        return readObject(jsonReader, fieldType, fieldName, features);
    }

    private Throwable createObject(String message, Throwable cause) {
        Throwable object = null;
        if (objectClass == UncheckedIOException.class) {
            if (message != null && cause != null) {
                object = new UncheckedIOException(message, (IOException) cause);
            } else if (cause != null) {
                object = new UncheckedIOException((IOException) cause);
            }
        } else if (objectClass == RuntimeException.class) {
            if (message != null && cause != null) {
                object = new RuntimeException(message, cause);
            } else if (cause != null) {
                object = new RuntimeException(cause);
            } else if (message != null) {
                object = new RuntimeException(message);
            } else {
                object = new RuntimeException();
            }
        } else if (objectClass == IOException.class) {
            if (message != null && cause != null) {
                object = new IOException(message, cause);
            } else if (cause != null) {
                object = new IOException(cause);
            } else if (message != null) {
                object = new IOException(message);
            } else {
                object = new IOException();
            }
        } else if (objectClass == Exception.class) {
            if (message != null && cause != null) {
                object = new Exception(message, cause);
            } else if (cause != null) {
                object = new Exception(cause);
            } else if (message != null) {
                object = new Exception(message);
            } else {
                object = new Exception();
            }
        } else if (objectClass == Throwable.class) {
            if (message != null && cause != null) {
                object = new Throwable(message, cause);
            } else if (cause != null) {
                object = new Throwable(cause);
            } else if (message != null) {
                object = new Throwable(message);
            } else {
                object = new Throwable();
            }
        } else if (objectClass == IllegalStateException.class) {
            if (message != null && cause != null) {
                object = new IllegalStateException(message, cause);
            } else if (cause != null) {
                object = new IllegalStateException(cause);
            } else if (message != null) {
                object = new IllegalStateException(message);
            } else {
                object = new IllegalStateException();
            }
        }
        return object;
    }
}
