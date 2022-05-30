package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;
import java.util.function.Function;

class FieldReaderListField<T>
        extends FieldReaderObjectField<T>
        implements FieldReaderList<T, Object> {
    private Type itemType;
    final long fieldClassHash;
    ObjectReader itemReader;

    FieldReaderListField(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            Type itemType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Collection defaultValue,
            JSONSchema schema,
            Field field) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, defaultValue, schema, field);
        this.itemType = itemType;
        this.fieldClassHash = fieldClass == null ? 0 : Fnv.hashCode64(TypeUtils.getTypeName(fieldClass));
        this.fieldObjectReader = ObjectReaderImplList.of(fieldType, fieldClass, features);

        if (format != null) {
            if (itemType == Date.class) {
                itemReader = new ObjectReaderImplDate(format, locale);
            }
        }
    }

    @Override
    public Type getItemType() {
        return itemType;
    }

    @Override
    public ObjectReader getItemObjectReader(JSONReader.Context ctx) {
        if (itemReader != null) {
            return itemReader;
        }
        return itemReader = ctx.getObjectReader(itemType);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (jsonReader.nextIfNull()) {
            return;
        }

        if (jsonReader.isReference()) {
            String reference = jsonReader.readReference();
            if ("..".equals(reference)) {
                accept(object, object);
            } else {
                addResolveTask(jsonReader, object, reference);
            }
            return;
        }

        JSONReader.Context context = jsonReader.getContext();
        Function builder = this.fieldObjectReader.getBuildFunction();

        if (jsonReader.isJSONB()) {
            Class fieldClass = this.fieldClass;
            ObjectReader autoTypeReader = null;

            if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
                long typeHash = jsonReader.readTypeHashCode();
                if (typeHash != this.fieldClassHash && jsonReader.isSupportAutoType(features)) {
                    autoTypeReader = context.getObjectReaderAutoType(typeHash);
                    if (autoTypeReader == null) {
                        String typeName = jsonReader.getString();
                        autoTypeReader = context.getObjectReaderAutoType(typeName, fieldClass, fieldClassHash);
                    }

                    builder = autoTypeReader.getBuildFunction();
                }
            }

            Collection list;
            if (autoTypeReader != null) {
                list = (Collection) autoTypeReader.createInstance(jsonReader.getContext().getFeatures() | features);
            } else {
                list = (Collection) this.fieldObjectReader.createInstance(jsonReader.getContext().getFeatures() | features);
            }

            int entryCnt = jsonReader.startArray();
            ObjectReader itemObjectReader
                    = getItemObjectReader(
                    context);
            for (int i = 0; i < entryCnt; ++i) {
                Object value;
                if (jsonReader.isReference()) {
                    String reference = jsonReader.readReference();
                    if ("..".equals(reference)) {
                        value = list;
                    } else {
                        addResolveTask(jsonReader, list, i, reference);
                        if (list instanceof TreeSet) {
                            continue;
                        }
                        value = null;
                    }
                } else {
                    value = itemObjectReader.readJSONBObject(jsonReader, features);
                }
                list.add(value);
            }

            if (builder != null) {
                list = (Collection) builder.apply(list);
            }
            accept(object, list);
            return;
        }

        if (jsonReader.current() == '[') {
            JSONReader.Context ctx = context;
            ObjectReader itemObjectReader = getItemObjectReader(ctx);

            Collection list = (Collection) this.fieldObjectReader.createInstance(jsonReader.getContext().getFeatures() | features);
            jsonReader.next();
            for (; ; ) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                list.add(
                        itemObjectReader.readObject(jsonReader, 0)
                );

                if (jsonReader.nextIfMatch(',')) {
                    continue;
                }
            }

            if (builder != null) {
                list = (Collection) builder.apply(list);
            }

            accept(object, list);

            jsonReader.nextIfMatch(',');
            return;
        }

        if (jsonReader.isString()) {
            String str = jsonReader.readString();
            if (str.isEmpty()) {
                accept(object, null);
                return;
            }

            throw new JSONException("listField not support input : " + str);
        }

        throw new JSONException("listField not support input : " + jsonReader.current());
    }
}
