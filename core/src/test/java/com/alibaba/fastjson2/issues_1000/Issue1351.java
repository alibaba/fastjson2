package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1351 {
    @Test
    public void test() {
        Bean bean = new Bean();
        Inner inner = new Inner();
        ArrayList<Inner> innerList = new ArrayList<>();
        innerList.add(inner);
        HashMap<String, List<Inner>> map = new HashMap<>();
        map.put("innerList", innerList);
        List<Map<String, List<Inner>>> list = new ArrayList<>();
        list.add(map);
        bean.setList(list);
        String toJson = JSON.toJSONString(bean);
        assertEquals("{\"list\":[{\"innerList\":[{\"f1\":0}]}]}", toJson);
        Bean fromJson = JSON.parseObject(toJson, Bean.class);
        assertEquals(1, fromJson.getList().size());
    }

    public static class Bean {
        List<Map<String, List<Inner>>> list;

        public List<Map<String, List<Inner>>> getList() {
            return list;
        }

        public void setList(List<Map<String, List<Inner>>> list) {
            this.list = list;
        }
    }

    static class Inner {
        private int f1;

        public int getF1() {
            return f1;
        }

        public void setF1(int f1) {
            this.f1 = f1;
        }
    }
}
