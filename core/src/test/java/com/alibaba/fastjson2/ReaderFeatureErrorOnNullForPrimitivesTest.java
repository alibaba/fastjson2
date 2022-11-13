package com.alibaba.fastjson2;

import com.alibaba.fastjson2_vo.*;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONReader.Feature.ErrorOnNullForPrimitives;
import static org.junit.jupiter.api.Assertions.*;

public class ReaderFeatureErrorOnNullForPrimitivesTest {
    @Test
    public void testByte() {
        String json = "{\"v0000\":null}";
        assertEquals(0, JSON.parseObject(json, ByteValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(json, ByteValue1.class, ErrorOnNullForPrimitives)
        );

        byte[] jsonBytes = json.getBytes();
        assertEquals(0, JSON.parseObject(jsonBytes, ByteValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(jsonBytes, ByteValue1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSON.parseObject(json, Byte1.class).getV0000());
        assertNull(JSON.parseObject(json, Byte1.class, ErrorOnNullForPrimitives).getV0000());

        byte[] jsonbBytes = JSONObject.of("v0000", null).toJSONBBytes(JSONWriter.Feature.WriteNulls);
        assertEquals(0, JSONB.parseObject(jsonbBytes, ByteValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(jsonbBytes, ByteValue1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSONB.parseObject(jsonbBytes, Byte1.class).getV0000());
        assertNull(JSONB.parseObject(jsonbBytes, Byte1.class, ErrorOnNullForPrimitives).getV0000());
    }

    @Test
    public void testShort() {
        String json = "{\"v0000\":null}";
        assertEquals(0, JSON.parseObject(json, ShortValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(json, ShortValue1.class, ErrorOnNullForPrimitives)
        );

        byte[] jsonBytes = json.getBytes();
        assertEquals(0, JSON.parseObject(jsonBytes, ShortValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(jsonBytes, ShortValue1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSON.parseObject(json, Short1.class).getV0000());
        assertNull(JSON.parseObject(json, Short1.class, ErrorOnNullForPrimitives).getV0000());

        byte[] jsonbBytes = JSONObject.of("v0000", null).toJSONBBytes(JSONWriter.Feature.WriteNulls);
        assertEquals(0, JSONB.parseObject(jsonbBytes, ShortValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(jsonbBytes, ShortValue1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSONB.parseObject(jsonbBytes, Short1.class).getV0000());
        assertNull(JSONB.parseObject(jsonbBytes, Short1.class, ErrorOnNullForPrimitives).getV0000());
    }

    @Test
    public void testInt() {
        String json = "{\"v0000\":null}";
        assertEquals(0, JSON.parseObject(json, Int1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(json, Int1.class, ErrorOnNullForPrimitives)
        );

        byte[] jsonBytes = json.getBytes();
        assertEquals(0, JSON.parseObject(jsonBytes, Int1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(jsonBytes, Int1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSON.parseObject(json, Integer1.class).getV0000());
        assertNull(JSON.parseObject(json, Integer1.class, ErrorOnNullForPrimitives).getV0000());

        byte[] jsonbBytes = JSONObject.of("v0000", null).toJSONBBytes(JSONWriter.Feature.WriteNulls);
        assertEquals(0, JSONB.parseObject(jsonbBytes, Int1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(jsonbBytes, Int1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSONB.parseObject(jsonbBytes, Integer1.class).getV0000());
        assertNull(JSONB.parseObject(jsonbBytes, Integer1.class, ErrorOnNullForPrimitives).getV0000());
    }

    @Test
    public void testLong() {
        String json = "{\"v0000\":null}";
        assertEquals(0, JSON.parseObject(json, LongValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(json, LongValue1.class, ErrorOnNullForPrimitives)
        );

        byte[] jsonBytes = json.getBytes();
        assertEquals(0, JSON.parseObject(jsonBytes, LongValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(jsonBytes, LongValue1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSON.parseObject(json, Long1.class).getV0000());
        assertNull(JSON.parseObject(json, Long1.class, ErrorOnNullForPrimitives).getV0000());

        byte[] jsonbBytes = JSONObject.of("v0000", null).toJSONBBytes(JSONWriter.Feature.WriteNulls);
        assertEquals(0, JSONB.parseObject(jsonbBytes, LongValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(jsonbBytes, LongValue1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSONB.parseObject(jsonbBytes, Long1.class).getV0000());
        assertNull(JSONB.parseObject(jsonbBytes, Long1.class, ErrorOnNullForPrimitives).getV0000());
    }

    @Test
    public void testBoolean() {
        String json = "{\"v0000\":null}";
        assertFalse(JSON.parseObject(json, BooleanValue1.class).isV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(json, BooleanValue1.class, ErrorOnNullForPrimitives)
        );

        byte[] jsonBytes = json.getBytes();
        assertFalse(JSON.parseObject(jsonBytes, BooleanValue1.class).isV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(jsonBytes, BooleanValue1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSON.parseObject(json, Boolean1.class).getV0000());
        assertNull(JSON.parseObject(json, Boolean1.class, ErrorOnNullForPrimitives).getV0000());

        byte[] jsonbBytes = JSONObject.of("v0000", null).toJSONBBytes(JSONWriter.Feature.WriteNulls);

        assertFalse(JSONB.parseObject(jsonbBytes, BooleanValue1.class).isV0000());
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(jsonbBytes, BooleanValue1.class, ErrorOnNullForPrimitives)
        );
        assertNull(JSONB.parseObject(jsonbBytes, Boolean1.class).getV0000());
        assertNull(JSONB.parseObject(jsonbBytes, Boolean1.class, ErrorOnNullForPrimitives).getV0000());
    }

    @Test
    public void testFloat() {
        String json = "{\"v0000\":null}";
        assertEquals(0F, JSON.parseObject(json, FloatValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(json, FloatValue1.class, ErrorOnNullForPrimitives)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(json.getBytes(), FloatValue1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSON.parseObject(json, Float1.class).getV0000());
        assertNull(JSON.parseObject(json, Float1.class, ErrorOnNullForPrimitives).getV0000());

        byte[] jsonbBytes = JSONObject.of("v0000", null).toJSONBBytes(JSONWriter.Feature.WriteNulls);
        assertEquals(0F, JSONB.parseObject(jsonbBytes, FloatValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(jsonbBytes, FloatValue1.class, ErrorOnNullForPrimitives)
        );
    }

    @Test
    public void testDouble() {
        String json = "{\"v0000\":null}";
        assertEquals(0F, JSON.parseObject(json, DoubleValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(json, DoubleValue1.class, ErrorOnNullForPrimitives)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(json.getBytes(), DoubleValue1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSON.parseObject(json, Double1.class).getV0000());
        assertNull(JSON.parseObject(json, Double1.class, ErrorOnNullForPrimitives).getV0000());

        byte[] jsonbBytes = JSONObject.of("v0000", null).toJSONBBytes(JSONWriter.Feature.WriteNulls);
        assertEquals(0D, JSONB.parseObject(jsonbBytes, DoubleValue1.class).getV0000());
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(jsonbBytes, DoubleValue1.class, ErrorOnNullForPrimitives)
        );

        assertNull(JSONB.parseObject(jsonbBytes, Double1.class).getV0000());
        assertNull(JSONB.parseObject(jsonbBytes, Double1.class, ErrorOnNullForPrimitives).getV0000());
    }
}
