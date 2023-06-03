package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotWriteNumberClassName {
    @Test
    public void test() {
        assertEquals(
                "1L",
                JSON.toJSONString(1L, JSONWriter.Feature.WriteClassName)
        );
        assertEquals(
                "1",
                JSON.toJSONString(1L, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteNumberClassName)
        );

        assertEquals(
                "1L",
                new String(JSON.toJSONBytes(1L, JSONWriter.Feature.WriteClassName))
        );
        assertEquals(
                "1",
                new String(
                        JSON.toJSONBytes(
                                1L,
                                JSONWriter.Feature.WriteClassName,
                                JSONWriter.Feature.NotWriteNumberClassName
                        )
                )
        );
    }

    @Test
    public void test1() {
        assertEquals(
                "{\"id\":1L}",
                JSON.toJSONString(
                        new Bean1(1L),
                        JSONWriter.Feature.NotWriteRootClassName,
                        JSONWriter.Feature.WriteClassName
                )
        );

        assertEquals(
                "{\"id\":1}",
                JSON.toJSONString(new Bean1(1L),
                        JSONWriter.Feature.NotWriteRootClassName,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.NotWriteNumberClassName
                )
        );
    }

    public static class Bean1 {
        public Number id;

        public Bean1(Number id) {
            this.id = id;
        }
    }

    @Test
    public void test_short() {
        assertEquals(
                "1S",
                JSON.toJSONString((short) 1, JSONWriter.Feature.WriteClassName)
        );
        assertEquals(
                "1",
                JSON.toJSONString((short) 1, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteNumberClassName)
        );

        assertEquals(
                "1S",
                new String(JSON.toJSONBytes((short) 1, JSONWriter.Feature.WriteClassName))
        );
        assertEquals(
                "1",
                new String(
                        JSON.toJSONBytes(
                                (short) 1,
                                JSONWriter.Feature.WriteClassName,
                                JSONWriter.Feature.NotWriteNumberClassName
                        )
                )
        );
    }

    @Test
    public void test1_short() {
        assertEquals(
                "{\"id\":1S}",
                JSON.toJSONString(
                        new Bean1((short) 1),
                        JSONWriter.Feature.NotWriteRootClassName,
                        JSONWriter.Feature.WriteClassName
                )
        );

        assertEquals(
                "{\"id\":1}",
                JSON.toJSONString(new Bean1((short) 1),
                        JSONWriter.Feature.NotWriteRootClassName,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.NotWriteNumberClassName
                )
        );
    }

    @Test
    public void test_byte() {
        assertEquals(
                "1B",
                JSON.toJSONString((byte) 1, JSONWriter.Feature.WriteClassName)
        );
        assertEquals(
                "1",
                JSON.toJSONString((byte) 1, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteNumberClassName)
        );

        assertEquals(
                "1B",
                new String(JSON.toJSONBytes((byte) 1, JSONWriter.Feature.WriteClassName))
        );
        assertEquals(
                "1",
                new String(
                        JSON.toJSONBytes(
                                (byte) 1,
                                JSONWriter.Feature.WriteClassName,
                                JSONWriter.Feature.NotWriteNumberClassName
                        )
                )
        );
    }

    @Test
    public void test1_byte() {
        assertEquals(
                "{\"id\":1B}",
                JSON.toJSONString(
                        new Bean1((byte) 1),
                        JSONWriter.Feature.NotWriteRootClassName,
                        JSONWriter.Feature.WriteClassName
                )
        );

        assertEquals(
                "{\"id\":1}",
                JSON.toJSONString(new Bean1((byte) 1),
                        JSONWriter.Feature.NotWriteRootClassName,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.NotWriteNumberClassName
                )
        );
    }

    @Test
    public void test_float() {
        assertEquals(
                "1.0F",
                JSON.toJSONString(1F, JSONWriter.Feature.WriteClassName)
        );
        assertEquals(
                "1.0",
                JSON.toJSONString(1F, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteNumberClassName)
        );

        assertEquals(
                "1.0F",
                new String(JSON.toJSONBytes(1F, JSONWriter.Feature.WriteClassName))
        );
        assertEquals(
                "1.0",
                new String(
                        JSON.toJSONBytes(
                                1F,
                                JSONWriter.Feature.WriteClassName,
                                JSONWriter.Feature.NotWriteNumberClassName
                        )
                )
        );
    }

    @Test
    public void test1_float() {
        assertEquals(
                "{\"id\":1.0F}",
                JSON.toJSONString(
                        new Bean1((float) 1),
                        JSONWriter.Feature.NotWriteRootClassName,
                        JSONWriter.Feature.WriteClassName
                )
        );

        assertEquals(
                "{\"id\":1.0}",
                JSON.toJSONString(new Bean1((float) 1),
                        JSONWriter.Feature.NotWriteRootClassName,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.NotWriteNumberClassName
                )
        );
    }

    @Test
    public void test_double() {
        assertEquals(
                "1.0D",
                JSON.toJSONString(1D, JSONWriter.Feature.WriteClassName)
        );
        assertEquals(
                "1.0",
                JSON.toJSONString(1D, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteNumberClassName)
        );

        assertEquals(
                "1.0D",
                new String(JSON.toJSONBytes(1D, JSONWriter.Feature.WriteClassName))
        );
        assertEquals(
                "1.0",
                new String(
                        JSON.toJSONBytes(
                                1D,
                                JSONWriter.Feature.WriteClassName,
                                JSONWriter.Feature.NotWriteNumberClassName
                        )
                )
        );
    }

    @Test
    public void test1_double() {
        assertEquals(
                "{\"id\":1.0D}",
                JSON.toJSONString(
                        new Bean1((double) 1),
                        JSONWriter.Feature.NotWriteRootClassName,
                        JSONWriter.Feature.WriteClassName
                )
        );

        assertEquals(
                "{\"id\":1.0}",
                JSON.toJSONString(new Bean1((double) 1),
                        JSONWriter.Feature.NotWriteRootClassName,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.NotWriteNumberClassName
                )
        );
    }
}
