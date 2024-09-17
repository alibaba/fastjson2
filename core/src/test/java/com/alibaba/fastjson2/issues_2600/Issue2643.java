package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2643 {
    final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testJsonSetter() throws Exception {
        final String json = "{ \"department\": \"IT\" }";
        final Bean jackson = mapper.readValue(json, Bean.class);
        final Bean fastJson2 = JSON.parseObject(json, Bean.class);

        assertEquals(jackson.getDept(), fastJson2.getDept());
        assertEquals(jackson.getDept(), JSON.parseObject(json, Bean1.class).getDept());
    }

    public static class Bean {
        @JsonSetter("department")
        String dept;

        public String getDept() {
            return this.dept;
        }

        public void setDept(final String dept) {
            this.dept = dept;
        }
    }

    public static class Bean1 {
        @JSONField(name = "department")
        String dept;

        public String getDept() {
            return this.dept;
        }

        public void setDept(final String dept) {
            this.dept = dept;
        }
    }
}
