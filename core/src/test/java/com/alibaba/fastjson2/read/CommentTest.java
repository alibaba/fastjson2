package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommentTest {
    @Test
    public void test() {
        JSONObject object = JSON.parseObject(STR);
        assertEquals("x1.x2.com", object.get("a1"));
    }

    @Test
    public void test1() {
        String str = "[\n" +
                "//aaaa\n" +
                "1,\n" +
                "2\n" +
                "]";
        JSONArray array = JSON.parseArray(str);
        assertEquals("[1,2]", array.toString());

        JSONArray array2 = JSON.parseArray(str.getBytes());
        assertEquals("[1,2]", array2.toString());
    }

    @Test
    public void test2() {
        String str = "[\n" +
                "//aaaa\n" +
                "1,\n" +
                "2\n" +
                "]";
        List<Long> array = JSON.parseArray(str, Long.class);
        assertEquals("[1,2]", JSON.toJSONString(array));
    }

    @Test
    public void test3() {
        String str = "[\n" +
                "//aaaa\n" +
                "{\"id\":123}\n" +
                "]";
        assertEquals("[{\"id\":123}]", JSON.toJSONString(JSON.parseArray(str, Bean3.class)));
        assertEquals("[{\"id\":123}]", JSON.toJSONString(JSON.parseArray(str.toCharArray(), Bean3.class)));
        assertEquals("[{\"id\":123}]", JSON.toJSONString(JSON.parseArray(str.getBytes(), Bean3.class)));
    }

    @Test
    public void test4() {
        String str = "[\n" +
                "//aaaa\n" +
                "//中国\n" +
                "{\n" +
                "//id\n" +
                "\"id\":123\n" +
                "}\n" +
                "]";
        assertEquals("[{\"id\":123}]", JSON.toJSONString(JSON.parseArray(str, Bean3.class)));
        assertEquals("[{\"id\":123}]", JSON.toJSONString(JSON.parseArray(str.toCharArray(), Bean3.class)));
        assertEquals("[{\"id\":123}]", JSON.toJSONString(JSON.parseArray(str.getBytes(), Bean3.class)));
    }

    public static class Bean3 {
        public int id;
    }

    public static String STR = " {\n" +
            "    \"a1\": \"x1.x2.com\",\n" +
            "    \"a2\": \"\",\n" +
            "    \"a3\": \"\",\n" +
            "    \"a4\": \"xn-e-p\",\n" +
            "    \"a5\":\"xn-mobile-sign\",\n" +
            "\n" +
            "    \"e1\":\"xn-e-p\",\n" +
            "    \"e2\":\"e1/m2\",\n" +
            "    \"e3\":\"fu::123456789:r/mailoa\",\n" +
            "    \"e4\":\"mailImageSession\",\n" +
            "\n" +
            "    \"r1\":\"zn-shz\",\n" +
            "    \"r2\":\"a.b.c.com\",\n" +
            "    \"r3\":\"acs:ram::xxxxx:role/xxx\",\n" +
            "    \"r4\":\"xxx\",\n" +
            "    //设置临时访问凭证的有效时间为3600秒\n" +
            "    \"r5\":\"3600\",\n" +
            "    \"r6\":\"kmesncs.com\",\n" +
            "    \"r7\":{\n" +
            "        \"m1\":\"url1\",\n" +
            "        \"m2\":\"kme\",\n" +
            "        \"m3\":\"xb\"\n" +
            "    },\n" +
            "      //现有上传bucket存放了代寄点门头照片\n" +
            "    \"r8\": \"xn-upload-production\",\n" +
            "        //历史代寄点存放的目录\n" +
            "    \"r9\":[\"c1\"\n" +
            "                        ,\"c2\",\n" +
            "                        \"c3\"\n" +
            "                        ,\"c4\"],\n" +
            "    \"r10\":\"http://abc.b.e.ef.com\",\n" +
            "    \"r11\":\"\",\n" +
            "    \"r12\":\"\",\n" +
            "    \"r13\":\"xn-express-production\"\n" +
            "    }";
}
