package com.alibaba.fastjson2.v1issues.builder;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuilderTest3_private {
    @Test
    public void test_create() throws Exception {
        VO vo = JSON.parseObject("{\"id\":12304,\"name\":\"ljw\"}", VO.class);

        assertEquals(12304, vo.getId());
        assertEquals("ljw", vo.getName());
    }

    @JSONType(builder = VOBuilder.class)
    public static class VO {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static class VOBuilder {
        private VO vo = new VO();

        public VO create() {
            return vo;
        }

        @JSONField(name = "id")
        public VOBuilder kkId(int id) {
            vo.id = id;
            return this;
        }

        @JSONField(name = "name")
        public VOBuilder kkName(String name) {
            vo.name = name;
            return this;
        }
    }
}
