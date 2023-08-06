package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kraity
 */
public class Issue1707 {
    @Test
    public void test0() {
        String text = "{\"order\":{\"column\":\"post_sort\",\"sequences\":\"asc\"}}";
        TestModule test = JSON.parseObject(text, TestModule.class);

        assertNotNull(test);
        assertNotNull(test.order);
        assertEquals(2, test.order.size());
        assertEquals("asc", test.order.get("sequences"));
    }

    @Test
    public void test1() {
        String text = "{\"dictKey\":\"sys_post\",\"tableName\":\"sys_post\",\"queryColumns\":{\"code\":\"post_id\",\"sort\":\"post_sort\",\"label\":\"post_name\",\"value\":\"post_id\"},\"params\":{\"orCondition\":[{\"column\":\"status\",\"operator\":\"<>\",\"value\":1},{\"column\":\"status\",\"operator\":\"is\",\"value\":null}]},\"orders\":[{\"column\":\"post_sort\",\"sequences\":\"asc\"},{\"column\":\"create_time\",\"sequences\":\"desc\"}]}";
        DictionaryModule dic = JSON.parseObject(text, DictionaryModule.class);

        assertNotNull(dic);
        assertNotNull(dic.orders);
        assertEquals(2, dic.orders.size());
        assertEquals(2, dic.orders.get(0).size());
        assertEquals(2, dic.orders.get(1).size());
        assertEquals("desc", dic.orders.get(1).get("sequences"));
    }

    @Getter
    @Setter
    public static class TestModule
            implements Serializable {
        private static final long serialVersionUID = -1L;
        private Hashtable<String, String> order;
    }

    @Getter
    @Setter
    public static class DictionaryModule
            implements Serializable {
        private static final long serialVersionUID = -1L;
        private String dictKey;
        private String tableName;
        private Hashtable<String, String> queryColumns;
        private Hashtable<String, List<HashMap<String, Object>>> params;
        private List<Hashtable<String, String>> orders;
    }
}
