package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NonDefaulConstructorTest {
    @Test
    public void testBuild() throws Exception {
        ObjectReaderCreator creator = TestUtils.READER_CREATOR;
        ObjectReader<VO> objectReader = creator.createObjectReaderNoneDefaultConstructor(
                VO.class.getConstructor(int.class, String.class), "id", "name"
        );
        {
            VO vo = objectReader.readObject(JSONReader.of("{\"id\":1001,\"name\":\"bill\"}"), 0);
            assertEquals(1001, vo.id);
            assertEquals("bill", vo.name);
        }
        {
            Map map = new LinkedHashMap<>();
            map.put("id", 1001);
            map.put("name", "bill");
            byte[] jsonbBytes = JSONB.toBytes(map);

            VO vo = objectReader.readObject(JSONReader.ofJSONB(jsonbBytes), 0);
            assertEquals(1001, vo.id);
            assertEquals("bill", vo.name);
        }
    }

    @Test
    public void testBuild_2() throws Exception {
        String str = JSONObject
                .of("id", 101)
                .fluentPut("name", "DataWorks")
                .toString();

        Exception error = null;
        try {
            JSON.parseObject(str, VO2.class);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    public static class VO {
        private int id;
        private String name;

        public VO(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class VO2 {
        private int id;
        private String name;

        public VO2(int id, String name) {
            throw new RuntimeException();
        }
    }
}
