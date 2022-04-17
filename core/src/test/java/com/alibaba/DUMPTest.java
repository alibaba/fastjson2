package com.alibaba;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson_perf.Int2Test;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Base64;

public class DUMPTest {
    private String str;
    private byte[] jsonbBytes;

    public DUMPTest() throws Exception {
        InputStream is = Int2Test.class.getClassLoader().getResourceAsStream("long_text_2022-01-05-20-07-33.txt");
        str = IOUtils.toString(is, "UTF-8");
        jsonbBytes = Base64.getDecoder().decode(str);
    }

    @Test
    public void test_dump() {
//        System.out.println(str);
        JSONB.parse(jsonbBytes);


    }
}
