package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONCompiler;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderInt16ValueFuncTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = TestUtils.createObjectReaderLambda(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "123");
        assertEquals(123, bean.value);
        assertNotNull(fieldReader.method);

        fieldReader.accept(bean, (byte) 101);
        assertEquals(101, bean.value);

        fieldReader.accept(bean, (short) 101);
        assertEquals(101, bean.value);

        fieldReader.accept(bean, 102);
        assertEquals(102, bean.value);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                101,
                objectReader.readObject(
                        JSONReader.of("{\"value\":101}"),
                        0
                ).value
        );
    }

    public static class Bean {
        private short value;

        public short getValue() {
            return value;
        }

        public void setValue(short value) {
            this.value = value;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        ObjectReader<Bean1> objectReader = TestUtils.createObjectReaderLambda(Bean1.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, (short) 123));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123L));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123F));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123D));

        assertEquals(
                201,
                objectReader.readObject(
                        JSONReader.of("{\"value\":201}"),
                        0
                ).value
        );
    }

    public static class Bean1 {
        @JSONField(schema = "{'minimum':128}")
        private short value;

        public short getValue() {
            return value;
        }

        public void setValue(short value) {
            this.value = value;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        ObjectReader objectReader = TestUtils.createObjectReaderLambda(Bean2.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(Exception.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, (short) 123));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123L));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123F));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123D));
    }

    @JSONCompiler(JSONCompiler.CompilerOption.LAMBDA)
    public static class Bean2 {
        public void setValue(short value) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void test3() {
        ObjectReader<Bean3> objectReader = TestUtils.createObjectReaderLambda(Bean3.class);
        assertEquals(
                (short) 123,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":123}")
                ).value
        );
    }

    @JSONCompiler(JSONCompiler.CompilerOption.LAMBDA)
    public static class Bean3 {
        private short value;
        public final int id;

        public Bean3(@JSONField(name = "id") int id) {
            this.id = id;
        }

        public short getValue() {
            return value;
        }

        public void setValue(short value) {
            this.value = value;
        }
    }

    @Test
    public void test4() {
        Bean4 bean = new Bean4();
        bean.value = Short.valueOf((short) 256);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":256}", str);
        Bean4 bean1 = JSON.parseObject(str, Bean4.class);
        assertEquals(bean.value, bean1.value);

        Bean4 bean2 = JSON.parseObject(str).toJavaObject(Bean4.class);
        assertEquals(bean.value, bean2.value);

        Bean4 bean3 = JSONObject.of("value", (short) 256).toJavaObject(Bean4.class);
        assertEquals(bean.value, bean3.value);
    }

    @JSONCompiler(JSONCompiler.CompilerOption.LAMBDA)
    private static class Bean4 {
        @JSONField(schema = "{'minimum':128}")
        private short value;

        public short getValue() {
            return value;
        }

        public void setValue(short value) {
            this.value = value;
        }
    }
}
