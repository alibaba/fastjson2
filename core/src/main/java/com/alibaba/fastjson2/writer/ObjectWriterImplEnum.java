package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.*;

/**
 * ObjectWriterImplEnum provides serialization support for Enum types to JSON format.
 * This writer handles enum serialization with various strategies including name, ordinal,
 * toString, and custom value fields.
 *
 * <p>This class provides support for:
 * <ul>
 *   <li>Enum serialization using enum name (default)</li>
 *   <li>Enum serialization using enum ordinal</li>
 *   <li>Enum serialization using toString() method</li>
 *   <li>Enum serialization using custom value fields (annotated with @JSONField)</li>
 *   <li>Type information writing for polymorphic deserialization</li>
 *   <li>Optimized JSONB encoding with cached names</li>
 *   <li>Support for renamed enum values via annotations</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Define an enum
 * enum Status {
 *     ACTIVE, INACTIVE, PENDING
 * }
 *
 * // Default serialization (by name)
 * Status status = Status.ACTIVE;
 * String json = JSON.toJSONString(status); // "ACTIVE"
 *
 * // Serialize using toString
 * String json = JSON.toJSONString(status, JSONWriter.Feature.WriteEnumUsingToString);
 *
 * // Enum with custom value field
 * enum Priority {
 *     @JSONField(value = 1) HIGH,
 *     @JSONField(value = 2) MEDIUM,
 *     @JSONField(value = 3) LOW
 * }
 * String json = JSON.toJSONString(Priority.HIGH); // Serializes custom value
 * }</pre>
 *
 * @param <E> the enum type
 * @since 2.0.0
 */
final class ObjectWriterImplEnum<E extends Enum<E>>
        extends ObjectWriterPrimitiveImpl {
    final Member valueField;

    final Class defineClass;
    final Class enumType;
    final long features;

    byte[] typeNameJSONB;
    long typeNameHash;

    final Enum[] enumConstants;
    final String[] names;
    final long[] hashCodes;

    byte[][] jsonbNames;
    final String[] annotationNames;

    public ObjectWriterImplEnum(
            Class defineClass,
            Class enumType,
            Member valueField,
            String[] annotationNames,
            long features
    ) {
        this.defineClass = defineClass;
        this.enumType = enumType;
        this.features = features;
        this.valueField = valueField;

        if (valueField instanceof AccessibleObject) {
            try {
                ((AccessibleObject) valueField).setAccessible(true);
            } catch (Throwable ignored) {
                // ignored
            }
        }

        this.enumConstants = (Enum[]) enumType.getEnumConstants();
        this.names = new String[enumConstants.length];
        this.hashCodes = new long[enumConstants.length];
        for (int i = 0; i < enumConstants.length; i++) {
            String name = enumConstants[i].name();
            this.names[i] = name;
            hashCodes[i] = Fnv.hashCode64(name);
        }
        this.annotationNames = annotationNames;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            if (typeNameJSONB == null) {
                String typeName = TypeUtils.getTypeName(enumType);
                typeNameJSONB
                        = JSONB.toBytes(
                        typeName);
                typeNameHash = Fnv.hashCode64(typeName);
            }
            jsonWriter.writeTypeName(typeNameJSONB, typeNameHash);
        }

        Enum e = (Enum) object;
        if (jsonWriter.isEnabled(JSONWriter.Feature.WriteEnumUsingToString)) {
            jsonWriter.writeString(e.toString());
        } else {
            if (jsonbNames == null) {
                jsonbNames = new byte[this.names.length][];
            }

            int ordinal = e.ordinal();
            byte[] jsonbName = this.jsonbNames[ordinal];
            if (jsonbName == null) {
                jsonbName = JSONB.toBytes(this.names[ordinal]);
                this.jsonbNames[ordinal] = jsonbName;
            }
            jsonWriter.writeRaw(jsonbName);
        }
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        Enum e = (Enum) object;
        if (e == null) {
            jsonWriter.writeNull();
            return;
        }

        if (valueField != null) {
            Object fieldValue;
            try {
                if (valueField instanceof Field) {
                    fieldValue = ((Field) valueField).get(object);
                } else {
                    fieldValue = ((Method) valueField).invoke(object);
                }
                if (fieldValue != object) {
                    jsonWriter.writeAny(fieldValue);
                    return;
                }
            } catch (Exception error) {
                throw new JSONException("getEnumValue error", error);
            }
        }

        long features2 = jsonWriter.getFeatures(features | this.features);

        if ((features2 & JSONWriter.Feature.WriteEnumUsingToString.mask) != 0) {
            jsonWriter.writeString(e.toString());
            return;
        }

        if ((features2 & JSONWriter.Feature.WriteEnumUsingOrdinal.mask) != 0) {
            jsonWriter.writeInt32(e.ordinal());
            return;
        }

        String enumName = null;
        if (annotationNames != null) {
            int ordinal = e.ordinal();
            if (ordinal < annotationNames.length) {
                enumName = annotationNames[ordinal];
            }
        }
        if (enumName == null) {
            enumName = e.name();
        }
        jsonWriter.writeString(enumName);
    }
}
