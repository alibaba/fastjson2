package com.alibaba.fastjson2.v1issues.writeAsArray;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteAsArray_byte_public {
    @Test
    public void test_0() throws Exception {
        VO vo = new VO();
        vo.setId((byte) 123);
        vo.setName("wenshao");

        String text = JSON.toJSONString(vo, JSONWriter.Feature.BeanToArray);
        assertEquals("[123,\"wenshao\"]", text);
    }

    public static class VO {
        private byte id;
        private String name;

        public byte getId() {
            return id;
        }

        public void setId(byte id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
