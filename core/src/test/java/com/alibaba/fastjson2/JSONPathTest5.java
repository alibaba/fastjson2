package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPathTest5 {
    @Test
    public void test_0() {
        List<A> list = Arrays.asList(
                new A(101, "DataWorks"),
                new A(102, "MaxCompute"),
                new A(103, "Flink")
        );
        byte[] jsonbBytes = JSONB.toBytes(list);

        JSONBDump.dump(jsonbBytes);

        JSONPath path = JSONPath.of("$[0].id");
        assertEquals(
                101,
                path.extract(
                        JSONReader.ofJSONB(jsonbBytes)
                )
        );

        JSONArray array = JSONB.parseArray(jsonbBytes);
        assertEquals(
                101,
                path.eval(array)
        );

        JSONArray array2 = (JSONArray) JSONB.parse(jsonbBytes);
        assertEquals(
                101,
                path.eval(array2)
        );
    }

    public static class A {
        public int id;
        public String name;

        public A() {
        }

        public A(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
