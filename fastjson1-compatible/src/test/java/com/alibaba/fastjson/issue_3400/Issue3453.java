package com.alibaba.fastjson.issue_3400;

import com.alibaba.fastjson.JSONValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Description:  <br>
 *
 * @author byw
 * @create 2020/9/20
 */
public class Issue3453 {
    @Test
    public void test_for_issue() throws Exception {
        String str = "[\n" +
                " {\n" +
                " \"altitude\": 109.0,\n" +
                " \"angle\": 5.0,\n" +
                " \"index\": 1,\n" +
                " \"type\": 1\n" +
                " },\n" +
                " {\n" +
                " \"altitude\": 1307.0,\n" +
                " \"angle\": 5.0,\n" +
                " \"index\": 2,\n" +
                " \"type\": 1\n" +
                " },\n" +
                " {\n" +
                " \"altitude\": 22.0,\n" +
                " \"angle\": 7.0,\n" +
                " \"index\": 3,\n" +
                " \"type\": 1\n" +
                " },\n" +
                " {\n" +
                " \"altitude\": 22.0,\n" +
                " \"angle\": 7.0,\n" +
                " \"index\": 4,\n" +
                " \"type\": 2\n" +
                " },\n" +
                " {\n" +
                " \"altitude\": 22.0,\n" +
                " \"angle\": 7.0,\n" +
                " \"index\": 5,\n" +
                " \"type\": 2\n" +
                " },\n" +
                " {\n" +
                " \"altitude\": 22.0,\n" +
                " \"angle\": 7.0,\n" +
                " \"index\": 6,\n" +
                " \"type\": 2\n" +
                " },\n" +
                " {\n" +
                " \"altitude\": 22.0,\n" +
                " \"angle\": 7.0,\n" +
                " \"index\": 7,\n" +
                " \"type\": 2\n" +
                " }\n" +
                "]";
        JSONValidator validator = JSONValidator.from(str);
        Assertions.assertTrue(validator.validate());
        JSONValidator.Type type = validator.getType();
        Assertions.assertEquals("Array", type.name());
    }
}
