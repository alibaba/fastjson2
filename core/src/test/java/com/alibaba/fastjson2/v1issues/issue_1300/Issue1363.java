package com.alibaba.fastjson2.v1issues.issue_1300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by wenshao on 05/08/2017.
 */
public class Issue1363 {
    @Test
    public void test_for_issue() throws Exception {
        DataSimpleVO a = new DataSimpleVO("a", 1);
        DataSimpleVO b = new DataSimpleVO("b", 2);
        b.value = a;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(a.name, a);
        b.value1 = map;

        String jsonStr = JSON.toJSONString(b);
        System.out.println(jsonStr);
        DataSimpleVO obj = JSON.parseObject(jsonStr, DataSimpleVO.class);
        assertEquals(jsonStr, JSON.toJSONString(obj));
    }

    @Test
    public void test_for_issue_1() throws Exception {
        DataSimpleVO a = new DataSimpleVO("a", 1);
        DataSimpleVO b = new DataSimpleVO("b", 2);
        b.value1 = a;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(a.name, a);
        b.value = map;

        String jsonStr = JSON.toJSONString(b);
        System.out.println(jsonStr);
        DataSimpleVO obj = JSON.parseObject(jsonStr, DataSimpleVO.class);
        System.out.println(obj.toString());
        assertNotNull(obj.value1);
        assertEquals(jsonStr, JSON.toJSONString(obj));
    }

    public static class DataSimpleVO {
        public String name;
        public Object value;
        public Object value1;

        public DataSimpleVO() {
        }

        @JSONCreator
        public DataSimpleVO(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return "DataSimpleVO [name=" + name + ", value=" + value + ", value1=" + value1 + "]";
        }
    }
}
