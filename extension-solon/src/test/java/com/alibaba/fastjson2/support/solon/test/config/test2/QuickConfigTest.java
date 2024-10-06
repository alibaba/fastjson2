package com.alibaba.fastjson2.support.solon.test.config.test2;

import com.alibaba.fastjson2.support.solon.Fastjson2RenderFactory;
import com.alibaba.fastjson2.support.solon.test._model.OrderDo;
import com.alibaba.fastjson2.support.solon.test._model.UserDo;
import org.junit.jupiter.api.Test;
import org.noear.snack.ONode;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.test.SolonTest;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author noear 2023/1/16 created
 */
@Import(profiles = "classpath:features2_test2.yml")
@SolonTest
public class QuickConfigTest {
    @Inject
    Fastjson2RenderFactory renderFactory;

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
        renderFactory.create().render(userDo, ctx);
        String output = ctx.attr("output");

        System.out.println(output);

        assert ONode.load(output).count() == 5;

        //error: int 没转为 string
        assert "{\"b1\":1,\"d1\":1.0,\"map1\":{\"time\":\"2023-01-16 17:39:53\",\"long\":\"12\",\"int\":12},\"n1\":\"1\",\"s1\":\"noear\"}".equals(output);
    }

    @Test
    public void hello3() throws Throwable {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("long", 1L);
        data.put("order", new OrderDo());

        ContextEmpty ctx = new ContextEmpty();
        renderFactory.create().render(data, ctx);
        String output = ctx.attr("output");

        System.out.println(output);

        assert "{\"long\":\"1\",\"order\":{\"orderId\":\"2\"}}".equals(output);
    }
}
