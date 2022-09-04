package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
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

            int entryCnt = jsonReader.startArray();

            Collection list;
            if (autoTypeReader != null) {
                list = (Collection) autoTypeReader.createInstance(context.getFeatures() | features);
            } else if (this.fieldObjectReader.getClass() == ObjectReaderImplList.class && ((ObjectReaderImplList) fieldObjectReader).instanceType == ArrayList.class) {
                list = new ArrayList(entryCnt);
            } else {
                list = (Collection) this.fieldObjectReader.createInstance(context.getFeatures() | features);
            }

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
                    value = itemObjectReader.readJSONBObject(jsonReader, itemType, null, features);
                }
                list.add(value);
            }

            if (builder != null) {
                list = (Collection) builder.apply(list);
            }
            accept(object, list);
            return;
        }

        boolean set = false;
        if (jsonReader.current() == '[' || (set = jsonReader.nextIfSet())) {
            JSONReader.Context ctx = context;
            ObjectReader itemObjectReader = null;

            Collection list = null;
            jsonReader.next();

            Object first = null, second = null;
            int i = 0;
            for (; ; ++i) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                if (itemObjectReader == null) {
                    itemObjectReader = getItemObjectReader(ctx);
                }

                Object itemObject = itemObjectReader.readObject(jsonReader, itemType, null, features);
                if (i == 0) {
                    first = itemObject;
                } else if (i == 1) {
                    second = itemObject;
                } else if (i == 2) {
                    if (fieldClass == java.util.List.class) {
                        list = new ArrayList();
                    } else {
                        list = (Collection) this.fieldObjectReader.createInstance(context.getFeatures() | features);
                    }
                    list.add(first);
                    list.add(second);
                    list.add(itemObject);
                } else {
                    list.add(itemObject);
                }

                if (jsonReader.nextIfMatch(',')) {
                    continue;
                }
            }

            if (list == null) {
                if (fieldClass == java.util.List.class) {
                    list = new ArrayList(i);
                } else if (set && fieldClass == Collection.class) {
                    list = new LinkedHashSet();
                } else {
                    list = (Collection) this.fieldObjectReader.createInstance(context.getFeatures() | features);
                }

                if (i == 1) {
                    list.add(first);
                } else if (i == 2) {
                    list.add(first);
                    list.add(second);
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

            throw new JSONException(jsonReader.info("listField not support input : " + str));
        }

        ObjectReader itemObjectReader = getItemObjectReader(jsonReader);
        Object itemObject = itemObjectReader.readObject(jsonReader, itemType, null, features);

        Collection list = (Collection) this.fieldObjectReader.createInstance(context.getFeatures() | features);
        list.add(itemObject);
        if (builder != null) {
            list = (Collection) builder.apply(list);
        }

        accept(object, list);

        jsonReader.nextIfMatch(',');
    }
}
