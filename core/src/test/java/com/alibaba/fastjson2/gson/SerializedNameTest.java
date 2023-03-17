package com.alibaba.fastjson2.gson;

import com.alibaba.fastjson2.JSON;
import com.google.gson.annotations.SerializedName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializedNameTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 1001;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"bean_id\":1001}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.id, bean1.id);

        Bean bean2 = JSON.parseObject("{\"x_id\":1001}", Bean.class);
        assertEquals(bean.id, bean2.id);
    }

    public static class Bean {
        @SerializedName(value = "bean_id", alternate = "x_id")
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
