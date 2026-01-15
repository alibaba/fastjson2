package com.alibaba.fastjson2.support.solon.test.config.test0;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.support.solon.Fastjson2EntityConverter;
import com.alibaba.fastjson2.support.solon.test._model.UserDo;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.test.SolonTest;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author noear 2023/1/16 created
 */
@Import(profiles = "classpath:features2_test0.yml")
@SolonTest
public class QuickConfigTest {
    @Inject
    Fastjson2EntityConverter entityConverter;

    @Test
    public void hello2() throws Throwable {
        UserDo userDo = new UserDo();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("time", new Date(1673861993477L));
        data.put("long", 12L);
        data.put("int", 12);
        data.put("null", null);

        userDo.setMap1(data);

        ContextEmpty ctx = new ContextEmpty();
        entityConverter.write(userDo, ctx);
        String output = ctx.attr("output");

        System.out.println(output);

        assertEquals(5, JSONObject.parseObject(output).size());

        JSONAssert.assertEquals("{\"b1\":true,\"d1\":1.0,\"map1\":{\"time\":1673861993477,\"long\":12,\"int\":12},\"n1\":1,\"s1\":\"noear\"}", output, false);
    }
}
