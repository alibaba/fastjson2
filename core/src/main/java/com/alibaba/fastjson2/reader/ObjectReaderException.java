package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;

public class ObjectReaderException<T>
        extends ObjectReaderAdapter<T> {
    static final long HASH_TYPE = Fnv.hashCode64("@type");
    static final long HASH_MESSAGE = Fnv.hashCode64("message");
    static final long HASH_DETAIL_MESSAGE = Fnv.hashCode64("detailMessage");
    static final long HASH_LOCALIZED_MESSAGE = Fnv.hashCode64("localizedMessage");
    static final long HASH_CAUSE = Fnv.hashCode64("cause");
    static final long HASH_STACKTRACE = Fnv.hashCode64("stackTrace");
    static final long HASH_SUPPRESSED_EXCEPTIONS = Fnv.hashCode64("suppressedExceptions");

    private FieldReader fieldReaderStackTrace;
    final List<Constructor> constructors;
    final Constructor constructor0;
    final Constructor constructor1;
    final Constructor constructor2;
    final List<String[]> constructorParameters;

    protected ObjectReaderException(Class<T> objectClass) {
        this(
                objectClass,
                Arrays.asList(BeanUtils.getConstructor(objectClass)),
                ObjectReaders.fieldReader("stackTrace", StackTraceElement[].class, Throwable::setStackTrace)
        );
    }

    protected ObjectReaderException(
            Class<T> objectClass,
            List<Constructor> constructors,
            FieldReader... fieldReaders
    ) {
        super(objectClass, null, objectClass.getName(), 0, null, null, null, fieldReaders);
        this.constructors = constructors;

        Constructor constructor0 = null, constructor1 = null, constructor2 = null;

        for (Constructor constructor : constructors) {
            if (constructor != null && constructor2 == null) {
                int paramCount = constructor.getParameterCount();

                if (paramCount == 0) {
                    constructor0 = constructor;
                    continue;
                }

                if (paramCount == 1
                        && constructor.getParameterTypes()[0] == String.class) {
                    constructor1 = constructor;
                }

                if (paramCount == 2
                        && constructor.getParameterTypes()[0] == String.class
                        && constructor.getParameterTypes()[1] == Throwable.class
                ) {
                    constructor2 = constructor;
                }
            }
        }

        constructors.sort((Constructor left, Constructor right) -> {
            int x = left.getParameterCount();
            int y = right.getParameterCount();
            if (x < y) {
                return 1;
            }
            if (x > y) {
                return -1;
            }
            return 0;
        });

        constructorParameters = new ArrayList<>(constructors.size());
        for (Constructor constructor : constructors) {
            int paramCount = constructor.getParameterCount();
            String[] parameterNames = null;
            if (paramCount > 0) {
                parameterNames = ASMUtils.lookupParameterNames(constructor);
            }
            constructorParameters.add(parameterNames);
        }

        this.constructor0 = constructor0;
        this.constructor1 = constructor1;
        this.constructor2 = constructor2;

        FieldReader fieldReaderStackTrace = null;
        for (FieldReader fieldReader : fieldReaders) {
            if ("stackTrace".equals(fieldReader.fieldName) && fieldReader.fieldClass == StackTraceElement[].class) {
                fieldReaderStackTrace = fieldReader;
            }
        }
        this.fieldReaderStackTrace = fieldReaderStackTrace;
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        boolean objectStart = jsonReader.nextIfObjectStart();
        if (!objectStart) {
            if (jsonReader.nextIfEmptyString()) {
                return null;
            }
        }

        String message = null, localizedMessage = null;
        Throwable cause = null;
        StackTraceElement[] stackTrace = null;
        List<Throwable> suppressedExceptions = null;

        String stackTraceReference = null;
        String suppressedExceptionsReference = null;
        String causeReference = null;
        Map<String, Object> fieldValues = null;
        Map<String, String> references = null;
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
            } else if (hash == HASH_LOCALIZED_MESSAGE) {
                localizedMessage = jsonReader.readString();
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
                FieldReader fieldReader = getFieldReader(hash);
                if (fieldValues == null) {
                    fieldValues = new HashMap<>();
                }

                String name;
                if (fieldReader != null) {
                    name = fieldReader.fieldName;
                } else {
                    name = jsonReader.getFieldName();
                }

                Object fieldValue;
                if (jsonReader.isReference()) {
                    String reference = jsonReader.readReference();
                    if (references == null) {
                        references = new HashMap<>();
                    }
                    references.put(name, reference);
                    continue;
                }

                if (fieldReader != null) {
                    fieldValue = fieldReader.readFieldValue(jsonReader);
                } else {
                    fieldValue = jsonReader.readAny();
                }
                fieldValues.put(name, fieldValue);
            }
        }

        Throwable object = createObject(message, cause);

        if (object == null) {
            for (int i = 0; i < constructors.size(); i++) {
                String[] paramNames = constructorParameters.get(i);
                if (paramNames == null || paramNames.length == 0) {
                    continue;
                }

                boolean matchAll = true;
                for (int j = 0; j < paramNames.length; j++) {
                    String paramName = paramNames[j];

                    if (paramName == null) {
                        matchAll = false;
                        break;
                    }

                    switch (paramName) {
                        case "message":
                        case "cause":
                            break;
                        default:
                            if (!fieldValues.containsKey(paramName)) {
                                matchAll = false;
                            }
                            break;
                    }
                }

                if (!matchAll) {
                    continue;
                }

                Object[] args = new Object[paramNames.length];
                for (int j = 0; j < paramNames.length; j++) {
                    String paramName = paramNames[j];
                    Object fieldValue;
                    switch (paramName) {
                        case "message":
                            fieldValue = message;
                            break;
                        case "cause":
                            fieldValue = cause;
                            break;
                        default:
                            fieldValue = fieldValues.get(paramName);
                            break;
                    }
                    args[j] = fieldValue;
                }

                Constructor constructor = constructors.get(i);
                try {
                    object = (Throwable) constructor.newInstance(args);
                    break;
                } catch (Throwable e) {
                    throw new JSONException("create error, objectClass " + constructor + ", " + e.getMessage(), e);
                }
            }
        }

        if (object == null) {
            throw new JSONException(jsonReader.info(jsonReader.info("not support : " + objectClass.getName())));
        }

        if (stackTrace != null) {
            object.setStackTrace(stackTrace);
        }

        if (stackTraceReference != null) {
            jsonReader.addResolveTask(fieldReaderStackTrace, object, JSONPath.of(stackTraceReference));
        }

        if (fieldValues != null) {
            for (Iterator<Map.Entry<String, Object>> it = fieldValues.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Object> entry = it.next();
                FieldReader fieldReader = getFieldReader(entry.getKey());
                if (fieldReader != null) {
                    fieldReader.accept(object, entry.getValue());
                }
            }
        }

        if (references != null) {
            for (Iterator<Map.Entry<String, String>> it = references.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, String> entry = it.next();
                FieldReader fieldReader = getFieldReader(entry.getKey());
                if (fieldReader == null) {
                    continue;
                }
                fieldReader.addResolveTask(jsonReader, object, entry.getValue());
            }
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

        try {
            if (constructor2 != null) {
                if (cause != null || (message != null && constructor1 == null)) {
                    return (Throwable) constructor2.newInstance(message, cause);
                }
            }

            if (constructor1 != null) {
                if (message != null || constructor0 == null) {
                    return (Throwable) constructor1.newInstance(message);
                }
            }

            if (constructor0 != null) {
                return (Throwable) constructor0.newInstance();
            }
        } catch (Throwable e) {
            throw new JSONException("create Exception error, class " + objectClass.getName() + ", " + e.getMessage(), e);
        }

        return object;
    }
}
