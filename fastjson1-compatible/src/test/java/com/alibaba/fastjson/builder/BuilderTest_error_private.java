package com.alibaba.fastjson.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BuilderTest_error_private {
    @Test
    public void test_0() throws Exception {
        Exception error = null;
        try {
            JSON.parseObject("{\"id\":12304,\"name\":\"ljw\"}", VO.class);
        } catch (JSONException | com.alibaba.fastjson2.JSONException ex) {
            error = ex;
        }
        Assertions.assertNotNull(error);
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

        public VO build() {
            throw new IllegalStateException();
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
