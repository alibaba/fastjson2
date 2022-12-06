package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.List;

import static com.alibaba.fastjson2.JSONWriter.Feature.BeanToArray;
import static com.alibaba.fastjson2.JSONWriter.Feature.WriteClassName;

public class ObjectWriter11<T>
        extends ObjectWriterAdapter<T> {
    public final FieldWriter fieldWriter0;
    public final FieldWriter fieldWriter1;
    public final FieldWriter fieldWriter2;
    public final FieldWriter fieldWriter3;
    public final FieldWriter fieldWriter4;
    public final FieldWriter fieldWriter5;
    public final FieldWriter fieldWriter6;
    public final FieldWriter fieldWriter7;
    public final FieldWriter fieldWriter8;
    public final FieldWriter fieldWriter9;
    public final FieldWriter fieldWriter10;

    ObjectWriter11(Class objectClass, long features, FieldWriter[] fieldWriters) {
        super(objectClass, features, fieldWriters);
        fieldWriter0 = fieldWriters[0];
        fieldWriter1 = fieldWriters[1];
        fieldWriter2 = fieldWriters[2];
        fieldWriter3 = fieldWriters[3];
        fieldWriter4 = fieldWriters[4];
        fieldWriter5 = fieldWriters[5];
        fieldWriter6 = fieldWriters[6];
        fieldWriter7 = fieldWriters[7];
        fieldWriter8 = fieldWriters[8];
        fieldWriter9 = fieldWriters[9];
        fieldWriter10 = fieldWriters[10];
    }

    public ObjectWriter11(
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
        this.fieldWriter7 = fieldWriters.get(7);
        this.fieldWriter8 = fieldWriters.get(8);
        this.fieldWriter9 = fieldWriters.get(9);
        this.fieldWriter10 = fieldWriters.get(10);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
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
        fieldWriter7.write(jsonWriter, object);
        fieldWriter8.write(jsonWriter, object);
        fieldWriter9.write(jsonWriter, object);
        fieldWriter10.write(jsonWriter, object);

        jsonWriter.endObject();
    }

    @Override
    public final FieldWriter getFieldWriter(long hashCode) {
        if (hashCode == fieldWriter0.hashCode) {
            return fieldWriter0;
        }

        if (hashCode == fieldWriter1.hashCode) {
            return fieldWriter1;
        }

        if (hashCode == fieldWriter2.hashCode) {
            return fieldWriter2;
        }

        if (hashCode == fieldWriter3.hashCode) {
            return fieldWriter3;
        }

        if (hashCode == fieldWriter4.hashCode) {
            return fieldWriter4;
        }

        if (hashCode == fieldWriter5.hashCode) {
            return fieldWriter5;
        }

        if (hashCode == fieldWriter6.hashCode) {
            return fieldWriter6;
        }

        if (hashCode == fieldWriter7.hashCode) {
            return fieldWriter7;
        }

        if (hashCode == fieldWriter8.hashCode) {
            return fieldWriter8;
        }

        if (hashCode == fieldWriter9.hashCode) {
            return fieldWriter9;
        }

        if (hashCode == fieldWriter10.hashCode) {
            return fieldWriter10;
        }

        return null;
    }
}
