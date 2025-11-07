package com.alibaba.fastjson2.support.solon.test.config.test4;

import com.alibaba.fastjson2.support.solon.Fastjson2EntityConverter;
import com.alibaba.fastjson2.support.solon.test._model.UserDo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.test.SolonTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author noear 2023/1/16 created
 */
@Import(profiles = "classpath:features2_test4.yml")
@SolonTest
public class QuickConfigTest {
    @Inject
    Fastjson2EntityConverter entityConverter;

    @Test
    public void hello2() throws Throwable {
        UserDo userDo = new UserDo();

        Map<String, Object> data = new HashMap<>();
        data.put("time", new Date(1673861993477L));
        data.put("long", 12L);
        data.put("int", 12);
        data.put("null", null);

        userDo.setMap1(data);

        ContextEmpty ctx = new ContextEmpty();
        entityConverter.write(userDo, ctx);
        String output = ctx.attr("output");

        System.out.println(output);

        //完美
        Assertions.assertEquals("{\"b0\":0,\"b1\":1,\"d0\":0,\"d1\":1.0,\"list0\":[],\"map0\":null,\"map1\":{\"null\":null,\"time\":\"2023-01-16 17:39:53\",\"long\":\"12\",\"int\":12},\"n0\":\"0\",\"n1\":\"1\",\"obj0\":null,\"s0\":\"\",\"s1\":\"noear\"}", output);
    }
}
