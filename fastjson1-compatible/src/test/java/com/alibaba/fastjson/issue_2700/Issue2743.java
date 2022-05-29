package com.alibaba.fastjson.issue_2700;

//
//import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2743 {
    // 场景：验证字符串数组，楼主提供的用例
    @Test
    public void test_0() throws Exception {
        String json = "{\"info\":{\"com.xxx.service.xxxServiceForOrder@queryGoodsV2(Long,Long,Long)\":[{\"method\":\"queryPrepayGoodsV2\"}]}}";
        Object obj = JSONPath.extract(json,
                "$['info']['com.xxx.service.xxxServiceForOrder@queryGoodsV2(Long,Long,Long)']");
        assertEquals("[{\"method\":\"queryPrepayGoodsV2\"}]", obj.toString());
    }

    // 场景：验证数字数组
    @Test
    public void test_1() throws Exception {
        String json = "[10,11,12,13,14,15,16,17,18,19,20]";
        Object obj = JSONPath.extract(json, "$[3,4]");
        assertEquals("[13,14]", obj.toString());
    }
//
//    // 场景：验证修复bug用的正则表达式
//    public void test_2() throws Exception {
//        String strArrayRegex = "\'\\s*,\\s*\'";
//        Pattern strArrayPattern = Pattern.compile(strArrayRegex);
//
//        assertFalse(
//                strArrayPattern.matcher("'com.xxx.service.xxxServiceForOrder@queryGoodsV2(Long,Long,Long)'").find());
//        assertTrue(strArrayPattern.matcher("'id','name'").find());
//        assertTrue(strArrayPattern.matcher("'id'    ,    'name'").find());
//        assertTrue(strArrayPattern.matcher("'id',    'name'").find());
//        assertTrue(strArrayPattern.matcher("'id'    ,'name'").find());
//
//        String[] strs = { "'com.xxx.service.xxxServiceForOrder@queryGoodsV2(Long,Long,Long)'" };
//        assertArrayEquals(strs, strs[0].split(strArrayRegex));
//
//        strs = new String[] { "'id", "name'" };
//        assertArrayEquals(strs, "'id','name'".split(strArrayRegex));
//        assertArrayEquals(strs, "'id'    ,    'name'".split(strArrayRegex));
//        assertArrayEquals(strs, "'id'    ,'name'".split(strArrayRegex));
//        assertArrayEquals(strs, "'id',    'name'".split(strArrayRegex));
//    }
}
