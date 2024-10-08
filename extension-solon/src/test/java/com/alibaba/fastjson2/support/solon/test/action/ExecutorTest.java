package com.alibaba.fastjson2.support.solon.test.action;

import org.junit.jupiter.api.Test;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Param;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.test.SolonTest;

/**
 * @author noear 2024/9/17 created
 */
@SolonTest
public class ExecutorTest {
    @Test
    public void test() throws Throwable {
        ContextEmpty ctx = new ContextEmpty();
        ctx.headerMap().add("Content-Type", "text/json");
        ctx.pathNew("/a1");
        ctx.bodyNew("{\"name\":\"noear\",\"label\":\"A\"}");

        Solon.app().tryHandle(ctx);
        ctx.result = ctx.attr("output");
        System.out.println(ctx.result);
        assert "Hello noear A".equals(ctx.result);

        ctx = new ContextEmpty();
        ctx.headerMap().add("Content-Type", "text/json");
        ctx.pathNew("/a2");
        ctx.bodyNew("{\"name\":\"noear\",\"label\":\"A\"}");

        Solon.app().tryHandle(ctx);
        ctx.result = ctx.attr("output");
        System.out.println(ctx.result);
        assert "\"A\"".equals(ctx.result);
    }

    @Controller
    public static class Demo {
        @Mapping("/a1")
        public String a1(@Param("name") String name, @Param("label") Label label) {
            return "Hello " + name + " " + label;
        }

        @Mapping("/a2")
        public Label a2(@Param("name") String name, @Param("label") Label label) {
            return label;
        }
    }

    public enum Label {
        A,
        B
    }
}
