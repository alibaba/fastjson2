package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1258 {
    @Getter
    @Setter
    public static class Bean {
        @JSONField(name = "e")
        private String event;

        @JSONField(name = "E")
        private Long et;
    }

    @Test
    public void testSimpleBean() {
        String str = "{\"E\":123,\"e\":\"abc\"}";
        SimpleBean bean = JSON.parseObject(str, SimpleBean.class);
        assertEquals(123L, bean.et);
    }

    @Test
    public void testSimpleBean1() {
        String str = "{\"E\":123,\"e\":\"abc\"}";
        ObjectReader<SimpleBean> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(SimpleBean.class);
        SimpleBean bean = objectReader.readObject(JSONReader.of(str));
        assertEquals(123L, bean.et);
    }

    @Getter
    @Setter
    @Data
    private class SimpleBean {
        @JSONField(name = "E")
        private Long et;
    }

    @Test
    public void test1() {
        String str = "{\"E\":123,\"e\":\"abc\"}";
        Bean1 bean = JSON.parseObject(str, Bean1.class);
        assertEquals(123L, bean.et);
    }

    public static class Bean1 {
        private Long et;

        public Bean1(@JSONField(name = "E")Long et) {
            this.et = et;
        }
    }
}
