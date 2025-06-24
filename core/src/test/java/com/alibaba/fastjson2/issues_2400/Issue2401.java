package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2401 {
    @Test
    public void test() {
        String str = "{\n" +
                "\t\"objects\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"objclass\": \"GamePropertySheet\",\n" +
                "\t\t\t\"aliases\": [\n" +
                "\t\t\t\t\"DefaultGameProps\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"objdata\": {\n" +
                "\t\t\t\t\"Plants\": [\n" +
                "\t\t\t\t\t\"plant1\",\n" +
                "\t\t\t\t\t\"plant2\"\n" +
                "\t\t\t\t]\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";

        String path = "$.objects[?(@.aliases[0] == 'DefaultGameProps')].objdata";
        Object result = JSONPath.eval(str, path);
        assertEquals("[{\"Plants\":[\"plant1\",\"plant2\"]}]", JSON.toJSONString(result));
    }
}
