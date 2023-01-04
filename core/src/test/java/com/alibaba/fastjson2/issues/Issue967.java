package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue967 {
    @Test
    public void test() {
        BeanA beanA = new BeanA();
        BeanB beanB = new BeanB();
        ArrayList<BeanB> list = new ArrayList<>();
        list.add(beanB);
        beanA.setList(list);

        SimplePropertyPreFilter filter1 = new SimplePropertyPreFilter();
        filter1.getExcludes().add("removeMe");
        String s1 = JSON.toJSONString(beanA, filter1);
        assertEquals("{\"list\":[{\"prop1\":\"test prop 1\",\"prop2\":\"test prop 2\"}]}", s1);

        SimplePropertyPreFilter filter2 = new SimplePropertyPreFilter(beanA.getClass());
        filter2.getExcludes().add("removeMe");
        String s2 = JSON.toJSONString(beanA, filter2);
        assertEquals("{\"list\":[{\"prop1\":\"test prop 1\",\"prop2\":\"test prop 2\"}]}", s2);
    }

    @Data
    public class BeanA {
        private List<BeanB> list;
        private String removeMe = "Please remove me";
    }

    @Data
    public class BeanB {
        private String prop1 = "test prop 1";
        private String prop2 = "test prop 2";
    }
}
