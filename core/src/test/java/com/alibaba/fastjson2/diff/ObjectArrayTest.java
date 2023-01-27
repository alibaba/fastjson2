package com.alibaba.fastjson2.diff;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.diff.mock.MetaData;
import com.alibaba.fastjson2.diff.mock.MockJson;
import com.alibaba.fastjson2.diff.path.JsonCompareResult;
import com.alibaba.fastjson2.diff.path.JsonComparedOption;
import org.junit.jupiter.api.Test;

import java.util.Stack;

public class ObjectArrayTest extends MockJson {

    private final static String expectPath = "diff/array/ObjectArrayExpect.json";

    private final static String actualPath = "diff/array/ObjectArrayActual.json";

    @Test
    public void diffKeepOrder() {
        MetaData metaData = load(expectPath, actualPath);
        JsonComparedOption jsonComparedOption = new JsonComparedOption().setIgnoreOrder(false);
        DefaultJSONDiff defaultJsonDifference = new DefaultJSONDiff();
        JsonCompareResult jsonCompareResult = defaultJsonDifference
            .option(jsonComparedOption)
            .detectDiff((JSONArray) metaData.getExpect(), (JSONArray) metaData.getActual());
        System.out.println(JSON.toJSONString(jsonCompareResult));
    }

    @Test
    public void diffIgnoreOrder() {
        MetaData metaData = load(expectPath, actualPath);
        JsonComparedOption jsonComparedOption = new JsonComparedOption().setIgnoreOrder(true).setKeyFunction(path -> {
            System.out.println(path);
            return null;
        });
        DefaultJSONDiff defaultJsonDifference = new DefaultJSONDiff();
        JsonCompareResult jsonCompareResult = defaultJsonDifference
            .option(jsonComparedOption)
            .detectDiff((JSONArray) metaData.getExpect(), (JSONArray) metaData.getActual());
        System.out.println(JSON.toJSONString(jsonCompareResult));
    }

    @Test
    public void diffIgnoreOrderAndKeyFUnction() {
        MetaData metaData = load(expectPath, actualPath);
        JsonComparedOption jsonComparedOption = new JsonComparedOption()
            .setIgnoreOrder(true)
            .setKeyFunction(path -> {
                Stack<String> keys = new Stack<>();
                keys.push("date");
                return keys;
            });
        DefaultJSONDiff defaultJsonDifference = new DefaultJSONDiff();
        JsonCompareResult jsonCompareResult = defaultJsonDifference
            .option(jsonComparedOption)
            .detectDiff((JSONArray) metaData.getExpect(), (JSONArray) metaData.getActual());
        System.out.println(JSON.toJSONString(jsonCompareResult));
    }


}
