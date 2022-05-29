package com.alibaba.fastjson2.mixins;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MixinAPITest {
    static class BaseClass {
        private int a;
        private int b;

        public BaseClass() {
        }

        public BaseClass(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }
    }

    interface MixIn1 {
        @JSONField(name = "apple")
        int getA();

        @JSONField(name = "apple")
        void setA(int a);

        @JSONField(name = "banana")
        int getB();

        @JSONField(name = "banana")
        void setB(int b);
    }

    @Test
    public void test_mixIn_get_methods() throws Exception {
        BaseClass base = new BaseClass(1, 2);

        JSON.mixIn(BaseClass.class, MixIn1.class);

        String str = JSON.toJSONString(base);
//        assertEquals("{\"apple\":1,\"banana\":2}", str);
        BaseClass base2 = JSON.parseObject(str, BaseClass.class);
        assertEquals(base.a, base2.a);
        assertEquals(base.b, base2.b);
    }
}
