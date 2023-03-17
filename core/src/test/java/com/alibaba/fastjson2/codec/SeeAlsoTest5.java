package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeeAlsoTest5 {
    @Test
    public void test() {
        Bean bean = (Bean) JSON.parseObject(
                "{\"@type\":\"com.alibaba.fastjson2.codec.SeeAlsoTest5$Bean\",\"id\":123}",
                IBean.class
        );
        assertEquals(123, bean.id);
    }

    @JSONType(seeAlso = {Bean.class})
    public interface IBean {
    }

    public static class Bean
            implements IBean {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
