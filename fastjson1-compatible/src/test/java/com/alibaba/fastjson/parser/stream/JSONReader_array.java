package com.alibaba.fastjson.parser.stream;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JSONReader_array {
    @Test
    public void test_array() throws Exception {
        JSONReader reader = new JSONReader(new StringReader("[[],[],3,null,{\"name\":\"jobs\"},{\"id\":123},{\"id\":1},{\"id\":2}]"));
        reader.startArray();

        JSONArray first = (JSONArray) reader.readObject();
        JSONArray second = (JSONArray) reader.readObject();

        assertNotNull(first);
        assertNotNull(second);

        assertEquals(new Integer(3), reader.readInteger());
        assertNull(reader.readString());

        {
            Map<String, Object> map = new HashMap<String, Object>();
            reader.readObject(map);
            assertEquals("jobs", map.get("name"));
        }

        {
            VO vo = new VO();
            reader.readObject(vo);
            assertEquals(123, vo.getId());
        }

        while (reader.hasNext()) {
            VO vo = reader.readObject(VO.class);
            assertNotNull(vo);
        }
        reader.endArray();
        reader.close();
    }

    public static class VO {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
