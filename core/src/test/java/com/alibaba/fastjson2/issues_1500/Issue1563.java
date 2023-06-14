package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Issue1563 {
    @Test
    public void test() {
        List<List<Object>> data = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            List<Object> row = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                row.add("i_" + i + "_j_" + j);
            }
            data.add(row);
        }
        MyData myData = new MyData(data);
        assertEquals("{\"data\":[[\"i_0_j_0\",\"i_0_j_1\"],[\"i_1_j_0\",\"i_1_j_1\"]]}", JSON.toJSONString(myData));
    }

    public static class MyData {
        private final List<List<Object>> list;

        public MyData(List<List<Object>> list) {
            this.list = list;
        }

        public Iterable<Object> getData() {
            Iterator<List<Object>> it = list.iterator();
            return () -> new Iterator<Object>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Object next() {
                    return it.next();
                }
            };
        }
    }
}
