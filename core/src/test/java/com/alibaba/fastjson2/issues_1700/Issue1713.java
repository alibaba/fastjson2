package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.ContextNameFilter;
import com.alibaba.fastjson2.filter.ContextValueFilter;
import com.alibaba.fastjson2.filter.Filter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class Issue1713 {
    @Data
    public static class Bean {
        private String filed1;
        public String filed2;
    }

    @Test
    public void test() {
        Bean bean = new Bean();
        bean.filed1 = "1";
        bean.filed2 = "2";
        List<Filter> filters = new ArrayList<>();
        filters.add((ContextValueFilter) (context, object, name, value) -> {
            System.out.println("ContextValueFilter field: " + context.getField());
            return value;
        });

        filters.add((ContextNameFilter) (context, object, name, value) -> {
            System.out.println("ContextNameFilter field: " + context.getField());
            return name;
        });

        System.out.print(JSON.toJSONString(bean, filters.toArray(new Filter[1])));
//        ContextNameFilter field: private java.lang.String com.alibaba.fastjson2.issuse_1700.Issue1713$Bean.filed1
//        ContextValueFilter field: private java.lang.String com.alibaba.fastjson2.issuse_1700.Issue1713$Bean.filed1
//        ContextNameFilter field: public java.lang.String com.alibaba.fastjson2.issuse_1700.Issue1713$Bean.filed2
//        ContextValueFilter field: public java.lang.String com.alibaba.fastjson2.issuse_1700.Issue1713$Bean.filed2
//        {"filed1":"1","filed2":"2"}
    }
}
