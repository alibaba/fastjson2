package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class JSONPath_18 {
    String str = "{ \"inputs\": {\n" +
            "        \"sls_log_archive.mytable/project_id=101/category=log_game/import_time=1655031945_0\": [\n" +
            "            1323,\n" +
            "            19483761\n" +
            "        ]\n" +
            "    }}";

    @Test
    public void test() {
        assertEquals(
                1323L,
                JSONPath
                        .of("$.inputs.values()[*][0].sum()")
                        .eval(JSON.parseObject(str))
        );

        assertEquals(
                19483761L,
                JSONPath
                        .of("$.inputs.values()[*][1].sum()")
                        .eval(JSON.parseObject(str))
        );
        assertEquals(
                19483761L,
                JSONPath
                        .of("$.inputs.values()[*][1].sum()")
                        .extract(JSONReader.of(str))
        );
    }

    @Test
    public void test1() {
        assertEquals(
                "[\"sls_log_archive.mytable/project_id=101/category=log_game/import_time=1655031945_0\"]",
                JSONPath
                        .of("$.inputs.keys()")
                        .eval(JSON.parseObject(str))
                        .toString()
        );
    }

    @Test
    public void test2() {
        Object object = new Object();
        assertSame(object, JSONPath.of("$").eval(object));
    }

    @Test
    public void testEntrySet() {
        assertEquals(
                "[{\"key\":\"sls_log_archive.mytable/project_id=101/category=log_game/import_time=1655031945_0\",\"value\":[1323,19483761]}]",
                JSONPath
                        .of("$.inputs.entrySet()")
                        .eval(JSON.parseObject(str))
                        .toString()
        );
    }
}
