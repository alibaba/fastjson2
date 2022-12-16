package com.alibaba.fastjson.writeAsArray;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteAsArray_0_public {
    @Test
    public void test_0() throws Exception {
        VO vo = new VO();
        vo.setId(123);
        vo.setName("wenshao");

        String text = JSON.toJSONString(vo, SerializerFeature.BeanToArray);
        assertEquals("[123,\"wenshao\"]", text);
    }

    public static class VO {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
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
