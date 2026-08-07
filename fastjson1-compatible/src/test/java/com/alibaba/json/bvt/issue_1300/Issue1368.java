package com.alibaba.json.bvt.issue_1300;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by kimmking on 03/08/2017.
 */
public class Issue1368 {
    @Test
    public void test_for_issue() throws Exception {
        ExtendedServletRequestDataBinder binder = new ExtendedServletRequestDataBinder(new Object());
        String json = JSON.toJSONString(binder);
        System.out.println(json);
        assertTrue(json.indexOf("$ref") >= 0);
    }
}
