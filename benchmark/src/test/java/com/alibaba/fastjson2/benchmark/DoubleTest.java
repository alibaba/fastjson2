package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.benchmark.eishay.EishayParseTreeString;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public class DoubleTest {
    static String str;

    static {
        try {
            InputStream is = EishayParseTreeString.class.getClassLoader().getResourceAsStream("data/double_array_20.json");
            str = IOUtils.toString(is, "UTF-8");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        JSON.parseObject(str, double[].class);
        System.out.println(str);
    }
}
