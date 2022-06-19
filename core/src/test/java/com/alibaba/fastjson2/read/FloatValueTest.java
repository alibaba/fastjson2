package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatValueTest {
    @Test
    public void test() {
        for (ObjectReaderCreator creator : TestUtils.readerCreators()) {
            ObjectReader<Bean> objectReader = creator.createObjectReader(Bean.class);

            assertEquals(123F, objectReader.readObject(JSONReader.of("{\"value\":123}")).value);
            assertEquals(123F, objectReader.createInstance(JSONObject.of("value", 123), 0).value);
        }
    }

    public static class Bean {
        private float value;

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }
}
