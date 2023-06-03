package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue1515 {
    @Test
    public void test() {
        A a = new A("a");
        ArrayList<A> listA = new ArrayList<>();
        listA.add(a);
        B b = new B(listA, listA);

        // fastjson2
        String jsonStr = JSON.toJSONString(b, JSONWriter.Feature.ReferenceDetection);
        assertEquals("{\"listA\":[{\"a\":\"a\"}],\"refListA\":{\"$ref\":\"$.listA\"}}", jsonStr);
        B parsed = JSON.parseObject(jsonStr, B.class);
        assertSame(parsed.listA, parsed.refListA);
    }

    @Data
    @AllArgsConstructor
    class A {
        String a;
    }

    @Data
    @AllArgsConstructor
    class B {
        ArrayList<A> listA;
        ArrayList<A> refListA;
    }
}
