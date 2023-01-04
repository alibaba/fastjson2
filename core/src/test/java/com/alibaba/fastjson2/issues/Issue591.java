package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue591 {
    @Test
    void test2() {
        String testStr = "{\"time\": \"09:00\", \"nest\": {\"time\": \"09:00\"}, \"nests\": [{\"time\": \"09:00\"}]}";

        TestAA aa = JSONObject.parseObject(testStr, TestAA.class);
        assertNotNull(aa);
        assertNotNull(aa.time);
        assertNotNull(aa.nest.time);
    }

    @Data
    public class TestAA {
        private LocalTime time;
        private TestBB nest;
//        private List<TestBB> nests;
    }

    public class TestBB {
        @JSONField(name = "time", format = "HH:mm")
        private LocalTime time;

        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }
    }
}
