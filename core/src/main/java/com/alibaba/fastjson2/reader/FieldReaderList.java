package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public interface FieldReaderList<T, V>
        extends FieldReader<T> {
    @Override
    default Type getFieldType() {
        return List.class;
    }

    @Override
    default Class getFieldClass() {
        return List.class;
    }

    @Override
    Type getItemType();

    default List<V> createList() {
        return new ArrayList<>();
    }

    @Override
    default ObjectReader<V> getItemObjectReader(JSONReader.Context ctx) {
        return ctx.getObjectReader(getItemType());
    }

    @Override
    default void readFieldValue(JSONReader jsonReader, T object) {
        JSONReader.Context context = jsonReader.getContext();
        if (jsonReader.isJSONB()) {
            int entryCnt = jsonReader.startArray();

            Object[] array = new Object[entryCnt];
            ObjectReader itemObjectReader = getItemObjectReader(context);
            for (int i = 0; i < entryCnt; ++i) {
                ObjectReader autoTypeReader = jsonReader.checkAutoType(getItemClass(), getItemClassHash(), getFeatures());
                if (autoTypeReader != null) {
                    array[i] = autoTypeReader.readJSONBObject(jsonReader, getFieldType(), getFieldName(), 0);
                } else {
                    array[i] = itemObjectReader.readJSONBObject(jsonReader, getFieldType(), getFieldName(), 0);
                }
            }
            List list = Arrays.asList(array);
            accept(object, list);
            return;
        }

        if (jsonReader.current() == '[') {
            JSONReader.Context ctx = context;
            ObjectReader itemObjectReader = getItemObjectReader(ctx);

            List list = createList();
            jsonReader.next();
            for (; ; ) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                list.add(
                        itemObjectReader.readObject(jsonReader, null, null, 0)
                );

                if (jsonReader.nextIfMatch(',')) {
                    continue;
                }
            }
            accept(object, list);

            jsonReader.nextIfMatch(',');
            return;
        }

        ObjectReader objectReader = getObjectReader(jsonReader);
        long features = getFeatures();
        Object value = jsonReader.isJSONB()
                ? objectReader.readJSONBObject(jsonReader, null, null, features)
                : objectReader.readObject(jsonReader, null, null, features);
        accept(object, value);
    }

    @Override
    default Object readFieldValue(JSONReader jsonReader) {
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

            List list = createList();
            jsonReader.next();
            for (; ; ) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                list.add(
                        itemObjectReader.readObject(jsonReader, null, null, 0)
                );

                if (jsonReader.nextIfMatch(',')) {
                    continue;
                }
            }

            jsonReader.nextIfMatch(',');

            return list;
        }

        if (jsonReader.isString()) {
            String str = jsonReader.readString();
            Type itemType = getItemType();
            if (itemType instanceof Class
                    && Number.class.isAssignableFrom((Class<?>) itemType)
            ) {
                Function typeConvert = jsonReader.getContext().getProvider().getTypeConvert(String.class, itemType);
                if (typeConvert != null) {
                    List list = createList();

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
    default ObjectReader checkObjectAutoType(JSONReader jsonReader) {
        if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();
            long features = getFeatures();

            JSONReader.Context context = jsonReader.getContext();

            boolean isSupportAutoType = jsonReader.isSupportAutoType(features);
            if (!isSupportAutoType && context.getContextAutoTypeBeforeHandler() == null) {
                throw new JSONException(jsonReader.info("autoType not support input " + jsonReader.getString()));
            }

            ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
            if (autoTypeObjectReader == null) {
                String typeName = jsonReader.getString();
                autoTypeObjectReader = context.getObjectReaderAutoType(typeName, getFieldClass(), features);
            }

            if (autoTypeObjectReader instanceof ObjectReaderImplList) {
                ObjectReaderImplList listReader = (ObjectReaderImplList) autoTypeObjectReader;

                autoTypeObjectReader = new ObjectReaderImplList(getFieldType(), getFieldClass(), listReader.instanceType, getItemType(), listReader.builder);
            }

            if (autoTypeObjectReader == null) {
                throw new JSONException(jsonReader.info("auotype not support : " + jsonReader.getString()));
            }

            return autoTypeObjectReader;
        }
        return null;
    }
}
