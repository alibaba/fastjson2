package com.alibaba.fastjson2.support.solon.test.config.test1;

import com.alibaba.fastjson2.support.solon.Fastjson2RenderFactory;
import com.alibaba.fastjson2.support.solon.test._model.UserDo;
import org.junit.jupiter.api.Test;
import org.noear.snack.ONode;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.test.SolonTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 只对时间进行格式化
 */
@Import(profiles = "classpath:features2_test1-2.yml")
@SolonTest
public class QuickConfigTest2 {
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

        //完美
        assert "{\"b1\":true,\"d1\":1.0,\"map1\":{\"time\":1673861993477,\"long\":12,\"int\":12},\"n1\":1,\"s1\":\"noear\"}".equals(output);
    }
}
