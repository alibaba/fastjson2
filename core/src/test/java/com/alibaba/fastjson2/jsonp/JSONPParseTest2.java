package com.alibaba.fastjson2.jsonp;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 21/02/2017.
 */
public class JSONPParseTest2 {
    @Test
    public void test_f() throws Exception {
        String text = "parent.callback ({'id':1, 'name':'idonans'} );   /**/ ";

        JSONPObject jsonpObject = JSON.parseObject(text, JSONPObject.class);
        assertEquals("parent.callback", jsonpObject.getFunction());

        assertEquals(1, jsonpObject.getParameters().size());
        JSONObject param = (JSONObject) jsonpObject.getParameters().get(0);
        assertEquals(1, param.get("id"));
        assertEquals("idonans", param.get("name"));

        String json = JSON.toJSONString(jsonpObject);
        assertEquals("parent.callback({\"id\":1,\"name\":\"idonans\"})", json);
    }
}
