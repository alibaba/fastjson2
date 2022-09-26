package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.List;

import static com.alibaba.fastjson2.JSONWriter.Feature.BeanToArray;
import static com.alibaba.fastjson2.JSONWriter.Feature.WriteClassName;

final class ObjectWriterAdapter7<T>
        extends ObjectWriterAdapter<T> {
    final FieldWriter fieldWriter0;
    final FieldWriter fieldWriter1;
    final FieldWriter fieldWriter2;
    final FieldWriter fieldWriter3;
    final FieldWriter fieldWriter4;
    final FieldWriter fieldWriter5;
    final FieldWriter fieldWriter6;

    public ObjectWriterAdapter7(Class objectClass, long features, FieldWriter[] fieldWriters) {
        super(objectClass, features, fieldWriters);
        fieldWriter0 = fieldWriters[0];
        fieldWriter1 = fieldWriters[1];
        fieldWriter2 = fieldWriters[2];
        fieldWriter3 = fieldWriters[3];
        fieldWriter4 = fieldWriters[4];
        fieldWriter5 = fieldWriters[5];
        fieldWriter6 = fieldWriters[6];
    }

    public ObjectWriterAdapter7(
            Class<T> objectClass,
            String typeKey,
            String typeName,
            long features,
            List<FieldWriter> fieldWriters
    ) {
        super(objectClass, typeKey, typeName, features, fieldWriters);
        this.fieldWriter0 = fieldWriters.get(0);
        this.fieldWriter1 = fieldWriters.get(1);
        this.fieldWriter2 = fieldWriters.get(2);
        this.fieldWriter3 = fieldWriters.get(3);
        this.fieldWriter4 = fieldWriters.get(4);
        this.fieldWriter5 = fieldWriters.get(5);
        this.fieldWriter6 = fieldWriters.get(6);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        long featuresAll = features | this.features | jsonWriter.getFeatures();
        boolean beanToArray = (featuresAll & BeanToArray.mask) != 0;

        if (jsonWriter.isJSONB()) {
            if (beanToArray) {
                writeArrayMappingJSONB(jsonWriter, object, fieldName, fieldType, features);
                return;
            }

            writeJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        if (beanToArray) {
            writeArrayMapping(jsonWriter, object, fieldName, fieldType, features | this.features);
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
            writeWithFilter(jsonWriter, object, fieldName, fieldType, 0);
            return;
        }

        jsonWriter.startObject();

        if (((features | this.features) & WriteClassName.mask) != 0 || jsonWriter.isWriteTypeInfo(object, features)) {
            writeTypeInfo(jsonWriter);
        }

        fieldWriter0.write(jsonWriter, object);
        fieldWriter1.write(jsonWriter, object);
        fieldWriter2.write(jsonWriter, object);
        fieldWriter3.write(jsonWriter, object);
        fieldWriter4.write(jsonWriter, object);
        fieldWriter5.write(jsonWriter, object);
        fieldWriter6.write(jsonWriter, object);

        jsonWriter.endObject();
    }
}
