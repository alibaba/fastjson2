package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONPath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author 风起
 * @date 2022/05/23
 * @since 2022/5/23 19:58
 */
public class Issue88 {

    String testJson
        = "{\"status\":\"success\",\"data\":{\"resultType\":\"matrix\",\"result\":[{\"metric\":{},"
        + "\"values\":[[1632273205,\"3\"],[1632273210,\"3\"],[1632273215,\"3\"],[1632273220,\"3\"],[1632273225,"
        + "\"3\"],[1632273230,\"3\"],[1632273235,\"3\"],[1632273240,\"3\"],[1632273245,\"3\"],[1632273250,\"3\"],"
        + "[1632273255,\"3\"],[1632273260,\"3\"],[1632273265,\"3\"],[1632273270,\"3\"],[1632273275,\"3\"],"
        + "[1632273280,\"3\"],[1632273285,\"3\"],[1632273290,\"3\"],[1632273295,\"3\"],[1632273300,\"3\"],"
        + "[1632273305,\"3\"]]}]}}";

    @Test
    public void test() {
        Object result = JSONPath.eval(testJson, "$.data.result[0].values[*][0]");
        assertNotNull(result);
        assertEquals("[1632273205,1632273210,1632273215,1632273220,1632273225,1632273230,1632273235,1632273240,"
                         + "1632273245,1632273250,1632273255,1632273260,1632273265,1632273270,1632273275,1632273280,"
                         + "1632273285,1632273290,1632273295,1632273300,1632273305]", result.toString());
        System.out.println(result);
    }

    @Test
    public void test1() {
        Object result = JSONPath.extract(testJson, "$.data.result[0].values[*][0]");
        assertNotNull(result);
        assertEquals("[1632273205,1632273210,1632273215,1632273220,1632273225,1632273230,1632273235,1632273240,"
                         + "1632273245,1632273250,1632273255,1632273260,1632273265,1632273270,1632273275,1632273280,"
                         + "1632273285,1632273290,1632273295,1632273300,1632273305]", result.toString());
        System.out.println(result);
    }

}
