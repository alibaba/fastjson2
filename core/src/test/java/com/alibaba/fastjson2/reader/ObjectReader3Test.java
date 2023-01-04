package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectReader3Test {
    @Test
    public void test() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        assertNotNull(objectReader.getFieldReader("userId1"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId1")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid1")));

        assertNotNull(objectReader.getFieldReader("userId2"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId2")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid2")));

        assertNotNull(objectReader.getFieldReader("userId3"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId3")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid3")));

        assertNull(objectReader.getFieldReader("id"));
        assertNull(objectReader.getFieldReader(0));
        assertNull(objectReader.getFieldReaderLCase(0));
    }

    private class Bean {
        public int userId1;
        public int userId2;
        public int userId3;
    }

    @Test
    public void test1() {
        assertEquals(
                0,
                JSONB.parseObject(
                        JSONArray.of().toJSONBBytes(),
                        Bean1.class,
                        JSONReader.Feature.SupportArrayToBean
                ).userId0
        );

        assertEquals(
                101,
                JSONB.parseObject(
                        JSONArray.of(101).toJSONBBytes(),
                        Bean1.class,
                        JSONReader.Feature.SupportArrayToBean
                ).userId0
        );

        assertEquals(
                101,
                JSONB.parseObject(
                        JSONArray.of(101, 102).toJSONBBytes(),
                        Bean1.class,
                        JSONReader.Feature.SupportArrayToBean
                ).userId0
        );

        assertEquals(
                "[[0,0,0],[101,0,0],[201,202,0],[301,302,303],[401,402,403],[501,502,503],[601,602,603]]",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONArray.of(
                                        JSONArray.of(),
                                        JSONArray.of(101),
                                        JSONArray.of(201, 202),
                                        JSONArray.of(301, 302, 303),
                                        JSONArray.of(401, 402, 403),
                                        JSONArray.of(501, 502, 503, 504),
                                        JSONArray.of(601, 602, 603, 604)
                                ).toJSONBBytes(),
                                Bean1[].class,
                                JSONReader.Feature.SupportArrayToBean
                        ),
                        JSONWriter.Feature.BeanToArray
                )
        );
    }

    private static class Bean1 {
        public int userId0;
        public int userId1;
        public int userId2;
    }

    @Test
    public void test2() {
        assertEquals(
                0,
                JSONB.parseObject(
                        JSONArray.of().toJSONBBytes(),
                        Bean2.class,
                        JSONReader.Feature.SupportArrayToBean
                ).userId0
        );

        assertEquals(
                101,
                JSONB.parseObject(
                        JSONArray.of(101).toJSONBBytes(),
                        Bean2.class,
                        JSONReader.Feature.SupportArrayToBean
                ).userId0
        );

        assertEquals(
                101,
                JSONB.parseObject(
                        JSONArray.of(101, 102).toJSONBBytes(),
                        Bean2.class,
                        JSONReader.Feature.SupportArrayToBean
                ).userId0
        );

        assertEquals(
                "[[0,0,0],[101,0,0],[201,202,0],[301,302,303],[401,402,403],[501,502,503],[601,602,603]]",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONArray.of(
                                        JSONArray.of(),
                                        JSONArray.of(101),
                                        JSONArray.of(201, 202),
                                        JSONArray.of(301, 302, 303),
                                        JSONArray.of(401, 402, 403),
                                        JSONArray.of(501, 502, 503, 504),
                                        JSONArray.of(601, 602, 603, 604)
                                ).toJSONBBytes(),
                                Bean2[].class,
                                JSONReader.Feature.SupportArrayToBean
                        ),
                        JSONWriter.Feature.BeanToArray
                )
        );
    }

    public static class Bean2 {
        public int userId0;
        public int userId1;
        public int userId2;
    }
}
