package com.alibaba.fastjson2.write;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameLengthTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        byte[] bytes = JSON.toJSONBytes(bean);
        String str = new String(bytes);
        assertEquals("{\"v\":0,\"v1\":0,\"v12\":0,\"v123\":0,\"v1234\":0,\"v12345\":0,\"v123456\":0,\"v1234567\":0,\"v12345678\":0,\"v123456789\":0}", str);
        Bean bean1 = JSON.parseObject(bytes, Bean.class);
        assertEquals(str, new String(JSON.toJSONBytes(bean1)));
    }

    public static class Bean {
        public int v;
        public int v1;
        public int v12;
        public int v123;
        public int v1234;
        public int v12345;
        public int v123456;
        public int v1234567;
        public int v12345678;
        public int v123456789;
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{\"v11\":1,\"v211\":2,\"v3111\":3,\"v41111\":4}", Bean1.class);
        byte[] bytes = JSON.toJSONBytes(bean);
        String str = new String(bytes);
        assertEquals("{\"v11\":1,\"v211\":2,\"v3111\":3,\"v41111\":4}", str);
        Bean1 bean1 = JSON.parseObject(bytes, Bean1.class);
        assertEquals(str, new String(JSON.toJSONBytes(bean1)));
    }

    public static class Bean1 {
        public int v11;
        public int v211;
        public int v3111;
        public int v41111;
    }

    @Test
    public void test2() {
        String input = "{\"uri\":101,\"width\":1024}";
        Bean2 bean = JSON.parseObject(input, Bean2.class);
        byte[] bytes = JSON.toJSONBytes(bean);
        String str = new String(bytes);
        assertEquals(input, str);
        Bean2 bean1 = JSON.parseObject(bytes, Bean2.class);
        assertEquals(str, new String(JSON.toJSONBytes(bean1)));
    }

    public static class Bean2 {
        public int uri;
        public int width;
    }

    @Test
    public void test3() {
        String input = "{\"v11\":101,\"v22\":1024}";
        Bean3 bean = JSON.parseObject(input, Bean3.class);
        byte[] bytes = JSON.toJSONBytes(bean);
        String str = new String(bytes);
        assertEquals(input, str);
        Bean3 bean1 = JSON.parseObject(bytes, Bean3.class);
        assertEquals(str, new String(JSON.toJSONBytes(bean1)));
    }

    public static class Bean3 {
        public int v11;
        public int v22;
    }

    @Test
    public void test4() {
        String input = "{\"v001\":101,\"v002\":1024}";
        Bean4 bean = JSON.parseObject(input, Bean4.class);
        byte[] bytes = JSON.toJSONBytes(bean);
        String str = new String(bytes);
        assertEquals(input, str);
        Bean4 bean1 = JSON.parseObject(bytes, Bean4.class);
        assertEquals(str, new String(JSON.toJSONBytes(bean1)));
    }

    public static class Bean4 {
        public int v001;
        public int v002;
    }

    @Test
    public void test5() {
        String input = "{\"v001\":101,\"v002\":102,\"v003\":103}";
        Bean5 bean = JSON.parseObject(input, Bean5.class);
        byte[] bytes = JSON.toJSONBytes(bean);
        String str = new String(bytes);
        assertEquals(input, str);
        Bean5 bean1 = JSON.parseObject(bytes, Bean5.class);
        assertEquals(str, new String(JSON.toJSONBytes(bean1)));
    }

    public static class Bean5 {
        public int v001;
        public int v002;
        public int v003;
    }

    @Test
    public void Bean6() {
        String input = "{\"v1234\":101,\"v2345\":102,\"v3456\":103}";
        Bean6 bean = JSON.parseObject(input, Bean6.class);
        byte[] bytes = JSON.toJSONBytes(bean);
        String str = new String(bytes);
        assertEquals(input, str);
        Bean6 bean1 = JSON.parseObject(bytes, Bean6.class);
        assertEquals(str, new String(JSON.toJSONBytes(bean1)));
    }

    public static class Bean6 {
        public int v1234;
        public int v2345;
        public int v3456;
    }

    @Test
    public void test7() {
        String str = "{\"v1234\":101}";
        byte[] bytes = str.getBytes();
        char[] chars = str.toCharArray();

        JSONReader readerUTF8 = JSONReader.of(bytes);

        readerUTF8.next();
        assertEquals('"', readerUTF8.current());
        assertEquals(842102306, readerUTF8.getRawInt());

        JSONReader readerUTF16 = JSONReader.of(chars);
        readerUTF16.next();
        assertEquals('"', readerUTF16.current());
        assertEquals(842102306, readerUTF16.getRawInt());
    }

    @Test
    public void test7_long() {
        String str = "{\"v12345\":101}";
        byte[] bytes = str.getBytes();
        char[] chars = str.toCharArray();

        JSONReader readerUTF8 = JSONReader.of(bytes);

        readerUTF8.next();
        assertEquals('"', readerUTF8.current());
        assertEquals(2464933765545293346L, readerUTF8.getRawLong());

        JSONReader readerUTF16 = JSONReader.of(chars);
        readerUTF16.next();
        assertEquals('"', readerUTF16.current());
        assertEquals(2464933765545293346L, readerUTF16.getRawLong());
    }

    @Test
    public void test8() {
        String input = "{\"profile_background_image_url\":\"http://a3.twimg.com/a/1301071706/images/themes/theme1/bg.png\",\"profile_use_background_image\":\"true\"}";
        Bean8 bean = JSON.parseObject(input, Bean8.class);
        byte[] bytes = JSON.toJSONBytes(bean);
        String str = new String(bytes);
        assertEquals(input, str);
        Bean8 bean1 = JSON.parseObject(bytes, Bean8.class);
        assertEquals(str, new String(JSON.toJSONBytes(bean1)));
    }

    public static class Bean8 {
        @JSONField(name = "profile_background_image_url")
        public String profileBackgroundImageUrl;
        @JSONField(name = "profile_use_background_image")
        public String profileUseBackgroundImage;
    }

    @Test
    public void test9() {
        Bean9 bean = new Bean9();
        JSONObject jsonObject = JSONObject.from(bean);
        for (Iterator<Map.Entry<String, Object>> it = jsonObject.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            entry.setValue(entry.getKey().length());
        }

        String str = jsonObject.toJSONString();
        {
            Bean9 bean1 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean9.class);
            assertEquals(str, JSON.toJSONString(bean1));
        }
        {
            Bean9 bean1 = JSON.parseObject(str.toCharArray(), Bean9.class);
            assertEquals(str, JSON.toJSONString(bean1));
        }
    }

    public static class Bean9 {
        public int v2;
        public int v32;
        public int v423;
        public int v5234;
        public int v62345;
        public int v723456;
        public int v8234567;
        public int v92345678;
        public int v103456789;
        public int v113456789A;
        public int v123456789A1;
        public int v133456789A23;
        public int v143456789A234;
        public int v153456789A2345;
        public int v163456789A23456;
        public int v173456789A234567;
        public int v183456789A2345678;
        public int v193456789A23456789;
        public int v203456789A23456789A;
    }

    @Test
    public void test10() {
        Bean10 bean = new Bean10();
        JSONObject jsonObject = JSONObject.from(bean);
        for (Iterator<Map.Entry<String, Object>> it = jsonObject.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            entry.setValue(entry.getKey().length());
        }

        String str = jsonObject.toJSONString();
        {
            Bean10 bean1 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean10.class);
            assertEquals(str, JSON.toJSONString(bean1));
        }
        {
            Bean10 bean1 = JSON.parseObject(str.toCharArray(), Bean10.class);
            assertEquals(str, JSON.toJSONString(bean1));
        }
    }

    public static class Bean10 {
        public int v203456789A23456789A;
        public int v213456789A23456789A1;
        public int v223456789A23456789A12;
        public int v233456789A23456789A123;
        public int v243456789A23456789A1234;
        public int v253456789A23456789A12345;
        public int v263456789A23456789A123456;
        public int v273456789A23456789A1234567;
        public int v283456789A23456789A12345678;
        public int v293456789A23456789A123456789;
        public int v303456789A23456789A123456789A;
    }

    @Test
    public void test11() {
        Bean11 bean = new Bean11();
        JSONObject jsonObject = JSONObject.from(bean);
        for (Iterator<Map.Entry<String, Object>> it = jsonObject.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            entry.setValue(entry.getKey().length());
        }

        String str = jsonObject.toJSONString();
        {
            Bean11 bean1 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean11.class);
            assertEquals(str, JSON.toJSONString(bean1));
        }
        {
            Bean11 bean1 = JSON.parseObject(str.toCharArray(), Bean11.class);
            assertEquals(str, JSON.toJSONString(bean1));
        }
    }

    public static class Bean11 {
        public int v303456789A23456789A123456789A;
        public int v313456789A23456789A123456789A1;
        public int v323456789A23456789A123456789A12;
        public int v333456789A23456789A123456789A123;
        public int v343456789A23456789A123456789A1234;
        public int v353456789A23456789A123456789A12345;
        public int v363456789A23456789A123456789A123456;
        public int v373456789A23456789A123456789A1234567;
        public int v383456789A23456789A123456789A12345678;
        public int v393456789A23456789A123456789A123456789;
        public int v403456789A23456789A123456789A123456789A;
        public int v413456789A23456789A123456789A123456789A1;
        public int v423456789A23456789A123456789A123456789A12;
        public int v433456789A23456789A123456789A123456789A123;
    }
}
