package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBStrTest {
    @Test
    public void test_0() {
        {
            String str = "中国";
            byte[] bytes = JSONB.toBytes((Object) str);
            String str1 = (String) JSONB.parse(bytes);
            assertEquals(str, str1);
            String str2 = JSONB.parseObject(bytes, String.class);
            assertEquals(str, str2);
        }
        {
            String str = "良亮国AA级护眼台灯学习专用学生儿童书桌写作业灯宿舍床头阅读灯";
            byte[] bytes = JSONB.toBytes((Object) str);
            String str1 = (String) JSONB.parse(bytes);
            assertEquals(str, str1);
            String str2 = JSONB.parseObject(bytes, String.class);
            assertEquals(str, str2);
        }
        {
            String str = "abcdefg中国hijklam1233235252362";
            byte[] bytes = JSONB.toBytes((Object) str);
            String str1 = (String) JSONB.parse(bytes);
            assertEquals(str, str1);
            String str2 = JSONB.parseObject(bytes, String.class);
            assertEquals(str, str2);
        }
        {
            String str = "01234567890123456789012345678901234567890123456789012345678901234567890123456789";
            byte[] bytes = JSONB.toBytes((Object) str);
            String str1 = (String) JSONB.parse(bytes);
            assertEquals(str, str1);
            String str2 = JSONB.parseObject(bytes, String.class);
            assertEquals(str, str2);
        }
        {
            String str = "queryParams\":\"^^$$Z491371xxx9e09b0c5cc198c0586|null|xxyzbcuy2_gc_202112270958258383_167837|gexxxralV2{$_$}{中国acdedfgh";
            byte[] bytes = JSONB.toBytes((Object) str);
            String str1 = (String) JSONB.parse(bytes);
            assertEquals(str, str1);
            String str2 = JSONB.parseObject(bytes, String.class);
            assertEquals(str, str2);
        }
    }

    @Test
    public void test_1() {
        {
            String str = "1234中";
            byte[] bytes = JSONB.toBytes((Object) str);
            String str1 = (String) JSONB.parse(bytes);
            assertEquals(str, str1);
            String str2 = JSONB.parseObject(bytes, String.class);
            assertEquals(str, str2);
        }
    }
}
