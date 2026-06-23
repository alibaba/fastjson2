package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.MapMultiValueType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FieldReaderObjectTest {
    @Test
    public void testMapMultiValueTypeReaderIsBuiltIn() {
        ObjectReader objectReader = new ObjectReaderImplMapMultiValueType(
                MapMultiValueType.of("data", Bean.class)
        );

        assertFalse(FieldReaderObject.isCustomReader(objectReader));
    }

    @Test
    public void testMapMultiValueTypeReaderJSONB() {
        MapMultiValueType<JSONObject> type = MapMultiValueType.of("data", Bean.class);
        ObjectReader objectReader = new ObjectReaderImplMapMultiValueType(type);
        byte[] bytes = JSONB.toBytes(JSON.parseObject("{\"data\":{\"id\":123}}"));

        Object value = FieldReaderObject.readJSONBObject(
                objectReader,
                JSONReader.ofJSONB(bytes),
                type,
                JSONObject.class,
                "data",
                0
        );

        Bean bean = (Bean) ((Map) value).get("data");
        assertEquals(123, bean.id);
    }

    public static class Bean {
        public int id;
    }
}
