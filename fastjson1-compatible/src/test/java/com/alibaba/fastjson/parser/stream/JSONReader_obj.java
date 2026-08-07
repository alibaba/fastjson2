package com.alibaba.fastjson.parser.stream;

import com.alibaba.fastjson.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReader_obj {
    @Test
    public void test_array() throws Exception {
        JSONReader reader = new JSONReader(new StringReader("[{\"id\":123}]"));

        reader.startArray();

        VO vo = new VO();
        reader.readObject(vo);

        assertEquals(123, vo.getId());
        reader.endArray();
        reader.close();
    }

    @Test
    public void test_obj() throws Exception {
        JSONReader reader = new JSONReader(new StringReader("{\"id\":123}"));

        VO vo = new VO();
        reader.readObject(vo);

        assertEquals(123, vo.getId());
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
