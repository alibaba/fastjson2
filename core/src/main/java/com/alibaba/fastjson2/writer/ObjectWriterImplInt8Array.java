package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

final class ObjectWriterImplInt8Array
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplInt8Array INSTANCE = new ObjectWriterImplInt8Array();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[Byte");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[Byte");

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        boolean writeAsString = (features & WriteNonStringValueAsString.mask) != 0;

        Byte[] array = (Byte[]) object;
        jsonWriter.startArray();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Byte value = array[i];
            if (value == null) {
                jsonWriter.writeNull();
            } else {
                byte byteValue = value;
                if (writeAsString) {
                    jsonWriter.writeString(byteValue);
                } else {
                    jsonWriter.writeInt8(byteValue);
                }
            }
        }
        jsonWriter.endArray();
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        boolean writeAsString = (features & WriteNonStringValueAsString.mask) != 0;

        Byte[] array = (Byte[]) object;
        jsonWriter.startArray(array.length);
	    for (Byte value : array) {
		    if (value == null) {
			    jsonWriter.writeNull();
		    } else {
			    byte byteValue = value;
			    if (writeAsString) {
				    jsonWriter.writeString(byteValue);
			    } else {
				    jsonWriter.writeInt8(byteValue);
			    }
		    }
	    }
    }
}
