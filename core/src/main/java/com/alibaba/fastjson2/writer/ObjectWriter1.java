package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Type;
import java.util.List;

import static com.alibaba.fastjson2.JSONWriter.Feature.BeanToArray;

public class ObjectWriter1<T>
        extends ObjectWriterAdapter<T> {
    public final FieldWriter fieldWriter0;

    public ObjectWriter1(
            Class<T> objectClass,
            String typeKey,
            String typeName,
            long features,
            List<FieldWriter> fieldWriters
    ) {
        super(objectClass, typeKey, typeName, features, fieldWriters);
        this.fieldWriter0 = fieldWriters.get(0);
    }

    @Override
    public void writeJSONB(JSONWriterJSONB jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        long featuresAll = features | this.features | jsonWriter.getFeatures();

        if ((featuresAll & BeanToArray.mask) != 0) {
            writeArrayMappingJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        super.writeJSONB(jsonWriter, object, fieldName, fieldType, features);
    }

    @Override
    public void writeUTF8(JSONWriterUTF8 jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        long featuresAll = features | this.features | jsonWriter.getFeatures();

        if ((featuresAll & BeanToArray.mask) != 0) {
            writeArrayMappingUTF8(jsonWriter, object, fieldName, fieldType, features);
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

        if (jsonWriter.isWriteTypeInfo(object, this.features | features)) {
            writeTypeInfo(jsonWriter);
        }

        fieldWriter0.writeUTF8(jsonWriter, object);

        jsonWriter.endObject();
    }

    @Override
    public void writeUTF16(JSONWriterUTF16 jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        long featuresAll = features | this.features | jsonWriter.getFeatures();

        if ((featuresAll & BeanToArray.mask) != 0) {
            writeArrayMappingUTF16(jsonWriter, object, fieldName, fieldType, features);
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

        if (jsonWriter.isWriteTypeInfo(object, this.features | features)) {
            writeTypeInfo(jsonWriter);
        }

        fieldWriter0.writeUTF16(jsonWriter, object);

        jsonWriter.endObject();
    }

    @Override
    public final FieldWriter getFieldWriter(long hashCode) {
        if (hashCode == fieldWriter0.hashCode) {
            return fieldWriter0;
        }

        return null;
    }
}
