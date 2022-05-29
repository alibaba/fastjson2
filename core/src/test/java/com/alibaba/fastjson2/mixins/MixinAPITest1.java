package com.alibaba.fastjson2.mixins;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MixinAPITest1 {
    static class BaseClass {
        public int a;
        public int b;

        public BaseClass() {
        }

        public BaseClass(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    class MixIn1 {
        @JSONField(name = "apple")
        public int a;
        @JSONField(name = "banana")
        public int b;
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
