package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapFinalFiledTest {
    @Test
    public void test() {
        for (ObjectReaderCreator creator : TestUtils.readerCreators()) {
            ObjectReader<Bean> objectReader = creator.createObjectReader(Bean.class);

            assertEquals(123, objectReader.readObject(JSONReader.of("{\"values\":{\"id\":123}}")).values.get("id"));
            assertEquals(123,
                    objectReader.createInstance(
                            JSONObject.of("values", JSONObject.of("id", 123)),
                    0
                    ).values.get("id")
            );
        }
    }

    public static class Bean {
        public final Map<String, Object> values = new HashMap<>();
    }
}
