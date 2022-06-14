package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPath_18 {
    @Test
    public void test() {
        String str = "{ \"inputs\": {\n" +
                "        \"sls_log_archive.sls_cn_hongkong_to_odps_temp/project_id=83611/category=log_game/import_time=1655031945_0\": [\n" +
                "            1323,\n" +
                "            19483761\n" +
                "        ]\n" +
                "    }}";

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
    }
}
