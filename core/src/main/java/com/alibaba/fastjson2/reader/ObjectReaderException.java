package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;

final class ObjectReaderException<T>
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

    final Constructor constructorDefault;
    final Constructor constructorMessage;
    final Constructor constructorMessageCause;
    final Constructor constructorCause;

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

        Constructor constructorDefault = null;
        Constructor constructorMessage = null;
        Constructor constructorMessageCause = null;
        Constructor constructorCause = null;

        for (Constructor constructor : constructors) {
            if (constructor != null && constructorMessageCause == null) {
                int paramCount = constructor.getParameterCount();

                if (paramCount == 0) {
                    constructorDefault = constructor;
                    continue;
                }

                Class[] paramTypes = constructor.getParameterTypes();
                Class paramType0 = paramTypes[0];
                if (paramCount == 1) {
                    if (paramType0 == String.class) {
                        constructorMessage = constructor;
                    } else if (Throwable.class.isAssignableFrom(paramType0)) {
                        constructorCause = constructor;
                    }
                }

                if (paramCount == 2
                        && paramType0 == String.class
                        && Throwable.class.isAssignableFrom(paramTypes[1])
                ) {
                    constructorMessageCause = constructor;
                }
            }
        }
        this.constructorDefault = constructorDefault;
        this.constructorMessage = constructorMessage;
        this.constructorMessageCause = constructorMessageCause;
        this.constructorCause = constructorCause;

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
            if (jsonReader.nextIfNullOrEmptyString()) {
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
            int nullCount = 0;
            for (StackTraceElement item : stackTrace) {
                if (item == null) {
                    nullCount++;
                }
            }
            if (stackTrace.length == 0 || nullCount != stackTrace.length) {
                object.setStackTrace(stackTrace);
            }
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
        try {
            if (constructorMessageCause != null) {
                if (cause != null && message != null) {
                    return (Throwable) constructorMessageCause.newInstance(message, cause);
                }
            }

            if (constructorMessage != null) {
                if (message != null) {
                    return (Throwable) constructorMessage.newInstance(message);
                }
            }

            if (constructorCause != null) {
                if (cause != null) {
                    return (Throwable) constructorCause.newInstance(cause);
                }
            }

            if (constructorMessageCause != null) {
                if (cause != null || message != null) {
                    return (Throwable) constructorMessageCause.newInstance(message, cause);
                }
            }

            if (constructorDefault != null) {
                return (Throwable) constructorDefault.newInstance();
            }

            if (constructorMessageCause != null) {
                return (Throwable) constructorMessageCause.newInstance(message, cause);
            }

            if (constructorMessage != null) {
                return (Throwable) constructorMessage.newInstance(message);
            }

            if (constructorCause != null) {
                return (Throwable) constructorCause.newInstance(cause);
            }
        } catch (Throwable e) {
            throw new JSONException("create Exception error, class " + objectClass.getName() + ", " + e.getMessage(), e);
        }

        return object;
    }
}
