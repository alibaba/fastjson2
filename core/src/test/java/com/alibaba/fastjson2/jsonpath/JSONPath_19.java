package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPath_19 {
    private static String str;

    static {
        try {
            InputStream is = JSONPath_19.class.getClassLoader().getResourceAsStream("data/eishay.json");
            str = IOUtils.toString(is, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void test() {
        System.out.println(str);

        String path = "$.media['bitrate','duration']";

        JSONPath jsonPath = JSONPath.of(path);
        assertEquals("[262144,18000000]",
                JSON.toJSONString(
                        jsonPath.extract(
                                JSONReader.of(str)
                        )
                )
        );
    }
}
