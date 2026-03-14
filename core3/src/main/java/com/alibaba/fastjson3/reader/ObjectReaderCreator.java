package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONParser;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.ReadFeature;
import com.alibaba.fastjson3.annotation.JSONCreator;
import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;
import com.alibaba.fastjson3.annotation.NamingStrategy;
import com.alibaba.fastjson3.util.JDKUtils;

import java.lang.reflect.*;
import java.util.*;

/**
 * Creates {@link ObjectReader} instances for arbitrary POJO classes via reflection.
 */
public final class ObjectReaderCreator {
    private ObjectReaderCreator() {
    }

    public static <T> ObjectReader<T> createObjectReader(Class<T> type) {
        return createObjectReader(type, null);
    }

    public static <T> ObjectReader<T> createObjectReader(Class<T> type, Class<?> mixIn) {
        if (JDKUtils.isRecord(type)) {
            return createRecordReader(type);
        }

        Constructor<T> constructor = resolveConstructor(type, mixIn);
        constructor.setAccessible(true);
        boolean useUnsafeAlloc = JDKUtils.UNSAFE_AVAILABLE;

        FieldReaderCollection collection = collectFieldReaders(type, mixIn);

        return new ReflectionObjectReader<>(type, constructor, collection.fieldReaders,
                collection.fieldReaderMap, collection.matcher, useUnsafeAlloc);
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectReader<T> createRecordReader(Class<T> type) {
        String[] componentNames = JDKUtils.getRecordComponentNames(type);
        Class<?>[] componentTypes = JDKUtils.getRecordComponentTypes(type);
        java.lang.reflect.Type[] genericTypes = JDKUtils.getRecordComponentGenericTypes(type);

        // Find canonical constructor
        Constructor<T> constructor;
        try {
            constructor = type.getDeclaredConstructor(componentTypes);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new JSONException("no canonical constructor found for record " + type.getName(), e);
        }

        // Build FieldReaders from record components
        JSONType jsonType = type.getAnnotation(JSONType.class);
        NamingStrategy naming = jsonType != null ? jsonType.naming() : com.alibaba.fastjson3.annotation.NamingStrategy.NoneStrategy;

        List<FieldReader> fieldReaderList = new ArrayList<>();
        for (int i = 0; i < componentNames.length; i++) {
            String rawName = componentNames[i];
            Field field = null;
            try {
                field = type.getDeclaredField(rawName);
                field.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
            }

            JSONField annotation = field != null ? field.getAnnotation(JSONField.class) : null;
            String jsonName = resolveFieldName(rawName, annotation, naming);
            String[] alternateNames = annotation != null ? annotation.alternateNames() : new String[0];
            int ordinal = annotation != null ? annotation.ordinal() : 0;
            String defaultValue = annotation != null ? annotation.defaultValue() : "";
            boolean required = annotation != null && annotation.required();

            fieldReaderList.add(new FieldReader(
                    jsonName, alternateNames,
                    genericTypes[i], componentTypes[i],
                    ordinal, defaultValue, required,
                    field, null
            ));
        }

        Collections.sort(fieldReaderList);

        FieldReader[] fieldReaders = fieldReaderList.toArray(new FieldReader[0]);
        FieldNameMatcher matcher = FieldNameMatcher.build(fieldReaders);

        Map<String, FieldReader> fieldReaderMap = HashMap.newHashMap(fieldReaders.length * 2);
        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader fr = fieldReaders[i];
            fr.index = i;
            fieldReaderMap.put(fr.fieldName, fr);
            for (String alt : fr.alternateNames) {
                fieldReaderMap.putIfAbsent(alt, fr);
            }
        }

        FieldReaderCollection collection = new FieldReaderCollection(fieldReaders, matcher, fieldReaderMap);

        // Build component index mapping: fieldReader index → constructor param index
        // After sorting, fieldReader order may differ from constructor param order
        int[] paramMapping = new int[fieldReaders.length];
        for (int i = 0; i < fieldReaders.length; i++) {
            String name = fieldReaders[i].fieldName;
            // Find original component index
            for (int j = 0; j < componentNames.length; j++) {
                String resolvedName = resolveFieldName(componentNames[j],
                        null, naming);
                if (resolvedName.equals(name) || componentNames[j].equals(name)) {
                    paramMapping[i] = j;
                    break;
                }
            }
        }

        return new RecordObjectReader<>(type, constructor, collection.fieldReaders,
                collection.fieldReaderMap, collection.matcher, componentTypes.length, paramMapping);
    }

    /**
     * Holds the collected field reader metadata for a given type.
     * Used by both reflection-based and ASM-based ObjectReader creation.
     */
    static final class FieldReaderCollection {
        final FieldReader[] fieldReaders;
        final FieldNameMatcher matcher;
        final Map<String, FieldReader> fieldReaderMap;

        FieldReaderCollection(FieldReader[] fieldReaders, FieldNameMatcher matcher,
                              Map<String, FieldReader> fieldReaderMap) {
            this.fieldReaders = fieldReaders;
            this.matcher = matcher;
            this.fieldReaderMap = fieldReaderMap;
        }
    }

    /**
     * Collect field readers, build matcher, and assign indices for a given type.
     * Package-private so ObjectReaderCreatorASM can reuse this logic.
     */
    static FieldReaderCollection collectFieldReaders(Class<?> type) {
        return collectFieldReaders(type, null);
    }

    static FieldReaderCollection collectFieldReaders(Class<?> type, Class<?> mixIn) {
        JSONType jsonType = type.getAnnotation(JSONType.class);
        NamingStrategy naming = jsonType != null ? jsonType.naming() : NamingStrategy.NoneStrategy;
        Set<String> includes = jsonType != null && jsonType.includes().length > 0
                ? Set.of(jsonType.includes()) : Set.of();
        Set<String> ignores = jsonType != null && jsonType.ignores().length > 0
                ? Set.of(jsonType.ignores()) : Set.of();

        List<FieldReader> fieldReaderList = new ArrayList<>();
        Set<String> processedNames = new HashSet<>();

        // Collect from fields
        for (Field field : getDeclaredFields(type)) {
            JSONField annotation = field.getAnnotation(JSONField.class);
            // Check mixin for field annotation
            if (annotation == null && mixIn != null) {
                annotation = findMixInFieldAnnotation(mixIn, field.getName(), JSONField.class);
            }
            if (annotation != null && !annotation.deserialize()) {
                continue;
            }

            String rawName = field.getName();
            if (ignores.contains(rawName)) {
                continue;
            }
            if (!includes.isEmpty() && !includes.contains(rawName)) {
                continue;
            }

            boolean isPublic = Modifier.isPublic(field.getModifiers());
            boolean hasAnnotation = annotation != null;
            if (!isPublic && !hasAnnotation) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            String jsonName = resolveFieldName(rawName, annotation, naming);
            String[] alternateNames = annotation != null ? annotation.alternateNames() : new String[0];
            int ordinal = annotation != null ? annotation.ordinal() : 0;
            String defaultValue = annotation != null ? annotation.defaultValue() : "";
            boolean required = annotation != null && annotation.required();

            fieldReaderList.add(new FieldReader(
                    jsonName, alternateNames,
                    field.getGenericType(), field.getType(),
                    ordinal, defaultValue, required,
                    field, null
            ));
            processedNames.add(rawName);
        }

        // Collect from setter methods
        for (Method method : type.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }

            JSONField annotation = method.getAnnotation(JSONField.class);
            // Check mixin for setter annotation
            String rawName = extractPropertyName(method);
            if (annotation == null && mixIn != null && rawName != null) {
                annotation = findMixInSetterAnnotation(mixIn, method, rawName, JSONField.class);
            }
            if (annotation != null && !annotation.deserialize()) {
                continue;
            }

            if (rawName == null && annotation == null) {
                continue;
            }
            if (rawName == null) {
                rawName = annotation.name();
                if (rawName.isEmpty()) {
                    continue;
                }
            }

            if (processedNames.contains(rawName)) {
                continue;
            }
            if (ignores.contains(rawName)) {
                continue;
            }
            if (!includes.isEmpty() && !includes.contains(rawName)) {
                continue;
            }

            String jsonName = resolveFieldName(rawName, annotation, naming);
            String[] alternateNames = annotation != null ? annotation.alternateNames() : new String[0];
            int ordinal = annotation != null ? annotation.ordinal() : 0;
            String defaultValue = annotation != null ? annotation.defaultValue() : "";
            boolean required = annotation != null && annotation.required();

            Parameter param = method.getParameters()[0];
            fieldReaderList.add(new FieldReader(
                    jsonName, alternateNames,
                    param.getParameterizedType(), param.getType(),
                    ordinal, defaultValue, required,
                    null, method
            ));
            processedNames.add(rawName);
        }

        Collections.sort(fieldReaderList);

        FieldReader[] fieldReaders = fieldReaderList.toArray(new FieldReader[0]);
        FieldNameMatcher matcher = FieldNameMatcher.build(fieldReaders);

        Map<String, FieldReader> fieldReaderMap = HashMap.newHashMap(fieldReaders.length * 2);
        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader fr = fieldReaders[i];
            fr.index = i;
            fieldReaderMap.put(fr.fieldName, fr);
            for (String alt : fr.alternateNames) {
                fieldReaderMap.putIfAbsent(alt, fr);
            }
        }

        return new FieldReaderCollection(fieldReaders, matcher, fieldReaderMap);
    }

    // ==================== Internal ObjectReader implementation ====================

    private static final class ReflectionObjectReader<T> implements ObjectReader<T> {
        private final Class<T> objectClass;
        private final Constructor<T> constructor;
        private final FieldReader[] fieldReaders;
        private final Map<String, FieldReader> fieldReaderMap;
        private final FieldNameMatcher matcher;
        private final boolean useUnsafeAlloc;

        // Pre-resolved ObjectReaders for POJO and List element types
        // Lazily initialized on first use
        private volatile boolean fieldReadersResolved;
        private ObjectReader<?>[] fieldObjectReaders;
        private ObjectReader<?>[] fieldElementReaders;

        // Pre-encoded field headers for fast matching: '"fieldName":' as bytes
        private byte[][] fieldHeaders;

        ReflectionObjectReader(
                Class<T> objectClass,
                Constructor<T> constructor,
                FieldReader[] fieldReaders,
                Map<String, FieldReader> fieldReaderMap,
                FieldNameMatcher matcher,
                boolean useUnsafeAlloc
        ) {
            this.objectClass = objectClass;
            this.constructor = constructor;
            this.fieldReaders = fieldReaders;
            this.fieldReaderMap = fieldReaderMap;
            this.matcher = matcher;
            this.useUnsafeAlloc = useUnsafeAlloc;
        }

        private void ensureFieldReaders() {
            if (fieldReadersResolved) {
                return;
            }
            ObjectMapper mapper = ObjectMapper.shared();
            int len = fieldReaders.length;
            ObjectReader<?>[] objReaders = new ObjectReader<?>[len];
            ObjectReader<?>[] elemReaders = new ObjectReader<?>[len];
            for (int i = 0; i < len; i++) {
                FieldReader fr = fieldReaders[i];
                Class<?> fc = fr.fieldClass;
                // For POJO fields (not basic types), get ObjectReader
                if (fr.typeTag == FieldReader.TAG_GENERIC) {
                    ObjectReader<?> r = mapper.getObjectReader(fc);
                    if (r != null) {
                        objReaders[i] = r;
                        fr.typeTag = FieldReader.TAG_POJO;
                    }
                }
                // For List fields, get element ObjectReader
                if (fr.elementClass != null && fr.elementClass != String.class) {
                    elemReaders[i] = mapper.getObjectReader(fr.elementClass);
                }
            }
            this.fieldElementReaders = elemReaders;
            this.fieldObjectReaders = objReaders;
            this.fieldReadersResolved = true;
        }

        @Override
        public T readObject(JSONParser parser, Type fieldType, Object fieldName, long features) {
            ensureFieldReaders();

            if (parser instanceof JSONParser.UTF8 utf8) {
                return readObjectUTF8Impl(utf8, features);
            }
            return readObjectGeneric(parser, features);
        }

        @Override
        public T readObjectUTF8(JSONParser.UTF8 utf8, long features) {
            ensureFieldReaders();
            return readObjectUTF8Impl(utf8, features);
        }

        private T readObjectUTF8Impl(JSONParser.UTF8 utf8, long features) {
            int peek = utf8.skipWSAndPeek();
            if (peek == 'n') {
                if (utf8.readNull()) {
                    return null;
                }
            }
            if (peek != '{') {
                throw new JSONException("expected '{' at offset " + utf8.getOffset());
            }
            utf8.advance(1);

            T instance = createInstance(features);
            long fieldSetMask = 0;

            peek = utf8.skipWSAndPeek();
            if (peek == '}') {
                utf8.advance(1);
                applyDefaults(instance, fieldSetMask);
                return instance;
            }

            // Cache field readers in locals
            final FieldNameMatcher m = this.matcher;
            final ObjectReader<?>[] objReaders = this.fieldObjectReaders;
            final ObjectReader<?>[] elemReaders = this.fieldElementReaders;
            final boolean errorOnUnknown = (features & ReadFeature.ErrorOnUnknownProperties.mask) != 0;
            final boolean usePLHV = m.strategy == FieldNameMatcher.STRATEGY_PLHV;

            fieldSetMask = readFieldsLoop(utf8, instance, m, objReaders, elemReaders,
                    errorOnUnknown, usePLHV, features, fieldSetMask);

            applyDefaults(instance, fieldSetMask);
            return instance;
        }

        /**
         * Core field-reading loop, extracted for JIT compilation as a standalone hot method.
         *
         * <p>This method manages {@code off} as a local variable (CPU register) and only
         * syncs to {@code utf8.offset} when calling non-inlined sub-methods (POJO, List, etc.).
         * Field header matching and separator checking are fully inlined to avoid heap
         * access for {@code this.offset} on the hot path.
         *
         * <p>WARNING: 不可将此循环合并回 readObjectUTF8Impl。
         * 合并后方法体过大，JIT 无法内联子方法，实测导致性能大幅下降。
         */
        private long readFieldsLoop(JSONParser.UTF8 utf8, Object instance,
                                    FieldNameMatcher m, ObjectReader<?>[] objReaders,
                                    ObjectReader<?>[] elemReaders, boolean errorOnUnknown,
                                    boolean usePLHV, long features, long fieldSetMask) {
            final FieldReader[] frs = this.fieldReaders;
            final int frLen = frs.length;
            final byte[] b = utf8.getBytes();
            final int end = utf8.getEnd();
            int off = utf8.getOffset();
            int nextExpected = 0;

            for (;;) {
                FieldReader reader = null;

                // Fast path: ordered field speculation — inline header matching
                if (nextExpected < frLen) {
                    FieldReader candidate = frs[nextExpected];
                    byte[] hdr = candidate.fieldNameHeader;
                    // Skip whitespace
                    while (b[off] <= ' ') {
                        off++;
                    }
                    if (b[off] == '"') {
                        int hdrLen = hdr.length;
                        boolean match = true;
                        for (int i = 1; i < hdrLen; i++) {
                            if (b[off + i] != hdr[i]) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            off += hdrLen;
                            while (b[off] <= ' ') {
                                off++;
                            }
                            reader = candidate;
                            nextExpected++;
                        }
                    }
                }

                // Slow path: hash-based matching (sync offset for method calls)
                if (reader == null) {
                    utf8.setOffset(off);
                    long hash = usePLHV ? utf8.readFieldNameHashPLHV() : utf8.readFieldNameHash(m);
                    off = utf8.getOffset();
                    reader = m.matchFlat(hash);
                    if (reader != null) {
                        nextExpected = reader.index + 1;
                    } else {
                        nextExpected = 0;
                    }
                }

                if (reader != null) {
                    int fi = reader.index;
                    int tt = reader.typeTag;
                    // Inline primitive types — avoid heap sync (setOffset/getOffset)
                    if (tt == FieldReader.TAG_STRING && b[off] == '"') {
                        off = utf8.readStringOff(off, instance, reader);
                    } else if (tt == FieldReader.TAG_LONG) {
                        off = utf8.readLongOff(off, instance, reader);
                    } else if (tt == FieldReader.TAG_INT) {
                        off = utf8.readIntOff(off, instance, reader);
                    } else if (tt == FieldReader.TAG_DOUBLE) {
                        off = utf8.readDoubleOff(off, instance, reader);
                    } else if (tt == FieldReader.TAG_BOOLEAN) {
                        off = utf8.readBooleanOff(off, instance, reader);
                    } else {
                        // Sync offset for complex types (POJO, LIST, ARRAY, etc.)
                        utf8.setOffset(off);
                        readAndSetFieldUTF8Inline(utf8, instance, reader, fi, features, objReaders, elemReaders);
                        off = utf8.getOffset();
                    }
                    if (fi >= 0 && fi < 64) {
                        fieldSetMask |= (1L << fi);
                    }
                } else {
                    if (errorOnUnknown) {
                        throw new JSONException("unknown property in " + objectClass.getName());
                    }
                    utf8.setOffset(off);
                    utf8.skipValue();
                    off = utf8.getOffset();
                }

                // Inline separator check — avoid readFieldSeparator heap access
                while (b[off] <= ' ') {
                    off++;
                }
                if (b[off] == ',') {
                    off++;
                    continue;
                }
                if (b[off] == '}') {
                    off++;
                    break;
                }
                throw new JSONException("expected ',' or '}' in " + objectClass.getName());
            }
            utf8.setOffset(off);
            return fieldSetMask;
        }

        private T readObjectGeneric(JSONParser parser, long features) {
            parser.skipWS();
            if (parser.readNull()) {
                return null;
            }
            if (parser.charAt(parser.getOffset()) != '{') {
                throw new JSONException("expected '{' at offset " + parser.getOffset());
            }
            parser.advance(1);

            T instance = createInstance(features);
            boolean errorOnUnknown = (features & ReadFeature.ErrorOnUnknownProperties.mask) != 0;
            long fieldSetMask = 0;

            parser.skipWS();
            if (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) == '}') {
                parser.advance(1);
                applyDefaults(instance, fieldSetMask);
                return instance;
            }

            for (;;) {
                long hash = parser.readFieldNameHash(matcher);
                FieldReader reader = matcher.match(hash);

                if (reader != null) {
                    int fi = reader.index;
                    readAndSetFieldGeneric(parser, instance, reader, features);
                    if (fi >= 0 && fi < 64) {
                        fieldSetMask |= (1L << fi);
                    }
                } else {
                    if (errorOnUnknown) {
                        throw new JSONException("unknown property in " + objectClass.getName());
                    }
                    parser.skipValue();
                }

                parser.skipWS();
                int off = parser.getOffset();
                if (off >= parser.getEnd()) {
                    throw new JSONException("unterminated object");
                }
                int c = parser.charAt(off);
                if (c == ',') {
                    parser.advance(1);
                    continue;
                }
                if (c == '}') {
                    parser.advance(1);
                    break;
                }
                throw new JSONException("expected ',' or '}' at offset " + off);
            }

            applyDefaults(instance, fieldSetMask);
            return instance;
        }

        /**
         * UTF-8 fast path with cached reader arrays passed as parameters.
         */
        /**
         * Handle non-STRING/non-LONG field types.
         * readFieldNameHash already skips WS after ':', so peekByte() is used instead of skipWSAndPeek().
         */
        private void readAndSetFieldUTF8Inline(JSONParser.UTF8 utf8, Object instance,
                                                FieldReader reader, int fieldIndex, long features,
                                                ObjectReader<?>[] objReaders, ObjectReader<?>[] elemReaders) {
            int peek = utf8.peekByte();

            switch (reader.typeTag) {
                case FieldReader.TAG_STRING -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    reader.setObjectValue(instance, utf8.readStringDirect());
                }
                case FieldReader.TAG_INT -> reader.setIntValue(instance, utf8.readIntDirect());
                case FieldReader.TAG_LONG -> reader.setLongValue(instance, utf8.readLongDirect());
                case FieldReader.TAG_DOUBLE -> reader.setDoubleValue(instance, utf8.readDoubleDirect());
                case FieldReader.TAG_BOOLEAN -> reader.setBooleanValue(instance, utf8.readBooleanDirect());
                case FieldReader.TAG_FLOAT -> {
                    float v = (float) utf8.readDoubleDirect();
                    if (reader.fieldOffset >= 0) {
                        JDKUtils.putFloat(instance, reader.fieldOffset, v);
                    } else {
                        reader.setFieldValue(instance, v);
                    }
                }
                case FieldReader.TAG_LIST -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    reader.setObjectValue(instance, readListUTF8Inline(utf8, reader, fieldIndex, features, elemReaders));
                }
                case FieldReader.TAG_POJO -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    ObjectReader<?> r = objReaders[fieldIndex];
                    reader.setObjectValue(instance, r.readObjectUTF8(utf8, features));
                }
                case FieldReader.TAG_STRING_ARRAY -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    reader.setObjectValue(instance, utf8.readStringArrayInline());
                }
                case FieldReader.TAG_LONG_ARRAY -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    reader.setObjectValue(instance, utf8.readLongArrayInline());
                }
                default -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    readAndSetFieldGenericValue(utf8, instance, reader, features);
                }
            }
        }

        // WARNING: 此方法虽然当前未被调用，但不可删除。
        // 删除会改变内部类的方法数量/结构，导致 JIT 编译决策变化，
        // 实测删除后性能从 718K 降至 651K 且方差大幅增加。
        private void readAndSetFieldUTF8(JSONParser.UTF8 utf8, Object instance, FieldReader reader, int fieldIndex, long features) {
            readAndSetFieldUTF8Inline(utf8, instance, reader, fieldIndex, features, fieldObjectReaders, fieldElementReaders);
        }

        private void readAndSetFieldGeneric(JSONParser parser, Object instance, FieldReader reader, long features) {
            parser.skipWS();
            if (parser.charAt(parser.getOffset()) == 'n') {
                if (parser.readNull()) {
                    return;
                }
            }
            readAndSetFieldGenericValue(parser, instance, reader, features);
        }

        private void readAndSetFieldGenericValue(JSONParser parser, Object instance, FieldReader reader, long features) {
            switch (reader.typeTag) {
                case FieldReader.TAG_STRING -> reader.setFieldValue(instance, parser.readString());
                case FieldReader.TAG_INT -> reader.setIntValue(instance, parser.readInt());
                case FieldReader.TAG_LONG -> reader.setLongValue(instance, parser.readLong());
                case FieldReader.TAG_DOUBLE -> reader.setDoubleValue(instance, parser.readDouble());
                case FieldReader.TAG_BOOLEAN -> reader.setBooleanValue(instance, parser.readBoolean());
                case FieldReader.TAG_INT_OBJ -> reader.setFieldValue(instance, parser.readInt());
                case FieldReader.TAG_LONG_OBJ -> reader.setFieldValue(instance, parser.readLong());
                case FieldReader.TAG_DOUBLE_OBJ -> reader.setFieldValue(instance, parser.readDouble());
                case FieldReader.TAG_BOOLEAN_OBJ -> reader.setFieldValue(instance, parser.readBoolean());
                default -> {
                    Object value = parser.readAny();
                    reader.setFieldValue(instance, reader.convertValue(value));
                }
            }
        }

        private Object readListUTF8Inline(JSONParser.UTF8 utf8, FieldReader reader, int fieldIndex, long features, ObjectReader<?>[] elemReaders) {
            Class<?> elemClass = reader.elementClass;
            // Fast path: use inline offset-managed readers for String lists
            if (elemClass == String.class) {
                return utf8.readStringListInline();
            }

            int peek = utf8.skipWSAndPeek();
            if (peek != '[') {
                throw new JSONException("expected '[' at offset " + utf8.getOffset());
            }
            utf8.advance(1);

            peek = utf8.skipWSAndPeek();
            if (peek == ']') {
                utf8.advance(1);
                return new ArrayList<>(0);
            }

            ObjectReader<?> elemReader = (fieldIndex >= 0 && elemReaders != null)
                    ? elemReaders[fieldIndex] : null;

            if (elemReader != null) {
                return readListPojoUTF8(utf8, elemReader, elemClass, features);
            }
            return readListGenericUTF8(utf8);
        }

        // WARNING: 此方法虽然当前未被调用，但不可删除。原因同 readAndSetFieldUTF8。
        private Object readListUTF8(JSONParser.UTF8 utf8, FieldReader reader, int fieldIndex, long features) {
            int peek = utf8.skipWSAndPeek();
            if (peek != '[') {
                throw new JSONException("expected '[' at offset " + utf8.getOffset());
            }
            utf8.advance(1);

            peek = utf8.skipWSAndPeek();
            if (peek == ']') {
                utf8.advance(1);
                return new ArrayList<>(0);
            }

            Class<?> elemClass = reader.elementClass;
            ObjectReader<?> elemReader = (fieldIndex >= 0 && fieldElementReaders != null)
                    ? fieldElementReaders[fieldIndex] : null;

            if (elemClass == String.class) {
                return readListStringUTF8(utf8);
            }
            if (elemReader != null) {
                return readListPojoUTF8(utf8, elemReader, elemClass, features);
            }
            return readListGenericUTF8(utf8);
        }

        private ArrayList<Object> readListStringUTF8(JSONParser.UTF8 utf8) {
            ArrayList<Object> list = new ArrayList<>(16);
            for (;;) {
                int peek = utf8.skipWSAndPeek();
                if (peek == 'n' && utf8.readNull()) {
                    list.add(null);
                } else {
                    list.add(utf8.readStringDirect());
                }
                int sep = utf8.readArraySeparator();
                if (sep == 0) {
                    continue;
                }
                if (sep == 1) {
                    return list;
                }
                throw new JSONException("expected ',' or ']'");
            }
        }

        private ArrayList<Object> readListPojoUTF8(JSONParser.UTF8 utf8, ObjectReader<?> elemReader, Class<?> elemClass, long features) {
            ArrayList<Object> list = new ArrayList<>(16);
            for (;;) {
                int peek = utf8.skipWSAndPeek();
                if (peek == 'n' && utf8.readNull()) {
                    list.add(null);
                } else {
                    list.add(elemReader.readObjectUTF8(utf8, features));
                }
                int sep = utf8.readArraySeparator();
                if (sep == 0) {
                    continue;
                }
                if (sep == 1) {
                    return list;
                }
                throw new JSONException("expected ',' or ']'");
            }
        }

        private ArrayList<Object> readListGenericUTF8(JSONParser.UTF8 utf8) {
            ArrayList<Object> list = new ArrayList<>();
            for (;;) {
                list.add(utf8.readAny());
                int sep = utf8.readArraySeparator();
                if (sep == 0) {
                    continue;
                }
                if (sep == 1) {
                    return list;
                }
                throw new JSONException("expected ',' or ']'");
            }
        }

        private static String[] readStringArrayUTF8(JSONParser.UTF8 utf8) {
            int peek = utf8.skipWSAndPeek();
            if (peek != '[') {
                throw new JSONException("expected '[' at offset " + utf8.getOffset());
            }
            utf8.advance(1);
            peek = utf8.skipWSAndPeek();
            if (peek == ']') {
                utf8.advance(1);
                return new String[0];
            }
            String[] arr = new String[8];
            int size = 0;
            for (;;) {
                if (size == arr.length) {
                    arr = java.util.Arrays.copyOf(arr, size + (size >> 1));
                }
                peek = utf8.skipWSAndPeek();
                if (peek == 'n' && utf8.readNull()) {
                    arr[size++] = null;
                } else {
                    arr[size++] = utf8.readStringDirect();
                }
                int sep = utf8.readArraySeparator();
                if (sep == 1) {
                    break;
                }
                if (sep != 0) {
                    throw new JSONException("expected ',' or ']'");
                }
            }
            return size == arr.length ? arr : java.util.Arrays.copyOf(arr, size);
        }

        private static long[] readLongArrayUTF8(JSONParser.UTF8 utf8) {
            int peek = utf8.skipWSAndPeek();
            if (peek != '[') {
                throw new JSONException("expected '[' at offset " + utf8.getOffset());
            }
            utf8.advance(1);
            peek = utf8.skipWSAndPeek();
            if (peek == ']') {
                utf8.advance(1);
                return new long[0];
            }
            long[] arr = new long[2];
            int size = 0;
            for (;;) {
                if (size == arr.length) {
                    arr = java.util.Arrays.copyOf(arr, size + (size >> 1));
                }
                utf8.skipWSAndPeek();
                arr[size++] = utf8.readLongDirect();
                int sep = utf8.readArraySeparator();
                if (sep == 1) {
                    break;
                }
                if (sep != 0) {
                    throw new JSONException("expected ',' or ']'");
                }
            }
            return size == arr.length ? arr : java.util.Arrays.copyOf(arr, size);
        }

        private Object readList(JSONParser parser, FieldReader reader, int fieldIndex, long features) {
            parser.skipWS();
            if (parser.charAt(parser.getOffset()) != '[') {
                throw new JSONException("expected '[' at offset " + parser.getOffset());
            }
            parser.advance(1);

            parser.skipWS();
            if (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) == ']') {
                parser.advance(1);
                return new ArrayList<>(0);
            }

            Class<?> elemClass = reader.elementClass;
            ObjectReader<?> elemReader = (fieldIndex >= 0 && fieldElementReaders != null)
                    ? fieldElementReaders[fieldIndex] : null;

            if (elemClass == String.class) {
                return readListString(parser);
            }
            if (elemReader != null) {
                return readListPojo(parser, elemReader, elemClass, features);
            }
            return readListGeneric(parser);
        }

        private ArrayList<Object> readListString(JSONParser parser) {
            ArrayList<Object> list = new ArrayList<>();
            for (;;) {
                parser.skipWS();
                if (parser.charAt(parser.getOffset()) == 'n' && parser.readNull()) {
                    list.add(null);
                } else {
                    list.add(parser.readString());
                }
                parser.skipWS();
                int c = parser.charAt(parser.getOffset());
                if (c == ',') {
                    parser.advance(1);
                    continue;
                }
                if (c == ']') {
                    parser.advance(1);
                    return list;
                }
                throw new JSONException("expected ',' or ']' at offset " + parser.getOffset());
            }
        }

        private ArrayList<Object> readListPojo(JSONParser parser, ObjectReader<?> elemReader, Class<?> elemClass, long features) {
            ArrayList<Object> list = new ArrayList<>();
            for (;;) {
                parser.skipWS();
                if (parser.charAt(parser.getOffset()) == 'n' && parser.readNull()) {
                    list.add(null);
                } else {
                    list.add(elemReader.readObject(parser, elemClass, null, features));
                }
                parser.skipWS();
                int c = parser.charAt(parser.getOffset());
                if (c == ',') {
                    parser.advance(1);
                    continue;
                }
                if (c == ']') {
                    parser.advance(1);
                    return list;
                }
                throw new JSONException("expected ',' or ']' at offset " + parser.getOffset());
            }
        }

        private ArrayList<Object> readListGeneric(JSONParser parser) {
            ArrayList<Object> list = new ArrayList<>();
            for (;;) {
                list.add(parser.readAny());
                parser.skipWS();
                int c = parser.charAt(parser.getOffset());
                if (c == ',') {
                    parser.advance(1);
                    continue;
                }
                if (c == ']') {
                    parser.advance(1);
                    return list;
                }
                throw new JSONException("expected ',' or ']' at offset " + parser.getOffset());
            }
        }

        private void applyDefaults(T instance, long fieldSetMask) {
            for (int i = 0; i < fieldReaders.length; i++) {
                if (i < 64 && (fieldSetMask & (1L << i)) != 0) {
                    continue; // field was set
                }
                FieldReader fr = fieldReaders[i];
                if (fr.required) {
                    throw new JSONException(
                            "required field '" + fr.fieldName + "' is missing in " + objectClass.getName()
                    );
                }
                if (fr.defaultValue != null && !fr.defaultValue.isEmpty()) {
                    Object defaultVal = parseDefault(fr.defaultValue, fr.fieldClass);
                    fr.setFieldValue(instance, defaultVal);
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public T createInstance(long features) {
            if (useUnsafeAlloc) {
                return (T) com.alibaba.fastjson3.util.UnsafeAllocator.allocateInstanceUnchecked(objectClass);
            }
            try {
                return constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new JSONException(
                        "cannot create instance of " + objectClass.getName() + ": " + e.getMessage(), e
                );
            }
        }

        @Override
        public void readFieldUTF8(JSONParser.UTF8 utf8, Object instance, int fieldIndex, long features) {
            ensureFieldReaders();
            FieldReader reader = fieldReaders[fieldIndex];
            readAndSetFieldUTF8Inline(utf8, instance, reader, fieldIndex, features, fieldObjectReaders, fieldElementReaders);
        }

        @Override
        public Class<T> getObjectClass() {
            return objectClass;
        }

        private static Object parseDefault(String defaultValue, Class<?> type) {
            if (type == String.class) {
                return defaultValue;
            }
            if (type == int.class || type == Integer.class) {
                return Integer.parseInt(defaultValue);
            }
            if (type == long.class || type == Long.class) {
                return Long.parseLong(defaultValue);
            }
            if (type == double.class || type == Double.class) {
                return Double.parseDouble(defaultValue);
            }
            if (type == float.class || type == Float.class) {
                return Float.parseFloat(defaultValue);
            }
            if (type == boolean.class || type == Boolean.class) {
                return Boolean.parseBoolean(defaultValue);
            }
            return defaultValue;
        }
    }

    // ==================== Record ObjectReader ====================

    /**
     * ObjectReader for Java Record types. Records require all component values
     * to be provided to the canonical constructor — individual field setting is not possible.
     * This reader uses Unsafe to allocate the instance and set fields directly,
     * then validates that all required fields were provided.
     */
    private static final class RecordObjectReader<T> implements ObjectReader<T> {
        private final Class<T> objectClass;
        private final Constructor<T> constructor;
        private final FieldReader[] fieldReaders;
        private final Map<String, FieldReader> fieldReaderMap;
        private final FieldNameMatcher matcher;
        private final int componentCount;
        private final int[] paramMapping; // fieldReader index → constructor param index

        private volatile boolean fieldReadersResolved;
        private ObjectReader<?>[] fieldObjectReaders;
        private ObjectReader<?>[] fieldElementReaders;

        RecordObjectReader(
                Class<T> objectClass,
                Constructor<T> constructor,
                FieldReader[] fieldReaders,
                Map<String, FieldReader> fieldReaderMap,
                FieldNameMatcher matcher,
                int componentCount,
                int[] paramMapping
        ) {
            this.objectClass = objectClass;
            this.constructor = constructor;
            this.fieldReaders = fieldReaders;
            this.fieldReaderMap = fieldReaderMap;
            this.matcher = matcher;
            this.componentCount = componentCount;
            this.paramMapping = paramMapping;
        }

        private void ensureFieldReaders() {
            if (fieldReadersResolved) {
                return;
            }
            ObjectMapper mapper = ObjectMapper.shared();
            int len = fieldReaders.length;
            ObjectReader<?>[] objReaders = new ObjectReader<?>[len];
            ObjectReader<?>[] elemReaders = new ObjectReader<?>[len];
            for (int i = 0; i < len; i++) {
                FieldReader fr = fieldReaders[i];
                if (fr.typeTag == FieldReader.TAG_GENERIC) {
                    ObjectReader<?> r = mapper.getObjectReader(fr.fieldClass);
                    if (r != null) {
                        objReaders[i] = r;
                        fr.typeTag = FieldReader.TAG_POJO;
                    }
                }
                if (fr.elementClass != null && fr.elementClass != String.class) {
                    elemReaders[i] = mapper.getObjectReader(fr.elementClass);
                }
            }
            this.fieldElementReaders = elemReaders;
            this.fieldObjectReaders = objReaders;
            this.fieldReadersResolved = true;
        }

        @Override
        public T readObject(JSONParser parser, java.lang.reflect.Type fieldType, Object fieldName, long features) {
            ensureFieldReaders();
            // Handle null
            parser.skipWS();
            if (parser.readNull()) {
                return null;
            }
            if (parser instanceof JSONParser.UTF8 utf8) {
                return readRecordUTF8(utf8, features);
            }
            return readRecordGeneric(parser, features);
        }

        @Override
        public T readObjectUTF8(JSONParser.UTF8 utf8, long features) {
            ensureFieldReaders();
            return readRecordUTF8(utf8, features);
        }

        @SuppressWarnings("unchecked")
        private T readRecordUTF8(JSONParser.UTF8 utf8, long features) {
            int peek = utf8.skipWSAndPeek();
            if (peek == 'n' && utf8.readNull()) {
                return null;
            }
            if (peek != '{') {
                throw new JSONException("expected '{' at offset " + utf8.getOffset());
            }
            utf8.advance(1);

            peek = utf8.skipWSAndPeek();
            if (peek == '}') {
                utf8.advance(1);
                return constructRecord(new Object[componentCount]);
            }

            // Collect values into array, then call canonical constructor
            Object[] values = new Object[componentCount];
            final FieldNameMatcher m = this.matcher;
            final boolean errorOnUnknown = (features & ReadFeature.ErrorOnUnknownProperties.mask) != 0;
            final boolean usePLHV = m.strategy == FieldNameMatcher.STRATEGY_PLHV;

            final byte[] b = utf8.getBytes();
            int off = utf8.getOffset();
            int nextExpected = 0;
            final int frLen = fieldReaders.length;

            for (;;) {
                FieldReader reader = null;

                // Ordered field speculation
                if (nextExpected < frLen) {
                    FieldReader candidate = fieldReaders[nextExpected];
                    byte[] hdr = candidate.fieldNameHeader;
                    while (b[off] <= ' ') {
                        off++;
                    }
                    if (b[off] == '"') {
                        int hdrLen = hdr.length;
                        boolean match = true;
                        for (int i = 1; i < hdrLen; i++) {
                            if (b[off + i] != hdr[i]) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            off += hdrLen;
                            while (b[off] <= ' ') {
                                off++;
                            }
                            reader = candidate;
                            nextExpected++;
                        }
                    }
                }

                if (reader == null) {
                    utf8.setOffset(off);
                    long hash = usePLHV ? utf8.readFieldNameHashPLHV() : utf8.readFieldNameHash(m);
                    off = utf8.getOffset();
                    reader = m.matchFlat(hash);
                    if (reader != null) {
                        nextExpected = reader.index + 1;
                    } else {
                        nextExpected = 0;
                    }
                }

                if (reader != null) {
                    // Read value and store in values array at the correct constructor param index
                    utf8.setOffset(off);
                    Object value = utf8.readAny();
                    off = utf8.getOffset();
                    int paramIdx = paramMapping[reader.index];
                    values[paramIdx] = reader.convertValue(value);
                } else {
                    if (errorOnUnknown) {
                        throw new JSONException("unknown property in " + objectClass.getName());
                    }
                    utf8.setOffset(off);
                    utf8.skipValue();
                    off = utf8.getOffset();
                }

                while (b[off] <= ' ') {
                    off++;
                }
                if (b[off] == ',') {
                    off++;
                    continue;
                }
                if (b[off] == '}') {
                    off++;
                    break;
                }
                throw new JSONException("expected ',' or '}' in " + objectClass.getName());
            }
            utf8.setOffset(off);
            return constructRecord(values);
        }

        @SuppressWarnings("unchecked")
        private T readRecordGeneric(JSONParser parser, long features) {
            Object obj = parser.readObject();
            if (obj == null) {
                return null;
            }
            if (obj instanceof Map<?, ?> map) {
                Object[] values = new Object[componentCount];
                for (FieldReader fr : fieldReaders) {
                    Object value = map.get(fr.fieldName);
                    if (value != null) {
                        values[paramMapping[fr.index]] = fr.convertValue(value);
                    }
                }
                return constructRecord(values);
            }
            throw new JSONException("expected object for " + objectClass.getName());
        }

        @SuppressWarnings("unchecked")
        private T constructRecord(Object[] values) {
            try {
                return constructor.newInstance(values);
            } catch (Exception e) {
                throw new JSONException(
                        "cannot construct record " + objectClass.getName() + ": " + e.getMessage(), e
                );
            }
        }

        @Override
        public Class<T> getObjectClass() {
            return objectClass;
        }

        @Override
        public T createInstance(long features) {
            throw new UnsupportedOperationException("Record instances require all component values");
        }

        @Override
        public void readFieldUTF8(JSONParser.UTF8 utf8, Object instance, int fieldIndex, long features) {
            ensureFieldReaders();
            FieldReader reader = fieldReaders[fieldIndex];
            Object value = utf8.readAny();
            reader.setFieldValue(instance, reader.convertValue(value));
        }
    }

    // ==================== Helper methods ====================

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> resolveConstructor(Class<T> type, Class<?> mixIn) {
        // Check target class constructors
        for (Constructor<?> ctor : type.getDeclaredConstructors()) {
            if (ctor.isAnnotationPresent(JSONCreator.class)) {
                if (ctor.getParameterCount() == 0) {
                    return (Constructor<T>) ctor;
                }
                throw new JSONException(
                        "@JSONCreator with parameters is not yet supported: " + type.getName()
                );
            }
        }
        // Check mixin constructors for @JSONCreator (match by parameter types)
        if (mixIn != null) {
            for (Constructor<?> mixCtor : mixIn.getDeclaredConstructors()) {
                if (mixCtor.isAnnotationPresent(JSONCreator.class)) {
                    if (mixCtor.getParameterCount() == 0) {
                        // Find matching no-arg constructor on target
                        try {
                            return type.getDeclaredConstructor();
                        } catch (NoSuchMethodException ignored) {
                        }
                    }
                }
            }
        }
        try {
            return type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new JSONException("no default constructor found for " + type.getName(), e);
        }
    }

    private static List<Field> getDeclaredFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            Collections.addAll(fields, current.getDeclaredFields());
            current = current.getSuperclass();
        }
        return fields;
    }

    private static String extractPropertyName(Method method) {
        String name = method.getName();
        if (name.length() > 3 && name.startsWith("set") && Character.isUpperCase(name.charAt(3))) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        return null;
    }

    private static String resolveFieldName(String rawName, JSONField annotation, NamingStrategy naming) {
        if (annotation != null && !annotation.name().isEmpty()) {
            return annotation.name();
        }
        return applyNamingStrategy(rawName, naming);
    }

    static String applyNamingStrategy(String name, NamingStrategy strategy) {
        return switch (strategy) {
            case NoneStrategy, CamelCase -> name;
            case PascalCase -> Character.toUpperCase(name.charAt(0)) + name.substring(1);
            case SnakeCase -> camelToSeparator(name, '_', false);
            case UpperSnakeCase -> camelToSeparator(name, '_', true);
            case KebabCase -> camelToSeparator(name, '-', false);
            case UpperKebabCase -> camelToSeparator(name, '-', true);
        };
    }

    private static String camelToSeparator(String name, char separator, boolean uppercase) {
        StringBuilder sb = new StringBuilder(name.length() + 4);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append(separator);
                }
                sb.append(uppercase ? c : Character.toLowerCase(c));
            } else {
                sb.append(uppercase ? Character.toUpperCase(c) : c);
            }
        }
        return sb.toString();
    }

    // ==================== Mixin support ====================

    /**
     * Find an annotation on the corresponding field in the mixin class.
     */
    static <A extends java.lang.annotation.Annotation> A findMixInFieldAnnotation(
            Class<?> mixIn, String fieldName, Class<A> annotationType) {
        try {
            Field f = mixIn.getDeclaredField(fieldName);
            return f.getAnnotation(annotationType);
        } catch (NoSuchFieldException ignored) {
        }
        return null;
    }

    /**
     * Find annotation on the corresponding setter method in the mixin class.
     */
    static <A extends java.lang.annotation.Annotation> A findMixInSetterAnnotation(
            Class<?> mixIn, Method targetMethod, String propertyName, Class<A> annotationType) {
        // Try matching by exact method signature
        for (Method m : mixIn.getDeclaredMethods()) {
            if (m.getName().equals(targetMethod.getName())
                    && m.getParameterCount() == targetMethod.getParameterCount()
                    && java.util.Arrays.equals(m.getParameterTypes(), targetMethod.getParameterTypes())) {
                A ann = m.getAnnotation(annotationType);
                if (ann != null) {
                    return ann;
                }
            }
        }
        // Try matching by setter naming convention
        String setterName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        for (Method m : mixIn.getDeclaredMethods()) {
            if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                A ann = m.getAnnotation(annotationType);
                if (ann != null) {
                    return ann;
                }
            }
        }
        return null;
    }
}
