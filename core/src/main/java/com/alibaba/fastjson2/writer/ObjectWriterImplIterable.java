package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ObjectWriterImplIterable extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplIterable INSTANCE = new ObjectWriterImplIterable();

    Type itemType;
    long features;

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Iterable iterable = (Iterable) object;

        List list = new ArrayList();
        for (Object o : iterable) {
            list.add(o);
        }

        boolean refDetect = jsonWriter.isRefDetect();

        jsonWriter.startArray(list.size());

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }

            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
            }

            boolean itemRefDetect = refDetect && !ObjectWriterProvider.isNotReferenceDetect(itemClass);
            if (itemRefDetect) {
                String refPath = jsonWriter.setPath(i, item);
                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(item);
                    continue;
                }
            }

            itemObjectWriter.writeJSONB(jsonWriter, item, i, this.itemType, this.features);

            if (itemRefDetect) {
                jsonWriter.popPath(item);
            }
        }
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Iterable iterable = (Iterable) object;

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;
        jsonWriter.startArray();
        int i = 0;
        for (Iterator it = iterable.iterator(); it.hasNext(); ) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Object item = it.next();
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
            }

            itemObjectWriter.write(jsonWriter, item, i, this.itemType, this.features);

            ++i;
        }
        jsonWriter.endArray();
    }
}
