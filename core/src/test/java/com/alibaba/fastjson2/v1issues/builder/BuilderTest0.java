package com.alibaba.fastjson2.v1issues.builder;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuilderTest0 {
    @Test
    public void test_0() throws Exception {
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

    public static class VOBuilder {
        private VO vo = new VO();

        public VO build() {
            return vo;
        }

        public VOBuilder withId(int id) {
            vo.id = id;
            return this;
        }

        public VOBuilder withName(String name) {
            vo.name = name;
            return this;
        }
    }
}
