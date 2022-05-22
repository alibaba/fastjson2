package com.alibaba.fastjson2.v1issues.writeAsArray;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteAsArray_char_public {
    @Test
    public void test_0() {
        VO vo = new VO();
        vo.setId('x');
        vo.setName("wenshao");

        String text = JSON.toJSONString(vo, JSONWriter.Feature.BeanToArray);
        assertEquals("[\"x\",\"wenshao\"]", text);
    }

    public static class VO {
        private char id;
        private String name;

        public char getId() {
            return id;
        }

        public void setId(char id) {
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
