package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author liuhaoxing
 * @date 2023-02-27
 **/
public class EnumNonAsciiTest {
    @Test
    public void testEnum() {
        Bean bean = new Bean();
        bean.setTestEnum(TestEnum.第二);
        bean.setName("zhangsan");
        byte[] bytes = JSON.toJSONBytes(bean, JSONWriter.Feature.WriteEnumsUsingName);
        Bean bean1 = JSONObject.parseObject(new String(bytes, StandardCharsets.UTF_8), Bean.class);

        assertEquals(bean1.getName(), bean.getName());
        assertEquals(bean1.getTestEnum(), bean.getTestEnum());
    }

    @Data
    static class Bean {
        private TestEnum testEnum;
        private String name;
    }

    @SuppressWarnings("all")
    enum TestEnum {
        第一,
        第二,
    }
}
