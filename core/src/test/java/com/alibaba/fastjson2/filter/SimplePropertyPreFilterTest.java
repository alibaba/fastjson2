package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplePropertyPreFilterTest {
    @Test
    public void test() {
        PropertyPreFilter[] filters = new PropertyPreFilter[2];
        SimplePropertyPreFilter filter1 = new SimplePropertyPreFilter(BeanB.class);
        String[] inc1 = {"uname", "age"};
        filter1.getIncludes().addAll(Arrays.asList(inc1));
        filters[0] = filter1;
        SimplePropertyPreFilter filter2 = new SimplePropertyPreFilter(BeanC.class);
        String[] inc2 = {"season"};
        filter2.getIncludes().addAll(Arrays.asList(inc2));
        filters[1] = filter2;

        List<BeanA> list = new ArrayList<>();
        BeanA beanA = new BeanA();
        BeanB beanB = new BeanB();
        BeanC beanC = new BeanC();
        beanB.setUname("user1");
        beanB.setId("1");
        beanB.setAge("20");
        beanC.setSeason("2024");
        beanC.setYear("2023");
        beanC.setEvent("nba");
        beanA.setB(beanB);
        beanA.setC(beanC);
        beanA.setNumber(2);
        list.add(beanA);

        String str = JSON.toJSONString(list, filters, JSONWriter.Feature.WriteMapNullValue);
        assertEquals("[{\"b\":{\"age\":\"20\",\"id\":\"1\",\"uname\":\"user1\"},\"c\":{\"season\":\"2024\"},\"number\":2}]", str);
    }

    @Data
    static class BeanA {
        private BeanB b;
        private BeanC c;
        private int number;
    }

    @Data
    static class BeanB {
        private String uname;
        private String id;
        private String age;
    }

    @Data
    static class BeanC {
        private String season;
        private String year;
        private String event;
    }
}
