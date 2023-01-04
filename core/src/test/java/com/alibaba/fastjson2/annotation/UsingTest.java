package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UsingTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 123;

        String str = JSON.toJSONString(bean);
        assertEquals("{\"id\":\"123元\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.id, bean1.id);
    }

    public static class Bean {
        @JSONField(serializeUsing = IdCodec.class, deserializeUsing = IdCodec.class)
        public int id;
    }

    public static class IdCodec
            implements ObjectReader, ObjectWriter {
        public long getFeatures() {
            return 0;
        }

        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            String str = jsonReader.readString();
            str = str.replace("元", "");
            if (fieldType == byte.class) {
                return Byte.parseByte(str);
            }
            if (fieldType == short.class) {
                return Short.parseShort(str);
            }
            if (fieldType == float.class) {
                return Float.parseFloat(str);
            }
            if (fieldType == double.class) {
                return Double.parseDouble(str);
            }
            return Integer.parseInt(str);
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            jsonWriter.writeString(object.toString() + "元");
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.id = 123;

        String str = JSON.toJSONString(bean);
        assertEquals("{\"id\":\"123元\"}", str);

        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.id, bean1.id);
    }

    public static class Bean2 {
        @JSONField(serializeUsing = IdCodec.class, deserializeUsing = IdCodec.class)
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.id = 123;

        String str = JSON.toJSONString(bean);
        assertEquals("{\"id\":\"123元\"}", str);

        Bean3 bean1 = JSON.parseObject(str, Bean3.class);
        assertEquals(bean.id, bean1.id);
    }

    public static class Bean3 {
        @JSONField(serializeUsing = IdCodec.class, deserializeUsing = IdCodec.class)
        private long id;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    @Test
    public void test4() {
        Bean4 bean = new Bean4();
        bean.id = 123;

        String str = JSON.toJSONString(bean);
        assertEquals("{\"id\":\"123元\"}", str);

        Bean4 bean1 = JSON.parseObject(str, Bean4.class);
        assertEquals(bean.id, bean1.id);
    }

    public static class Bean4 {
        @JSONField(serializeUsing = IdCodec.class, deserializeUsing = IdCodec.class)
        private short id;

        public short getId() {
            return id;
        }

        public void setId(short id) {
            this.id = id;
        }
    }

    @Test
    public void test5() {
        Bean5 bean = new Bean5();
        bean.id = 123;

        String str = JSON.toJSONString(bean);
        assertEquals("{\"id\":\"123元\"}", str);

        Bean5 bean1 = JSON.parseObject(str, Bean5.class);
        assertEquals(bean.id, bean1.id);
    }

    public static class Bean5 {
        @JSONField(serializeUsing = IdCodec.class, deserializeUsing = IdCodec.class)
        private byte id;

        public byte getId() {
            return id;
        }

        public void setId(byte id) {
            this.id = id;
        }
    }

    @Test
    public void test6() {
        Bean6 bean = new Bean6();
        bean.id = 123;

        String str = JSON.toJSONString(bean);
        assertEquals("{\"id\":\"123.0元\"}", str);

        Bean6 bean1 = JSON.parseObject(str, Bean6.class);
        assertEquals(bean.id, bean1.id);
    }

    public static class Bean6 {
        @JSONField(serializeUsing = IdCodec.class, deserializeUsing = IdCodec.class)
        private float id;

        public float getId() {
            return id;
        }

        public void setId(float id) {
            this.id = id;
        }
    }

    @Test
    public void test7() {
        Bean7 bean = new Bean7();
        bean.id = 123;

        String str = JSON.toJSONString(bean);
        assertEquals("{\"id\":\"123.0元\"}", str);

        Bean7 bean1 = JSON.parseObject(str, Bean7.class);
        assertEquals(bean.id, bean1.id);
    }

    public static class Bean7 {
        @JSONField(serializeUsing = IdCodec.class, deserializeUsing = IdCodec.class)
        private double id;

        public double getId() {
            return id;
        }

        public void setId(double id) {
            this.id = id;
        }
    }
}
