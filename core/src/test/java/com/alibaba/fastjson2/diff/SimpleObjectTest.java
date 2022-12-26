package com.alibaba.fastjson2.diff;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.diff.mock.MetaData;
import com.alibaba.fastjson2.diff.mock.MockJson;
import com.alibaba.fastjson2.diff.path.JsonCompareResult;
import com.alibaba.fastjson2.diff.path.JsonComparedOption;
import org.junit.jupiter.api.Test;

public class SimpleObjectTest extends MockJson {

    private final static String expectPath = "diff/object/SimpleObjectExpect.json";

    private final static String actualPath = "diff/object/SimpleObjectActual.json";

    @Test
    public void diffKeepOrder() {
        MetaData metaData = load(expectPath, actualPath);
        JsonComparedOption jsonComparedOption = new JsonComparedOption().setIgnoreOrder(false);
        DefaultJSONDiff defaultJsonDifference = new DefaultJSONDiff();
        JsonCompareResult jsonCompareResult = defaultJsonDifference
                .option(jsonComparedOption)
                .detectDiff((JSONObject) metaData.getExpect(), (JSONObject) metaData.getActual());
        System.out.println(JSON.toJSONString(jsonCompareResult));
    }

    @Test
    public void diffIgnoreOrder() {
        MetaData metaData = load(expectPath, actualPath);
        JsonComparedOption jsonComparedOption = new JsonComparedOption()
                .setIgnoreOrder(true)
                .setKeyFunction(path -> {
                    System.out.println(path);
                    return null;
                });
        DefaultJSONDiff defaultJsonDifference = new DefaultJSONDiff();
        JsonCompareResult jsonCompareResult = defaultJsonDifference
                .option(jsonComparedOption)
                .detectDiff((JSONObject) metaData.getExpect(), (JSONObject) metaData.getActual());
        System.out.println(JSON.toJSONString(jsonCompareResult));
    }

}
