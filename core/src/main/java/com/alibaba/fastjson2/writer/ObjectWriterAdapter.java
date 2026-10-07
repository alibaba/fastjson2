package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.alibaba.fastjson2.JSONB.Constants.BC_TYPED_ANY;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;

public class ObjectWriterAdapter<T>
        implements ObjectWriter<T> {
    boolean hasFilter;
    PropertyPreFilter propertyPreFilter;
    PropertyFilter propertyFilter;
    NameFilter nameFilter;
    ValueFilter valueFilter;

    /**
     * Variants linked to this writer (for example the serving of {@link JSONWriter.Feature#SortFieldNamesAlphabetically}
     * for the same type): filters set on this writer are applied to all of them. Held weakly, so replaced or
     * unregistered variants leave neither retention nor traversal cost behind; cleared entries are compacted
     * away on the next link. The array is replaced under a lock, so reads never see a variant before its
     * filters were copied.
     */
    volatile WeakReference<ObjectWriterAdapter>[] linkedVariants = EMPTY_VARIANTS;

    @SuppressWarnings("unchecked")
    static final WeakReference<ObjectWriterAdapter>[] EMPTY_VARIANTS = new WeakReference[0];

    /**
     * Guards the linked-variants array and the filter updates forwarded to it: filter copying and
     * joining the list are atomic with respect to setters on this writer.
     */
    private final Object linkedVariantsLock = new Object();

    /**
     * Caller features forwarded to nested tree conversions in {@link #toJSONObject(Object, long)}: the ones that
     * select the writer variant or that the conversion applies itself. Value-format features are not applied to
     * the tree at any depth.
     */
    static final long TREE_FEATURES = SortFieldNamesAlphabetically.mask | FieldBased.mask
            | WriteNulls.mask | WriteEnumsUsingName.mask;

    static final String TYPE = "@type";

    final Class objectClass;
    final List<FieldWriter> fieldWriters;
    protected final FieldWriter[] fieldWriterArray;

    final String typeKey;
    byte[] typeKeyJSONB;
    protected final String typeName;
    protected final long typeNameHash;
    protected long typeNameSymbolCache;
    protected final byte[] typeNameJSONB;

    byte[] nameWithColonUTF8;
    char[] nameWithColonUTF16;

    final long features;

    final long[] hashCodes;
    final short[] mapping;

    final boolean hasValueField;
    final boolean serializable;
    final boolean containsNoneFieldGetter;
    final boolean googleCollection;

    public ObjectWriterAdapter(Class<T> objectClass, List<FieldWriter> fieldWriters) {
        this(objectClass, null, null, 0, fieldWriters);
    }

    public ObjectWriterAdapter(
            Class<T> objectClass,
            String typeKey,
            String typeName,
            long features,
            List<FieldWriter> fieldWriters
    ) {
        if (typeName == null && objectClass != null) {
            if (Enum.class.isAssignableFrom(objectClass) && !objectClass.isEnum()) {
                typeName = objectClass.getSuperclass().getName();
            } else {
                typeName = TypeUtils.getTypeName(objectClass);
            }
        }

        this.objectClass = objectClass;
        this.typeKey = typeKey == null || typeKey.isEmpty() ? TYPE : typeKey;
        this.typeName = typeName;
        this.typeNameHash = typeName != null ? Fnv.hashCode64(typeName) : 0;
        this.typeNameJSONB = JSONB.toBytes(typeName);
        this.features = features;
        this.fieldWriters = fieldWriters;
        this.serializable = objectClass == null || java.io.Serializable.class.isAssignableFrom(objectClass);
        this.googleCollection =
                "com.google.common.collect.AbstractMapBasedMultimap$RandomAccessWrappedList".equals(typeName)
                || "com.google.common.collect.AbstractMapBasedMultimap$WrappedSet".equals(typeName);

        this.fieldWriterArray = new FieldWriter[fieldWriters.size()];
        fieldWriters.toArray(fieldWriterArray);

        this.hasValueField = fieldWriterArray.length == 1 && (fieldWriterArray[0].features & FieldInfo.VALUE_MASK) != 0;

        boolean containsNoneFieldGetter = false;
        long[] hashCodes = new long[fieldWriterArray.length];
        for (int i = 0; i < fieldWriterArray.length; i++) {
            FieldWriter fieldWriter = fieldWriterArray[i];
            long hashCode = Fnv.hashCode64(fieldWriter.fieldName);
            hashCodes[i] = hashCode;

            if (fieldWriter.method != null && (fieldWriter.features & FieldInfo.FIELD_MASK) == 0) {
                containsNoneFieldGetter = true;
            }
        }
        this.containsNoneFieldGetter = containsNoneFieldGetter;

        this.hashCodes = Arrays.copyOf(hashCodes, hashCodes.length);
        Arrays.sort(this.hashCodes);

        mapping = new short[this.hashCodes.length];
        for (int i = 0; i < hashCodes.length; i++) {
            long hashCode = hashCodes[i];
            int index = Arrays.binarySearch(this.hashCodes, hashCode);
            mapping[index] = (short) i;
        }
    }

    @Override
    public long getFeatures() {
        return features;
    }

    @Override
    public FieldWriter getFieldWriter(long hashCode) {
        int m = Arrays.binarySearch(hashCodes, hashCode);
        if (m < 0) {
            return null;
        }

        int index = this.mapping[m];
        return fieldWriterArray[index];
    }

    @Override
    public final boolean hasFilter(JSONWriter jsonWriter) {
        return hasFilter | jsonWriter.hasFilter(containsNoneFieldGetter);
    }

    protected final boolean hasFilter0(JSONWriter jsonWriter) {
        return hasFilter | jsonWriter.hasFilter();
    }

    public void setPropertyFilter(PropertyFilter propertyFilter) {
        synchronized (linkedVariantsLock) {
            this.propertyFilter = propertyFilter;
            if (propertyFilter != null) {
                hasFilter = true;
            }
            for (WeakReference<ObjectWriterAdapter> linked : this.linkedVariants) {
                ObjectWriterAdapter variant = linked.get();
                if (variant != null) {
                    variant.setPropertyFilter(propertyFilter);
                }
            }
        }
    }

    public void setValueFilter(ValueFilter valueFilter) {
        synchronized (linkedVariantsLock) {
            this.valueFilter = valueFilter;
            if (valueFilter != null) {
                hasFilter = true;
            }
            for (WeakReference<ObjectWriterAdapter> linked : this.linkedVariants) {
                ObjectWriterAdapter variant = linked.get();
                if (variant != null) {
                    variant.setValueFilter(valueFilter);
                }
            }
        }
    }

    public void setNameFilter(NameFilter nameFilter) {
        synchronized (linkedVariantsLock) {
            this.nameFilter = nameFilter;
            if (nameFilter != null) {
                hasFilter = true;
            }
            for (WeakReference<ObjectWriterAdapter> linked : this.linkedVariants) {
                ObjectWriterAdapter variant = linked.get();
                if (variant != null) {
                    variant.setNameFilter(nameFilter);
                }
            }
        }
    }

    public void setPropertyPreFilter(PropertyPreFilter propertyPreFilter) {
        synchronized (linkedVariantsLock) {
            this.propertyPreFilter = propertyPreFilter;
            if (propertyPreFilter != null) {
                hasFilter = true;
            }
            for (WeakReference<ObjectWriterAdapter> linked : this.linkedVariants) {
                ObjectWriterAdapter variant = linked.get();
                if (variant != null) {
                    variant.setPropertyPreFilter(propertyPreFilter);
                }
            }
        }
    }

    /**
     * Links {@code variant} to this writer: the filters set on this writer so far are copied to it,
     * and later ones follow. Copying and joining the list hold the same lock as the setters, so a
     * concurrent filter update cannot slip between them. Every linked variant stays connected until it
     * leaves the provider caches, and linking is idempotent.
     */
    void linkSortedVariant(ObjectWriterAdapter variant) {
        if (variant == this) {
            return;
        }
        synchronized (linkedVariantsLock) {
            if (propertyPreFilter != null) {
                variant.setPropertyPreFilter(propertyPreFilter);
            }
            if (propertyFilter != null) {
                variant.setPropertyFilter(propertyFilter);
            }
            if (nameFilter != null) {
                variant.setNameFilter(nameFilter);
            }
            if (valueFilter != null) {
                variant.setValueFilter(valueFilter);
            }

            // one scan builds a dense array: GC can clear a reference between passes,
            // and interior null slots would NPE later setters and links
            List<WeakReference<ObjectWriterAdapter>> live = new ArrayList<>(this.linkedVariants.length + 1);
            for (WeakReference<ObjectWriterAdapter> linked : this.linkedVariants) {
                ObjectWriterAdapter resolved = linked.get();
                if (resolved == variant) {
                    return;
                }
                if (resolved != null) {
                    live.add(linked);
                }
            }
            WeakReference<ObjectWriterAdapter>[] next = live.toArray(new WeakReference[live.size() + 1]);
            next[live.size()] = new WeakReference<>(variant);
            this.linkedVariants = next;
        }
    }

    @Override
    public void writeArrayMappingJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            writeClassInfo(jsonWriter);
        }

        int size = fieldWriters.size();
        jsonWriter.startArray(size);
        for (int i = 0; i < size; ++i) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            fieldWriter.writeValue(jsonWriter, object);
        }
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        long featuresAll = features | this.features | jsonWriter.getFeatures();

        if (!serializable) {
            if ((featuresAll & JSONWriter.Feature.ErrorOnNoneSerializable.mask) != 0) {
                errorOnNoneSerializable();
                return;
            }

            if ((featuresAll & JSONWriter.Feature.IgnoreNoneSerializable.mask) != 0) {
                jsonWriter.writeNull();
                return;
            }
        }

        if ((featuresAll & JSONWriter.Feature.IgnoreNoneSerializable.mask) != 0) {
            writeWithFilter(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        int size = fieldWriterArray.length;
        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            writeClassInfo(jsonWriter);
        }
        jsonWriter.startObject();
        for (int i = 0; i < size; ++i) {
            fieldWriters.get(i)
                    .write(jsonWriter, object);
        }
        jsonWriter.endObject();
    }

    protected final void writeClassInfo(JSONWriter jsonWriter) {
        SymbolTable symbolTable = jsonWriter.symbolTable;
        if (symbolTable != null) {
            if (writeClassInfoSymbol(jsonWriter, symbolTable)) {
                return;
            }
        }

        jsonWriter.writeTypeName(typeNameJSONB, typeNameHash);
    }

    private boolean writeClassInfoSymbol(JSONWriter jsonWriter, SymbolTable symbolTable) {
        int symbolTableIdentity = System.identityHashCode(symbolTable);

        int symbol;
        if (typeNameSymbolCache == 0) {
            symbol = symbolTable.getOrdinalByHashCode(typeNameHash);
            if (symbol != -1) {
                typeNameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
            }
        } else {
            int identity = (int) typeNameSymbolCache;
            if (identity == symbolTableIdentity) {
                symbol = (int) (typeNameSymbolCache >> 32);
            } else {
                symbol = symbolTable.getOrdinalByHashCode(typeNameHash);
                if (symbol != -1) {
                    typeNameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
                }
            }
        }

        if (symbol != -1) {
            jsonWriter.writeRaw(BC_TYPED_ANY);
            jsonWriter.writeInt32(-symbol);
            return true;
        }
        return false;
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (hasValueField) {
            FieldWriter fieldWriter = fieldWriterArray[0];
            fieldWriter.writeValue(jsonWriter, object);
            return;
        }

        long featuresAll = features | this.features | jsonWriter.getFeatures();
        boolean beanToArray = (featuresAll & BeanToArray.mask) != 0;

        if (jsonWriter.jsonb) {
            if (beanToArray) {
                writeArrayMappingJSONB(jsonWriter, object, fieldName, fieldType, features);
                return;
            }

            writeJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        if (googleCollection) {
            Collection collection = (Collection) object;
            ObjectWriterImplCollection.INSTANCE.write(jsonWriter, collection, fieldName, fieldType, features);
            return;
        }

        if (beanToArray) {
            writeArrayMapping(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        if (!serializable) {
            if ((featuresAll & JSONWriter.Feature.ErrorOnNoneSerializable.mask) != 0) {
                errorOnNoneSerializable();
                return;
            }

            if ((featuresAll & JSONWriter.Feature.IgnoreNoneSerializable.mask) != 0) {
                jsonWriter.writeNull();
                return;
            }
        }

        if (hasFilter(jsonWriter)) {
            writeWithFilter(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        jsonWriter.startObject();

        if (jsonWriter.isWriteTypeInfo(object, this.features | features)) {
            writeTypeInfo(jsonWriter);
        }

        final int size = fieldWriters.size();
        for (int i = 0; i < size; i++) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            fieldWriter.write(jsonWriter, object);
        }

        jsonWriter.endObject();
    }

    public Map<String, Object> toMap(Object object) {
        final int size = fieldWriters.size();
        JSONObject map = new JSONObject(size, 1F);
        for (int i = 0; i < size; i++) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            map.put(
                    fieldWriter.fieldName,
                    fieldWriter.getFieldValue(object)
            );
        }
        return map;
    }

    @Override
    public List<FieldWriter> getFieldWriters() {
        return fieldWriters;
    }

    byte[] jsonbClassInfo;

    @Override
    public boolean writeTypeInfo(JSONWriter jsonWriter) {
        if (jsonWriter.utf8) {
            if (nameWithColonUTF8 == null) {
                int typeKeyLength = typeKey.length();
                int typeNameLength = typeName.length();
                byte[] chars = new byte[typeKeyLength + typeNameLength + 5];
                chars[0] = '"';
                typeKey.getBytes(0, typeKeyLength, chars, 1);
                chars[typeKeyLength + 1] = '"';
                chars[typeKeyLength + 2] = ':';
                chars[typeKeyLength + 3] = '"';
                typeName.getBytes(0, typeNameLength, chars, typeKeyLength + 4);
                chars[typeKeyLength + typeNameLength + 4] = '"';

                nameWithColonUTF8 = chars;
            }
            jsonWriter.writeNameRaw(nameWithColonUTF8);
            return true;
        } else if (jsonWriter.utf16) {
            if (nameWithColonUTF16 == null) {
                int typeKeyLength = typeKey.length();
                int typeNameLength = typeName.length();
                char[] chars = new char[typeKeyLength + typeNameLength + 5];
                chars[0] = '"';
                typeKey.getChars(0, typeKeyLength, chars, 1);
                chars[typeKeyLength + 1] = '"';
                chars[typeKeyLength + 2] = ':';
                chars[typeKeyLength + 3] = '"';
                typeName.getChars(0, typeNameLength, chars, typeKeyLength + 4);
                chars[typeKeyLength + typeNameLength + 4] = '"';

                nameWithColonUTF16 = chars;
            }
            jsonWriter.writeNameRaw(nameWithColonUTF16);
            return true;
        } else if (jsonWriter.jsonb) {
            if (typeKeyJSONB == null) {
                typeKeyJSONB = JSONB.toBytes(typeKey);
            }
            jsonWriter.writeRaw(typeKeyJSONB);
            jsonWriter.writeRaw(typeNameJSONB);
            return true;
        }

        jsonWriter.writeString(typeKey);
        jsonWriter.writeColon();
        jsonWriter.writeString(typeName);
        return true;
    }

    @Override
    public void writeWithFilter(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        if (jsonWriter.isWriteTypeInfo(object, fieldType, this.features | features)) {
            if (jsonWriter.jsonb) {
                writeClassInfo(jsonWriter);
                jsonWriter.startObject();
            } else {
                jsonWriter.startObject();
                writeTypeInfo(jsonWriter);
            }
        } else {
            jsonWriter.startObject();
        }

        JSONWriter.Context context = jsonWriter.context;
        long features2 = context.getFeatures() | features;
        boolean refDetect = (features2 & ReferenceDetection.mask) != 0;
        boolean ignoreNonFieldGetter = (features2 & IgnoreNonFieldGetter.mask) != 0;

        BeforeFilter beforeFilter = context.getBeforeFilter();
        if (beforeFilter != null) {
            beforeFilter.writeBefore(jsonWriter, object);
        }

        PropertyPreFilter propertyPreFilter = context.getPropertyPreFilter();
        if (propertyPreFilter == null) {
            propertyPreFilter = this.propertyPreFilter;
        }

        NameFilter nameFilter = context.getNameFilter();
        if (nameFilter == null) {
            nameFilter = this.nameFilter;
        } else {
            if (this.nameFilter != null) {
                nameFilter = NameFilter.compose(this.nameFilter, nameFilter);
            }
        }

        ContextNameFilter contextNameFilter = context.getContextNameFilter();

        ValueFilter valueFilter = context.getValueFilter();
        if (valueFilter == null) {
            valueFilter = this.valueFilter;
        } else {
            if (this.valueFilter != null) {
                valueFilter = ValueFilter.compose(this.valueFilter, valueFilter);
            }
        }

        ContextValueFilter contextValueFilter = context.getContextValueFilter();

        PropertyFilter propertyFilter = context.getPropertyFilter();
        if (propertyFilter == null) {
            propertyFilter = this.propertyFilter;
        }

        LabelFilter labelFilter = context.getLabelFilter();

        for (int i = 0; i < fieldWriters.size(); i++) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            Field field = fieldWriter.field;

            if (ignoreNonFieldGetter
                    && fieldWriter.method != null
                    && (fieldWriter.features & FieldInfo.FIELD_MASK) == 0) {
                continue;
            }

            // pre property filter
            final String fieldWriterFieldName = fieldWriter.fieldName;
            if (propertyPreFilter != null
                    && !propertyPreFilter.process(jsonWriter, object, fieldWriterFieldName)) {
                continue;
            }

            if (labelFilter != null) {
                String label = fieldWriter.label;
                if (label != null && !label.isEmpty()) {
                    if (!labelFilter.apply(label)) {
                        continue;
                    }
                }
            }

            // fast return
            if (nameFilter == null
                    && propertyFilter == null
                    && contextValueFilter == null
                    && contextNameFilter == null
                    && valueFilter == null
            ) {
                fieldWriter.write(jsonWriter, object);
                continue;
            }

            Object fieldValue;
            try {
                fieldValue = fieldWriter.getFieldValue(object);
            } catch (Throwable e) {
                if ((context.getFeatures() & JSONWriter.Feature.IgnoreErrorGetter.mask) != 0) {
                    continue;
                }
                throw e;
            }

            if (fieldValue == null && !jsonWriter.isWriteNulls()) {
                continue;
            }

            if (!refDetect && ("this$0".equals(fieldWriterFieldName) || "this$1".equals(fieldWriterFieldName) || "this$2".equals(fieldWriterFieldName))) {
                continue;
            }

            BeanContext beanContext = null;

            // name filter
            String filteredName = fieldWriterFieldName;
            if (nameFilter != null) {
                filteredName = nameFilter.process(object, filteredName, fieldValue);
            }

            if (contextNameFilter != null) {
                if (beanContext == null) {
                    if (field == null && fieldWriter.method != null) {
                        field = BeanUtils.getDeclaredField(objectClass, fieldWriter.fieldName);
                    }

                    beanContext = new BeanContext(
                            objectClass,
                            fieldWriter.method,
                            field,
                            fieldWriter.fieldName,
                            fieldWriter.label,
                            fieldWriter.fieldClass,
                            fieldWriter.fieldType,
                            fieldWriter.features,
                            fieldWriter.format
                    );
                    filteredName = contextNameFilter.process(beanContext, object, filteredName, fieldValue);
                }
            }

            // property filter
            if (propertyFilter != null
                    && !propertyFilter.apply(object, fieldWriterFieldName, fieldValue)) {
                continue;
            }

            boolean nameChanged = filteredName != null && filteredName != fieldWriterFieldName;

            Object filteredValue = fieldValue;
            if (valueFilter != null) {
                filteredValue = valueFilter.apply(object, fieldWriterFieldName, fieldValue);
            }
            if (contextValueFilter != null) {
                if (beanContext == null) {
                    if (field == null && fieldWriter.method != null) {
                        field = BeanUtils.getDeclaredField(objectClass, fieldWriter.fieldName);
                    }

                    beanContext = new BeanContext(
                            objectClass,
                            fieldWriter.method,
                            field,
                            fieldWriter.fieldName,
                            fieldWriter.label,
                            fieldWriter.fieldClass,
                            fieldWriter.fieldType,
                            fieldWriter.features,
                            fieldWriter.format
                    );
                }
                filteredValue = contextValueFilter.process(beanContext, object, filteredName, filteredValue);
            }

            if (filteredValue != fieldValue) {
                if (nameChanged) {
                    jsonWriter.writeName(filteredName);
                    jsonWriter.writeColon();
                } else {
                    fieldWriter.writeFieldName(jsonWriter);
                }

                if (filteredValue == null) {
                    jsonWriter.writeNull();
                } else {
                    ObjectWriter fieldValueWriter = fieldWriter.getObjectWriter(jsonWriter, filteredValue.getClass());
                    fieldValueWriter.write(jsonWriter, filteredValue, fieldName, fieldType, features);
                }
            } else {
                if (!nameChanged) {
                    fieldWriter.write(jsonWriter, object);
                } else {
                    jsonWriter.writeName(filteredName);
                    jsonWriter.writeColon();

                    if (fieldValue == null) {
                        ObjectWriter fieldValueWriter = fieldWriter.getObjectWriter(jsonWriter, fieldWriter.fieldClass);
                        fieldValueWriter.write(jsonWriter, null, fieldName, fieldType, features);
                    } else {
                        ObjectWriter fieldValueWriter = fieldWriter.getObjectWriter(jsonWriter, fieldValue.getClass());
                        fieldValueWriter.write(jsonWriter, fieldValue, fieldName, fieldType, features);
                    }
                }
            }
        }

        AfterFilter afterFilter = context.getAfterFilter();
        if (afterFilter != null) {
            afterFilter.writeAfter(jsonWriter, object);
        }

        jsonWriter.endObject();
    }

    public JSONObject toJSONObject(T object) {
        return toJSONObject(object, 0);
    }

    public JSONObject toJSONObject(T object, long features) {
        JSONObject jsonObject = new JSONObject();
        long nestedFeatures = features & TREE_FEATURES;

        for (int i = 0, size = fieldWriters.size(); i < size; i++) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            Object fieldValue = fieldWriter.getFieldValue(object);
            String format = fieldWriter.format;
            Class fieldClass = fieldWriter.fieldClass;
            if (format != null) {
                if (fieldClass == Date.class) {
                    if ("millis".equals(format)) {
                        fieldValue = ((Date) fieldValue).getTime();
                    } else {
                        fieldValue = DateUtils.format((Date) fieldValue, format);
                    }
                } else if (fieldClass == LocalDate.class) {
                    fieldValue = DateUtils.format((LocalDate) fieldValue, format);
                } else if (fieldClass == LocalDateTime.class) {
                    fieldValue = DateUtils.format((LocalDateTime) fieldValue, format);
                }
            }

            long fieldFeatures = fieldWriter.features;
            if ((fieldFeatures & FieldInfo.UNWRAPPED_MASK) != 0) {
                if (fieldValue instanceof Map) {
                    jsonObject.putAll((Map) fieldValue);
                    continue;
                }

                ObjectWriter fieldObjectWriter = fieldWriter.getInitWriter();
                if (fieldObjectWriter == null) {
                    fieldObjectWriter = JSONFactory.getObjectWriter(fieldClass, this.features | features);
                }
                List<FieldWriter> unwrappedFieldWriters = fieldObjectWriter.getFieldWriters();
                for (int j = 0, unwrappedSize = unwrappedFieldWriters.size(); j < unwrappedSize; j++) {
                    FieldWriter unwrappedFieldWriter = unwrappedFieldWriters.get(j);
                    Object unwrappedFieldValue = unwrappedFieldWriter.getFieldValue(fieldValue);
                    jsonObject.put(unwrappedFieldWriter.fieldName, unwrappedFieldValue);
                }
                continue;
            }

            if (fieldValue != null) {
                String fieldValueClassName = fieldValue.getClass().getName();
                if (Collection.class.isAssignableFrom(fieldClass)
                        && fieldValue.getClass() != JSONObject.class
                        && !fieldValueClassName.equals("com.alibaba.fastjson.JSONObject")
                ) {
                    Collection collection = (Collection) fieldValue;
                    JSONArray array = new JSONArray(collection.size());
                    for (Object item : collection) {
                        Object itemJSON = item == object
                                ? jsonObject
                                : toJSON(item, nestedFeatures);
                        array.add(itemJSON);
                    }
                    fieldValue = array;
                }
            }

            if (fieldValue == null && ((this.features | features) & WriteNulls.mask) == 0) {
                continue;
            }

            if (fieldValue == object) {
                fieldValue = jsonObject;
            }
            if (fieldValue instanceof Enum) {
                if ((features & WriteEnumsUsingName.mask) != 0) {
                    fieldValue = ((Enum) fieldValue).name();
                }
            }
            if (fieldWriter instanceof FieldWriterObject && fieldValue != null && !(fieldValue instanceof Map)) {
                // the init memo only ever holds a natural writer: trust it only when no variant
                // bit can select a different variant, otherwise resolve with the full merged word
                long variantBits = (this.features | features | fieldFeatures)
                        & (JSONWriter.Feature.SortFieldNamesAlphabetically.mask
                                | JSONWriter.Feature.BeanToArray.mask
                                | JSONWriter.Feature.FieldBased.mask);
                ObjectWriter valueWriter = variantBits == 0 ? fieldWriter.getInitWriter() : null;
                if (valueWriter == null) {
                    valueWriter = JSONFactory.getObjectWriter(fieldWriter.fieldType,
                            this.features | features | fieldFeatures);
                }
                // The cached writer was selected by the first value seen on this field (e.g. an Object
                // or generic field whose fieldClass erases to Object). When a later value has a
                // different runtime type, reusing that writer throws ClassCastException (wrapped as
                // "key get error"); and a subclass value would be written with the parent writer,
                // silently dropping the subclass fields. Re-resolve by the actual value class using
                // the same typeMatch check the write path applies in
                // FieldWriterObject.getObjectWriter(jsonWriter, valueClass), so toJSON stays
                // consistent with toJSONString. See issue #7714.
                FieldWriterObject objectFieldWriter = (FieldWriterObject) fieldWriter;
                Class fieldValueClass = fieldValue.getClass();
                boolean reResolve = objectFieldWriter.initValueClass != null
                        ? !objectFieldWriter.isTypeMatch(fieldValueClass)
                        // the sorted variant's stores are suppressed, so priming never happens there;
                        // fall back to the runtime type instead of silently dropping subclass fields
                        : fieldWriter.fieldClass != fieldValueClass
                                && fieldWriter.fieldClass.isAssignableFrom(fieldValueClass);
                if (reResolve) {
                    valueWriter = JSONFactory.getObjectWriter(fieldValueClass,
                            this.features | features | fieldFeatures);
                    // When the re-resolved writer is not an ObjectWriterAdapter (arrays, Date, enums,
                    // etc.), convert the value via JSON.toJSON so it matches the shape the unprimed
                    // path and toJSONString produce, instead of leaving the raw Java object in the
                    // JSONObject. See issue #7714.
                    if (!(valueWriter instanceof ObjectWriterAdapter)) {
                        fieldValue = toJSON(fieldValue, nestedFeatures);
                    }
                }
                if (valueWriter instanceof ObjectWriterAdapter) {
                    ObjectWriterAdapter objectWriterAdapter = (ObjectWriterAdapter) valueWriter;
                    if (!objectWriterAdapter.getFieldWriters().isEmpty()) {
                        fieldValue = objectWriterAdapter.toJSONObject(fieldValue, nestedFeatures);
                    } else {
                        fieldValue = toJSON(fieldValue, nestedFeatures);
                    }
                }
            }
            jsonObject.put(fieldWriter.fieldName, fieldValue);
        }

        return jsonObject;
    }

    /**
     * Converts the specified value to a {@link JSONArray} or {@link JSONObject}, honoring the
     * caller's feature word merged with the context defaults. Writer-variant bits (such as
     * {@link JSONWriter.Feature#SortFieldNamesAlphabetically}) reach nested conversions through
     * the context; value-format bits are masked off by {@link #TREE_FEATURES} before that.
     *
     * @param object the specified value
     * @param features the caller's feature word, a mask of {@link JSONWriter.Feature} bits
     * @return {@link JSONArray} or {@link JSONObject} or {@code null}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static Object toJSON(Object object, long features) {
        if (object == null) {
            return null;
        }

        if (object instanceof JSONObject || object instanceof JSONArray) {
            return object;
        }

        JSONWriter.Context writeContext = JSONFactory.createWriteContext();
        writeContext.setFeatures(writeContext.getFeatures() | features);
        Class<?> valueClass = object.getClass();
        ObjectWriter<?> objectWriter = writeContext.getObjectWriter(valueClass, valueClass);
        if (objectWriter instanceof ObjectWriterAdapter
                && !writeContext.isEnabled(JSONWriter.Feature.ReferenceDetection)
                && (objectWriter.getFeatures() & JSONWriter.Feature.WriteClassName.mask) == 0) {
            ObjectWriterAdapter objectWriterAdapter = (ObjectWriterAdapter) objectWriter;
            return objectWriterAdapter.toJSONObject(object, writeContext.getFeatures());
        }

        String str;
        try (JSONWriter writer = JSONWriter.of(writeContext)) {
            objectWriter.write(writer, object, null, null, writeContext.getFeatures());
            str = writer.toString();
        } catch (NullPointerException | NumberFormatException ex) {
            throw new JSONException("toJSONString error", ex);
        }

        return JSON.parse(str);
    }

    @Override
    public String toString() {
        return objectClass.getName();
    }

    protected void errorOnNoneSerializable() {
        throw new JSONException("not support none serializable class " + objectClass.getName());
    }
}
