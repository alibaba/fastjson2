package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.utf8.UTF8Encode;
import com.dslplatform.json.DslJson;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DoubleTest {
    static String str = UTF8Encode.readFromClasspath("data/double_array_20.json");

    public static void main(String[] args) throws Exception {
        JSON.parseObject(str, double[].class);
        System.out.println(str);
    }

//    @Test
    public void parseDouble2() throws Exception {
        String str = "113.92966694974888";
        double d = Double.parseDouble(str);
        byte[] bytes = str.getBytes();
        double v = JSONReader.of(bytes).readDoubleValue();

        double v1 = new DslJson<>().deserialize(Double.class, bytes, bytes.length);
        assertEquals(d, v);
    }

//    @Test
    public void parseDoubles1() throws Exception {
        String[] strings = new String[] {
                "11.392966694974881",
                "11.392966694974882",
                "11.392966694974883",
                "11.392966694974884",
                "11.392966694974885",
                "11.392966694974886",
                "11.392966694974887",
                "11.392966694974888",
                "11.392966694974889",
                "11.392966694974890",
        };

        for (String str : strings) {
            byte[] bytes = str.getBytes();
            double d = Double.parseDouble(str);
            double v = new DslJson<>().deserialize(Double.class, bytes, bytes.length);
            assertEquals(d, v);
        }
    }
}
