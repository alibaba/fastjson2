package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2_vo.Int2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Int2Test {
    @Test
    public void test() {
        Int2 bean = new Int2();
        bean.setV0000(101);
        bean.setV0001(102);

        String str = JSON.toJSONString(bean);
        Int2 bean1 = JSON.parseObject(str, Int2.class);
        assertEquals(bean.getV0000(), bean1.getV0000());
        assertEquals(bean.getV0001(), bean1.getV0001());
    }
}
