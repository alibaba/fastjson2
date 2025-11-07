package com.alibaba.fastjson2.example.solontest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonTest;

/**
 * @author noear 2024/10/2 created
 */
@SolonTest(SolonTestApp.class)
public class DemoTest
        extends HttpTester {
    @Test
    public void ok_post_json() throws Exception {
        String json = "{\"password\":\"1234\",\"username\":\"world\"}";

        String json2 = path("/demo").bodyJson(json).post();

        Assertions.assertEquals(json, json2);
    }

    @Test
    public void ok_get() throws Exception {
        String json = "{\"password\":\"1234\",\"username\":\"world\"}";

        String json2 = path("/demo?username=world&password=1234").get();

        Assertions.assertEquals(json, json2);
    }

    @Test
    public void error() throws Exception {
        String json = "{\"code\":404,\"description\":\"Not Found: GET /error\"}";

        String json2 = path("/error").get();

        Assertions.assertEquals(json, json2);
    }
}
