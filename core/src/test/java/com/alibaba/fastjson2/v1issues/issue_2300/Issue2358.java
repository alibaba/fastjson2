package com.alibaba.fastjson2.v1issues.issue_2300;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2358 {
    @Test
    public void test_for_issue() throws Exception {
        String str = "[{\n" +
                "  \"test1\":\"1\",\n" +
                "  \"test2\":\"2\"\n" +
                "},\n" +
                " {\n" +
                "   \"test1\":\"1\",\n" +
                "   \"test2\":\"2\"\n" +
                " }]";

        List<TestJson2> testJsons = JSON.parseArray(str, TestJson2.class);
        assertNotNull(testJsons);
        assertEquals(2, testJsons.size());
        assertEquals("1", testJsons.get(0).test1);
    }

    class TestJson {
        private String test1;
        private String test2;

        public String getTest1() {
            return test1;
        }

        public void setTest1(String test1) {
            this.test1 = test1;
        }

        public String getTest2() {
            return test2;
        }

        public void setTest2(String test2) {
            this.test2 = test2;
        }
    }

    class TestJson2 {
        private String test1;
        private String test2;

        public String getTest1() {
            return test1;
        }

        public void setTest1(String test1) {
            this.test1 = test1;
        }

        public String getTest2() {
            return test2;
        }

        public void setTest2(String test2) {
            this.test2 = test2;
        }
    }
}
