package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3749 {
    @Test
    public void test() {
        Model m = new Model();
        m.setNow(new Date("2025/08/22 14:00:00"));
        assertEquals("{\"now\":\"2025/08/22 14:00:00\"}", JSON.toJSONString(m));
    }

    @Data
    public class Model {
        @JSONField(format = "yyyy/MM/dd HH:mm:ss")
        @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", shape = JsonFormat.Shape.STRING, timezone = "GTM+8")
        private Date now;
    }
}
