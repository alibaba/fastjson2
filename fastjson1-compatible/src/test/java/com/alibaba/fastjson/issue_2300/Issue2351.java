package com.alibaba.fastjson.issue_2300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2351 {
    @Test
    public void test_for_issue() throws Exception {
//        ParserConfig.getGlobalInstance().setAsmEnable(false);
        // 创建空白对象
        Bean1 c = new Bean1();
        c.a = "";

        // 序列化
        // 输出[null,null]
        String s = JSON.toJSONString(c, SerializerFeature.BeanToArray);
        assertEquals("[\"\",null]", s);

        // 反序列化报错
        // Exception in thread "main" com.alibaba.fastjson.JSONException: syntax error, expect [, actual [
        JSON.parseObject(s, Bean1.class, Feature.SupportArrayToBean);
    }

    public static class Bean1 {
        public String a;

        public List<Bean2> b;
    }

    public static class Bean2 {
        private String c;

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }
    }
}
