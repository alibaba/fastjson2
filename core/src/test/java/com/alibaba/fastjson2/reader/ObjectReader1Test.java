package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectReader1Test {
    @Test
    public void test() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        assertNotNull(objectReader.getFieldReader("userId"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid")));

        assertNull(objectReader.getFieldReader("id"));
        assertNull(objectReader.getFieldReader(0));
        assertNull(objectReader.getFieldReaderLCase(0));
    }

    private class Bean {
        public int userId;
    }

    @Test
    public void test1() {
        assertEquals(
                0,
                JSONB.parseObject(
                        JSONArray.of().toJSONBBytes(),
                        Bean1.class,
                        JSONReader.Feature.SupportArrayToBean
                ).userId
        );

        assertEquals(
                101,
                JSONB.parseObject(
                        JSONArray.of(101).toJSONBBytes(),
                        Bean1.class,
                        JSONReader.Feature.SupportArrayToBean
                ).userId
        );

        assertEquals(
                101,
                JSONB.parseObject(
                        JSONArray.of(101, 102).toJSONBBytes(),
                        Bean1.class,
                        JSONReader.Feature.SupportArrayToBean
                ).userId
        );
    }

    private static class Bean1 {
        public int userId;
    }

    @Test
    public void test2_0() {
        assertEquals(
                0,
                JSONB.parseObject(
                        JSONArray.of().toJSONBBytes(),
                        Bean2.class,
                        JSONReader.Feature.SupportArrayToBean
                ).userId
        );

        assertEquals(
                101,
                JSONB.parseObject(
                        JSONArray.of(101).toJSONBBytes(),
                        Bean2.class,
                        JSONReader.Feature.SupportArrayToBean
                ).userId
        );

        assertEquals(
                "[[0],[101],[102],[104]]",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONArray.of(
                                        JSONArray.of(),
                                        JSONArray.of(101),
                                        JSONArray.of(102, 103),
                                        JSONArray.of(104, 105, 106)
                                ).toJSONBBytes(),
                                Bean2[].class,
                                JSONReader.Feature.SupportArrayToBean
                        ),
                        JSONWriter.Feature.BeanToArray
                )
        );
    }

    public static class Bean2 {
        public int userId;
    }
}
