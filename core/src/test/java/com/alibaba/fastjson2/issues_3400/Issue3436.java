package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue3436 {
    @Getter
    @Setter
    public static class Bean {
        private double a;
        private double b;
        private double c;
    }

    @Test
    public void test() {
        Bean bean = new Bean();
        bean.setA(1.0);
        bean.setB(2.0);
        bean.setC(3.0);

        List<Bean> beanList = new ArrayList<>();
        beanList.add(bean);
        beanList.add(bean);

        String json2 = JSON.toJSONString(beanList, JSONWriter.Feature.ReferenceDetection);
        assertEquals("[{\"a\":1.0,\"b\":2.0,\"c\":3.0},{\"$ref\":\"$[0]\"}]", json2);

        List<Bean> json2Bean = JSON.parseArray(json2, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertSame(json2Bean.get(0), json2Bean.get(1));
    }
}
