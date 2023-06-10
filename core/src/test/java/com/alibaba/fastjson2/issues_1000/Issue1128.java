package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1128 {
    @Test
    public void test() {
        Bean test1 = JSON.parseObject("{\"code\": \"test\",\"name\": null}", Bean.class, JSONReader.Feature.InitStringFieldAsEmpty);
        assertNull(test1.getName());

        Bean test2 = JSON.parseObject("{\"code\": \"test\"}", Bean.class, JSONReader.Feature.InitStringFieldAsEmpty);
        assertEquals("", test2.getName());
    }

    public static class Bean {
        private String code;
        private String name;

        public Bean() {
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void test1() {
        Bean1 test1 = JSON.parseObject("{\"code\": \"test\",\"name\": null}", Bean1.class, JSONReader.Feature.InitStringFieldAsEmpty);
        assertNull(test1.getName());

        Bean1 test2 = JSON.parseObject("{\"code\": \"test\"}", Bean1.class, JSONReader.Feature.InitStringFieldAsEmpty);
        assertEquals("", test2.getName());
    }

    @Test
    public void test1x() {
        ObjectReader<Bean1> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean1.class);
        Bean1 test1 = objectReader.readObject(
                JSONReader.of("{\"code\": \"test\",\"name\": null}"),
                JSONReader.Feature.InitStringFieldAsEmpty.mask
        );
        assertNull(test1.getName());

        Bean1 test2 = objectReader.readObject(
                JSONReader.of("{\"code\": \"test\"}"),
                JSONReader.Feature.InitStringFieldAsEmpty.mask
        );
        assertEquals("", test2.getName());
    }

    static class Bean1 {
        private String code;
        private String name;

        private Bean1() {
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
