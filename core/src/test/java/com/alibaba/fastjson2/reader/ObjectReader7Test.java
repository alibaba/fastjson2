package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectReader7Test {
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

        assertNotNull(objectReader.getFieldReader("userId4"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId4")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid4")));

        assertNotNull(objectReader.getFieldReader("userId5"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId5")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid5")));

        assertNotNull(objectReader.getFieldReader("userId6"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId6")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid6")));

        assertNotNull(objectReader.getFieldReader("userId7"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId7")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid7")));

        assertNull(objectReader.getFieldReader("id"));
        assertNull(objectReader.getFieldReader(0));
        assertNull(objectReader.getFieldReaderLCase(0));
    }

    private class Bean {
        public int userId1;
        public int userId2;
        public int userId3;
        public int userId4;
        public int userId5;
        public int userId6;
        public int userId7;
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
                "[" +
                        "[0,0,0,0,0,0,0],[101,0,0,0,0,0,0],[201,202,0,0,0,0,0],[301,302,303,0,0,0,0]," +
                        "[401,402,403,0,0,0,0],[501,502,503,504,0,0,0],[601,602,603,604,0,0,0],[701,702,703,704,705,0,0]," +
                        "[801,802,803,804,805,0,0],[901,902,903,904,905,906,0],[1001,1002,1003,1004,1005,1006,0]," +
                        "[1101,1102,1103,1104,1105,1106,1107],[1201,1202,1203,1204,1205,1206,1207]," +
                        "[1301,1302,1303,1304,1305,1306,1307],[1401,1402,1403,1404,1405,1406,1407]" +
                        "]",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONArray.of(
                                        JSONArray.of(),
                                        JSONArray.of(101),
                                        JSONArray.of(201, 202),
                                        JSONArray.of(301, 302, 303),
                                        JSONArray.of(401, 402, 403),
                                        JSONArray.of(501, 502, 503, 504),
                                        JSONArray.of(601, 602, 603, 604),
                                        JSONArray.of(701, 702, 703, 704, 705),
                                        JSONArray.of(801, 802, 803, 804, 805),
                                        JSONArray.of(901, 902, 903, 904, 905, 906),
                                        JSONArray.of(1001, 1002, 1003, 1004, 1005, 1006),
                                        JSONArray.of(1101, 1102, 1103, 1104, 1105, 1106, 1107),
                                        JSONArray.of(1201, 1202, 1203, 1204, 1205, 1206, 1207),
                                        JSONArray.of(1301, 1302, 1303, 1304, 1305, 1306, 1307, 1308),
                                        JSONArray.of(1401, 1402, 1403, 1404, 1405, 1406, 1407, 1408)
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
        public int userId3;
        public int userId4;
        public int userId5;
        public int userId6;
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
                "[" +
                        "[0,0,0,0,0,0,0],[101,0,0,0,0,0,0],[201,202,0,0,0,0,0],[301,302,303,0,0,0,0]," +
                        "[401,402,403,0,0,0,0],[501,502,503,504,0,0,0],[601,602,603,604,0,0,0],[701,702,703,704,705,0,0]," +
                        "[801,802,803,804,805,0,0],[901,902,903,904,905,906,0],[1001,1002,1003,1004,1005,1006,0]," +
                        "[1101,1102,1103,1104,1105,1106,1107],[1201,1202,1203,1204,1205,1206,1207]," +
                        "[1301,1302,1303,1304,1305,1306,1307],[1401,1402,1403,1404,1405,1406,1407]" +
                        "]",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONArray.of(
                                        JSONArray.of(),
                                        JSONArray.of(101),
                                        JSONArray.of(201, 202),
                                        JSONArray.of(301, 302, 303),
                                        JSONArray.of(401, 402, 403),
                                        JSONArray.of(501, 502, 503, 504),
                                        JSONArray.of(601, 602, 603, 604),
                                        JSONArray.of(701, 702, 703, 704, 705),
                                        JSONArray.of(801, 802, 803, 804, 805),
                                        JSONArray.of(901, 902, 903, 904, 905, 906),
                                        JSONArray.of(1001, 1002, 1003, 1004, 1005, 1006),
                                        JSONArray.of(1101, 1102, 1103, 1104, 1105, 1106, 1107),
                                        JSONArray.of(1201, 1202, 1203, 1204, 1205, 1206, 1207),
                                        JSONArray.of(1301, 1302, 1303, 1304, 1305, 1306, 1307, 1308),
                                        JSONArray.of(1401, 1402, 1403, 1404, 1405, 1406, 1407, 1408)
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
        public int userId3;
        public int userId4;
        public int userId5;
        public int userId6;
    }
}
