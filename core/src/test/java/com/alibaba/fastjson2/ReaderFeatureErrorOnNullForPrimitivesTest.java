package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2_vo.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

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

    @Test
    public void test() {
        JSONObject[] objects = new JSONObject[] {
                JSONObject.of("f0", null),
                JSONObject.of("f1", null),
                JSONObject.of("f2", null),
                JSONObject.of("f3", null),
                JSONObject.of("f4", null),
                JSONObject.of("f5", null),
                JSONObject.of("f6", null)
        };

        String[] strings = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            strings[i] = objects[i].toJSONString(JSONWriter.Feature.WriteNulls);
        }

        for (String string : strings) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(
                            string,
                            Bean.class,
                            ErrorOnNullForPrimitives
                    )
            );
        }

        for (String string : strings) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(
                            string.getBytes(StandardCharsets.UTF_8),
                            Bean.class,
                            ErrorOnNullForPrimitives
                    )
            );
        }

        for (JSONObject object : objects) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(
                            object.toJSONBBytes(JSONWriter.Feature.WriteNulls),
                            Bean.class,
                            ErrorOnNullForPrimitives
                    )
            );
        }
    }

    private static class Bean {
        private byte f0;
        private short f1;
        private int f2;
        private long f3;
        private float f4;
        private double f5;
        private boolean f6;

        public byte getF0() {
            return f0;
        }

        public void setF0(byte f0) {
            this.f0 = f0;
        }

        public short getF1() {
            return f1;
        }

        public void setF1(short f1) {
            this.f1 = f1;
        }

        public int getF2() {
            return f2;
        }

        public void setF2(int f2) {
            this.f2 = f2;
        }

        public long getF3() {
            return f3;
        }

        public void setF3(long f3) {
            this.f3 = f3;
        }

        public float getF4() {
            return f4;
        }

        public void setF4(float f4) {
            this.f4 = f4;
        }

        public double getF5() {
            return f5;
        }

        public void setF5(double f5) {
            this.f5 = f5;
        }

        public boolean isF6() {
            return f6;
        }

        public void setF6(boolean f6) {
            this.f6 = f6;
        }
    }

    @Test
    public void test3() {
        JSONObject[] objects = new JSONObject[] {
                JSONObject.of("f0", null),
                JSONObject.of("f1", null),
                JSONObject.of("f2", null),
                JSONObject.of("f3", null),
                JSONObject.of("f4", null),
                JSONObject.of("f5", null),
                JSONObject.of("f6", null)
        };

        String[] strings = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            strings[i] = objects[i].toJSONString(JSONWriter.Feature.WriteNulls);
        }

        for (String string : strings) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(
                            string,
                            Bean1.class,
                            ErrorOnNullForPrimitives
                    )
            );
        }

        for (String string : strings) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(
                            string.getBytes(StandardCharsets.UTF_8),
                            Bean1.class,
                            ErrorOnNullForPrimitives
                    )
            );
        }

        for (JSONObject object : objects) {
            assertThrows(
                    JSONException.class,
                    () -> JSONB.parseObject(
                            object.toJSONBBytes(JSONWriter.Feature.WriteNulls),
                            Bean1.class,
                            ErrorOnNullForPrimitives
                    )
            );
        }
    }

    public static class Bean1 {
        public byte f0;
        public short f1;
        public int f2;
        public long f3;
        public float f4;
        public double f5;
        public boolean f6;
    }

    @Test
    public void test4() {
        JSONObject[] objects = new JSONObject[] {
                JSONObject.of("f0", null),
                JSONObject.of("f1", null),
                JSONObject.of("f2", null),
                JSONObject.of("f3", null),
                JSONObject.of("f4", null),
                JSONObject.of("f5", null),
                JSONObject.of("f6", null)
        };

        String[] strings = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            strings[i] = objects[i].toJSONString(JSONWriter.Feature.WriteNulls);
        }

        ObjectReader<Bean4> objectReader = ObjectReaders.objectReader(
                Bean4.class,
                Bean4::new,
                ObjectReaders.fieldReaderByte("f0", Bean4::setF0),
                ObjectReaders.fieldReaderShort("f1", Bean4::setF1),
                ObjectReaders.fieldReaderInt("f2", Bean4::setF2),
                ObjectReaders.fieldReaderLong("f3", Bean4::setF3),
                ObjectReaders.fieldReaderFloat("f4", Bean4::setF4),
                ObjectReaders.fieldReaderDouble("f5", Bean4::setF5),
                ObjectReaders.fieldReaderBool("f6", Bean4::setF6)
        );
        JSONFactory.getDefaultObjectReaderProvider().register(Bean4.class, objectReader);

        for (String string : strings) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(
                            string,
                            Bean4.class,
                            ErrorOnNullForPrimitives
                    )
            );
        }

        for (String string : strings) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(
                            string.getBytes(StandardCharsets.UTF_8),
                            Bean4.class,
                            ErrorOnNullForPrimitives
                    )
            );
        }

        for (JSONObject object : objects) {
            assertThrows(
                    JSONException.class,
                    () -> JSONB.parseObject(
                            object.toJSONBBytes(JSONWriter.Feature.WriteNulls),
                            Bean4.class,
                            ErrorOnNullForPrimitives
                    )
            );
        }
    }

    private static class Bean4 {
        private byte f0;
        private short f1;
        private int f2;
        private long f3;
        private float f4;
        private double f5;
        private boolean f6;

        public byte getF0() {
            return f0;
        }

        public void setF0(byte f0) {
            this.f0 = f0;
        }

        public short getF1() {
            return f1;
        }

        public void setF1(short f1) {
            this.f1 = f1;
        }

        public int getF2() {
            return f2;
        }

        public void setF2(int f2) {
            this.f2 = f2;
        }

        public long getF3() {
            return f3;
        }

        public void setF3(long f3) {
            this.f3 = f3;
        }

        public float getF4() {
            return f4;
        }

        public void setF4(float f4) {
            this.f4 = f4;
        }

        public double getF5() {
            return f5;
        }

        public void setF5(double f5) {
            this.f5 = f5;
        }

        public boolean isF6() {
            return f6;
        }

        public void setF6(boolean f6) {
            this.f6 = f6;
        }
    }
}
