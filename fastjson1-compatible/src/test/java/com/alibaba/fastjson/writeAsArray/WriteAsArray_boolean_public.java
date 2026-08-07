package com.alibaba.fastjson.writeAsArray;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteAsArray_boolean_public {
    @Test
    public void test_0() throws Exception {
        VO vo = new VO();
        vo.setId(true);
        vo.setName("wenshao");

        String text = JSON.toJSONString(vo, SerializerFeature.BeanToArray);
        assertEquals("[true,\"wenshao\"]", text);
    }

    public static class VO {
        private boolean id;
        private String name;

        public boolean getId() {
            return id;
        }

        public void setId(boolean id) {
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
