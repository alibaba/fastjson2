package com.alibaba.fastjson2.support.solon.test.config.test1_2;

import com.alibaba.fastjson2.support.solon.Fastjson2EntityConverter;
import com.alibaba.fastjson2.support.solon.test._model.CustomDateDo;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.test.SolonTest;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Date;

/**
 * 时间进行格式化 + long,int 转为字符串 + 常见类型转为非null + 所有null输出
 */
@Import(profiles = "classpath:features2_test1-2.yml")
@SolonTest
public class QuickConfigTest {
    @Inject
    Fastjson2EntityConverter entityConverter;

    @Test
    public void hello2() throws Throwable {
        CustomDateDo dateDo = new CustomDateDo();

        dateDo.setDate(new Date(1673861993477L));
        dateDo.setDate2(new Date(1673861993477L));

        ContextEmpty ctx = new ContextEmpty();
        entityConverter.write(dateDo, ctx);
        String output = ctx.attr("output");

        System.out.println(output);

        //完美
        JSONAssert.assertEquals("{\"date\":1673861993477,\"date2\":\"2023-01-16 17:39:53\"}", output, false);
    }
}
