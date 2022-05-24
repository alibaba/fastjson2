package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.util.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;

final class ObjectWriterImplMap
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final byte[] TYPE_NAME_JSONObject1O = JSONB.toBytes("JO10");
    static final long TYPE_HASH_JSONObject1O = Fnv.hashCode64("JO10");

    final Type objectType;
    final Class objectClass;

    final Type keyType;
    final Type valueType;
    final boolean valueTypeRefDetect;
    volatile ObjectWriter valueWriter;

    final byte[] jsonbTypeInfo;
    final long typeNameHash;
    final long features;

    final boolean jsonObject1; // fastjson 1 JSONObject
    final Field jsonObject1InnerMap;
    long jsonObject1InnerMapOffset = -1;

    public ObjectWriterImplMap(Class objectClass, long features) {
        this(null, null, objectClass, objectClass, features);
    }

    public ObjectWriterImplMap(Type keyType, Type valueType, Class objectClass, Type objectType, long features) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.objectClass = objectClass;
        this.objectType = objectType;
        this.features = features;

        if (valueType == null) {
            this.valueTypeRefDetect = true;
        } else {
            this.valueTypeRefDetect = !ObjectWriterProvider.isNotReferenceDetect(TypeUtils.getClass(valueType));
        }

        String typeName = TypeUtils.getTypeName(objectClass);
        jsonObject1 = "JO1".equals(typeName);
        this.jsonbTypeInfo = JSONB.toBytes(typeName);
        this.typeNameHash = Fnv.hashCode64(typeName);
        if (jsonObject1) {
            jsonObject1InnerMap = BeanUtils.getDeclaredField(objectClass, "map");
            if (jsonObject1InnerMap != null) {
                jsonObject1InnerMap.setAccessible(true);
                if (JDKUtils.UNSAFE_SUPPORT) {
                    jsonObject1InnerMapOffset = UnsafeUtils.objectFieldOffset(jsonObject1InnerMap);
                }
            }
        } else {
            jsonObject1InnerMap = null;
        }
    }

    public static ObjectWriterImplMap of(Class objectClass) {
        return new ObjectWriterImplMap(null, null, objectClass, objectClass, 0);
    }

    public static ObjectWriterImplMap of(Type type) {
        Class objectClass = TypeUtils.getClass(type);
        return new ObjectWriterImplMap(objectClass, 0);
    }

    public static ObjectWriterImplMap of(Type type, Class defineClass) {
        Type keyType = null, valueType = null;

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length == 2) {
                keyType = actualTypeArguments[0];
                valueType = actualTypeArguments[1];
            }
        }

        return new ObjectWriterImplMap(keyType, valueType, defineClass, type, 0);
    }

    @Override
    public void writeArrayMappingJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        Map map = (Map) object;

        jsonWriter.startObject();
        boolean writeNulls = jsonWriter.isWriteNulls();
        for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                if (writeNulls) {
                    jsonWriter.writeString(key);
                    jsonWriter.writeNull();
                }
                continue;
            }

            jsonWriter.writeString(key);

            Class<?> valueType = value.getClass();
            if (valueType == String.class) {
                jsonWriter.writeString((String) value);
            } else {
                ObjectWriter valueWriter = jsonWriter.getObjectWriter(valueType);
                valueWriter.writeJSONB(jsonWriter, value, key, this.valueType, this.features);
            }
        }

        jsonWriter.endObject();
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if ((fieldType == this.objectType && jsonWriter.isWriteMapTypeInfo(object, objectClass, features))
                || jsonWriter.isWriteTypeInfo(object, fieldType, features)
        ) {
            boolean ordered = false;
            if (jsonObject1InnerMap != null) {
                if (jsonObject1InnerMapOffset != -1) {
                    Object innerMap = UnsafeUtils.UNSAFE.getObject(object, jsonObject1InnerMapOffset);
                    ordered = innerMap instanceof LinkedHashMap;
                } else {
                    try {
                        Object innerMap = jsonObject1InnerMap.get(object);
                        ordered = innerMap instanceof LinkedHashMap;
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }

            if (ordered) {
                jsonWriter.writeTypeName(TYPE_NAME_JSONObject1O, TYPE_HASH_JSONObject1O);
            } else {
                jsonWriter.writeTypeName(jsonbTypeInfo, typeNameHash);
            }
        }

        Map map = (Map) object;

        JSONWriter.Context context = jsonWriter.getContext();
        jsonWriter.startObject();

        Type fieldValueType = this.valueType;
        if (fieldType == this.objectType) {
            fieldValueType = this.valueType;
        } else if (fieldType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) fieldType).getActualTypeArguments();
            if (actualTypeArguments.length == 2) {
                fieldValueType = actualTypeArguments[1];
            }
        }

        long contextFeatures = context.getFeatures();
        boolean writeNulls = (contextFeatures & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) != 0;
        boolean fieldBased = (contextFeatures & JSONWriter.Feature.FieldBased.mask) != 0;
        ObjectWriterProvider provider = context.getProvider();

        Class itemClass = null;
        ObjectWriter itemWriter = null;
        JSONB.SymbolTable symbolTable = jsonWriter.getSymbolTable();
        boolean contextRefDetect = (contextFeatures & JSONWriter.Feature.ReferenceDetection.mask) != 0;

        int i = 0;
        for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext(); ++i) {
            Map.Entry entry = it.next();

            Object entryKey = entry.getKey();

            Object value = entry.getValue();
            if (value == null) {
                if (writeNulls) {
                    if (entryKey instanceof String) {
                        jsonWriter.writeString((String) entryKey);
                    } else {
                        Class<?> entryKeyClass = entryKey.getClass();
                        boolean keyRefDetect = contextRefDetect
                                && !ObjectWriterProvider.isNotReferenceDetect(entryKeyClass);

                        String refPath = null;
                        if (keyRefDetect) {
                            jsonWriter.setPath(i, entry);
                            refPath = jsonWriter.setPath("key", entryKey);
                        }
                        if (refPath != null) {
                            jsonWriter.writeReference(refPath);
                        } else {
                            ObjectWriter keyWriter = provider.getObjectWriter(entryKeyClass, entryKeyClass, fieldBased);
                            keyWriter.writeJSONB(jsonWriter, entryKey, null, null, 0);
                        }
                        if (keyRefDetect) {
                            jsonWriter.popPath(entry);
                            jsonWriter.popPath(entryKey);
                        }
                    }
                    jsonWriter.writeNull();
                }
                continue;
            }

            if (entryKey instanceof String || (contextFeatures & JSONWriter.Feature.WriteClassName.mask) == 0) {
                String key = (String) entryKey;

                if (symbolTable != null) {
                    jsonWriter.writeSymbol(key);

                    if (value instanceof String) {
                        jsonWriter.writeSymbol((String) value);
                        continue;
                    }
                } else {
                    jsonWriter.writeString(key);
                }
            } else {
                if (contextRefDetect) {
                    jsonWriter.config(JSONWriter.Feature.ReferenceDetection, false);
                }
                Class<?> entryKeyClass = entryKey.getClass();
                ObjectWriter keyWriter = provider.getObjectWriter(entryKeyClass, entryKeyClass, fieldBased);
                keyWriter.writeJSONB(jsonWriter, entryKey, null, null, 0);
                if (contextRefDetect) {
                    jsonWriter.config(JSONWriter.Feature.ReferenceDetection, true);
                }
            }

            Class<?> valueClass = value.getClass();
            if (valueClass == String.class) {
                jsonWriter.writeString((String) value);
                continue;
            }

            boolean valueRefDetecChanged = false;
            boolean valueRefDetect;
            if (valueClass == this.valueType) {
                valueRefDetect = contextRefDetect && this.valueTypeRefDetect;
            } else {
                valueRefDetect = contextRefDetect && !ObjectWriterProvider.isNotReferenceDetect(valueClass);
            }

            if (valueRefDetect) {
                if (value == object) {
                    jsonWriter.writeReference("..");
                    continue;
                }

                String refPath;
                if (entryKey instanceof String) {
                    refPath = jsonWriter.setPath((String) entryKey, value);
                } else if (ObjectWriterProvider.isPrimitiveOrEnum(entryKey.getClass())) {
                    refPath = jsonWriter.setPath(entryKey.toString(), value);
                } else {
                    if (map.size() != 1 && !(map instanceof SortedMap) && !(map instanceof LinkedHashMap)) {
                        refPath = null; // skip
                        jsonWriter.config(JSONWriter.Feature.ReferenceDetection, false);
                        valueRefDetecChanged = true;
                        valueRefDetect = false;
                    } else {
                        refPath = jsonWriter.setPath(i, value);
                    }
                }

                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(value);
                    continue;
                }
            }

            ObjectWriter valueWriter;
            if (valueClass == this.valueType && this.valueWriter != null) {
                valueWriter = this.valueWriter;
            } else if (itemClass == valueClass) {
                valueWriter = itemWriter;
            } else {
                valueWriter = provider.getObjectWriter(valueClass, valueClass, fieldBased);
                if (itemWriter == null) {
                    itemWriter = valueWriter;
                    itemClass = valueClass;
                }

                if (valueClass == this.valueType) {
                    this.valueWriter = valueWriter;
                }
            }

            valueWriter.writeJSONB(jsonWriter, value, entryKey, fieldValueType, this.features);

            if (valueRefDetecChanged) {
                jsonWriter.config(JSONWriter.Feature.ReferenceDetection, true);
            } else {
                if (valueRefDetect) {
                    jsonWriter.popPath(value);
                }
            }
        }

        jsonWriter.endObject();
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isJSONB()) {
            writeJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        if (hasFilter(jsonWriter)) {
            writeWithFilter(jsonWriter, object);
            return;
        }

        boolean refDetect = jsonWriter.isRefDetect();

        jsonWriter.startObject();
        Map map = (Map) object;

        for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = it.next();
            Object value = entry.getValue();
            Object key = entry.getKey();
            String strKey = key == null ? "null" : key.toString();

            if (value == null) {
                if (jsonWriter.isWriteNulls()) {
                    jsonWriter.writeName(strKey);
                    jsonWriter.writeColon();
                    jsonWriter.writeNull();
                }
                continue;
            }

            jsonWriter.writeName(strKey);
            jsonWriter.writeColon();

            Class<?> valueType = value.getClass();
            if (valueType == String.class) {
                jsonWriter.writeString((String) value);
                continue;
            }

            ObjectWriter valueWriter;
            if (valueType == this.valueType) {
                if (this.valueWriter != null) {
                    valueWriter = this.valueWriter;
                } else {
                    valueWriter = this.valueWriter = jsonWriter.getObjectWriter(valueType);
                }
            } else {
                valueWriter = jsonWriter.getObjectWriter(valueType);
            }

            if (refDetect && !ObjectWriterProvider.isPrimitiveOrEnum(value.getClass())) {
                if (value == object) {
                    jsonWriter.writeReference("..");
                    continue;
                }

                String refPath = jsonWriter.setPath(strKey, value);
                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(value);
                    continue;
                }
            }

            valueWriter.write(jsonWriter, value, key, this.valueType, this.features);

            if (refDetect) {
                jsonWriter.popPath(value);
            }
        }

        jsonWriter.endObject();
    }

    @Override
    public void writeWithFilter(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.startObject();
        Map map = (Map) object;

        JSONWriter.Context ctx = jsonWriter.getContext();
        PropertyPreFilter propertyPreFilter = ctx.getPropertyPreFilter();
        NameFilter nameFilter = ctx.getNameFilter();
        ValueFilter valueFilter = ctx.getValueFilter();
        PropertyFilter propertyFilter = ctx.getPropertyFilter();

        for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = it.next();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }

            String key = entry.getKey().toString();

            if (propertyPreFilter != null) {
                if (!propertyPreFilter.process(jsonWriter, object, key)) {
                    continue;
                }
            }

            if (nameFilter != null) {
                key = nameFilter.process(object, key, value);
            }

            if (propertyFilter != null) {
                if (!propertyFilter.apply(object, key, value)) {
                    continue;
                }
            }

            jsonWriter.writeName(key);
            jsonWriter.writeColon();

            if (valueFilter != null) {
                value = valueFilter.apply(object, key, value);
            }

            if (value == null) {
                continue;
            }

            Class<?> valueType = value.getClass();
            ObjectWriter valueWriter = jsonWriter.getObjectWriter(valueType);
            valueWriter.write(jsonWriter, value, fieldName, fieldType, this.features);
        }

        jsonWriter.endObject();
    }
}
