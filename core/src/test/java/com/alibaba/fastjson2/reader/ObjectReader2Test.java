package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectReader2Test {
    @Test
    public void test() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        assertNotNull(objectReader.getFieldReader("userId1"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId1")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid1")));

        assertNotNull(objectReader.getFieldReader("userId2"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId2")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid2")));

        assertNull(objectReader.getFieldReader("id"));
        assertNull(objectReader.getFieldReader(0));
        assertNull(objectReader.getFieldReaderLCase(0));
    }

    @Test
    public void testBuilder0() {
        ObjectReader<Bean> objectReader = new ObjectReaderAdapter(
                Bean.class,
                Bean::new,
                ObjectReaders.fieldReaderInt("userId1", (Bean o, int v) -> o.userId1 = v),
                ObjectReaders.fieldReaderInt("userId2", (Bean o, int v) -> o.userId2 = v)
        );
        Bean bean = objectReader.readObject(JSONReader.of("{\"userId1\":101,\"userId2\":102}"));
        assertEquals(101, bean.userId1);
        assertEquals(102, bean.userId2);
    }

    private class Bean {
        public int userId1;
        public int userId2;
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
                "[[0,0],[101,0],[102,103],[104,105],[107,108]]",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONArray.of(
                                        JSONArray.of(),
                                        JSONArray.of(101),
                                        JSONArray.of(102, 103),
                                        JSONArray.of(104, 105, 106),
                                        JSONArray.of(107, 108, 109)
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
                "[[0,0],[101,0],[102,103],[104,105],[107,108]]",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONArray.of(
                                        JSONArray.of(),
                                        JSONArray.of(101),
                                        JSONArray.of(102, 103),
                                        JSONArray.of(104, 105, 106),
                                        JSONArray.of(107, 108, 109)
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
    }
}
