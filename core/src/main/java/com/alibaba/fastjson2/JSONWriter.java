package com.alibaba.fastjson2;

import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.*;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.isJavaScriptSupport;

/**
 * JSONWriter is the core class for writing and serializing Java objects to JSON format in FASTJSON2.
 * It provides methods to convert various Java data types into JSON representation, supporting both
 * standard JSON and binary JSON (JSONB) formats.
 *
 * <p>JSONWriter supports multiple output destinations including strings, byte arrays, streams, and writers.
 * It also supports different character encodings such as UTF-8, UTF-16, and others.</p>
 *
 * <p>Example usage:
 * <pre>
 * // Writing to a string
 * try (JSONWriter writer = JSONWriter.of()) {
 *     writer.writeAny(object);
 *     String json = writer.toString();
 * }
 *
 * // Writing to a stream
 * try (ByteArrayOutputStream out = new ByteArrayOutputStream();
 *      JSONWriter writer = JSONWriter.of(out, StandardCharsets.UTF_8)) {
 *     writer.writeAny(object);
 *     byte[] jsonBytes = out.toByteArray();
 * }
 *
 * // Writing with specific features
 * try (JSONWriter writer = JSONWriter.of(JSONWriter.Feature.PrettyFormat)) {
 *     writer.writeAny(object);
 *     String prettyJson = writer.toString();
 * }
 * </pre>
 *
 *
 * <p>JSONWriter instances are not thread-safe and should not be shared between multiple threads.
 * Each thread should create its own JSONWriter instance or use the factory methods to create
 * new instances as needed.</p>
 *
 * @see JSONWriter.Context
 * @see JSONWriter.Feature
 * @since 2.0.0
 */
public abstract class JSONWriter
        implements Closeable {
    static final long WRITE_ARRAY_NULL_MASK = NullAsDefaultValue.mask | WriteNullListAsEmpty.mask;
    static final byte PRETTY_NON = 0, PRETTY_TAB = 1, PRETTY_2_SPACE = 2, PRETTY_4_SPACE = 4;
    static final long NONE_DIRECT_FEATURES = ReferenceDetection.mask | NotWriteEmptyArray.mask | NotWriteDefaultValue.mask;

    public final Context context;
    public final boolean utf8;
    public final boolean utf16;
    public final boolean jsonb;
    public final boolean useSingleQuote;
    public final SymbolTable symbolTable;

    protected final Charset charset;
    public final char quote;
    protected final int maxArraySize;

    protected boolean startObject;
    protected int level;
    protected int off;
    protected Object rootObject;
    protected IdentityHashMap<Object, Path> refs;
    protected Path path;
    protected String lastReference;
    protected byte pretty;
    protected Object attachment;

    protected JSONWriter(
            Context context,
            SymbolTable symbolTable,
            boolean jsonb,
            Charset charset
    ) {
        this.context = context;
        this.symbolTable = symbolTable;
        this.charset = charset;
        this.jsonb = jsonb;
        this.utf8 = !jsonb && charset == StandardCharsets.UTF_8;
        this.utf16 = !jsonb && charset == StandardCharsets.UTF_16;
        this.useSingleQuote = !jsonb && (context.features & UseSingleQuotes.mask) != 0;

        quote = useSingleQuote ? '\'' : '"';

        // 64M or 1G
        maxArraySize = (context.features & LargeObject.mask) != 0 ? 1073741824 : 67108864;
        if ((context.features & PrettyFormatWith4Space.mask) != 0) {
            pretty = PRETTY_4_SPACE;
        } else if ((context.features & PrettyFormatWith2Space.mask) != 0) {
            pretty = PRETTY_2_SPACE;
        } else if ((context.features & PrettyFormat.mask) != 0) {
            pretty = PRETTY_TAB;
        } else {
            pretty = PRETTY_NON;
        }
    }

    /**
     * Gets the charset used by this JSONWriter.
     *
     * @return the charset
     */
    public final Charset getCharset() {
        return charset;
    }

    /**
     * Checks if this JSONWriter is using UTF-8 encoding.
     *
     * @return true if using UTF-8 encoding, false otherwise
     */
    public final boolean isUTF8() {
        return utf8;
    }

    /**
     * Checks if this JSONWriter is using UTF-16 encoding.
     *
     * @return true if using UTF-16 encoding, false otherwise
     */
    public final boolean isUTF16() {
        return utf16;
    }

    /**
     * Checks if the IgnoreNoneSerializable feature is enabled.
     *
     * @return true if the feature is enabled, false otherwise
     */
    public final boolean isIgnoreNoneSerializable() {
        return (context.features & MASK_IGNORE_NONE_SERIALIZABLE) != 0;
    }

    /**
     * Checks if the IgnoreNoneSerializable feature is enabled for the specified object.
     *
     * @param object the object to check
     * @return true if the feature is enabled and the object is not serializable, false otherwise
     */
    public final boolean isIgnoreNoneSerializable(Object object) {
        return (context.features & MASK_IGNORE_NONE_SERIALIZABLE) != 0
                && object != null
                && !Serializable.class.isAssignableFrom(object.getClass());
    }

    /**
     * Gets the symbol table used by this JSONWriter.
     *
     * @return the symbol table, or null if not set
     */
    public final SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Configures features for this JSONWriter.
     *
     * @param features the features to enable
     */
    public final void config(Feature... features) {
        context.config(features);
    }

    /**
     * Configures a specific feature for this JSONWriter.
     *
     * @param feature the feature to configure
     * @param state true to enable the feature, false to disable it
     */
    public final void config(Feature feature, boolean state) {
        context.config(feature, state);
    }

    /**
     * Gets the context used by this JSONWriter.
     *
     * @return the context
     */
    public final Context getContext() {
        return context;
    }

    /**
     * Gets the current nesting level of this JSONWriter.
     *
     * @return the current nesting level
     */
    public final int level() {
        return level;
    }

    /**
     * Sets the root object for this JSONWriter.
     * This method initializes the root object and sets the path to the root path.
     *
     * @param rootObject the root object to set
     */
    public final void setRootObject(Object rootObject) {
        this.rootObject = rootObject;
        this.path = JSONWriter.Path.ROOT;
    }

    /**
     * Sets the path for the specified object with the given name.
     * This method is used for reference detection during serialization.
     *
     * @param name the name of the path segment
     * @param object the object to set the path for
     * @return the previous path as a string, or null if no previous path exists
     */
    public final String setPath(String name, Object object) {
        if (!isRefDetect(object)) {
            return null;
        }

        this.path = new Path(this.path, name);

        Path previous;
        if (object == rootObject) {
            previous = Path.ROOT;
        } else {
            if (refs == null || (previous = refs.get(object)) == null) {
                if (refs == null) {
                    refs = new IdentityHashMap(8);
                }
                refs.put(object, this.path);
                return null;
            }
        }

        return previous.toString();
    }

    /**
     * Sets the path for the specified object using the provided field writer.
     * This method is used for reference detection during serialization.
     *
     * @param fieldWriter the field writer to use for path generation
     * @param object the object to set the path for
     * @return the previous path as a string, or null if no previous path exists
     */
    public final String setPath(FieldWriter fieldWriter, Object object) {
        if (!isRefDetect(object)) {
            return null;
        }

        this.path = this.path == Path.ROOT
                ? fieldWriter.getRootParentPath()
                : fieldWriter.getPath(path);

        Path previous;
        if (object == rootObject) {
            previous = Path.ROOT;
        } else {
            if (refs == null || (previous = refs.get(object)) == null) {
                if (refs == null) {
                    refs = new IdentityHashMap(8);
                }
                refs.put(object, this.path);
                return null;
            }
        }

        return previous.toString();
    }

    /**
     * Sets the path for the specified object using the provided field writer without reference detection.
     * This method is used for reference detection during serialization.
     *
     * @param fieldWriter the field writer to use for path generation
     * @param object the object to set the path for
     * @return the previous path as a string, or null if no previous path exists
     */
    public final String setPath0(FieldWriter fieldWriter, Object object) {
        this.path = this.path == Path.ROOT
                ? fieldWriter.getRootParentPath()
                : fieldWriter.getPath(path);

        Path previous;
        if (object == rootObject) {
            previous = Path.ROOT;
        } else {
            if (refs == null || (previous = refs.get(object)) == null) {
                if (refs == null) {
                    refs = new IdentityHashMap(8);
                }
                refs.put(object, this.path);
                return null;
            }
        }

        return previous.toString();
    }

    /**
     * Adds a manager reference for the specified object.
     * This method adds a special reference to the object in the reference map,
     * marking it as a manager reference.
     *
     * @param object the object to add a manager reference for
     */
    public final void addManagerReference(Object object) {
        if (refs == null) {
            refs = new IdentityHashMap(8);
        }
        refs.putIfAbsent(object, Path.MANGER_REFERNCE);
    }

    /**
     * Writes a reference to the specified object at the given index.
     * This method sets the path for the object and writes a reference if one already exists.
     *
     * @param index the index to set the path for
     * @param object the object to write a reference for
     * @return true if a reference was written, false otherwise
     */
    public final boolean writeReference(int index, Object object) {
        String refPath = setPath(index, object);
        if (refPath != null) {
            writeReference(refPath);
            popPath(object);
            return true;
        }
        return false;
    }

    /**
     * Sets the path for the specified object at the given index.
     * This method is used for reference detection during serialization of array elements.
     *
     * @param index the index to set the path for
     * @param object the object to set the path for
     * @return the previous path as a string, or null if no previous path exists
     */
    public final String setPath(int index, Object object) {
        if (!isRefDetect(object)) {
            return null;
        }

        return setPath0(index, object);
    }

    /**
     * Sets the path for the specified object at the given index without reference detection.
     * This method is used for reference detection during serialization of array elements.
     *
     * @param index the index to set the path for
     * @param object the object to set the path for
     * @return the previous path as a string, or null if no previous path exists
     */
    public final String setPath0(int index, Object object) {
        if (path == null) {
            return null;
        }
        this.path = index == 0
                ? (path.child0 != null ? path.child0 : (path.child0 = new Path(path, index)))
                : index == 1
                ? (path.child1 != null ? path.child1 : (path.child1 = new Path(path, index)))
                : new Path(path, index);

        Path previous;
        if (object == rootObject) {
            previous = Path.ROOT;
        } else {
            if (refs == null || (previous = refs.get(object)) == null) {
                if (refs == null) {
                    this.refs = new IdentityHashMap(8);
                }
                refs.put(object, this.path);
                return null;
            }
        }

        return previous.toString();
    }

    /**
     * Removes the path for the specified object.
     * This method is used to clean up path information during serialization.
     *
     * @param object the object to remove the path for
     */
    public final void popPath(Object object) {
        if (!isRefDetect(object)) {
            return;
        }

        popPath0(object);
    }

    /**
     * Removes the path for the specified object without reference detection.
     * This method is used to clean up path information during serialization.
     *
     * @param object the object to remove the path for
     */
    public final void popPath0(Object object) {
        if (this.path == null
                || (context.features & MASK_REFERENCE_DETECTION) == 0
                || object == Collections.EMPTY_LIST
                || object == Collections.EMPTY_SET
        ) {
            return;
        }

        this.path = this.path.parent;
    }

    /**
     * Checks if any filter is configured for this JSONWriter.
     *
     * @return true if any filter is configured, false otherwise
     */
    public final boolean hasFilter() {
        return context.hasFilter;
    }

    /**
     * Checks if any filter or the specified feature is configured for this JSONWriter.
     *
     * @param feature the feature to check
     * @return true if any filter is configured or the specified feature is enabled, false otherwise
     */
    public final boolean hasFilter(long feature) {
        return context.hasFilter || (context.features & feature) != 0;
    }

    /**
     * Checks if any filter is configured for this JSONWriter or if the IgnoreNonFieldGetter feature
     * should be applied based on the containsNoneFieldGetter parameter.
     *
     * @param containsNoneFieldGetter whether to check for the IgnoreNonFieldGetter feature
     * @return true if any filter is configured or the IgnoreNonFieldGetter feature should be applied, false otherwise
     */
    public final boolean hasFilter(boolean containsNoneFieldGetter) {
        return context.hasFilter || containsNoneFieldGetter && (context.features & MASK_IGNORE_NON_FIELD_GETTER) != 0;
    }

    /**
     * Checks if the WriteNulls feature is enabled.
     *
     * @return true if the WriteNulls feature is enabled, false otherwise
     */
    public final boolean isWriteNulls() {
        return (context.features & WriteNulls.mask) != 0;
    }

    /**
     * Checks if the ReferenceDetection feature is enabled.
     *
     * @return true if the ReferenceDetection feature is enabled, false otherwise
     */
    public final boolean isRefDetect() {
        return (context.features & ReferenceDetection.mask) != 0
                && (context.features & FieldInfo.DISABLE_REFERENCE_DETECT) == 0;
    }

    /**
     * Checks if single quotes are being used for this JSONWriter.
     *
     * @return true if single quotes are being used, false otherwise
     */
    public final boolean isUseSingleQuotes() {
        return useSingleQuote;
    }

    /**
     * Checks if the ReferenceDetection feature is enabled for the specified object.
     *
     * @param object the object to check
     * @return true if the ReferenceDetection feature is enabled and the object is not null and not a non-reference detect type, false otherwise
     */
    public final boolean isRefDetect(Object object) {
        return (context.features & MASK_REFERENCE_DETECTION) != 0
                && (context.features & FieldInfo.DISABLE_REFERENCE_DETECT) == 0
                && object != null
                && !ObjectWriterProvider.isNotReferenceDetect(object.getClass());
    }

    /**
     * Checks if the specified object is contained in the reference map.
     *
     * @param value the object to check
     * @return true if the object is contained in the reference map, false otherwise
     */
    public final boolean containsReference(Object value) {
        return refs != null && refs.containsKey(value);
    }

    /**
     * Gets the path of the specified object in the reference map.
     *
     * @param value the object to get the path for
     * @return the path of the object, or "$" if the object is not in the reference map
     */
    public final String getPath(Object value) {
        Path path;
        return refs == null || (path = refs.get(value)) == null
                ? "$"
                : path.toString();
    }

    /**
     * If ReferenceDetection has been set, returns the path of the current object, otherwise returns null
     * @since 2.0.51
     * @return the path of the current object
     */
    public String getPath() {
        return path == null ? null : path.toString();
    }

    /**
     * Removes the reference to the specified object.
     * This method removes the mapping of the object from the reference map.
     *
     * @param value the object whose reference should be removed
     * @return true if the reference was removed, false otherwise
     */
    public final boolean removeReference(Object value) {
        return this.refs != null && this.refs.remove(value) != null;
    }

    /**
     * Checks if the BeanToArray feature is enabled.
     * When enabled, Java beans will be serialized as JSON arrays instead of JSON objects.
     *
     * @return true if the BeanToArray feature is enabled, false otherwise
     */
    public final boolean isBeanToArray() {
        return (context.features & MASK_BEAN_TO_ARRAY) != 0;
    }

    /**
     * Checks if the specified feature is enabled.
     *
     * @param feature the feature to check
     * @return true if the feature is enabled, false otherwise
     */
    public final boolean isEnabled(Feature feature) {
        return (context.features & feature.mask) != 0;
    }

    /**
     * Checks if the specified feature is enabled.
     *
     * @param feature the feature to check
     * @return true if the feature is enabled, false otherwise
     */
    public final boolean isEnabled(long feature) {
        return (context.features & feature) != 0;
    }

    /**
     * Gets the features bitmask.
     *
     * @return the features bitmask
     */
    public final long getFeatures() {
        return context.features;
    }

    /**
     * Gets the combined features bitmask including the specified additional features.
     *
     * @param features the additional features to combine with the current features
     * @return the combined features bitmask
     */
    public final long getFeatures(long features) {
        return context.features | features;
    }

    /**
     * Checks if the IgnoreErrorGetter feature is enabled.
     * When enabled, exceptions thrown by getter methods will be ignored rather than propagated.
     *
     * @return true if the IgnoreErrorGetter feature is enabled, false otherwise
     */
    /**
     * Checks if the IgnoreErrorGetter feature is enabled.
     *
     * @return true if the IgnoreErrorGetter feature is enabled, false otherwise
     */
    public final boolean isIgnoreErrorGetter() {
        return (context.features & MASK_IGNORE_ERROR_GETTER) != 0;
    }

    /**
     * Checks if type information should be written for the specified object and field class.
     *
     * @param object the object to check
     * @param fieldClass the field class to check
     * @return true if type information should be written, false otherwise
     */
    public final boolean isWriteTypeInfo(Object object, Class fieldClass) {
        long features = context.features;
        if ((features & MASK_WRITE_CLASS_NAME) == 0) {
            return false;
        }

        if (object == null) {
            return false;
        }

        Class objectClass = object.getClass();
        if (objectClass == fieldClass) {
            return false;
        }

        if ((features & MASK_NOT_WRITE_HASHMAP_ARRAY_LIST_CLASS_NAME) != 0) {
            if (objectClass == HashMap.class || objectClass == ArrayList.class) {
                return false;
            }
        }

        return (features & MASK_NOT_WRITE_ROOT_CLASSNAME) == 0
                || object != this.rootObject;
    }

    /**
     * Checks if type information should be written for the specified object and field type.
     *
     * @param object the object to check
     * @param fieldType the field type to check
     * @return true if type information should be written, false otherwise
     */
    public final boolean isWriteTypeInfo(Object object, Type fieldType) {
        long features = context.features;
        if ((features & MASK_WRITE_CLASS_NAME) == 0
                || object == null
        ) {
            return false;
        }

        Class objectClass = object.getClass();
        Class fieldClass = null;
        if (fieldType instanceof Class) {
            fieldClass = (Class) fieldType;
        } else if (fieldType instanceof GenericArrayType) {
            if (isWriteTypeInfoGenericArray((GenericArrayType) fieldType, objectClass)) {
                return false;
            }
        } else if (fieldType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) fieldType).getRawType();
            if (rawType instanceof Class) {
                fieldClass = (Class) rawType;
            }
        }
        if (objectClass == fieldClass) {
            return false;
        }

        if ((features & MASK_NOT_WRITE_HASHMAP_ARRAY_LIST_CLASS_NAME) != 0) {
            if (objectClass == HashMap.class || objectClass == ArrayList.class) {
                return false;
            }
        }

        return (features & MASK_NOT_WRITE_ROOT_CLASSNAME) == 0
                || object != this.rootObject;
    }

    private static boolean isWriteTypeInfoGenericArray(GenericArrayType fieldType, Class objectClass) {
        Type componentType = fieldType.getGenericComponentType();
        if (componentType instanceof ParameterizedType) {
            componentType = ((ParameterizedType) componentType).getRawType();
        }
        if (objectClass.isArray()) {
            return objectClass.getComponentType().equals(componentType);
        }
        return false;
    }

    /**
     * Checks if type information should be written for the specified object.
     * This method determines whether to include class name information in the serialized JSON
     * based on various feature settings and object characteristics.
     *
     * @param object the object to check for type information writing
     * @return true if type information should be written, false otherwise
     */
    public final boolean isWriteTypeInfo(Object object) {
        long features = context.features;
        if ((features & MASK_WRITE_CLASS_NAME) == 0) {
            return false;
        }

        if ((features & MASK_NOT_WRITE_HASHMAP_ARRAY_LIST_CLASS_NAME) != 0
                && object != null) {
            Class objectClass = object.getClass();
            if (objectClass == HashMap.class || objectClass == ArrayList.class) {
                return false;
            }
        }

        return (features & MASK_NOT_WRITE_ROOT_CLASSNAME) == 0
                || object != this.rootObject;
    }

    /**
     * Checks if type information should be written for the specified object, field type, and features.
     *
     * @param object the object to check
     * @param fieldType the field type to check
     * @param features the features to consider
     * @return true if type information should be written, false otherwise
     */
    public final boolean isWriteTypeInfo(Object object, Type fieldType, long features) {
        features |= context.features;

        if ((features & MASK_WRITE_CLASS_NAME) == 0) {
            return false;
        }

        if (object == null) {
            return false;
        }

        Class objectClass = object.getClass();
        Class fieldClass = null;
        if (fieldType instanceof Class) {
            fieldClass = (Class) fieldType;
        } else if (fieldType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) fieldType).getRawType();
            if (rawType instanceof Class) {
                fieldClass = (Class) rawType;
            }
        }
        if (objectClass == fieldClass) {
            return false;
        }

        if ((features & MASK_NOT_WRITE_HASHMAP_ARRAY_LIST_CLASS_NAME) != 0) {
            if (objectClass == HashMap.class) {
                if (fieldClass == null || fieldClass == Object.class || fieldClass == Map.class || fieldClass == AbstractMap.class) {
                    return false;
                }
            } else if (objectClass == ArrayList.class) {
                return false;
            }
        }

        return (features & NotWriteRootClassName.mask) == 0
                || object != this.rootObject;
    }

    /**
     * Checks if type information should be written for the specified object, field class, and features.
     *
     * @param object the object to check
     * @param fieldClass the field class to check
     * @param features the features to consider
     * @return true if type information should be written, false otherwise
     */
    public final boolean isWriteTypeInfo(Object object, Class fieldClass, long features) {
        if (object == null) {
            return false;
        }

        Class objectClass = object.getClass();
        if (objectClass == fieldClass) {
            return false;
        }

        features |= context.features;

        if ((features & WriteClassName.mask) == 0) {
            return false;
        }

        if ((features & NotWriteHashMapArrayListClassName.mask) != 0) {
            if (objectClass == HashMap.class) {
                if (fieldClass == null || fieldClass == Object.class || fieldClass == Map.class || fieldClass == AbstractMap.class) {
                    return false;
                }
            } else if (objectClass == ArrayList.class) {
                return false;
            }
        }

        return (features & NotWriteRootClassName.mask) == 0
                || object != this.rootObject;
    }

    /**
     * Checks if map type information should be written for the specified object, field class, and features.
     *
     * @param object the object to check
     * @param fieldClass the field class to check
     * @param features the features to consider
     * @return true if map type information should be written, false otherwise
     */
    public final boolean isWriteMapTypeInfo(Object object, Class fieldClass, long features) {
        if (object == null) {
            return false;
        }

        Class objectClass = object.getClass();
        if (objectClass == fieldClass) {
            return false;
        }

        features |= context.features;

        if ((features & WriteClassName.mask) == 0) {
            return false;
        }

        if ((features & NotWriteHashMapArrayListClassName.mask) != 0) {
            if (objectClass == HashMap.class) {
                return false;
            }
        }

        return (features & NotWriteRootClassName.mask) == 0 || object != this.rootObject;
    }

    /**
     * Checks if type information should be written for the specified object and features.
     *
     * @param object the object to check
     * @param features the features to consider
     * @return true if type information should be written, false otherwise
     */
    public final boolean isWriteTypeInfo(Object object, long features) {
        features |= context.features;

        if ((features & WriteClassName.mask) == 0) {
            return false;
        }

        if ((features & NotWriteHashMapArrayListClassName.mask) != 0) {
            if (object != null) {
                Class objectClass = object.getClass();
                if (objectClass == HashMap.class || objectClass == ArrayList.class) {
                    return false;
                }
            }
        }

        return (features & NotWriteRootClassName.mask) == 0
                || object != this.rootObject;
    }

    /**
     * Gets the ObjectWriter for the specified object class.
     * This method retrieves an ObjectWriter instance that can serialize objects of the specified class.
     *
     * @param objectClass the class of objects to be serialized
     * @return the ObjectWriter for the specified class
     */
    public final ObjectWriter getObjectWriter(Class objectClass) {
        boolean fieldBased = (context.features & MASK_FIELD_BASED) != 0;
        return context.provider.getObjectWriter(objectClass, objectClass, fieldBased);
    }

    /**
     * Gets the ObjectWriter for the specified object class with a specific format.
     * This method retrieves an ObjectWriter instance that can serialize objects of the specified class
     * using the provided format string.
     *
     * @param objectClass the class of objects to be serialized
     * @param format the format string to use for serialization
     * @return the ObjectWriter for the specified class and format
     */
    public final ObjectWriter getObjectWriter(Class objectClass, String format) {
        boolean fieldBased = (context.features & FieldBased.mask) != 0;
        return context.provider.getObjectWriter(objectClass, objectClass, format, fieldBased);
    }

    /**
     * Gets the ObjectWriter for the specified object type and class.
     * This method retrieves an ObjectWriter instance that can serialize objects of the specified type and class.
     *
     * @param objectType the type of objects to be serialized
     * @param objectClass the class of objects to be serialized
     * @return the ObjectWriter for the specified type and class
     */
    public final ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        boolean fieldBased = (context.features & FieldBased.mask) != 0;
        return context.provider.getObjectWriter(objectType, objectClass, fieldBased);
    }

    /**
     * Creates a new JSONWriter with default configuration.
     * The writer will output to an internal buffer and can be converted to a string using toString().
     *
     * <p>Example usage:
     * <pre>
     * try (JSONWriter writer = JSONWriter.of()) {
     *     writer.writeAny(object);
     *     String json = writer.toString();
     * }
     * </pre>
     *
     *
     * @return a new JSONWriter instance
     */
    public static JSONWriter of() {
        JSONWriter.Context writeContext = new JSONWriter.Context(defaultObjectWriterProvider);
        JSONWriter jsonWriter;
        if (JVM_VERSION == 8) {
            if (FIELD_STRING_VALUE != null && !ANDROID && !OPENJ9) {
                jsonWriter = new JSONWriterUTF16JDK8UF(writeContext);
            } else {
                jsonWriter = new JSONWriterUTF16JDK8(writeContext);
            }
        } else if ((defaultWriterFeatures & OptimizedForAscii.mask) != 0) {
            jsonWriter = ofUTF8(writeContext);
        } else {
            if (FIELD_STRING_VALUE != null && STRING_CODER != null && STRING_VALUE != null) {
                jsonWriter = new JSONWriterUTF16JDK9UF(writeContext);
            } else {
                jsonWriter = new JSONWriterUTF16(writeContext);
            }
        }
        return jsonWriter;
    }

    /**
     * Creates a new JSONWriter with the specified object writer provider and features.
     *
     * @param provider the object writer provider to use
     * @param features the features to enable
     * @return a new JSONWriter instance
     */
    public static JSONWriter of(ObjectWriterProvider provider, Feature... features) {
        Context context = new Context(provider);
        context.config(features);
        return of(context);
    }

    /**
     * Creates a new JSONWriter with the specified context.
     *
     * @param context the context to use
     * @return a new JSONWriter instance
     */
    public static JSONWriter of(Context context) {
        if (context == null) {
            context = createWriteContext();
        }

        JSONWriter jsonWriter;
        if (JVM_VERSION == 8) {
            if (FIELD_STRING_VALUE != null && !ANDROID && !OPENJ9) {
                jsonWriter = new JSONWriterUTF16JDK8UF(context);
            } else {
                jsonWriter = new JSONWriterUTF16JDK8(context);
            }
        } else if ((context.features & OptimizedForAscii.mask) != 0) {
            jsonWriter = new JSONWriterUTF8(context);
        } else {
            if (FIELD_STRING_VALUE != null && STRING_CODER != null && STRING_VALUE != null) {
                jsonWriter = new JSONWriterUTF16JDK9UF(context);
            } else {
                jsonWriter = new JSONWriterUTF16(context);
            }
        }

        return jsonWriter;
    }

    /**
     * Creates a new JSONWriter with the specified features.
     *
     * @param features the features to enable
     * @return a new JSONWriter instance
     */
    public static JSONWriter of(Feature... features) {
        Context writeContext = createWriteContext(features);
        JSONWriter jsonWriter;
        if (JVM_VERSION == 8) {
            if (FIELD_STRING_VALUE != null && !ANDROID && !OPENJ9) {
                jsonWriter = new JSONWriterUTF16JDK8UF(writeContext);
            } else {
                jsonWriter = new JSONWriterUTF16JDK8(writeContext);
            }
        } else if ((writeContext.features & OptimizedForAscii.mask) != 0) {
            jsonWriter = ofUTF8(writeContext);
        } else {
            if (FIELD_STRING_VALUE != null && STRING_CODER != null && STRING_VALUE != null) {
                jsonWriter = new JSONWriterUTF16JDK9UF(writeContext);
            } else {
                jsonWriter = new JSONWriterUTF16(writeContext);
            }
        }

        return jsonWriter;
    }

    /**
     * Creates a new JSONWriter instance using UTF-16 encoding with the specified features.
     *
     * @param features the features to enable for the new JSONWriter
     * @return a new JSONWriter instance using UTF-16 encoding
     */
    public static JSONWriterUTF16 ofUTF16(Feature... features) {
        Context writeContext = createWriteContext(features);
        JSONWriterUTF16 jsonWriter;
        if (JVM_VERSION == 8) {
            if (FIELD_STRING_VALUE != null && !ANDROID && !OPENJ9) {
                jsonWriter = new JSONWriterUTF16JDK8UF(writeContext);
            } else {
                jsonWriter = new JSONWriterUTF16JDK8(writeContext);
            }
        } else {
            if (FIELD_STRING_VALUE != null && STRING_CODER != null && STRING_VALUE != null) {
                jsonWriter = new JSONWriterUTF16JDK9UF(writeContext);
            } else {
                jsonWriter = new JSONWriterUTF16(writeContext);
            }
        }

        return jsonWriter;
    }

    public static JSONWriterUTF16 ofUTF16(Context context) {
        if (context == null) {
            context = createWriteContext();
        }

        JSONWriterUTF16 jsonWriter;
        if (JVM_VERSION == 8) {
            if (FIELD_STRING_VALUE != null && !ANDROID && !OPENJ9) {
                jsonWriter = new JSONWriterUTF16JDK8UF(context);
            } else {
                jsonWriter = new JSONWriterUTF16JDK8(context);
            }
        } else {
            if (FIELD_STRING_VALUE != null && STRING_CODER != null && STRING_VALUE != null) {
                jsonWriter = new JSONWriterUTF16JDK9UF(context);
            } else {
                jsonWriter = new JSONWriterUTF16(context);
            }
        }

        return jsonWriter;
    }

    /**
     * Creates a new JSONWriter instance for JSONB (binary JSON) format with default context.
     *
     * @return a new JSONWriter instance for JSONB format
     */
    public static JSONWriterJSONB ofJSONB() {
        return new JSONWriterJSONB(
                new JSONWriter.Context(defaultObjectWriterProvider),
                null
        );
    }

    /**
     * Creates a new JSONWriter instance for JSONB (binary JSON) format with the specified context.
     *
     * @param context the context to use for the new JSONWriter
     * @return a new JSONWriter instance for JSONB format
     */
    public static JSONWriterJSONB ofJSONB(JSONWriter.Context context) {
        return new JSONWriterJSONB(context, null);
    }

    /**
     * Creates a new JSONWriter instance for JSONB (binary JSON) format with the specified context and symbol table.
     *
     * @param context the context to use for the new JSONWriter
     * @param symbolTable the symbol table to use for the new JSONWriter
     * @return a new JSONWriter instance for JSONB format
     */
    public static JSONWriterJSONB ofJSONB(JSONWriter.Context context, SymbolTable symbolTable) {
        return new JSONWriterJSONB(context, symbolTable);
    }

    /**
     * Creates a new JSONWriter instance for JSONB (binary JSON) format with the specified features.
     *
     * @param features the features to enable for the new JSONWriter
     * @return a new JSONWriter instance for JSONB format
     */
    public static JSONWriterJSONB ofJSONB(Feature... features) {
        return new JSONWriterJSONB(
                new JSONWriter.Context(defaultObjectWriterProvider, features),
                null
        );
    }

    /**
     * Creates a new JSONWriter instance for JSONB (binary JSON) format with the specified symbol table.
     *
     * @param symbolTable the symbol table to use for the new JSONWriter
     * @return a new JSONWriter instance for JSONB format
     */
    public static JSONWriterJSONB ofJSONB(SymbolTable symbolTable) {
        return new JSONWriterJSONB(
                new JSONWriter.Context(defaultObjectWriterProvider),
                symbolTable
        );
    }

    /**
     * Creates a new JSONWriter instance with pretty formatting enabled.
     *
     * @return a new JSONWriter instance with pretty formatting
     */
    public static JSONWriter ofPretty() {
        return of(PrettyFormat);
    }

    /**
     * Enables pretty formatting on an existing JSONWriter instance.
     *
     * @param writer the JSONWriter instance to enable pretty formatting on
     * @return the same JSONWriter instance with pretty formatting enabled
     */
    public static JSONWriter ofPretty(JSONWriter writer) {
        if (writer.pretty == PRETTY_NON) {
            writer.pretty = PRETTY_TAB;
            writer.context.features |= PrettyFormat.mask;
        }
        return writer;
    }

    /**
     * Creates a new JSONWriter instance using UTF-8 encoding with default context.
     *
     * @return a new JSONWriter instance using UTF-8 encoding
     */
    public static JSONWriterUTF8 ofUTF8() {
        return ofUTF8(
                createWriteContext()
        );
    }

    /**
     * Creates a new JSONWriter instance using UTF-8 encoding with the specified context.
     *
     * @param context the context to use for the new JSONWriter
     * @return a new JSONWriter instance using UTF-8 encoding
     */
    public static JSONWriterUTF8 ofUTF8(JSONWriter.Context context) {
        return new JSONWriterUTF8(context);
    }

    /**
     * Creates a new JSONWriter instance using UTF-8 encoding with the specified features.
     *
     * @param features the features to enable for the new JSONWriter
     * @return a new JSONWriter instance using UTF-8 encoding
     */
    public static JSONWriterUTF8 ofUTF8(Feature... features) {
        return ofUTF8(
                createWriteContext(features)
        );
    }

    /**
     * Writes a byte array as either Base64-encoded string or as an array of integers,
     * depending on the WriteByteArrayAsBase64 feature.
     *
     * @param bytes the byte array to write
     */
    public void writeBinary(byte[] bytes) {
        if (bytes == null) {
            writeArrayNull();
            return;
        }

        if ((context.features & WriteByteArrayAsBase64.mask) != 0) {
            writeBase64(bytes);
            return;
        }

        startArray();
        for (int i = 0; i < bytes.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeInt32(bytes[i]);
        }
        endArray();
    }

    /**
     * Writes a byte array as Base64-encoded string.
     *
     * @param bytes the byte array to encode and write
     */
    public abstract void writeBase64(byte[] bytes);

    /**
     * Writes a byte array as hexadecimal string.
     *
     * @param bytes the byte array to encode and write
     */
    public abstract void writeHex(byte[] bytes);

    /**
     * Writes a character to the output.
     *
     * @param ch the character to write
     */
    protected abstract void write0(char ch);

    /**
     * Writes a raw string without any escaping or formatting.
     *
     * @param str the string to write
     */
    public abstract void writeRaw(String str);

    /**
     * Writes raw bytes without any escaping or formatting.
     *
     * @param bytes the bytes to write
     */
    public abstract void writeRaw(byte[] bytes);

    /**
     * Writes a raw byte without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param b the byte to write
     * @throws JSONException if the operation is not supported by this implementation
     */
    public void writeRaw(byte b) {
        throw new JSONException("UnsupportedOperation");
    }

    /**
     * Writes raw bytes representing a field name without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param bytes the bytes to write
     * @param offset the offset in the byte array
     * @param len the number of bytes to write
     * @throws JSONException if the operation is not supported by this implementation
     */
    public void writeNameRaw(byte[] bytes, int offset, int len) {
        throw new JSONException("UnsupportedOperation");
    }

    /**
     * Writes raw characters without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param chars the character array to write
     */
    public final void writeRaw(char[] chars) {
        writeRaw(chars, 0, chars.length);
    }

    /**
     * Writes raw characters without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param chars the character array to write
     * @param off the offset in the character array
     * @param charslen the number of characters to write
     * @throws JSONException if the operation is not supported by this implementation
     */
    public void writeRaw(char[] chars, int off, int charslen) {
        throw new JSONException("UnsupportedOperation");
    }

    /**
     * Writes a character without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param ch the character to write
     */
    public abstract void writeChar(char ch);

    /**
     * Writes a raw character without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param ch the character to write
     */
    public abstract void writeRaw(char ch);

    /**
     * Writes two raw characters without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param c0 the first character to write
     * @param c1 the second character to write
     * @throws JSONException if the operation is not supported by this implementation
     */
    public void writeRaw(char c0, char c1) {
        throw new JSONException("UnsupportedOperation");
    }

    /**
     * Writes raw bytes representing a field name without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param bytes the bytes to write
     */
    public abstract void writeNameRaw(byte[] bytes);

    /**
     * Writes a 2-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name the 2-character field name as a long value
     */
    public abstract void writeName2Raw(long name);

    /**
     * Writes a 3-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name the 3-character field name as a long value
     */
    public abstract void writeName3Raw(long name);

    /**
     * Writes a 4-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name the 4-character field name as a long value
     */
    public abstract void writeName4Raw(long name);

    /**
     * Writes a 5-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name the 5-character field name as a long value
     */
    public abstract void writeName5Raw(long name);

    /**
     * Writes a 6-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name the 6-character field name as a long value
     */
    public abstract void writeName6Raw(long name);

    /**
     * Writes a 7-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name the 7-character field name as a long value
     */
    public abstract void writeName7Raw(long name);

    /**
     * Writes an 8-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name0 the 8-character field name as a long value
     */
    public abstract void writeName8Raw(long name0);

    /**
     * Writes a 9-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name0 the first 8 characters of the field name as a long value
     * @param name1 the 9th character of the field name as an integer value
     */
    public abstract void writeName9Raw(long name0, int name1);

    /**
     * Writes a 10-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name0 the first 8 characters of the field name as a long value
     * @param name1 the last 2 characters of the field name as a long value
     */
    public abstract void writeName10Raw(long name0, long name1);

    /**
     * Writes an 11-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name0 the first 8 characters of the field name as a long value
     * @param name2 the last 3 characters of the field name as a long value
     */
    public abstract void writeName11Raw(long name0, long name2);

    /**
     * Writes a 12-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name0 the first 8 characters of the field name as a long value
     * @param name2 the last 4 characters of the field name as a long value
     */
    public abstract void writeName12Raw(long name0, long name2);

    /**
     * Writes a 13-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name0 the first 8 characters of the field name as a long value
     * @param name2 the last 5 characters of the field name as a long value
     */
    public abstract void writeName13Raw(long name0, long name2);

    /**
     * Writes a 14-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name0 the first 8 characters of the field name as a long value
     * @param name2 the last 6 characters of the field name as a long value
     */
    public abstract void writeName14Raw(long name0, long name2);

    /**
     * Writes a 15-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name0 the first 8 characters of the field name as a long value
     * @param name2 the last 7 characters of the field name as a long value
     */
    public abstract void writeName15Raw(long name0, long name2);

    /**
     * Writes a 16-character field name as raw bytes without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name0 the first 8 characters of the field name as a long value
     * @param name2 the last 8 characters of the field name as a long value
     */
    public abstract void writeName16Raw(long name0, long name2);

    /**
     * Writes a symbol as a JSON value.
     * This method is used for writing symbol values, which are typically used for identifiers or enumerated values.
     *
     * @param symbol the symbol to write
     * @throws JSONException if the operation is not supported by this implementation
     */
    public void writeSymbol(int symbol) {
        throw new JSONException("UnsupportedOperation");
    }

    /**
     * Writes raw bytes representing a field name without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param name the bytes representing the field name
     * @param nameHash the hash of the field name
     * @throws JSONException if the operation is not supported by this implementation
     */
    public void writeNameRaw(byte[] name, long nameHash) {
        throw new JSONException("UnsupportedOperation");
    }

    protected static boolean isWriteAsString(long value, long features) {
        return (features & (MASK_WRITE_NON_STRING_VALUE_AS_STRING | MASK_WRITE_LONG_AS_STRING)) != 0
                || ((features & MASK_BROWSER_COMPATIBLE) != 0 && !isJavaScriptSupport(value));
    }

    protected static boolean isWriteAsString(BigInteger value, long features) {
        return (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0
                || ((features & MASK_BROWSER_COMPATIBLE) != 0 && !isJavaScriptSupport(value));
    }

    protected static boolean isWriteAsString(BigDecimal value, long features) {
        return (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0
                || ((features & MASK_BROWSER_COMPATIBLE) != 0 && !isJavaScriptSupport(value));
    }

    /**
     * Writes raw characters representing a field name without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param chars the character array to write
     */
    public abstract void writeNameRaw(char[] chars);

    /**
     * Writes raw characters representing a field name without any escaping or formatting.
     * This method is used for low-level output operations where no JSON
     * formatting or escaping should be applied.
     *
     * @param bytes the character array to write
     * @param offset the offset in the character array
     * @param len the number of characters to write
     */
    public abstract void writeNameRaw(char[] bytes, int offset, int len);

    public void writeName(String name) {
        if (startObject) {
            startObject = false;
        } else {
            writeComma();
        }

        boolean unquote = (context.features & UnquoteFieldName.mask) != 0;
        if (unquote && (name.indexOf(quote) >= 0 || name.indexOf('\\') >= 0)) {
            unquote = false;
        }

        if (unquote) {
            writeRaw(name);
            return;
        }

        writeString(name);
    }

    /**
     * Writes a field name and its value as a key-value pair in a JSON object.
     * This method writes the field name followed by a colon and then the value.
     *
     * @param name the field name
     * @param value the field value
     */
    public final void writeNameValue(String name, Object value) {
        writeName(name);
        writeColon();
        writeAny(value);
    }

    /**
     * Writes a field name as a long value in a JSON object.
     * This method writes the field name as a numeric value.
     *
     * @param name the field name as a long value
     */
    public final void writeName(long name) {
        if (startObject) {
            startObject = false;
        } else {
            writeComma();
        }

        writeInt64(name);
    }

    /**
     * Writes a field name as an integer value in a JSON object.
     * This method writes the field name as a numeric value.
     *
     * @param name the field name as an integer value
     */
    public final void writeName(int name) {
        if (startObject) {
            startObject = false;
        } else {
            writeComma();
        }

        writeInt32(name);
    }

    /**
     * Writes a field name of any type in a JSON object.
     * This method writes the field name as a JSON value of any type.
     *
     * @param name the field name of any type
     */
    public void writeNameAny(Object name) {
        if (startObject) {
            startObject = false;
        } else {
            writeComma();
        }

        writeAny(name);
    }

    /**
     * Starts writing a JSON object.
     * This method writes the opening brace '{' and prepares the writer
     * for writing key-value pairs.
     */
    public abstract void startObject();

    /**
     * Ends writing a JSON object.
     * This method writes the closing brace '}' and completes the current object.
     */
    public abstract void endObject();

    /**
     * Starts writing a JSON array.
     * This method writes the opening bracket '[' and prepares the writer
     * for writing array elements.
     */
    public abstract void startArray();

    /**
     * Starts writing a JSON array with a specified initial capacity.
     * This method writes the opening bracket '[' and prepares the writer
     * for writing array elements. The size parameter is used for optimization
     * in some implementations.
     *
     * @param size the expected number of elements in the array
     * @throws JSONException if the operation is not supported by this implementation
     */
    public void startArray(int size) {
        throw new JSONException("UnsupportedOperation");
    }

    /**
     * Starts writing a JSON array with zero expected elements.
     * This is a convenience method equivalent to calling startArray(0).
     */
    public void startArray0() {
        startArray(0);
    }

    /**
     * Starts writing a JSON array with one expected element.
     * This is a convenience method equivalent to calling startArray(1).
     */
    public void startArray1() {
        startArray(1);
    }

    /**
     * Starts writing a JSON array with two expected elements.
     * This is a convenience method equivalent to calling startArray(2).
     */
    public void startArray2() {
        startArray(2);
    }

    /**
     * Starts writing a JSON array with three expected elements.
     * This is a convenience method equivalent to calling startArray(3).
     */
    public void startArray3() {
        startArray(3);
    }

    /**
     * Starts writing a JSON array with four expected elements.
     * This is a convenience method equivalent to calling startArray(4).
     */
    public void startArray4() {
        startArray(4);
    }

    /**
     * Starts writing a JSON array with five expected elements.
     * This is a convenience method equivalent to calling startArray(5).
     */
    public void startArray5() {
        startArray(5);
    }

    /**
     * Starts writing a JSON array with six expected elements.
     * This is a convenience method equivalent to calling startArray(6).
     */
    public void startArray6() {
        startArray(6);
    }

    /**
     * Starts writing a JSON array with seven expected elements.
     * This is a convenience method equivalent to calling startArray(7).
     */
    public void startArray7() {
        startArray(7);
    }

    /**
     * Starts writing a JSON array with eight expected elements.
     * This is a convenience method equivalent to calling startArray(8).
     */
    public void startArray8() {
        startArray(8);
    }

    /**
     * Starts writing a JSON array with nine expected elements.
     * This is a convenience method equivalent to calling startArray(9).
     */
    public void startArray9() {
        startArray(9);
    }

    /**
     * Starts writing a JSON array with ten expected elements.
     * This is a convenience method equivalent to calling startArray(10).
     */
    public void startArray10() {
        startArray(10);
    }

    /**
     * Starts writing a JSON array with eleven expected elements.
     * This is a convenience method equivalent to calling startArray(11).
     */
    public void startArray11() {
        startArray(11);
    }

    /**
     * Starts writing a JSON array with twelve expected elements.
     * This is a convenience method equivalent to calling startArray(12).
     */
    public void startArray12() {
        startArray(12);
    }

    /**
     * Starts writing a JSON array with thirteen expected elements.
     * This is a convenience method equivalent to calling startArray(13).
     */
    public void startArray13() {
        startArray(13);
    }

    /**
     * Starts writing a JSON array with fourteen expected elements.
     * This is a convenience method equivalent to calling startArray(14).
     */
    public void startArray14() {
        startArray(14);
    }

    /**
     * Starts writing a JSON array with fifteen expected elements.
     * This is a convenience method equivalent to calling startArray(15).
     */
    public void startArray15() {
        startArray(15);
    }

    /**
     * Starts writing a JSON array with the specified array and size.
     * This method is used for optimized array serialization when the array
     * and its expected size are known in advance.
     *
     * @param array the array to serialize
     * @param size the expected number of elements in the array
     * @throws JSONException if the operation is not supported by this implementation
     */
    public void startArray(Object array, int size) {
        throw new JSONException("UnsupportedOperation");
    }

    /**
     * Ends the current JSON array.
     */
    public abstract void endArray();

    /**
     * Writes a comma separator.
     */
    public abstract void writeComma();

    /**
     * Writes a colon separator.
     */
    public abstract void writeColon();

    /**
     * Writes a short array as integers.
     * Each element in the array is written as a separate integer value
     * in a JSON array.
     *
     * @param value the short array to write, can be null
     */
    public void writeInt16(short[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeInt16(value[i]);
        }
        endArray();
    }

    /**
     * Writes a byte value as an integer.
     *
     * @param value the byte value to write
     */
    public abstract void writeInt8(byte value);

    /**
     * Writes a byte array as integers.
     *
     * @param value the byte array to write
     */
    public abstract void writeInt8(byte[] value);

    /**
     * Writes a short value as an integer.
     *
     * @param value the short value to write
     */
    public abstract void writeInt16(short value);

    /**
     * Writes an int array as integers.
     *
     * @param value the int array to write
     */
    public abstract void writeInt32(int[] value);

    /**
     * Writes an int value.
     *
     * @param value the int value to write
     */
    public abstract void writeInt32(int value);

    /**
     * Writes an Integer object.
     *
     * @param i the Integer object to write
     */
    public abstract void writeInt32(Integer i);

    /**
     * Writes an int value with the specified decimal format.
     * If the format is null or JSONB mode is enabled, this method delegates to writeInt32(int).
     * @param value the int value to write
     * @param format the decimal format to use, or null to use default formatting
     */
    public final void writeInt32(int value, DecimalFormat format) {
        if (format == null || jsonb) {
            writeInt32(value);
            return;
        }

        writeString(format.format(value));
    }

    /**
     * Writes an int value with the specified string format.
     * If the format is null or JSONB mode is enabled, this method delegates to writeInt32(int).
     * @param value the int value to write
     * @param format the string format to use, or null to use default formatting
     */
    public final void writeInt32(int value, String format) {
        if (format == null || jsonb) {
            writeInt32(value);
            return;
        }

        writeString(String.format(format, value));
    }

    /**
     * Writes a long value.
     * @param i the long value to write
     */
    public abstract void writeInt64(long i);

    /**
     * Writes a Long object.
     * @param i the Long object to write
     */
    public abstract void writeInt64(Long i);

    /**
     * Writes a timestamp value as a long integer.
     * This is typically used for writing millisecond timestamps.
     * This method delegates to writeInt64(long).
     * @param i the timestamp value to write as milliseconds
     */
    public void writeMillis(long i) {
        writeInt64(i);
    }

    /**
     * Writes a long array as integers.
     * @param value the long array to write
     */
    public abstract void writeInt64(long[] value);

    /**
     * Writes a list of Long values as integers.
     * @param values the list of Long values to write
     */
    public abstract void writeListInt64(List<Long> values);

    /**
     * Writes a list of Integer values as integers.
     * @param values the list of Integer values to write
     */
    public abstract void writeListInt32(List<Integer> values);

    /**
     * Writes a float value.
     * @param value the float value to write
     */
    public abstract void writeFloat(float value);

    /**
     * Writes a float value with the specified decimal format.
     * If the format is null or JSONB mode is enabled, this method delegates to writeFloat(float).
     * NaN and infinite values are written as null.
     *
     * @param value the float value to write
     * @param format the decimal format to use, or null to use default formatting
     */
    public final void writeFloat(float value, DecimalFormat format) {
        if (format == null || jsonb) {
            writeFloat(value);
            return;
        }
        if (!Float.isFinite(value)) {
            if ((context.features & WriteFloatSpecialAsString.mask) != 0) {
                writeFloat(value);
            } else {
                writeNull();
            }
            return;
        }

        String str = format.format(value);
        writeRaw(str);
    }

    public abstract void writeFloat(float[] value);

    /**
     * Writes a float array with the specified decimal format.
     * If the format is null or JSONB mode is enabled, this method delegates to writeFloat(float[]).
     * NaN and infinite values are written as null.
     *
     * @param value the float array to write, can be null
     * @param format the decimal format to use, or null to use default formatting
     */
    public final void writeFloat(float[] value, DecimalFormat format) {
        if (format == null || jsonb) {
            writeFloat(value);
            return;
        }

        if (value == null) {
            writeNull();
            return;
        }

        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            if (!Float.isFinite(value[i])) {
                if ((context.features & WriteFloatSpecialAsString.mask) != 0) {
                    writeFloat(value[i]);
                } else {
                    writeNull();
                }
                continue;
            }
            String str = format.format(value[i]);
            writeRaw(str);
        }
        endArray();
    }

    /**
     * Writes a Float object.
     * If the value is null, a null value is written according to the NullAsDefaultValue feature.
     * Otherwise, the value is written as a double.
     *
     * @param value the Float object to write, can be null
     */
    public final void writeFloat(Float value) {
        if (value == null) {
            writeNumberNull();
        } else {
            writeDouble(value);
        }
    }

    /**
     * Writes a double value.
     * @param value the double value to write
     */
    public abstract void writeDouble(double value);

    /**
     * Writes a double value with the specified decimal format.
     * If the format is null or JSONB mode is enabled, this method delegates to writeDouble(double).
     * NaN and infinite values are written as null.
     *
     * @param value the double value to write
     * @param format the decimal format to use, or null to use default formatting
     */
    public final void writeDouble(double value, DecimalFormat format) {
        if (format == null || jsonb) {
            writeDouble(value);
            return;
        }
        if (!Double.isFinite(value)) {
            if ((context.features & WriteFloatSpecialAsString.mask) != 0) {
                writeDouble(value);
            } else {
                writeNull();
            }
            return;
        }

        String str = format.format(value);
        writeRaw(str);
    }

    /**
     * Writes a double array with two elements.
     * This is a convenience method for writing arrays that contain exactly two double values.
     *
     * @param value0 the first double value to write
     * @param value1 the second double value to write
     */
    public void writeDoubleArray(double value0, double value1) {
        startArray();
        writeDouble(value0);
        writeComma();
        writeDouble(value1);
        endArray();
    }

    /**
     * Writes a double array as floating-point numbers with the specified decimal format.
     * If the format is null or JSONB mode is enabled, this method delegates to writeDouble(double[]).
     *
     * @param value the double array to write, can be null
     * @param format the decimal format to use, or null to use default formatting
     */
    public final void writeDouble(double[] value, DecimalFormat format) {
        if (format == null || jsonb) {
            writeDouble(value);
            return;
        }

        if (value == null) {
            writeNull();
            return;
        }

        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            if (!Double.isFinite(value[i])) {
                if ((context.features & WriteFloatSpecialAsString.mask) != 0) {
                    writeDouble(value[i]);
                } else {
                    writeNull();
                }
                continue;
            }
            String str = format.format(value[i]);
            writeRaw(str);
        }
        endArray();
    }

    /**
     * Writes a double array as floating-point numbers.
     *
     * @param value the double array to write
     */
    public abstract void writeDouble(double[] value);

    /**
     * Writes a boolean value.
     *
     * @param value the boolean value to write
     */
    public abstract void writeBool(boolean value);

    /**
     * Writes a boolean array as boolean values.
     * Each element in the array is written as a separate boolean value
     * in a JSON array.
     *
     * @param value the boolean array to write, can be null
     */
    public void writeBool(boolean[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }

        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeBool(value[i]);
        }
        endArray();
    }

    /**
     * Writes a null value.
     */
    public abstract void writeNull();

    /**
     * Writes a null object value.
     * The serialization format depends on the context features:
     * <ul>
     *   <li>If {@link Feature#NullAsDefaultValue} is enabled, a default empty object or null character is written</li>
     *   <li>Otherwise, a null value is written</li>
     * </ul>
     *
     * @param fieldClass the class of the field being written
     */
    public void writeObjectNull(Class<?> fieldClass) {
        if ((this.context.features & (MASK_NULL_AS_DEFAULT_VALUE)) != 0) {
            if (fieldClass == Character.class) {
                writeString("\u0000");
            } else {
                writeRaw('{', '}');
            }
        } else {
            writeNull();
        }
    }

    /**
     * Writes a null string value.
     * The serialization format depends on the context features:
     * <ul>
     *   <li>If {@link Feature#NullAsDefaultValue} or {@link Feature#WriteNullStringAsEmpty} is enabled, an empty string is written</li>
     *   <li>Otherwise, a null value is written</li>
     * </ul>
     */
    public void writeStringNull() {
        String raw;
        long features = this.context.features;
        if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_STRING_AS_EMPTY)) != 0) {
            raw = (features & MASK_USE_SINGLE_QUOTES) != 0 ? "''" : "\"\"";
        } else {
            raw = "null";
        }
        writeRaw(raw);
    }

    /**
     * Writes a null array value using the current context features.
     * The serialization format depends on the context features:
     * <ul>
     *   <li>If {@link Feature#NullAsDefaultValue} or {@link Feature#WriteNullListAsEmpty} is enabled, an empty array is written</li>
     *   <li>Otherwise, a null value is written</li>
     * </ul>
     */
    public void writeArrayNull() {
        writeArrayNull(this.context.features);
    }

    /**
     * Writes a null array value using the specified features.
     * The serialization format depends on the provided features:
     * <ul>
     *   <li>If {@link Feature#NullAsDefaultValue} or {@link Feature#WriteNullListAsEmpty} is enabled, an empty array is written</li>
     *   <li>Otherwise, a null value is written</li>
     * </ul>
     *
     * @param features the features to use for serialization
     */
    public void writeArrayNull(long features) {
        String raw;
        if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_LIST_AS_EMPTY)) != 0) {
            raw = "[]";
        } else {
            raw = "null";
        }
        writeRaw(raw);
    }

    /**
     * Writes a null number value using the current context features.
     * The serialization format depends on the context features:
     * <ul>
     *   <li>If {@link Feature#NullAsDefaultValue} or {@link Feature#WriteNullNumberAsZero} is enabled, zero is written</li>
     *   <li>Otherwise, a null value is written</li>
     * </ul>
     */
    public final void writeNumberNull() {
        writeNumberNull(this.context.features);
    }

    /**
     * Writes a null number value using the specified features.
     * The serialization format depends on the provided features:
     * <ul>
     *   <li>If {@link Feature#NullAsDefaultValue} or {@link Feature#WriteNullNumberAsZero} is enabled, zero is written</li>
     *   <li>Otherwise, a null value is written</li>
     * </ul>
     *
     * @param features the features to use for serialization
     */
    public final void writeNumberNull(long features) {
        if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) != 0) {
            writeInt32(0);
        } else {
            writeNull();
        }
    }

    /**
     * Writes a null decimal value using the current context features.
     * The serialization format depends on the context features:
     * <ul>
     *   <li>If {@link Feature#NullAsDefaultValue} is enabled, 0.0 is written</li>
     *   <li>If {@link Feature#WriteNullNumberAsZero} is enabled, zero is written</li>
     *   <li>Otherwise, a null value is written</li>
     * </ul>
     */
    public final void writeDecimalNull() {
        writeDecimalNull(this.context.features);
    }

    /**
     * Writes a null decimal value using the specified features.
     * The serialization format depends on the provided features:
     * <ul>
     *   <li>If {@link Feature#NullAsDefaultValue} is enabled, 0.0 is written</li>
     *   <li>If {@link Feature#WriteNullNumberAsZero} is enabled, zero is written</li>
     *   <li>Otherwise, a null value is written</li>
     * </ul>
     *
     * @param features the features to use for serialization
     */
    public final void writeDecimalNull(long features) {
        if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) != 0) {
            writeDouble(0.0);
        } else {
            writeNull();
        }
    }

    /**
     * Writes a null long value using the current context features.
     * The serialization format depends on the context features:
     * <ul>
     *   <li>If {@link Feature#NullAsDefaultValue} or {@link Feature#WriteNullNumberAsZero} is enabled, zero is written</li>
     *   <li>Otherwise, a null value is written</li>
     * </ul>
     */
    public final void writeInt64Null() {
        if ((this.context.features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) != 0) {
            writeInt64(0);
        } else {
            writeNull();
        }
    }

    /**
     * Writes a null boolean value using the current context features.
     * The serialization format depends on the context features:
     * <ul>
     *   <li>If {@link Feature#NullAsDefaultValue} or {@link Feature#WriteNullBooleanAsFalse} is enabled, false is written</li>
     *   <li>Otherwise, a null value is written</li>
     * </ul>
     */
    public final void writeBooleanNull() {
        if ((this.context.features & (MASK_NULL_AS_DEFAULT_VALUE | WriteNullBooleanAsFalse.mask)) != 0) {
            writeBool(false);
        } else {
            writeNull();
        }
    }

    /**
     * Writes a BigDecimal value using default features and no specific format.
     * This method delegates to writeDecimal(BigDecimal, long, DecimalFormat) with features set to 0 and format set to null.
     * @param value the BigDecimal value to write, can be null
     */
    public final void writeDecimal(BigDecimal value) {
        writeDecimal(value, 0, null);
    }

    /**
     * Writes a BigDecimal value using the specified features and no specific format.
     * This method delegates to writeDecimal(BigDecimal, long, DecimalFormat) with format set to null.
     * @param value the BigDecimal value to write, can be null
     * @param features the features to use for serialization
     */
    public final void writeDecimal(BigDecimal value, long features) {
        writeDecimal(value, features, null);
    }

    /**
     * Writes a BigDecimal value using the specified features and decimal format.
     * @param value the BigDecimal value to write, can be null
     * @param features the features to use for serialization
     * @param format the decimal format to use, or null to use default formatting
     */
    public abstract void writeDecimal(BigDecimal value, long features, DecimalFormat format);

    /**
     * Writes an Enum value.
     * The serialization format depends on the context features:
     * <ul>
     *   <li>If {@link Feature#WriteEnumUsingToString} is enabled, the enum is written using its toString() method</li>
     *   <li>If {@link Feature#WriteEnumsUsingName} is enabled, the enum is written using its name()</li>
     *   <li>Otherwise, the enum is written as its ordinal value (integer)</li>
     * </ul>
     *
     * @param e the Enum value to write, can be null
     */
    public void writeEnum(Enum e) {
        if (e == null) {
            writeNull();
            return;
        }

        if ((context.features & WriteEnumUsingToString.mask) != 0) {
            writeString(e.toString());
        } else if ((context.features & WriteEnumsUsingName.mask) != 0) {
            writeString(e.name());
        } else {
            writeInt32(e.ordinal());
        }
    }

    /**
     * Writes a BigInteger value using default features.
     * This method delegates to writeBigInt(BigInteger, long) with features set to 0.
     * @param value the BigInteger value to write, can be null
     */
    public final void writeBigInt(BigInteger value) {
        writeBigInt(value, 0);
    }

    /**
     * Writes a BigInteger value using the specified features.
     * @param value the BigInteger value to write, can be null
     * @param features the features to use for serialization
     */
    public abstract void writeBigInt(BigInteger value, long features);

    /**
     * Writes a UUID value.
     * The UUID is typically serialized as a string in standard UUID format.
     *
     * @param value the UUID to write, can be null
     */
    public abstract void writeUUID(UUID value);

    /**
     * Checks if type name should be written for the given object and writes it if necessary.
     * This method is used when the WriteClassName feature is enabled to conditionally include
     * type information in the serialized JSON based on various criteria such as class type,
     * feature settings, and object relationships.
     *
     * @param object the object being serialized
     * @param fieldClass the expected field class type
     */
    public final void checkAndWriteTypeName(Object object, Class fieldClass) {
        long features = context.features;
        Class objectClass;
        if ((features & WriteClassName.mask) == 0
                || object == null
                || (objectClass = object.getClass()) == fieldClass
                || ((features & NotWriteHashMapArrayListClassName.mask) != 0 && (objectClass == HashMap.class || objectClass == ArrayList.class))
                || ((features & NotWriteRootClassName.mask) != 0 && object == this.rootObject)
        ) {
            return;
        }

        writeTypeName(TypeUtils.getTypeName(objectClass));
    }

    /**
     * Writes a type name for the current object.
     * This method is used when the WriteClassName feature is enabled to include
     * type information in the serialized JSON.
     *
     * @param typeName the type name to write
     * @throws JSONException if the operation is not supported by this implementation
     */
    public void writeTypeName(String typeName) {
        throw new JSONException("UnsupportedOperation");
    }

    /**
     * Writes a type name for the current object using byte array and hash.
     * This method is used when the WriteClassName feature is enabled to include
     * type information in the serialized JSON in a more efficient format.
     *
     * @param typeName the type name as byte array
     * @param typeNameHash the hash of the type name
     * @return true if the type name was written successfully, false otherwise
     * @throws JSONException if the operation is not supported by this implementation
     */
    public boolean writeTypeName(byte[] typeName, long typeNameHash) {
        throw new JSONException("UnsupportedOperation");
    }

    /**
     * Writes a string from a Reader.
     * This method reads characters from the provided Reader and writes them as a JSON string,
     * properly escaping any special characters as needed.
     *
     * @param reader the Reader to read characters from
     * @throws JSONException if an I/O error occurs while reading from the Reader
     */
    public final void writeString(Reader reader) {
        writeRaw(quote);

        try {
            char[] chars = new char[2048];
            for (; ; ) {
                int len = reader.read(chars, 0, chars.length);
                if (len < 0) {
                    break;
                }

                if (len > 0) {
                    writeString(chars, 0, len, false);
                }
            }
        } catch (Exception ex) {
            throw new JSONException("read string from reader error", ex);
        }

        writeRaw(quote);
    }

    /**
     * Writes a string value.
     * @param str the string to write, can be null
     */
    public abstract void writeString(String str);

    /**
     * Writes a boolean value as a string.
     *
     * @param value the boolean value to write
     * @since 2.0.49
     */
    public abstract void writeString(boolean value);

    /**
     * Writes a byte value as a string.
     *
     * @param value the byte value to write
     * @since 2.0.49
     */
    public abstract void writeString(byte value);

    /**
     * Writes a short value as a string.
     *
     * @param value the short value to write
     * @since 2.0.49
     */
    public abstract void writeString(short value);

    /**
     * Writes a boolean array as strings.
     *
     * @param value the boolean array to write
     * @since 2.0.49
     */
    public void writeString(boolean[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeString(value[i]);
        }
        endArray();
    }

    /**
     * Writes a byte array as strings.
     *
     * @param value the byte array to write
     * @since 2.0.49
     */
    public void writeString(byte[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeString(value[i]);
        }
        endArray();
    }

    /**
     * Writes a short array as strings.
     *
     * @param value the short array to write
     * @since 2.0.49
     */
    public void writeString(short[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeString(value[i]);
        }
        endArray();
    }

    /**
     * Writes an int array as strings.
     *
     * @param value the int array to write
     * @since 2.0.49
     */
    public void writeString(int[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeString(value[i]);
        }
        endArray();
    }

    /**
     * Writes a long array as strings.
     *
     * @param value the long array to write
     * @since 2.0.49
     */
    public void writeString(long[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeString(value[i]);
        }
        endArray();
    }

    /**
     * Writes a float array as strings.
     *
     * @param value the float array to write
     * @since 2.0.49
     */
    public void writeString(float[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeString(value[i]);
        }
        endArray();
    }

    /**
     * Writes a double array as strings.
     *
     * @param value the double array to write
     * @since 2.0.49
     */
    public void writeString(double[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeString(value[i]);
        }
        endArray();
    }

    /**
     * Writes an int value as a string.
     *
     * @param value the int value to write
     * @since 2.0.49
     */
    public abstract void writeString(int value);

    /**
     * Writes a float value as a string.
     *
     * @param value the float value to write
     * @since 2.0.49
     */
    public void writeString(float value) {
        writeString(Float.toString(value));
    }

    /**
     * Writes a double value as a string.
     *
     * @param value the double value to write
     * @since 2.0.49
     */
    public void writeString(double value) {
        writeString(Double.toString(value));
    }

    /**
     * Writes a long value as a string.
     *
     * @param value the long value to write
     * @since 2.0.49
     */
    public abstract void writeString(long value);

    /**
     * Writes a string from Latin-1 encoded bytes.
     *
     * @param value the Latin-1 encoded bytes to write
     */
    public abstract void writeStringLatin1(byte[] value);

    /**
     * Writes a string from UTF-16 encoded bytes.
     *
     * @param value the UTF-16 encoded bytes to write
     */
    public abstract void writeStringUTF16(byte[] value);

    /**
     * Writes a list of strings as a JSON array.
     * Each string in the list is written as a separate string value in the array.
     *
     * @param list the list of strings to write, can be null
     */
    public void writeString(List<String> list) {
        startArray();
        for (int i = 0, size = list.size(); i < size; i++) {
            if (i != 0) {
                writeComma();
            }

            String str = list.get(i);
            writeString(str);
        }
        endArray();
    }

    /**
     * Writes an array of strings as a JSON array.
     * Each string in the array is written as a separate string value in the array.
     *
     * @param strings the array of strings to write, can be null
     */
    public void writeString(String[] strings) {
        if (strings == null) {
            writeArrayNull();
            return;
        }

        startArray();
        for (int i = 0; i < strings.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeString(strings[i]);
        }
        endArray();
    }

    /**
     * Writes a symbol string.
     * Symbols are typically used for identifiers or enumerated values that may benefit
     * from optimized serialization.
     *
     * @param string the symbol string to write
     */
    public void writeSymbol(String string) {
        writeString(string);
    }

    /**
     * Writes a string from character array.
     *
     * @param chars the character array to write
     */
    public abstract void writeString(char[] chars);

    /**
     * Writes a string from character array with specified offset and length.
     *
     * @param chars the character array to write
     * @param off the offset in the array
     * @param len the number of characters to write
     */
    public abstract void writeString(char[] chars, int off, int len);

    /**
     * Writes a string from character array with specified offset and length.
     *
     * @param chars the character array to write
     * @param off the offset in the array
     * @param len the number of characters to write
     * @param quote whether to quote the string
     */
    public abstract void writeString(char[] chars, int off, int len, boolean quote);

    /**
     * Writes a LocalDate value.
     *
     * @param date the LocalDate to write
     */
    public abstract void writeLocalDate(LocalDate date);

    protected final boolean writeLocalDateWithFormat(LocalDate date) {
        Context context = this.context;
        if (context.dateFormatUnixTime || context.dateFormatMillis) {
            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.MIN);
            long millis = dateTime.atZone(context.getZoneId())
                    .toInstant()
                    .toEpochMilli();
            writeInt64(context.dateFormatMillis ? millis : millis / 1000);
            return true;
        }

        DateTimeFormatter formatter = context.getDateFormatter();
        if (formatter != null) {
            String str;
            if (context.isDateFormatHasHour()) {
                str = formatter.format(LocalDateTime.of(date, LocalTime.MIN));
            } else {
                str = formatter.format(date);
            }
            writeString(str);
            return true;
        }
        return false;
    }

    /**
     * Writes a LocalDateTime value.
     * @param dateTime the LocalDateTime to write
     */
    public abstract void writeLocalDateTime(LocalDateTime dateTime);

    /**
     * Writes a LocalTime value.
     * @param time the LocalTime to write
     */
    public abstract void writeLocalTime(LocalTime time);

    /**
     * Writes a ZonedDateTime value.
     * @param dateTime the ZonedDateTime to write
     */
    public abstract void writeZonedDateTime(ZonedDateTime dateTime);

    /**
     * Writes an OffsetDateTime value.
     * @param dateTime the OffsetDateTime to write
     */
    public abstract void writeOffsetDateTime(OffsetDateTime dateTime);

    /**
     * Writes an OffsetTime value.
     * @param dateTime the OffsetTime to write
     */
    public abstract void writeOffsetTime(OffsetTime dateTime);

    /**
     * Writes an Instant value as an ISO-8601 formatted string.
     * If the instant is null, a null value is written instead.
     *
     * @param instant the Instant to write, can be null
     */
    public void writeInstant(Instant instant) {
        if (instant == null) {
            writeNull();
            return;
        }

        String str = DateTimeFormatter.ISO_INSTANT.format(instant);
        writeRaw(this.quote);
        writeRaw(str);
        writeRaw(this.quote);
    }

    /**
     * Writes a date-time value in 14-character format (yyyyMMddHHmmss).
     *
     * @param year the year
     * @param month the month (1-12)
     * @param dayOfMonth the day of month (1-31)
     * @param hour the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     */
    public abstract void writeDateTime14(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second);

    /**
     * Writes a date-time value in 19-character format (yyyy-MM-dd HH:mm:ss).
     *
     * @param year the year
     * @param month the month (1-12)
     * @param dayOfMonth the day of month (1-31)
     * @param hour the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     */
    public abstract void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second);

    /**
     * Writes a date-time value in ISO8601 format.
     *
     * @param year the year
     * @param month the month (1-12)
     * @param dayOfMonth the day of month (1-31)
     * @param hour the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     * @param millis the millisecond (0-999)
     * @param offsetSeconds the timezone offset in seconds
     * @param timeZone whether to include timezone information
     */
    public abstract void writeDateTimeISO8601(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second,
            int millis,
            int offsetSeconds,
            boolean timeZone
    );

    /**
     * Writes a date in 8-character format (yyyyMMdd).
     *
     * @param year the year
     * @param month the month (1-12)
     * @param dayOfMonth the day of month (1-31)
     */
    public abstract void writeDateYYYMMDD8(int year, int month, int dayOfMonth);

    /**
     * Writes a date in 10-character format (yyyy-MM-dd).
     *
     * @param year the year
     * @param month the month (1-12)
     * @param dayOfMonth the day of month (1-31)
     */
    public abstract void writeDateYYYMMDD10(int year, int month, int dayOfMonth);

    /**
     * Writes a time in 8-character format (HH:mm:ss).
     *
     * @param hour the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     */
    public abstract void writeTimeHHMMSS8(int hour, int minute, int second);

    /**
     * Writes a list as a JSON array.
     *
     * @param array the list to write
     */
    public abstract void write(List array);

    /**
     * Writes a JSONObject as a JSON object.
     * This method delegates to the write(Map) method since JSONObject extends Map.
     *
     * @param map the JSONObject to write
     */
    public final void write(JSONObject map) {
        write((Map) map);
    }

    /**
     * Writes a Map as a JSON object.
     * Each entry in the map becomes a key-value pair in the JSON object.
     * Null values are handled according to the WriteMapNullValue feature.
     *
     * @param map the Map to write, can be null
     */
    public void write(Map<?, ?> map) {
        if (map == null) {
            this.writeNull();
            return;
        }

        if (map.isEmpty()) {
            writeRaw('{', '}');
            return;
        }

        if ((context.features & NONE_DIRECT_FEATURES) != 0) {
            ObjectWriter objectWriter = context.getObjectWriter(map.getClass());
            objectWriter.write(this, map, null, null, 0);
            return;
        }

        startObject();

        boolean first = true;
        for (Map.Entry entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null && (context.features & WriteMapNullValue.mask) == 0) {
                continue;
            }

            if (!first) {
                writeComma();
            }

            first = false;
            Object key = entry.getKey();
            if (key instanceof String) {
                writeString((String) key);
            } else {
                writeAny(key);
            }

            writeColon();

            if (value == null) {
                writeNull();
                continue;
            }

            Class<?> valueClass = value.getClass();
            if (valueClass == String.class) {
                writeString((String) value);
                continue;
            }

            if (valueClass == Integer.class) {
                writeInt32((Integer) value);
                continue;
            }

            if (valueClass == Long.class) {
                writeInt64((Long) value);
                continue;
            }

            if (valueClass == Boolean.class) {
                writeBool((Boolean) value);
                continue;
            }

            if (valueClass == BigDecimal.class) {
                writeDecimal((BigDecimal) value, 0, null);
                continue;
            }

            if (valueClass == JSONArray.class) {
                write((JSONArray) value);
                continue;
            }

            if (valueClass == JSONObject.class) {
                write((JSONObject) value);
                continue;
            }

            ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
            objectWriter.write(this, value, null, null, 0);
        }

        endObject();
    }

    /**
     * Writes any object using the appropriate writer based on its runtime type.
     * This method dynamically determines the correct serialization approach based
     * on the actual type of the object provided.
     *
     * @param value the object to write, can be null
     */
    public void writeAny(Object value) {
        if (value == null) {
            writeNull();
            return;
        }

        Class<?> valueClass = value.getClass();
        ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
        objectWriter.write(this, value, null, null, 0);
    }

    /**
     * Writes an object as if it were of the specified type.
     * This method is useful for writing objects with a specific type serialization,
     * even if the actual object is of a different type.
     *
     * @param value the object to write
     * @param type the type to serialize the object as
     * @since 2.0.43
     */
    public final void writeAs(Object value, Class type) {
        if (value == null) {
            writeNull();
            return;
        }

        ObjectWriter objectWriter = context.getObjectWriter(type);
        objectWriter.write(this, value, null, null, 0);
    }

    /**
     * Writes a reference to a previously serialized object.
     * This is used for handling circular references and avoiding infinite loops during serialization.
     *
     * @param path the JSON Pointer path to the referenced object
     */
    public abstract void writeReference(String path);

    /**
     * Closes this JSONWriter and releases any resources associated with it.
     * This method should be called when finished with the writer to ensure
     * proper cleanup of resources.
     *
     * @throws RuntimeException if an I/O error occurs
     */
    @Override
    public abstract void close();

    /**
     * Gets the current size of the output buffer.
     *
     * @return the size of the output buffer in bytes
     */
    public abstract int size();

    /**
     * Gets the content of the output buffer as a byte array.
     *
     * @return the content as a byte array
     */
    public abstract byte[] getBytes();

    /**
     * Gets the content of the output buffer as a byte array using the specified charset.
     *
     * @param charset the charset to use for encoding
     * @return the content as a byte array
     */
    public abstract byte[] getBytes(Charset charset);

    /**
     * Flushes the content of this JSONWriter to the specified Writer.
     * This method converts the current content to a string and writes it to the provided Writer,
     * then resets the internal buffer offset to zero.
     *
     * @param to the Writer to flush content to
     * @throws JSONException if an I/O error occurs while writing to the Writer
     */
    public void flushTo(java.io.Writer to) {
        try {
            String json = this.toString();
            to.write(json);
            off = 0;
        } catch (IOException e) {
            throw new JSONException("flushTo error", e);
        }
    }

    /**
     * Flushes the content of this JSONWriter to the specified OutputStream.
     * This method writes the current content directly to the provided OutputStream
     * without converting to a string first.
     *
     * @param to the OutputStream to flush content to
     * @return the number of bytes written
     * @throws IOException if an I/O error occurs while writing to the OutputStream
     */
    public abstract int flushTo(OutputStream to) throws IOException;

    /**
     * Flushes the content of this JSONWriter to the specified OutputStream using the specified charset.
     * This method writes the current content directly to the provided OutputStream
     * using the specified charset for encoding.
     *
     * @param out the OutputStream to flush content to
     * @param charset the charset to use for encoding
     * @return the number of bytes written
     * @throws IOException if an I/O error occurs while writing to the OutputStream
     */
    public abstract int flushTo(OutputStream out, Charset charset) throws IOException;

    /**
     * Context holds the configuration and state information for JSON writing operations.
     * It controls various aspects of the serialization process including formatting,
     * features, providers, filters, and other settings that affect how Java objects
     * are converted to JSON format.
     *
     * <p>The Context class is responsible for:</p>
     * <ul>
     *   <li>Managing writer features that control serialization behavior</li>
     *   <li>Handling date/time formatting and timezone settings</li>
     *   <li>Managing filters for customizing serialization output</li>
     *   <li>Storing serializer configuration such as max nesting level</li>
     *   <li>Providing object writer providers for type-specific serialization</li>
     * </ul>
     *
     * <p>Context instances can be created in several ways:</p>
     * <pre>
     * // Using default configuration
     * JSONWriter.Context context = new JSONWriter.Context();
     *
     * // With specific features enabled
     * JSONWriter.Context context = new JSONWriter.Context(
     *     JSONWriter.Feature.PrettyFormat,
     *     JSONWriter.Feature.WriteMapNullValue
     * );
     *
     * // With custom date format
     * JSONWriter.Context context = new JSONWriter.Context("yyyy-MM-dd HH:mm:ss");
     *
     * // With custom provider and features
     * ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
     * JSONWriter.Context context = new JSONWriter.Context(provider,
     *     JSONWriter.Feature.PrettyFormat
     * );
     * </pre>
     *
     * <p>Once created, a Context can be configured further:</p>
     * <pre>
     * context.setZoneId(ZoneId.of("UTC"));
     * context.setLocale(Locale.US);
     * context.setMaxLevel(1000);
     * context.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
     * </pre>
     *
     * <p>Context instances are typically used when creating JSONWriter instances:</p>
     * <pre>
     * JSONWriter.Context context = new JSONWriter.Context();
     * context.config(JSONWriter.Feature.PrettyFormat);
     *
     * try (JSONWriter writer = JSONWriter.of(context)) {
     *     writer.writeAny(object);
     *     String json = writer.toString();
     * }
     * </pre>
     *
     * <p>Note that Context instances are not thread-safe and should not be shared
     * between multiple concurrent writing operations. Each JSONWriter should have
     * its own Context instance or use the default context provided by factory methods.</p>
     *
     * @see JSONWriter
     * @see JSONWriter.Feature
     * @see ObjectWriterProvider
     * @since 2.0.0
     */
    public static final class Context {
        public final ObjectWriterProvider provider;
        DateTimeFormatter dateFormatter;
        String dateFormat;
        Locale locale;
        boolean dateFormatMillis;
        boolean dateFormatISO8601;
        boolean dateFormatUnixTime;
        boolean formatyyyyMMddhhmmss19;
        boolean formatHasDay;
        boolean formatHasHour;
        long features;
        ZoneId zoneId;
        int maxLevel;
        boolean hasFilter;
        PropertyPreFilter propertyPreFilter;
        PropertyFilter propertyFilter;
        NameFilter nameFilter;
        ValueFilter valueFilter;
        BeforeFilter beforeFilter;
        AfterFilter afterFilter;
        LabelFilter labelFilter;
        ContextValueFilter contextValueFilter;
        ContextNameFilter contextNameFilter;

        /**
         * Creates a new Context with the specified object writer provider.
         *
         * @param provider the object writer provider to use
         * @throws IllegalArgumentException if provider is null
         */
        public Context(ObjectWriterProvider provider) {
            if (provider == null) {
                throw new IllegalArgumentException("objectWriterProvider must not null");
            }

            this.features = defaultWriterFeatures;
            this.provider = provider;
            this.zoneId = defaultWriterZoneId;
            this.maxLevel = defaultMaxLevel;

            String format = defaultWriterFormat;
            if (format != null) {
                setDateFormat(format);
            }
        }

        /**
         * Creates a new Context with the specified features.
         *
         * @param features the features to enable
         */
        public Context(Feature... features) {
            this.features = defaultWriterFeatures;
            this.provider = getDefaultObjectWriterProvider();
            this.zoneId = defaultWriterZoneId;
            this.maxLevel = defaultMaxLevel;

            String format = defaultWriterFormat;
            if (format != null) {
                setDateFormat(format);
            }

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
        }

        /**
         * Creates a new Context with the specified date format and features.
         *
         * @param format the date format pattern to use
         * @param features the features to enable
         */
        public Context(String format, Feature... features) {
            this.features = defaultWriterFeatures;
            this.provider = getDefaultObjectWriterProvider();
            this.zoneId = defaultWriterZoneId;
            this.maxLevel = defaultMaxLevel;

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }

            if (format == null) {
                format = defaultWriterFormat;
            }
            if (format != null) {
                setDateFormat(format);
            }
        }

        /**
         * Creates a new Context with the specified object writer provider and features.
         *
         * @param provider the object writer provider to use
         * @param features the features to enable
         * @throws IllegalArgumentException if provider is null
         */
        public Context(ObjectWriterProvider provider, Feature... features) {
            if (provider == null) {
                throw new IllegalArgumentException("objectWriterProvider must not null");
            }

            this.features = defaultWriterFeatures;
            this.provider = provider;
            this.zoneId = defaultWriterZoneId;
            this.maxLevel = defaultMaxLevel;

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }

            String format = defaultWriterFormat;
            if (format != null) {
                setDateFormat(format);
            }
        }

        /**
         * Gets the features bitmask for this context.
         *
         * @return the features bitmask
         */
        public long getFeatures() {
            return features;
        }

        /**
         * Sets the features bitmask for this context.
         *
         * @param features the features bitmask to set
         * @since 2.0.51
         */
        public void setFeatures(long features) {
            this.features = features;
        }

        /**
         * Checks if the specified feature is enabled in this context.
         *
         * @param feature the feature to check
         * @return true if the feature is enabled, false otherwise
         */
        public boolean isEnabled(Feature feature) {
            return (this.features & feature.mask) != 0;
        }

        /**
         * Checks if the specified feature mask is enabled in this context.
         *
         * @param feature the feature mask to check
         * @return true if the feature is enabled, false otherwise
         */
        public boolean isEnabled(long feature) {
            return (this.features & feature) != 0;
        }

        /**
         * Configures features for this context.
         *
         * @param features the features to enable
         */
        public void config(Feature... features) {
            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
        }

        /**
         * Configures a specific feature for this context.
         *
         * @param feature the feature to configure
         * @param state true to enable the feature, false to disable it
         */
        public void config(Feature feature, boolean state) {
            if (state) {
                features |= feature.mask;
            } else {
                features &= ~feature.mask;
            }
        }

        /**
         * Configures filters for this context.
         *
         * @param filters the filters to configure
         */
        public void configFilter(Filter... filters) {
            for (int i = 0; i < filters.length; i++) {
                Filter filter = filters[i];
                if (filter instanceof NameFilter) {
                    if (this.nameFilter == null) {
                        this.nameFilter = (NameFilter) filter;
                    } else {
                        this.nameFilter = NameFilter.compose(this.nameFilter, (NameFilter) filter);
                    }
                }

                if (filter instanceof ValueFilter) {
                    if (this.valueFilter == null) {
                        this.valueFilter = (ValueFilter) filter;
                    } else {
                        this.valueFilter = ValueFilter.compose(this.valueFilter, (ValueFilter) filter);
                    }
                }

                if (filter instanceof PropertyFilter) {
                    if (this.propertyFilter == null) {
                        this.propertyFilter = (PropertyFilter) filter;
                    } else {
                        this.propertyFilter = PropertyFilter.compose(this.propertyFilter, (PropertyFilter) filter);
                    }
                }

                if (filter instanceof PropertyPreFilter) {
                    if (this.propertyPreFilter == null) {
                        this.propertyPreFilter = (PropertyPreFilter) filter;
                    } else {
                        this.propertyPreFilter = PropertyPreFilter.compose(this.propertyPreFilter, (PropertyPreFilter) filter);
                    }
                }

                if (filter instanceof BeforeFilter) {
                    this.beforeFilter = (BeforeFilter) filter;
                }

                if (filter instanceof AfterFilter) {
                    this.afterFilter = (AfterFilter) filter;
                }

                if (filter instanceof LabelFilter) {
                    if (this.labelFilter == null) {
                        this.labelFilter = (LabelFilter) filter;
                    } else {
                        this.labelFilter = LabelFilter.compose(this.labelFilter, (LabelFilter) filter);
                    }
                }

                if (filter instanceof ContextValueFilter) {
                    this.contextValueFilter = (ContextValueFilter) filter;
                }

                if (filter instanceof ContextNameFilter) {
                    this.contextNameFilter = (ContextNameFilter) filter;
                }
            }

            hasFilter = propertyPreFilter != null
                    || propertyFilter != null
                    || nameFilter != null
                    || valueFilter != null
                    || beforeFilter != null
                    || afterFilter != null
                    || labelFilter != null
                    || contextValueFilter != null
                    || contextNameFilter != null;
        }

        /**
         * Gets the ObjectWriter for the specified object type.
         * This method retrieves an ObjectWriter instance that can serialize objects of the specified type.
         *
         * @param <T> the type of objects to be serialized
         * @param objectType the class of objects to be serialized
         * @return the ObjectWriter for the specified type
         */
        public <T> ObjectWriter<T> getObjectWriter(Class<T> objectType) {
            boolean fieldBased = (features & FieldBased.mask) != 0;
            return provider.getObjectWriter(objectType, objectType, fieldBased);
        }

        /**
         * Gets the ObjectWriter for the specified object type and class.
         * This method retrieves an ObjectWriter instance that can serialize objects of the specified type and class.
         *
         * @param <T> the type of objects to be serialized
         * @param objectType the type of objects to be serialized
         * @param objectClass the class of objects to be serialized
         * @return the ObjectWriter for the specified type and class
         */
        public <T> ObjectWriter<T> getObjectWriter(Type objectType, Class<T> objectClass) {
            boolean fieldBased = (features & FieldBased.mask) != 0;
            return provider.getObjectWriter(objectType, objectClass, fieldBased);
        }

        /**
         * Gets the ObjectWriterProvider used by this context.
         *
         * @return the ObjectWriterProvider
         */
        public ObjectWriterProvider getProvider() {
            return provider;
        }

        /**
         * Gets the ZoneId used by this context.
         * If no ZoneId has been set, the system default ZoneId will be returned.
         *
         * @return the ZoneId
         */
        public ZoneId getZoneId() {
            if (zoneId == null) {
                zoneId = DateUtils.DEFAULT_ZONE_ID;
            }
            return zoneId;
        }

        /**
         * Sets the ZoneId for this context.
         *
         * @param zoneId the ZoneId to set
         */
        public void setZoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
        }

        /**
         * Gets the date format pattern for this context.
         *
         * @return the date format pattern, or null if not set
         */
        public String getDateFormat() {
            return dateFormat;
        }

        /**
         * Checks if the date format is configured to use milliseconds.
         *
         * @return true if milliseconds format is enabled, false otherwise
         */
        public boolean isDateFormatMillis() {
            return dateFormatMillis;
        }

        /**
         * Checks if the date format is configured to use Unix time.
         *
         * @return true if Unix time format is enabled, false otherwise
         */
        public boolean isDateFormatUnixTime() {
            return dateFormatUnixTime;
        }

        /**
         * Checks if the date format is configured to use ISO8601 format.
         *
         * @return true if ISO8601 format is enabled, false otherwise
         */
        public boolean isDateFormatISO8601() {
            return dateFormatISO8601;
        }

        /**
         * Checks if the date format includes day information.
         *
         * @return true if day information is included, false otherwise
         */
        public boolean isDateFormatHasDay() {
            return formatHasDay;
        }

        /**
         * Checks if the date format includes hour information.
         *
         * @return true if hour information is included, false otherwise
         */
        public boolean isDateFormatHasHour() {
            return formatHasHour;
        }

        /**
         * Checks if the date format is configured to use yyyy-MM-dd HH:mm:ss format (19 characters).
         *
         * @return true if this format is enabled, false otherwise
         */
        public boolean isFormatyyyyMMddhhmmss19() {
            return formatyyyyMMddhhmmss19;
        }

        /**
         * Gets the date formatter for this context.
         *
         * @return the date formatter, or null if not set
         */
        public DateTimeFormatter getDateFormatter() {
            if (dateFormatter == null && dateFormat != null && !dateFormatMillis && !dateFormatISO8601 && !dateFormatUnixTime) {
                dateFormatter = locale == null
                        ? DateTimeFormatter.ofPattern(dateFormat)
                        : DateTimeFormatter.ofPattern(dateFormat, locale);
            }
            return dateFormatter;
        }

        /**
         * Sets the date format pattern for this context.
         *
         * @param dateFormat the date format pattern to set
         */
        public void setDateFormat(String dateFormat) {
            if (dateFormat == null || !dateFormat.equals(this.dateFormat)) {
                dateFormatter = null;
            }

            if (dateFormat != null && !dateFormat.isEmpty()) {
                boolean dateFormatMillis = false, dateFormatISO8601 = false, dateFormatUnixTime = false, formatHasDay = false, formatHasHour = false, formatyyyyMMddhhmmss19 = false;
                switch (dateFormat) {
                    case "millis":
                        dateFormatMillis = true;
                        break;
                    case "iso8601":
                        dateFormatMillis = false;
                        dateFormatISO8601 = true;
                        break;
                    case "unixtime":
                        dateFormatMillis = false;
                        dateFormatUnixTime = true;
                        break;
                    case "yyyy-MM-ddTHH:mm:ss":
                        dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
                        formatHasDay = true;
                        formatHasHour = true;
                        break;
                    case "yyyy-MM-dd HH:mm:ss":
                        formatyyyyMMddhhmmss19 = true;
                        formatHasDay = true;
                        formatHasHour = true;
                        break;
                    default:
                        dateFormatMillis = false;
                        formatHasDay = dateFormat.contains("d");
                        formatHasHour = dateFormat.contains("H");
                        break;
                }
                this.dateFormatMillis = dateFormatMillis;
                this.dateFormatISO8601 = dateFormatISO8601;
                this.dateFormatUnixTime = dateFormatUnixTime;
                this.formatHasDay = formatHasDay;
                this.formatHasHour = formatHasHour;
                this.formatyyyyMMddhhmmss19 = formatyyyyMMddhhmmss19;
            }

            this.dateFormat = dateFormat;
        }

        /**
         * Gets the property pre-filter for this context.
         *
         * @return the property pre-filter, or null if not set
         */
        public PropertyPreFilter getPropertyPreFilter() {
            return propertyPreFilter;
        }

        /**
         * Sets the property pre-filter for this context.
         *
         * @param propertyPreFilter the property pre-filter to set
         */
        public void setPropertyPreFilter(PropertyPreFilter propertyPreFilter) {
            this.propertyPreFilter = propertyPreFilter;
            if (propertyPreFilter != null) {
                hasFilter = true;
            }
        }

        /**
         * Gets the name filter for this context.
         *
         * @return the name filter, or null if not set
         */
        public NameFilter getNameFilter() {
            return nameFilter;
        }

        /**
         * Sets the name filter for this context.
         *
         * @param nameFilter the name filter to set
         */
        public void setNameFilter(NameFilter nameFilter) {
            this.nameFilter = nameFilter;
            if (nameFilter != null) {
                hasFilter = true;
            }
        }

        /**
         * Gets the value filter for this context.
         *
         * @return the value filter, or null if not set
         */
        public ValueFilter getValueFilter() {
            return valueFilter;
        }

        /**
         * Sets the value filter for this context.
         *
         * @param valueFilter the value filter to set
         */
        public void setValueFilter(ValueFilter valueFilter) {
            this.valueFilter = valueFilter;
            if (valueFilter != null) {
                hasFilter = true;
            }
        }

        /**
         * Gets the context value filter for this context.
         *
         * @return the context value filter, or null if not set
         */
        public ContextValueFilter getContextValueFilter() {
            return contextValueFilter;
        }

        /**
         * Sets the context value filter for this context.
         *
         * @param contextValueFilter the context value filter to set
         */
        public void setContextValueFilter(ContextValueFilter contextValueFilter) {
            this.contextValueFilter = contextValueFilter;
            if (contextValueFilter != null) {
                hasFilter = true;
            }
        }

        /**
         * Gets the context name filter for this context.
         *
         * @return the context name filter, or null if not set
         */
        public ContextNameFilter getContextNameFilter() {
            return contextNameFilter;
        }

        /**
         * Sets the context name filter for this context.
         *
         * @param contextNameFilter the context name filter to set
         */
        public void setContextNameFilter(ContextNameFilter contextNameFilter) {
            this.contextNameFilter = contextNameFilter;
            if (contextNameFilter != null) {
                hasFilter = true;
            }
        }

        /**
         * Gets the property filter for this context.
         *
         * @return the property filter, or null if not set
         */
        public PropertyFilter getPropertyFilter() {
            return propertyFilter;
        }

        /**
         * Sets the property filter for this context.
         *
         * @param propertyFilter the property filter to set
         */
        public void setPropertyFilter(PropertyFilter propertyFilter) {
            this.propertyFilter = propertyFilter;
            if (propertyFilter != null) {
                hasFilter = true;
            }
        }

        /**
         * Gets the after filter for this context.
         *
         * @return the after filter, or null if not set
         */
        public AfterFilter getAfterFilter() {
            return afterFilter;
        }

        /**
         * Sets the after filter for this context.
         *
         * @param afterFilter the after filter to set
         */
        public void setAfterFilter(AfterFilter afterFilter) {
            this.afterFilter = afterFilter;
            if (afterFilter != null) {
                hasFilter = true;
            }
        }

        /**
         * Gets the before filter for this context.
         *
         * @return the before filter, or null if not set
         */
        public BeforeFilter getBeforeFilter() {
            return beforeFilter;
        }

        /**
         * Sets the before filter for this context.
         *
         * @param beforeFilter the before filter to set
         */
        public void setBeforeFilter(BeforeFilter beforeFilter) {
            this.beforeFilter = beforeFilter;
            if (beforeFilter != null) {
                hasFilter = true;
            }
        }

        /**
         * Gets the label filter for this context.
         *
         * @return the label filter, or null if not set
         */
        public LabelFilter getLabelFilter() {
            return labelFilter;
        }

        /**
         * Sets the label filter for this context.
         *
         * @param labelFilter the label filter to set
         */
        public void setLabelFilter(LabelFilter labelFilter) {
            this.labelFilter = labelFilter;
            if (labelFilter != null) {
                hasFilter = true;
            }
        }

        /**
         * Gets the maximum nesting level allowed for this context.
         *
         * @return the maximum nesting level
         */
        public int getMaxLevel() {
            return maxLevel;
        }

        /**
         * Sets the maximum nesting level allowed for this context.
         *
         * @param maxLevel the maximum nesting level to set
         */
        public void setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }
    }

    public static final long MASK_FIELD_BASED = 1;
    public static final long MASK_IGNORE_NONE_SERIALIZABLE = 1 << 1;
    public static final long MAKS_ERROR_ON_NONE_SERIALIZABLE = 1 << 2;
    public static final long MASK_BEAN_TO_ARRAY = 1 << 3;
    public static final long MASK_WRITE_MAP_NULL_VALUE = 1 << 4;
    public static final long MASK_BROWSER_COMPATIBLE = 1 << 5;
    public static final long MASK_NULL_AS_DEFAULT_VALUE = 1 << 6;
    public static final long MASK_WRITE_BOOLEAN_AS_NUMBER = 1 << 7;
    public static final long MASK_WRITE_NON_STRING_VALUE_AS_STRING = 1L << 8;
    public static final long MASK_WRITE_CLASS_NAME = 1 << 9;
    public static final long MASK_NOT_WRITE_ROOT_CLASSNAME = 1 << 10;
    public static final long MASK_NOT_WRITE_HASHMAP_ARRAY_LIST_CLASS_NAME = 1 << 11;
    public static final long MASK_NOT_WRITE_DEFAULT_VALUE = 1 << 12;
    public static final long MASK_WRITE_ENUMS_USING_NAME = 1 << 13;
    public static final long MASK_WRITE_ENUM_USING_TO_STRING = 1 << 14;
    public static final long MASK_IGNORE_ERROR_GETTER = 1L << 15;
    public static final long MASK_PRETTY_FORMAT = 1 << 16;
    public static final long MASK_REFERENCE_DETECTION = 1 << 17;
    public static final long MASK_WRITE_BIG_DECIMAL_AS_PLAIN = 1 << 19;
    public static final long MASK_USE_SINGLE_QUOTES = 1 << 20;
    public static final long MASK_WRITE_NULL_LIST_AS_EMPTY = 1 << 22;
    public static final long MASK_WRITE_NULL_STRING_AS_EMPTY = 1 << 23;
    public static final long MASK_WRITE_NULL_NUMBER_AS_ZERO = 1 << 24;
    public static final long MASK_WRITE_NULL_BOOLEAN_AS_FALSE = 1 << 25;
    public static final long MASK_NOT_WRITE_EMPTY_ARRAY = 1 << 26;
    public static final long MASK_WRITE_NON_STRING_KEY_AS_STRING = 1 << 27;
    public static final long MASK_WRITE_PAIR_AS_JAVA_BEAN = 1 << 28;
    public static final long MASK_ESCAPE_NONE_ASCII = 1L << 30;
    public static final long MASK_IGNORE_NON_FIELD_GETTER = 1L << 32;
    public static final long MASK_WRITE_LONG_AS_STRING = 1L << 34;
    public static final long MASK_BROWSER_SECURE = 1L << 35;
    public static final long MASK_WRITE_ENUM_USING_ORDINAL = 1L << 36;
    public static final long MASK_UNQUOTE_FIELD_NAME = 1L << 38;
    public static final long MASK_NOT_WRITE_NUMBER_CLASS_NAME = 1L << 40;
    public static final long MASK_WRITE_FLOAT_SPECIAL_AS_STRING = 1L << 45;

    /**
     * Feature is used to control the behavior of JSON writing and serialization in FASTJSON2.
     * Each feature represents a specific configuration option that can be enabled or disabled
     * to customize how Java objects are serialized to JSON format.
     *
     * <p>Features can be enabled in several ways:
     * <ul>
     *   <li>Using factory methods like {@link #of(Feature...)}</li>
     *   <li>Using {@link Context#config(Feature...)} method</li>
     *   <li>Using {@link JSONFactory#getDefaultWriterFeatures()} for global configuration</li>
     * </ul>
     *
     *
     * <p>Example usage:
     * <pre>
     * // Enable PrettyFormat feature for this writer only
     * try (JSONWriter writer = JSONWriter.of(JSONWriter.Feature.PrettyFormat)) {
     *     writer.writeAny(object);
     *     String json = writer.toString();
     * }
     *
     * // Enable multiple features
     * try (JSONWriter writer = JSONWriter.of(
     *         JSONWriter.Feature.PrettyFormat,
     *         JSONWriter.Feature.WriteMapNullValue)) {
     *     writer.writeAny(object);
     *     String json = writer.toString();
     * }
     *
     * // Using context configuration
     * JSONWriter.Context context = new JSONWriter.Context();
     * context.config(JSONWriter.Feature.PrettyFormat);
     * try (JSONWriter writer = JSONWriter.of(context)) {
     *     writer.writeAny(object);
     *     String json = writer.toString();
     * }
     * </pre>
     *
     *
     * <p>Features are implemented as bitmask flags for efficient storage and checking.
     * Each feature has a unique mask value that is used internally to determine
     * whether the feature is enabled in a given configuration.</p>
     *
     * @see JSONWriter.Context
     * @see JSONFactory
     * @since 2.0.0
     */
    public enum Feature {
        /**
         * Feature that determines whether to use field-based serialization instead of getter-based serialization.
         * When enabled, fields are directly accessed rather than using getter methods.
         * This can improve performance but may bypass validation logic in getters.
         *
         * <p>By default, this feature is disabled, meaning that getter-based serialization is used.</p>
         *
         * @since 2.0.0
         */
        FieldBased(MASK_FIELD_BASED),

        /**
         * Feature that determines whether to ignore non-serializable classes during serialization.
         * When enabled, classes that do not implement {@link java.io.Serializable} will be ignored
         * rather than causing an exception to be thrown.
         *
         * <p>By default, this feature is disabled, meaning that non-serializable classes are not ignored.</p>
         *
         * @since 2.0.0
         */
        IgnoreNoneSerializable(MASK_IGNORE_NONE_SERIALIZABLE),

        /**
         * Feature that determines whether to throw an exception when encountering non-serializable classes
         * during serialization.
         * When enabled, an exception will be thrown if a class does not implement {@link java.io.Serializable}.
         *
         * <p>By default, this feature is disabled, meaning that no exception is thrown for non-serializable classes.</p>
         *
         * @since 2.0.0
         */
        ErrorOnNoneSerializable(MAKS_ERROR_ON_NONE_SERIALIZABLE),

        /**
         * Feature that determines whether to serialize Java beans as JSON arrays instead of JSON objects.
         * When enabled, bean properties will be serialized as array elements in the order they are defined,
         * rather than as key-value pairs in an object.
         *
         * <p>By default, this feature is disabled, meaning that beans are serialized as JSON objects.</p>
         *
         * @since 2.0.0
         */
        BeanToArray(MASK_BEAN_TO_ARRAY),

        /**
         * Feature that determines whether to write null values during serialization.
         * When enabled, null values will be included in the output JSON.
         *
         * <p>By default, this feature is disabled, meaning that null values are omitted from the output.</p>
         *
         * @since 2.0.0
         */
        WriteNulls(MASK_WRITE_MAP_NULL_VALUE),

        /**
         * Feature that determines whether to write null values for map entries during serialization.
         * When enabled, null values in maps will be included in the output JSON.
         *
         * <p>By default, this feature is disabled, meaning that null map values are omitted from the output.</p>
         *
         * @since 2.0.0
         */
        WriteMapNullValue(MASK_WRITE_MAP_NULL_VALUE),

        /**
         * Feature that enables browser-compatible JSON output.
         * When enabled, the output will be formatted to be compatible with browser JavaScript engines.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        BrowserCompatible(MASK_BROWSER_COMPATIBLE),

        /**
         * Feature that determines whether to write default values instead of null values during serialization.
         * When enabled, default values (0 for numbers, false for booleans, empty string for strings) will be
         * written instead of null values.
         *
         * <p>By default, this feature is disabled, meaning that null values are handled according to other features.</p>
         *
         * @since 2.0.0
         */
        NullAsDefaultValue(MASK_NULL_AS_DEFAULT_VALUE),

        /**
         * Feature that determines whether to write boolean values as numbers during serialization.
         * When enabled, boolean values will be serialized as 1 (for true) and 0 (for false) instead of
         * true and false literals.
         *
         * <p>By default, this feature is disabled, meaning that boolean values are written as true/false.</p>
         *
         * @since 2.0.0
         */
        WriteBooleanAsNumber(MASK_WRITE_BOOLEAN_AS_NUMBER),

        /**
         * Feature that determines whether to write non-string values as strings during serialization.
         * When enabled, numeric and other non-string values will be converted to their string representation.
         *
         * <p>By default, this feature is disabled, meaning that values are written in their native JSON types.</p>
         *
         * @since 2.0.0
         */
        WriteNonStringValueAsString(MASK_WRITE_NON_STRING_VALUE_AS_STRING),

        /**
         * Feature that determines whether to write class names during serialization.
         * When enabled, class names will be included in the output JSON, typically using a special "@type" field.
         *
         * <p>By default, this feature is disabled, meaning that class names are not included in the output.</p>
         *
         * @since 2.0.0
         */
        WriteClassName(MASK_WRITE_CLASS_NAME),

        /**
         * Feature that determines whether to write the root class name during serialization.
         * When enabled, the class name of the root object will be included in the output JSON.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        NotWriteRootClassName(MASK_NOT_WRITE_ROOT_CLASSNAME),

        /**
         * Feature that determines whether to write class names for HashMap and ArrayList during serialization.
         * When enabled, class names for these common collection types will be omitted from the output.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        NotWriteHashMapArrayListClassName(MASK_NOT_WRITE_HASHMAP_ARRAY_LIST_CLASS_NAME),

        /**
         * Feature that determines whether to write default values during serialization.
         * When enabled, fields with default values (0 for numbers, false for booleans, etc.) will be omitted.
         *
         * <p>By default, this feature is disabled, meaning that all field values are written regardless of whether
         * they are default values.</p>
         *
         * @since 2.0.0
         */
        NotWriteDefaultValue(MASK_NOT_WRITE_DEFAULT_VALUE),

        /**
         * Feature that determines whether to write enum values using their name during serialization.
         * When enabled, enum values will be serialized as their name string rather than their ordinal value.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        WriteEnumsUsingName(MASK_WRITE_ENUMS_USING_NAME),

        /**
         * Feature that determines whether to write enum values using their toString() representation during serialization.
         * When enabled, enum values will be serialized using their toString() method rather than their name or ordinal.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        WriteEnumUsingToString(MASK_WRITE_ENUM_USING_TO_STRING),

        /**
         * Feature that determines whether to ignore errors when calling getter methods during serialization.
         * When enabled, exceptions thrown by getter methods will be ignored rather than propagated.
         *
         * <p>By default, this feature is disabled, meaning that getter method exceptions are propagated.</p>
         *
         * @since 2.0.0
         */
        IgnoreErrorGetter(MASK_IGNORE_ERROR_GETTER),

        /**
         * Feature that enables pretty-printed JSON output with formatting and indentation.
         * When enabled, the output JSON will be formatted with line breaks and indentation for readability.
         *
         * <p>By default, this feature is disabled, meaning that JSON is output in compact form.</p>
         *
         * @since 2.0.0
         */
        PrettyFormat(MASK_PRETTY_FORMAT),

        /**
         * Feature that enables reference detection during serialization.
         * When enabled, circular references and repeated objects will be detected and handled using
         * reference markers to avoid infinite loops and duplicate serialization.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        ReferenceDetection(MASK_REFERENCE_DETECTION),

        /**
         * Feature that determines whether to write field names as symbols during serialization.
         * When enabled, field names will be written as symbol references rather than string literals.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        WriteNameAsSymbol(1 << 18),

        /**
         * Feature that determines whether to write BigDecimal values in plain format during serialization.
         * When enabled, BigDecimal values will be written without exponential notation when possible.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        WriteBigDecimalAsPlain(MASK_WRITE_BIG_DECIMAL_AS_PLAIN),

        /**
         * Feature that determines whether to use single quotes instead of double quotes for strings.
         * When enabled, string values will be enclosed in single quotes rather than double quotes.
         *
         * <p>By default, this feature is disabled, meaning that double quotes are used.</p>
         *
         * @since 2.0.0
         */
        UseSingleQuotes(MASK_USE_SINGLE_QUOTES),

        /**
         * The serialized Map will first be sorted according to Key,
         * and is used in some scenarios where serialized content needs to be signed.
         * SortedMap and derived classes do not need to do this.
         * This Feature does not work for LinkedHashMap.
         * @deprecated Use {@link Feature#SortMapEntriesByKeys} instead.
         * @since 2.0.0
         */
        MapSortField(1 << 21),

        /**
         * Feature that determines whether to write null lists as empty arrays during serialization.
         * When enabled, null collection values will be serialized as empty JSON arrays ([]) rather than null.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        WriteNullListAsEmpty(MASK_WRITE_NULL_LIST_AS_EMPTY),

        /**
         * Feature that determines whether to write null strings as empty strings during serialization.
         * When enabled, null string values will be serialized as empty strings ("") rather than null.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 1.1
         */
        WriteNullStringAsEmpty(MASK_WRITE_NULL_STRING_AS_EMPTY),

        /**
         * Feature that determines whether to write null numbers as zero during serialization.
         * When enabled, null numeric values will be serialized as 0 rather than null.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 1.1
         */
        WriteNullNumberAsZero(MASK_WRITE_NULL_NUMBER_AS_ZERO),

        /**
         * Feature that determines whether to write null booleans as false during serialization.
         * When enabled, null boolean values will be serialized as false rather than null.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 1.1
         */
        WriteNullBooleanAsFalse(MASK_WRITE_NULL_BOOLEAN_AS_FALSE),

        /**
         * Feature that determines whether to avoid writing empty arrays during serialization.
         * When enabled, empty arrays will be omitted from the output rather than written as [].
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @deprecated use IgnoreEmpty
         * @since 2.0.7
         */
        NotWriteEmptyArray(MASK_NOT_WRITE_EMPTY_ARRAY),

        /**
         * Feature that determines whether to ignore empty values during serialization.
         * When enabled, empty collections, empty strings, and other empty values will be omitted from the output.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.51
         */
        IgnoreEmpty(MASK_NOT_WRITE_EMPTY_ARRAY),

        /**
         * Feature that determines whether to write non-string keys as strings during serialization.
         * When enabled, map keys that are not strings will be converted to their string representation.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        WriteNonStringKeyAsString(MASK_WRITE_NON_STRING_KEY_AS_STRING),

        /**
         * Feature that determines whether to write key-value pairs as Java beans during serialization.
         * When enabled, key-value pairs will be serialized using Java bean conventions.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.11
         */
        WritePairAsJavaBean(MASK_WRITE_PAIR_AS_JAVA_BEAN),

        /**
         * Feature that enables optimization for ASCII characters during serialization.
         * When enabled, the serializer will use optimized paths for ASCII-only content.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.12
         */
        OptimizedForAscii(1L << 29),

        /**
         * Feature that specifies that all characters beyond 7-bit ASCII range (i.e. code points of 128 and above)
         * need to be output using format-specific escapes (for JSON, backslash escapes),
         * if format uses escaping mechanisms (which is generally true for textual formats but not for binary formats).
         * Feature is disabled by default.
         *
         * @since 2.0.12
         */
        EscapeNoneAscii(MASK_ESCAPE_NONE_ASCII),

        /**
         * Feature that determines whether to write byte arrays as Base64-encoded strings during serialization.
         * When enabled, byte array values will be serialized as Base64-encoded strings rather than arrays of numbers.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.13
         */
        WriteByteArrayAsBase64(1L << 31),

        /**
         * Feature that determines whether to ignore non-field getter methods during serialization.
         * When enabled, only getter methods that correspond to actual fields will be considered.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.13
         */
        IgnoreNonFieldGetter(MASK_IGNORE_NON_FIELD_GETTER),

        /**
         * Feature that enables support for large objects during serialization.
         * When enabled, the serializer will use configurations appropriate for very large object graphs.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.16
         */
        LargeObject(1L << 33),

        /**
         * Feature that determines whether to write long values as strings during serialization.
         * When enabled, long numeric values will be serialized as strings rather than numbers to avoid precision loss
         * in JavaScript and other environments with limited integer precision.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.17
         */
        WriteLongAsString(MASK_WRITE_LONG_AS_STRING),

        /**
         * Feature that enables browser security measures during serialization.
         * When enabled, the output will be formatted to be secure when used in browser environments.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.20
         */
        BrowserSecure(MASK_BROWSER_SECURE),

        /**
         * Feature that determines whether to write enum values using their ordinal value during serialization.
         * When enabled, enum values will be serialized as their ordinal (position) rather than their name.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.20
         */
        WriteEnumUsingOrdinal(MASK_WRITE_ENUM_USING_ORDINAL),

        /**
         * Feature that determines whether to write the class name of Throwable objects during serialization.
         * When enabled, the class name of exception and error objects will be included in the output.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.30
         */
        WriteThrowableClassName(1L << 37),

        /**
         * Feature that determines whether to write field names without quotes during serialization.
         * When enabled, field names in JSON objects will not be enclosed in quotes.
         *
         * <p>By default, this feature is disabled, meaning that field names are quoted.</p>
         *
         * @since 2.0.33
         */
        UnquoteFieldName(MASK_UNQUOTE_FIELD_NAME),

        /**
         * Feature that determines whether to write class names for Set collections during serialization.
         * When enabled, class names for Set collections will be omitted from the output.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.34
         */
        NotWriteSetClassName(1L << 39),

        /**
         * Feature that determines whether to write class names for Number objects during serialization.
         * When enabled, class names for Number objects will be omitted from the output.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.34
         */
        NotWriteNumberClassName(MASK_NOT_WRITE_NUMBER_CLASS_NAME),

        /**
         * The serialized Map will first be sorted according to Key,
         * and is used in some scenarios where serialized content needs to be signed.
         * SortedMap and derived classes do not need to do this.
         *
         * @since 2.0.48
         */
        SortMapEntriesByKeys(1L << 41),

        /**
         * JSON formatting support using 2 spaces for indentation.
         * When enabled, pretty-printed JSON will use 2 spaces for each indentation level.
         *
         * <p>This feature requires {@link PrettyFormat} to also be enabled.</p>
         *
         * @since 2.0.54
         */
        PrettyFormatWith2Space(1L << 42),

        /**
         * JSON formatting support using 4 spaces for indentation.
         * When enabled, pretty-printed JSON will use 4 spaces for each indentation level.
         *
         * <p>This feature requires {@link PrettyFormat} to also be enabled.</p>
         *
         * @since 2.0.54
         */
        PrettyFormatWith4Space(1L << 43),

        /**
         * Feature that determines whether to write java.util.Date objects as milliseconds since epoch.
         * When enabled, Date objects will be serialized as numeric timestamps rather than formatted strings.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        WriterUtilDateAsMillis(1L << 44),

        /**
         * Feature that determines whether to write float/double NaN and Infinite values as Strings.
         * When enabled, NaN/Infinity will be serialized as "NaN", "Infinity", "-Infinity".
         *
         * @since 2.0.61
         */
        WriteFloatSpecialAsString(MASK_WRITE_FLOAT_SPECIAL_AS_STRING);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }

        /**
         * Checks if this feature is enabled in the specified features bitmask.
         *
         * @param features the features bitmask to check
         * @return true if this feature is enabled, false otherwise
         */
        public boolean isEnabled(long features) {
            return (features & mask) != 0;
        }
    }

    /**
     * Path represents a JSON pointer path used for reference detection during serialization.
     * It tracks the location of objects within a JSON structure to detect circular references
     * and avoid infinite loops during serialization.
     *
     * <p>The Path class is used internally by JSONWriter to manage object references and
     * generate JSON Pointer strings as defined in RFC 6901. Paths are hierarchical,
     * with each Path instance containing a reference to its parent Path, forming a tree
     * structure that mirrors the JSON structure being serialized.</p>
     *
     * <p>Path instances are immutable once created and are used in reference detection
     * to determine if an object has already been serialized at another location in the
     * JSON structure.</p>
     *
     * <p>Example paths:
     * <ul>
     *   <li>ROOT path: "$"</li>
     *   <li>Property path: \"$.name\"</li>
     *   <li>Array element path: \"$.items[0]\"</li>
     *   <li>Nested path: \"$.person.address.street\"</li>
     * </ul>
     *
     *
     * @since 2.0.0
     */
    public static final class Path {
        /**
         * The root path instance, representing the top level of the JSON structure.
         * This is the starting point for all path calculations.
         */
        public static final Path ROOT = new Path(null, "$");

        /**
         * The manager reference path instance, used for special reference handling.
         */
        public static final Path MANGER_REFERNCE = new Path(null, "#");

        /**
         * The parent path of this path segment, or null if this is the root path.
         */
        public final Path parent;

        /**
         * The name of this path segment, or null if this represents an array index.
         */
        final String name;

        /**
         * The array index of this path segment, or -1 if this represents a property name.
         */
        final int index;

        /**
         * The cached full path string representation, computed lazily.
         */
        String fullPath;

        /**
         * First child path cache for optimization.
         */
        Path child0;

        /**
         * Second child path cache for optimization.
         */
        Path child1;

        /**
         * Creates a new Path instance representing a named property.
         *
         * @param parent the parent path, or null for the root path
         * @param name the property name for this path segment
         */
        public Path(Path parent, String name) {
            this.parent = parent;
            this.name = name;
            this.index = -1;
        }

        /**
         * Creates a new Path instance representing an array index.
         *
         * @param parent the parent path, or null for the root path
         * @param index the array index for this path segment
         */
        public Path(Path parent, int index) {
            this.parent = parent;
            this.name = null;
            this.index = index;
        }

        /**
         * Compares this Path with another object for equality.
         * Two Path instances are considered equal if they have the same parent,
         * name, and index.
         *
         * @param o the object to compare with
         * @return true if the objects are equal, false otherwise
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Path path = (Path) o;
            return index == path.index && Objects.equals(parent, path.parent) && Objects.equals(name, path.name);
        }

        /**
         * Returns a hash code value for this Path.
         *
         * @return a hash code value for this Path
         */
        @Override
        public int hashCode() {
            return Objects.hash(parent, name, index);
        }

        /**
         * Returns a string representation of this Path in JSON Pointer format.
         *
         * @return a string representation of this Path
         */
        @Override
        public String toString() {
            if (fullPath != null) {
                return fullPath;
            }

            byte[] buf = new byte[16];
            int off = 0;

            int level = 0;
            Path[] items = new Path[4];
            for (Path p = this; p != null; p = p.parent) {
                if (items.length == level) {
                    items = Arrays.copyOf(items, items.length + 4);
                }
                items[level] = p;
                level++;
            }

            boolean ascii = true;

            for (int i = level - 1; i >= 0; i--) {
                Path item = items[i];
                String name = item.name;
                if (name == null) {
                    int intValue = item.index;
                    int intValueSize = IOUtils.stringSize(intValue);
                    while (off + intValueSize + 2 >= buf.length) {
                        int newCapacity = buf.length + (buf.length >> 1);
                        buf = Arrays.copyOf(buf, newCapacity);
                    }

                    buf[off++] = '[';
                    IOUtils.getChars(intValue, off + intValueSize, buf);
                    off += intValueSize;
                    buf[off++] = ']';
                } else {
                    if (off + 1 >= buf.length) {
                        int newCapacity = buf.length + (buf.length >> 1);
                        buf = Arrays.copyOf(buf, newCapacity);
                    }

                    if (i != level - 1) {
                        buf[off++] = '.';
                    }

                    if (JVM_VERSION == 8) {
                        char[] chars = getCharArray(name);
                        for (int j = 0; j < chars.length; j++) {
                            char ch = chars[j];
                            switch (ch) {
                                case '/':
                                case ':':
                                case ';':
                                case '`':
                                case '.':
                                case '~':
                                case '!':
                                case '@':
                                case '#':
                                case '%':
                                case '^':
                                case '&':
                                case '*':
                                case '[':
                                case ']':
                                case '<':
                                case '>':
                                case '?':
                                case '(':
                                case ')':
                                case '-':
                                case '+':
                                case '=':
                                case '\\':
                                case '"':
                                case '\'':
                                case ',':
                                    if (off + 1 >= buf.length) {
                                        int newCapacity = buf.length + (buf.length >> 1);
                                        buf = Arrays.copyOf(buf, newCapacity);
                                    }
                                    buf[off] = '\\';
                                    buf[off + 1] = (byte) ch;
                                    off += 2;
                                    break;
                                default:
                                    if ((ch >= 0x0001) && (ch <= 0x007F)) {
                                        if (off == buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        buf[off++] = (byte) ch;
                                    } else if (ch >= '\uD800' && ch < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
                                        ascii = false;
                                        final int uc;
                                        if (ch < '\uDBFF' + 1) { // Character.isHighSurrogate(c)
                                            if (name.length() - j < 2) {
                                                uc = -1;
                                            } else {
                                                char d = name.charAt(j + 1);
                                                // d >= '\uDC00' && d < ('\uDFFF' + 1)
                                                if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                                                    uc = ((ch << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                                                } else {
//                            throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                                                    buf[off++] = (byte) '?';
                                                    continue;
                                                }
                                            }
                                        } else {
                                            //
                                            // Character.isLowSurrogate(c)
                                            buf[off++] = (byte) '?';
                                            continue;
//                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                                        }

                                        if (uc < 0) {
                                            if (off == buf.length) {
                                                int newCapacity = buf.length + (buf.length >> 1);
                                                buf = Arrays.copyOf(buf, newCapacity);
                                            }
                                            buf[off++] = (byte) '?';
                                        } else {
                                            if (off + 3 >= buf.length) {
                                                int newCapacity = buf.length + (buf.length >> 1);
                                                buf = Arrays.copyOf(buf, newCapacity);
                                            }
                                            buf[off] = (byte) (0xf0 | ((uc >> 18)));
                                            buf[off + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                                            buf[off + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                                            buf[off + 3] = (byte) (0x80 | (uc & 0x3f));
                                            off += 4;
                                            j++; // 2 chars
                                        }
                                    } else if (ch > 0x07FF) {
                                        if (off + 2 >= buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        ascii = false;

                                        buf[off] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
                                        buf[off + 1] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                                        buf[off + 2] = (byte) (0x80 | ((ch) & 0x3F));
                                        off += 3;
                                    } else {
                                        if (off + 1 >= buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        ascii = false;

                                        buf[off] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
                                        buf[off + 1] = (byte) (0x80 | ((ch) & 0x3F));
                                        off += 2;
                                    }
                                    break;
                            }
                        }
                    } else {
                        for (int j = 0; j < name.length(); j++) {
                            char ch = name.charAt(j);
                            switch (ch) {
                                case '/':
                                case ':':
                                case ';':
                                case '`':
                                case '.':
                                case '~':
                                case '!':
                                case '@':
                                case '#':
                                case '%':
                                case '^':
                                case '&':
                                case '*':
                                case '[':
                                case ']':
                                case '<':
                                case '>':
                                case '?':
                                case '(':
                                case ')':
                                case '-':
                                case '+':
                                case '=':
                                case '\\':
                                case '"':
                                case '\'':
                                case ',':
                                    if (off + 1 >= buf.length) {
                                        int newCapacity = buf.length + (buf.length >> 1);
                                        buf = Arrays.copyOf(buf, newCapacity);
                                    }
                                    buf[off] = '\\';
                                    buf[off + 1] = (byte) ch;
                                    off += 2;
                                    break;
                                default:
                                    if ((ch >= 0x0001) && (ch <= 0x007F)) {
                                        if (off == buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        buf[off++] = (byte) ch;
                                    } else if (ch >= '\uD800' && ch < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
                                        ascii = false;
                                        final int uc;
                                        if (ch < '\uDBFF' + 1) { // Character.isHighSurrogate(c)
                                            if (name.length() - j < 2) {
                                                uc = -1;
                                            } else {
                                                char d = name.charAt(j + 1);
                                                // d >= '\uDC00' && d < ('\uDFFF' + 1)
                                                if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                                                    uc = ((ch << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                                                } else {
//                            throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                                                    buf[off++] = (byte) '?';
                                                    continue;
                                                }
                                            }
                                        } else {
                                            //
                                            // Character.isLowSurrogate(c)
                                            buf[off++] = (byte) '?';
                                            continue;
//                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                                        }

                                        if (uc < 0) {
                                            if (off == buf.length) {
                                                int newCapacity = buf.length + (buf.length >> 1);
                                                buf = Arrays.copyOf(buf, newCapacity);
                                            }

                                            buf[off++] = (byte) '?';
                                        } else {
                                            if (off + 4 >= buf.length) {
                                                int newCapacity = buf.length + (buf.length >> 1);
                                                buf = Arrays.copyOf(buf, newCapacity);
                                            }

                                            buf[off] = (byte) (0xf0 | ((uc >> 18)));
                                            buf[off + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                                            buf[off + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                                            buf[off + 3] = (byte) (0x80 | (uc & 0x3f));
                                            off += 4;
                                            j++; // 2 chars
                                        }
                                    } else if (ch > 0x07FF) {
                                        if (off + 2 >= buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        ascii = false;

                                        buf[off] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
                                        buf[off + 1] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                                        buf[off + 2] = (byte) (0x80 | ((ch) & 0x3F));
                                        off += 3;
                                    } else {
                                        if (off + 1 >= buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        ascii = false;

                                        buf[off] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
                                        buf[off + 1] = (byte) (0x80 | ((ch) & 0x3F));
                                        off += 2;
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

            if (ascii) {
                if (STRING_CREATOR_JDK11 != null) {
                    byte[] bytes;
                    if (off == buf.length) {
                        bytes = buf;
                    } else {
                        bytes = new byte[off];
                        System.arraycopy(buf, 0, bytes, 0, off);
                    }
                    return fullPath = STRING_CREATOR_JDK11.apply(bytes, LATIN1);
                }

                if (STRING_CREATOR_JDK8 != null) {
                    char[] chars = new char[off];
                    for (int i = 0; i < off; i++) {
                        chars[i] = (char) buf[i];
                    }
                    return fullPath = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                }
            }

            return fullPath = new String(buf, 0, off, ascii ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8);
        }
    }

    protected static IllegalArgumentException illegalYear(int year) {
        return new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + year);
    }

    /**
     * Increments the indentation level.
     * This method is used to track the current nesting level during JSON serialization.
     *
     * @deprecated This method is deprecated and will be removed in a future version
     * @since 2.0.51
     */
    public final void incrementIndent() {
        level++;
    }

    /**
     * Decrements the indentation level.
     * This method is used to track the current nesting level during JSON serialization.
     *
     * @deprecated This method is deprecated and will be removed in a future version
     * @since 2.0.51
     */
    public final void decrementIdent() {
        level--;
    }

    /**
     * Writes a line break followed by indentation whitespace.
     * This method is used for pretty-printing JSON output with proper indentation.
     *
     * @deprecated This method is deprecated and will be removed in a future version
     * @since 2.0.51
     */
    public void println() {
        writeRaw('\n');
        for (int i = 0; i < level; ++i) {
            writeRaw('\t');
        }
    }

    /**
     * Writes a reference to a previously serialized object.
     * This method is used to handle circular references by writing a reference
     * to an object that has already been serialized, instead of serializing it again.
     *
     * @param object the object for which to write a reference
     * @deprecated This method is deprecated and will be removed in a future version
     * @since 2.0.51
     */
    public final void writeReference(Object object) {
        if (refs == null) {
            return;
        }

        Path path = refs.get(object);
        if (path != null) {
            writeReference(path.toString());
        }
    }

    /**
     * Calculates the new capacity for the internal buffer based on the minimum required capacity.
     * This method implements a growth strategy that increases the capacity by 50% of the current capacity,
     * ensuring it meets the minimum required capacity. It also enforces a maximum array size limit
     * to prevent excessive memory allocation.
     *
     * @param minCapacity the minimum required capacity
     * @param oldCapacity the current capacity of the buffer
     * @return the new capacity, which is at least minCapacity and follows the growth strategy
     * @throws JSONLargeObjectException if the required capacity exceeds the maximum allowed array size
     * @since 2.0.51
     */
    protected final int newCapacity(int minCapacity, int oldCapacity) {
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity > maxArraySize) {
            if (minCapacity < maxArraySize) {
                newCapacity = maxArraySize;
            } else {
                throw new JSONLargeObjectException("Maximum array size exceeded. Try enabling LargeObject feature instead. "
                        + "Requested size: " + minCapacity + ", max size: " + maxArraySize);
            }
        }
        return newCapacity;
    }

    /**
     * Gets the attachment object associated with this JSONWriter.
     * Attachments can be used to store additional context or metadata during serialization.
     *
     * @return the attachment object, or null if no attachment is set
     */
    public Object getAttachment() {
        return attachment;
    }

    /**
     * Sets the attachment object for this JSONWriter.
     * Attachments can be used to store additional context or metadata during serialization.
     *
     * @param attachment the attachment object to set
     */
    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    /**
     * Throws a JSONException indicating that the nesting level has exceeded the maximum allowed level.
     * This method is called when the serialization process exceeds the configured maximum nesting depth
     * to prevent stack overflow and excessive memory consumption.
     *
     * @throws JSONException with a message indicating the level is too large
     * @since 2.0.51
     */
    protected final void overflowLevel() {
        throw new JSONException("level too large : " + level);
    }

    /**
     * Gets the current offset in the internal buffer.
     * The offset represents the position where the next character will be written.
     *
     * @return the current offset
     */
    public final int getOffset() {
        return off;
    }

    /**
     * Sets the offset in the internal buffer.
     * This method allows direct manipulation of the buffer position.
     *
     * @param offset the offset to set
     */
    public final void setOffset(int offset) {
        this.off = offset;
    }

    /**
     * Ensures that the internal buffer has at least the specified minimum capacity.
     * This method is used to dynamically expand the buffer when more space is needed
     * during the serialization process.
     *
     * @param minCapacity the minimum capacity required
     * @return the expanded buffer object
     * @since 2.0.51
     */
    public abstract Object ensureCapacity(int minCapacity);

    protected static JSONException overflowLevel(int level) {
        return new JSONException("level too large : " + level);
    }
}
