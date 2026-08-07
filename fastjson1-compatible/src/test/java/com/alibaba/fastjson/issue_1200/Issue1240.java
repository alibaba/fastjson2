package com.alibaba.fastjson.issue_1200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 01/06/2017.
 */
public class Issue1240 {
    @Test
    public void test_for_issue() throws Exception {
        ParserConfig parserConfig = new ParserConfig();
//        parserConfig.setAutoTypeSupport(true);
        Class<?> objectClass;
        try {
            objectClass = Class.forName("org.springframework.util.LinkedMultiValueMap");
        } catch (Throwable ignored) {
            return;
        }
        Method add = objectClass.getMethod("add", Object.class, Object.class);
        if (add == null) {
            return;
        }

        Object map = objectClass.newInstance();
        add.invoke(map, "test", "11111");
        String str = JSON.toJSONString(map);
        assertEquals("{\"test\":[\"11111\"]}", str);
//        JSON.parseObject(test, Object.class, parserConfig, JSON.DEFAULT_PARSER_FEATURE);
    }
}
