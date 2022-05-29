package com.alibaba.fastjson2.jsonb;

import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.JSONB.typeName;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeNameTest {
    @Test
    public void test_typeName() {
        assertEquals("INT32 0", typeName((byte) 0));
        assertEquals("INT32 1", typeName((byte) 1));
        assertEquals("INT32 2", typeName((byte) 2));
        assertEquals("INT32 3", typeName((byte) 3));
        assertEquals("INT32 4", typeName((byte) 4));
        assertEquals("INT32 5", typeName((byte) 5));
        assertEquals("INT32 6", typeName((byte) 6));
        assertEquals("INT32 7", typeName((byte) 7));
        assertEquals("INT32 8", typeName((byte) 8));
        assertEquals("INT32 9", typeName((byte) 9));
        assertEquals("INT32 10", typeName((byte) 10));
        assertEquals("STR_ASCII " + BC_STR_ASCII, typeName(BC_STR_ASCII));
        assertEquals("STR_UTF8 " + BC_STR_UTF8, typeName(BC_STR_UTF8));
        assertEquals("STR_UTF16 " + BC_STR_UTF16, typeName(BC_STR_UTF16));
        assertEquals("STR_UTF16LE " + BC_STR_UTF16LE, typeName(BC_STR_UTF16LE));
        assertEquals("STR_UTF16BE " + BC_STR_UTF16BE, typeName(BC_STR_UTF16BE));
        assertEquals("BINARY " + BC_BINARY, typeName(BC_BINARY));

        assertEquals("NULL " + BC_NULL, typeName(BC_NULL));
        assertEquals("TRUE " + BC_TRUE, typeName(BC_TRUE));
        assertEquals("FALSE " + BC_FALSE, typeName(BC_FALSE));

        assertEquals("INT32 " + BC_INT32_BYTE_MIN, typeName(BC_INT32_BYTE_MIN));
        assertEquals("INT32 " + BC_INT32_BYTE_MAX, typeName(BC_INT32_BYTE_MAX));
        assertEquals("INT32 " + BC_INT32_SHORT_MIN, typeName(BC_INT32_SHORT_MIN));
        assertEquals("INT32 " + BC_INT32_SHORT_MAX, typeName(BC_INT32_SHORT_MAX));
        assertEquals("INT32 " + BC_INT32, typeName(BC_INT32));

        assertEquals("INT64 " + BC_INT64, typeName(BC_INT64));
        assertEquals("INT64 " + BC_INT64_NUM_MIN, typeName(BC_INT64_NUM_MIN));
        assertEquals("INT64 " + BC_INT64_NUM_MAX, typeName(BC_INT64_NUM_MAX));
        assertEquals("INT64 " + BC_INT64_BYTE_MIN, typeName(BC_INT64_BYTE_MIN));
        assertEquals("INT64 " + BC_INT64_BYTE_MAX, typeName(BC_INT64_BYTE_MAX));
        assertEquals("INT64 " + BC_INT64_SHORT_MIN, typeName(BC_INT64_SHORT_MIN));
        assertEquals("INT64 " + BC_INT64_SHORT_MAX, typeName(BC_INT64_SHORT_MAX));
        assertEquals("INT64 " + BC_INT64_INT, typeName(BC_INT64_INT));

        assertEquals("BIGINT " + BC_BIGINT, typeName(BC_BIGINT));
        assertEquals("BIGINT " + BC_BIGINT_LONG, typeName(BC_BIGINT_LONG));

        assertEquals("INT8 " + BC_INT8, typeName(BC_INT8));
        assertEquals("INT16 " + BC_INT16, typeName(BC_INT16));

        assertEquals("FLOAT " + BC_FLOAT, typeName(BC_FLOAT));
        assertEquals("FLOAT " + BC_FLOAT_INT, typeName(BC_FLOAT_INT));

        assertEquals("DOUBLE " + BC_DOUBLE, typeName(BC_DOUBLE));
        assertEquals("DOUBLE " + BC_DOUBLE_LONG, typeName(BC_DOUBLE_LONG));
        assertEquals("DOUBLE " + BC_DOUBLE_NUM_0, typeName(BC_DOUBLE_NUM_0));
        assertEquals("DOUBLE " + BC_DOUBLE_NUM_1, typeName(BC_DOUBLE_NUM_1));

        assertEquals("DECIMAL " + BC_DECIMAL, typeName(BC_DECIMAL));
        assertEquals("DECIMAL " + BC_DECIMAL_LONG, typeName(BC_DECIMAL_LONG));

        assertEquals("LOCAL_TIME " + BC_LOCAL_TIME, typeName(BC_LOCAL_TIME));
        assertEquals("LOCAL_DATETIME " + BC_LOCAL_DATETIME, typeName(BC_LOCAL_DATETIME));
        assertEquals("TIMESTAMP " + BC_TIMESTAMP, typeName(BC_TIMESTAMP));
        assertEquals("TIMESTAMP_MINUTES " + BC_TIMESTAMP_MINUTES, typeName(BC_TIMESTAMP_MINUTES));
        assertEquals("TIMESTAMP_SECONDS " + BC_TIMESTAMP_SECONDS, typeName(BC_TIMESTAMP_SECONDS));
        assertEquals("TIMESTAMP_MILLIS " + BC_TIMESTAMP_MILLIS, typeName(BC_TIMESTAMP_MILLIS));
        assertEquals("TIMESTAMP_WITH_TIMEZONE " + BC_TIMESTAMP_WITH_TIMEZONE, typeName(BC_TIMESTAMP_WITH_TIMEZONE));
        assertEquals("LOCAL_DATE " + BC_LOCAL_DATE, typeName(BC_LOCAL_DATE));

        assertEquals("TYPED_ANY " + BC_TYPED_ANY, typeName(BC_TYPED_ANY));

        assertEquals("ARRAY " + BC_ARRAY, typeName(BC_ARRAY));
        assertEquals("ARRAY " + BC_ARRAY_FIX_MIN, typeName(BC_ARRAY_FIX_MIN));
        assertEquals("REFERENCE " + BC_REFERENCE, typeName(BC_REFERENCE));
        assertEquals("OBJECT " + BC_OBJECT, typeName(BC_OBJECT));
        assertEquals("OBJECT_END " + BC_OBJECT_END, typeName(BC_OBJECT_END));
        assertEquals("SYMBOL " + BC_SYMBOL, typeName(BC_SYMBOL));
        assertEquals("-128", typeName((byte) -128));
    }
}
