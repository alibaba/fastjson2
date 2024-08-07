package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * JSON 处理方法
 *
 * @author zhangrong
 * @date 2024/08/07
 */
public class Issue2856 {
    @Test
    public void test1() {
        Map map1 = new HashMap();

        List list1 = new LinkedList();
        map1.put("list1", list1);

        Map map2 = new HashMap();
        list1.add(map2);

        List list2 = new LinkedList();
        map2.put("list2", list2);

        Map map3 = new HashMap();
        list2.add(map3);

        List list3 = new LinkedList();
        map3.put("list3", list3);

        list3.add("str1");
        list3.add("str2");
        list3.add("str3");

        Object o1 = JSON.toJSON(map1);
        Object o2 = ((JSONObject) o1).get("list1");
        Object o3 = ((JSONArray) o2).get(0);
        Object o4 = ((JSONObject) o3).get("list2");
        Object o5 = ((JSONArray) o4).get(0);
        Object o6 = ((JSONObject) o5).get("list3");
    }

    @Test
    public void test2() {
        List list1 = new LinkedList();
        List list2 = new LinkedList();
        list2.add("str1");
        list2.add("str2");

        list1.add(list2);

        JSONArray o1 = (JSONArray) JSON.toJSON(list1);
        JSONArray inner = (JSONArray) o1.get(0);

        System.out.println(inner);
    }

    @Test
    public void test3() {
        List list1 = new LinkedList();
        Map map1 = new HashMap();

        map1.put("key", "val");
        list1.add(map1);

        JSONArray o1 = (JSONArray) JSON.toJSON(list1);
        JSONObject inner = (JSONObject) o1.get(0);

        System.out.println(inner);
    }
}
