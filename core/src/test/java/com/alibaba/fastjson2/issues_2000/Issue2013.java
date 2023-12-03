package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2013 {
    @Test
    public void test() {
        A a = new A();
        a.id = 1;
        a.name = "n1";
        a.carr = new C[2];
        a.carr[0] = new C();
        a.carr[0].id = 11;
        a.carr[1] = new C();
        a.carr[1].id = 22;

        A a2 = new A();
        a2.id = 2;
        a2.name = "n2";

        B b = new B();
        b.a = new A[]{a, a2};

        String str = JSON.toJSONString(b);
        assertEquals(
                "{\"a\":[[1,\"n1\",0,[{\"id\":11,\"count\":0},{\"id\":22,\"count\":0}]],[2,\"n2\",0,null]]}",
                str);

        B b2 = JSON.parseObject(str, B.class);
        assertEquals(str, JSON.toJSONString(b2));

        ObjectWriterProvider provider = new ObjectWriterProvider(ObjectWriterCreator.INSTANCE);
        JSONWriter jsonWriter = JSONWriter.of(JSONFactory.createWriteContext(provider));
        jsonWriter.writeAny(b);
        String str1 = jsonWriter.toString();
        assertEquals(str, str1);
    }

    public static class B {
        public A[] a;
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.BeanToArray,
            deserializeFeatures = JSONReader.Feature.SupportArrayToBean,
            orders = {"id", "name", "ver", "carr"}
    )
    public static class A {
        public int id;
        public String name;
        public int ver;
        public C[] carr;
    }

    @JSONType(orders = {"id", "count"})
    public static class C {
        public int id;
        public int count;
    }
}
