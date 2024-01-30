package com.alibaba.fastjson2.types;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaders;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NumberTest {
    @Test
    public void test() throws Exception {
        ObjectReader<Bean> objectReader = ObjectReaders.objectReader(
                Bean.class,
                Bean::new,
                ObjectReaderCreator.INSTANCE.createFieldReader("value", Number.class, Number.class, 0, Bean::setValue)
        );

        JSONObject jsonObject = JSONObject.of("value", 123);
        String str = jsonObject.toJSONString();
        Bean object = objectReader.readObject(JSONReader.of(str));
        assertEquals(jsonObject.getIntValue("value"), object.value);

        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertNotNull(fieldReader.getObjectReader(JSONReader.of(str)));
        assertNotNull(fieldReader.getObjectReader(JSONFactory.createReadContext()));

        fieldReader.accept(object, true);
        assertEquals(1, object.value);
        fieldReader.accept(object, false);
        assertEquals(0, object.value);

        fieldReader.accept(object, 2);
        assertEquals(2, object.value);

        fieldReader.accept(object, 3L);
        assertEquals(3L, object.value);

        fieldReader.accept(object, 4F);
        assertEquals(4F, object.value);

        fieldReader.accept(object, BigInteger.ONE);
        assertEquals(BigInteger.ONE, object.value);

        Bean object1 = objectReader.readObject(
                JSONReader.of(
                        "[1]",
                        JSONFactory.createReadContext(JSONReader.Feature.SupportArrayToBean)));
        assertEquals(1, object1.value);
    }

    public static class Bean {
        private Number value;

        public Number getValue() {
            return value;
        }

        public void setValue(Number value) {
            this.value = value;
        }
    }
}
