package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class ObjectWriterAsString implements ObjectWriter {
    private ObjectWriterAsString() {
    }

    public static final ObjectWriterAsString OF_INTEGER = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString(((Number) object).intValue());
            } else {
                jsonWriter.writeNull();
            }
        }
    };
    public static final ObjectWriterAsString OF_INT_VALUE = OF_INTEGER;

    public static final ObjectWriterAsString OF_LONG = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString(((Number) object).longValue());
            } else {
                jsonWriter.writeNull();
            }
        }
    };
    public static final ObjectWriterAsString OF_LONG_VALUE = OF_LONG;

    public static final ObjectWriterAsString OF_FLOAT = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString(((Number) object).floatValue());
            } else {
                jsonWriter.writeNull();
            }
        }
    };
    public static final ObjectWriterAsString OF_FLOAT_VALUE = OF_FLOAT;

    public static final ObjectWriterAsString OF_DOUBLE = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString(((Number) object).doubleValue());
            } else {
                jsonWriter.writeNull();
            }
        }
    };
    public static final ObjectWriterAsString OF_DOUBLE_VALUE = OF_DOUBLE;

    public static final ObjectWriterAsString OF_SHORT = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString(((Number) object).shortValue());
            } else {
                jsonWriter.writeNull();
            }
        }
    };
    public static final ObjectWriterAsString OF_SHORT_VALUE = OF_SHORT;

    public static final ObjectWriterAsString OF_BYTE = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString(((Number) object).byteValue());
            } else {
                jsonWriter.writeNull();
            }
        }
    };
    public static final ObjectWriterAsString OF_BYTE_VALUE = OF_BYTE;

    public static final ObjectWriterAsString OF_CHARACTER = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString(object.toString());
            } else {
                jsonWriter.writeNull();
            }
        }
    };
    public static final ObjectWriterAsString OF_CHAR_VALUE = OF_CHARACTER;

    public static final ObjectWriterAsString OF_BOOLEAN = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString(((Boolean) object).booleanValue());
            } else {
                jsonWriter.writeNull();
            }
        }
    };
    public static final ObjectWriterAsString OF_BOOLEAN_VALUE = OF_BOOLEAN;

    public static final ObjectWriterAsString OF_BIG_INTEGER = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeBigInt((BigInteger) object, JSONWriter.Feature.WriteNonStringValueAsString.mask);
            } else {
                jsonWriter.writeNull();
            }
        }
    };

    public static final ObjectWriterAsString OF_BIG_DECIMAL = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeDecimal((BigDecimal) object, JSONWriter.Feature.WriteNonStringValueAsString.mask, null);
            } else {
                jsonWriter.writeNull();
            }
        }
    };

    public static final ObjectWriterAsString OF_INT_ARRAY = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString((int[]) object);
            } else {
                jsonWriter.writeNull();
            }
        }
    };

    public static final ObjectWriterAsString OF_LONG_ARRAY = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString((long[]) object);
            } else {
                jsonWriter.writeNull();
            }
        }
    };

    public static final ObjectWriterAsString OF_FLOAT_ARRAY = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString((float[]) object);
            } else {
                jsonWriter.writeNull();
            }
        }
    };

    public static final ObjectWriterAsString OF_DOUBLE_ARRAY = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString((double[]) object);
            } else {
                jsonWriter.writeNull();
            }
        }
    };

    public static final ObjectWriterAsString OF_SHORT_ARRAY = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString((short[]) object);
            } else {
                jsonWriter.writeNull();
            }
        }
    };

    public static final ObjectWriterAsString OF_BYTE_ARRAY = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString((byte[]) object);
            } else {
                jsonWriter.writeNull();
            }
        }
    };

    public static final ObjectWriterAsString OF_BOOLEAN_ARRAY = new ObjectWriterAsString() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object != null) {
                jsonWriter.writeString((boolean[]) object);
            } else {
                jsonWriter.writeNull();
            }
        }
    };

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        throw new JSONException("ObjectWriterAsString.write() should NOT be called directly: "
                + object.getClass().getName());
    }
}
