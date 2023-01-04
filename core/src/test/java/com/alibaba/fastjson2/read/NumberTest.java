package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberTest {
    @Test
    public void test() {
        for (ObjectReaderCreator creator : TestUtils.readerCreators()) {
            ObjectReader<Bean> objectReader = creator.createObjectReader(Bean.class);

            assertEquals(123, objectReader.readObject(JSONReader.of("{\"value\":123}")).value);
            assertEquals(123, objectReader.createInstance(JSONObject.of("value", 123), 0).value);
        }
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
