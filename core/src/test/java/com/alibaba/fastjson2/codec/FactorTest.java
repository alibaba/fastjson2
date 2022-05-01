package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FactorTest {
    @Test
    public void testBuild() throws Exception {
        ObjectReaderCreator creator = TestUtils.READER_CREATOR;
        ObjectReader<VO> objectReader = creator.createObjectReaderFactoryMethod(
                VO.class.getMethod("of", int.class, String.class), "id", "name"
        );
        {
            VO vo = objectReader.readObject(JSONReader.of("{\"id\":1001,\"name\":\"bill\"}"), 0);
            assertEquals(1001, vo.id);
            assertEquals("bill", vo.name);
        }
        {
            JSONReader reader = JSONReader.of("[1001,\"bill\"]");
            reader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            VO vo = objectReader.readObject(reader, 0);
            assertEquals(1001, vo.id);
            assertEquals("bill", vo.name);
        }
        {
            Map map = new HashMap<>();
            map.put("id", 1001);
            map.put("name", "bill");
            byte[] jsonbBytes = JSONB.toBytes(map);
            VO vo = objectReader.readObject(JSONReader.ofJSONB(jsonbBytes), 0);
            assertEquals(1001, vo.id);
            assertEquals("bill", vo.name);
        }
        {
            java.util.List list = new ArrayList();
            list.add(1001);
            list.add("bill");
            byte[] jsonbBytes = JSONB.toBytes(list);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            VO vo = objectReader.readObject(jsonReader, 0);
            assertEquals(1001, vo.id);
            assertEquals("bill", vo.name);
        }
    }

    public static class VO {
        private int id;
        private String name;

        private VO(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static VO of(int id, String name) {
            return new VO(id, name);
        }
    }
}
