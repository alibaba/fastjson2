package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FieldReaderList<T, V>
        extends FieldReaderObject<T> {
    final long fieldClassHash;
    final long itemClassHash;

    public FieldReaderList(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            Type itemType,
            Class itemClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer function
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function);
        this.itemType = itemType;
        this.itemClass = itemClass;
        this.itemClassHash = this.itemClass == null ? 0 : Fnv.hashCode64(itemClass.getName());
        this.fieldClassHash = fieldClass == null ? 0 : Fnv.hashCode64(TypeUtils.getTypeName(fieldClass));

        if (format != null) {
            if (itemType == Date.class) {
                itemReader = new ObjectReaderImplDate(format, locale);
            }
        }
    }

    @Override
    public long getItemClassHash() {
        return itemClassHash;
    }

    public Collection<V> createList(JSONReader.Context context) {
        if (fieldClass == List.class || fieldClass == Collection.class || fieldClass == ArrayList.class) {
            return new ArrayList<>();
        }

        return (Collection<V>) getObjectReader(context).createInstance();
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (jsonReader.isJSONB()) {
            readFieldValueJSONB(jsonReader, object);
            return;
        }

        if (jsonReader.nextIfNull()) {
            accept(object, null);
            return;
        }

        JSONReader.Context context = jsonReader.getContext();
        ObjectReader objectReader = getObjectReader(context);

        Function builder = null;

        if (initReader != null) {
            builder = this.initReader.getBuildFunction();
        } else {
            if (objectReader instanceof ObjectReaderImplList) {
                builder = objectReader.getBuildFunction();
            }
        }

        char current = jsonReader.current();
        if (current == '[') {
            ObjectReader itemObjectReader = getItemObjectReader(context);

            Collection list = createList(context);
            jsonReader.next();
            for (int i = 0; ; ++i) {
                if (jsonReader.nextIfArrayEnd()) {
                    break;
                }

                Object item;
                if (jsonReader.isReference()) {
                    String path = jsonReader.readReference();
                    if ("..".equals(path)) {
                        item = list;
                    } else {
                        addResolveTask(jsonReader, (List) list, i, path);
                        continue;
                    }
                } else {
                    item = itemObjectReader.readObject(jsonReader, null, null, 0);
                }

                list.add(item);

                jsonReader.nextIfComma();
            }
            if (builder != null) {
                list = (Collection) builder.apply(list);
            }
            accept(object, list);

            jsonReader.nextIfComma();
            return;
        } else if (current == '{' && getItemObjectReader(context) instanceof ObjectReaderBean) {
            Object itemValue = jsonReader.isJSONB()
                    ? itemReader.readJSONBObject(jsonReader, null, null, features)
                    : itemReader.readObject(jsonReader, null, null, features);
            Collection list = (Collection) objectReader.createInstance(features);
            list.add(itemValue);
            if (builder != null) {
                list = (Collection) builder.apply(list);
            }
            accept(object, list);

            jsonReader.nextIfComma();
            return;
        }

        Object value = jsonReader.isJSONB()
                ? objectReader.readJSONBObject(jsonReader, null, null, features)
                : objectReader.readObject(jsonReader, null, null, features);
        accept(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        if (jsonReader.isJSONB()) {
            int entryCnt = jsonReader.startArray();

            Object[] array = new Object[entryCnt];
            ObjectReader itemObjectReader
                    = getItemObjectReader(
                    jsonReader.getContext());
            for (int i = 0; i < entryCnt; ++i) {
                array[i] = itemObjectReader.readObject(jsonReader, null, null, 0);
            }
            return Arrays.asList(array);
        }

        if (jsonReader.current() == '[') {
            JSONReader.Context ctx = jsonReader.getContext();
            ObjectReader itemObjectReader = getItemObjectReader(ctx);

            Collection list = createList(ctx);
            jsonReader.next();
            while (!jsonReader.nextIfArrayEnd()) {
                list.add(
                        itemObjectReader.readObject(jsonReader, null, null, 0)
                );

                jsonReader.nextIfComma();
            }

            jsonReader.nextIfComma();

            return list;
        }

        if (jsonReader.isString()) {
            String str = jsonReader.readString();
            if (itemType instanceof Class
                    && Number.class.isAssignableFrom((Class<?>) itemType)
            ) {
                Function typeConvert = jsonReader.getContext().getProvider().getTypeConvert(String.class, itemType);
                if (typeConvert != null) {
                    Collection list = createList(jsonReader.getContext());

                    if (str.indexOf(',') != -1) {
                        String[] items = str.split(",");

                        for (String item : items) {
                            Object converted = typeConvert.apply(item);
                            list.add(converted);
                        }
                    }

                    return list;
                }
            }
        }

        throw new JSONException(jsonReader.info("TODO : " + this.getClass()));
    }

    @Override
    public ObjectReader checkObjectAutoType(JSONReader jsonReader) {
        if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();
            final long features = jsonReader.features(this.features);
            JSONReader.Context context = jsonReader.getContext();
            JSONReader.AutoTypeBeforeHandler autoTypeFilter = context.getContextAutoTypeBeforeHandler();
            if (autoTypeFilter != null) {
                Class<?> filterClass = autoTypeFilter.apply(typeHash, fieldClass, features);
                if (filterClass == null) {
                    String typeName = jsonReader.getString();
                    filterClass = autoTypeFilter.apply(typeName, fieldClass, features);
                }
                if (filterClass != null) {
                    return context.getObjectReader(fieldClass);
                }
            }

            boolean isSupportAutoType = jsonReader.isSupportAutoType(features);
            if (!isSupportAutoType) {
                throw new JSONException(jsonReader.info("autoType not support input " + jsonReader.getString()));
            }

            ObjectReader autoTypeObjectReader = jsonReader.getObjectReaderAutoType(typeHash, fieldClass, features);

            if (autoTypeObjectReader instanceof ObjectReaderImplList) {
                ObjectReaderImplList listReader = (ObjectReaderImplList) autoTypeObjectReader;

                autoTypeObjectReader = new ObjectReaderImplList(fieldType, fieldClass, listReader.instanceType, itemType, listReader.builder);
            }

            if (autoTypeObjectReader == null) {
                throw new JSONException(jsonReader.info("auotype not support : " + jsonReader.getString()));
            }

            return autoTypeObjectReader;
        }
        return null;
    }
}
