package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

final class ObjectReaderException<T>
        implements ObjectReader<T> {
    static final long HASH_TYPE = Fnv.hashCode64("@type");
    static final long HASH_MESSAGE = Fnv.hashCode64("message");
    static final long HASH_DETAIL_MESSAGE = Fnv.hashCode64("detailMessage");
    static final long HASH_LOCALIZED_MESSAGE = Fnv.hashCode64("localizedMessage");
    static final long HASH_CAUSE = Fnv.hashCode64("cause");
    static final long HASH_STACKTRACE = Fnv.hashCode64("stackTrace");
    static final long HASH_SUPPRESSED_EXCEPTIONS = Fnv.hashCode64("suppressedExceptions");

    final Class<T> objectClass;
    private final Object fieldReaderStackTrace;
    final List<Constructor> constructors;

    final Constructor constructorDefault;
    final Constructor constructorMessage;
    final Constructor constructorMessageCause;
    final Constructor constructorCause;

    final List<String[]> constructorParameters;

    ObjectReaderException(Class<T> objectClass) {
        this(
                objectClass,
                Arrays.asList(BeanUtils.getConstructor(objectClass)),
                null /* ObjectReaders.fieldReader removed */
        );
    }

    @SuppressWarnings("unchecked")
    ObjectReaderException(
            Class<T> objectClass,
            List<Constructor> constructors,
            Object... fieldReaders
    ) {
        this.objectClass = objectClass;
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
            return Integer.compare(y, x);
        });

        constructorParameters = new ArrayList<>(constructors.size());
        for (Constructor constructor : constructors) {
            int paramCount = constructor.getParameterCount();
            String[] parameterNames = null;
            if (paramCount > 0) {
                parameterNames = ASMUtils.lookupParameterNames(constructor);

                Parameter[] parameters = constructor.getParameters();
                FieldInfo fieldInfo = new FieldInfo();
                for (int i = 0; i < parameters.length && i < parameterNames.length; i++) {
                    fieldInfo.init();

                    Parameter parameter = parameters[i];

                    ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
                    provider.getFieldInfo(fieldInfo, objectClass, constructor, i, parameter);
                    if (fieldInfo.fieldName != null) {
                        parameterNames[i] = fieldInfo.fieldName;
                    }
                }
            }

            constructorParameters.add(parameterNames);
        }

        this.fieldReaderStackTrace = null;
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T createInstance(Map map, long features) {
        if (map == null) {
            return null;
        }

        return readObject(JSONReader.of(JSON.toJSONString(map)), features);
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (false) {
            JSONReader.Context context = jsonReader.getContext();

            if (jsonReader.isSupportAutoType(features) || context.getContextAutoTypeBeforeHandler() != null) {
                jsonReader.next();
                long typeHash = jsonReader.readTypeHashCode();

                ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
                if (autoTypeObjectReader == null) {
                    String typeName = jsonReader.getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);

                    if (autoTypeObjectReader == null) {
                        throw new JSONException("autoType not support : " + typeName + ", offset " + jsonReader.getOffset());
                    }
                }
                return (T) autoTypeObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, 0);
            }
        }

        return readObject(jsonReader, fieldType, fieldName, features);
    }

    private Throwable createObject(String message, Throwable cause) {
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

        return null;
    }
}
