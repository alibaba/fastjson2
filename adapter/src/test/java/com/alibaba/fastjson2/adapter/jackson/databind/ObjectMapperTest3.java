package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonCreator;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectMapperTest3 {
    @Test
    public void test() {
        ObjectMapper mapper = new ObjectMapper();

        Bean bean = new Bean(1001);
        String str = mapper.writeValueAsString(bean);
        assertEquals("{\"bean_id\":1001}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.id, bean1.id);
    }

    public static class Bean {
        private int id;

        @JsonCreator
        public Bean(@JsonProperty(value = "bean_id")int id) {
            this.id = id;
        }

        @JsonProperty("bean_id")
        public int getId() {
            return id;
        }
    }

    @Test
    public void test1() {
        ObjectMapper mapper = new ObjectMapper();

        Bean1 bean = new Bean1();
        bean.id = 1001;
        String str = mapper.writeValueAsString(bean);
        assertEquals("{\"bean_id\":1001}", str);

        assertThrows(
                Exception.class,
                () -> mapper.readValue("{\"bean_id\":null}", Bean1.class)
        );
        assertThrows(
                Exception.class,
                () -> mapper.readValue("{}", Bean1.class)
        );
    }

    public static class Bean1 {
        @JsonProperty(value = "bean_id", required = true)
        public Integer id;
    }
}
