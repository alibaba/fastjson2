package com.alibaba.fastjson2.mixins;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MixinAPITest3 {
    static class BaseClass {
        public final int a;
        public final int b;

        public BaseClass(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    static class MixIn1 {
        @JSONField(name = "apple")
        public int a;
        @JSONField(name = "banana")
        public int b;

        @JSONCreator
        public MixIn1(@JSONField(name = "apple") int a, @JSONField(name = "banana") int b) {
        }
    }

    @Test
    public void test_mixIn_get_methods() throws Exception {
        BaseClass base = new BaseClass(1, 2);

        JSON.mixIn(BaseClass.class, MixIn1.class);

        String str = JSON.toJSONString(base);
        assertEquals("{\"apple\":1,\"banana\":2}", str);
        BaseClass base2 = JSON.parseObject(str, BaseClass.class);
        assertEquals(base.a, base2.a);
        assertEquals(base.b, base2.b);
    }
}
