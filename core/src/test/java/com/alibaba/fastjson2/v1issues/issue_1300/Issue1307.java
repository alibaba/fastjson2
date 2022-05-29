package com.alibaba.fastjson2.v1issues.issue_1300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by kimmking on 02/07/2017.
 */
public class Issue1307 {
    ValueFilter contextValueFilter = new ValueFilter() {
        public Object apply(Object obj, String name, Object value) {
            return "mark-" + value;
        }
    };
    ValueFilter valueFilter = new ValueFilter() {
        public Object apply(Object object, String name, Object value) {
            return value;
        }
    };

    @Test
    public void test_context_value_filter_not_effected() {
        List<Object> params = new ArrayList<Object>();
        Map data = new HashMap();
        data.put("name", "ace");
        params.add(data);
        //fail Actual   :[{"name":"ace"}]
        assertEquals("[{\"name\":\"mark-ace\"}]",
                JSON.toJSONString(params,
                        new Filter[]{
                                contextValueFilter
                        })
        );
    }

    @Test
    public void test_context_value_filter_effected() {
        List<Object> params = new ArrayList<Object>();
        Map data = new HashMap();
        data.put("name", "ace");
        params.add(data);
        //success
        assertEquals("[{\"name\":\"ace\"}]", JSON.toJSONString(params,
                new Filter[]{
                        valueFilter
                }));
    }
}
