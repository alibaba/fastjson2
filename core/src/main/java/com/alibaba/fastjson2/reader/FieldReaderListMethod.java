package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

final class FieldReaderListMethod<T> extends FieldReaderObjectMethod<T>
        implements FieldReaderList<T, Object> {
    final Type itemType;
    final Class itemClass;

    FieldReaderListMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        if (fieldType instanceof ParameterizedType) {
            itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
        } else {
            itemType = Object.class;
        }
        this.itemClass = TypeUtils.getClass(itemType);
    }

    FieldReaderListMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, Type itemType, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, null, method);
        this.itemType = itemType;
        this.itemClass = TypeUtils.getClass(itemType);
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null && (features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0) {
            return;
        }

        Collection collection = (Collection) value;

        boolean match = true;
        if (itemClass != null) {
            for (Object item : collection) {
                if (!itemClass.isInstance(item)) {
                    match = false;
                    break;
                }
            }
        }

        Collection fieldList;
        if (match) {
            fieldList = collection;
        } else {
            ObjectReader fieldReader = this.fieldObjectReader;
            if (fieldReader == null) {
                fieldReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(getFieldType());
            }

            fieldList = (Collection) fieldReader.createInstance(0);
            for (Object item : collection) {
                Object typedItem;
                if (itemClass.isInstance(item)) {
                    typedItem = item;
                } else if (item instanceof JSONObject) {
                    typedItem = ((JSONObject) item).toJavaObject(itemType);
                } else {
                    String itemJSONString = JSON.toJSONString(item);
                    typedItem = JSON.parseObject(itemJSONString, itemType);
                }
                fieldList.add(typedItem);
            }
        }

        try {
            method.invoke(object, fieldList);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error, " + getClass().getName(), e);
        }
    }

    @Override
    public Type getItemType() {
        return itemType;
    }
}
