package com.alibaba.fastjson2.builder;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONAutowired;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuilderTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 101;
        bean.name = "DataWorks";
        String str = JSON.toJSONString(bean);
        assertEquals("{\"Id\":101,\"Name\":\"DataWorks\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.id, bean1.id);
        assertEquals(bean.name, bean1.name);
    }

    @JSONAutowired
    public static class Bean {
        public int id;
        public String name;

        static final ObjectWriter objectWriter = ObjectWriters.objectWriter(
                Bean.class,
                ObjectWriters.fieldWriter("Id", (Bean e) -> e.id),
                ObjectWriters.fieldWriter("Name", (Bean e) -> e.name)
        );

        static final ObjectReader objectReader = ObjectReaders.of(
                Bean::new,
                ObjectReaders.fieldReaderInt("Id", (Bean obj, int id) -> obj.id = id),
                ObjectReaders.fieldReader("Name", String.class, (Bean obj, String name) -> obj.name = name)
        );
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.id = 101;
        bean.name = "DataWorks";
        String str = JSON.toJSONString(bean);
        assertEquals("{\"Id\":101,\"Name\":\"DataWorks\"}", str);

        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.id, bean1.id);
        assertEquals(bean.name, bean1.name);
    }

    @JSONAutowired
    private static class Bean1 {
        public int id;
        public String name;

        static final ObjectWriter objectWriter = ObjectWriters.objectWriter(
                Bean1.class,
                ObjectWriters.fieldWriter("Id", (Bean1 e) -> e.id),
                ObjectWriters.fieldWriter("Name", (Bean1 e) -> e.name)
        );

        static final ObjectReader objectReader = ObjectReaders.of(
                Bean1::new,
                ObjectReaders.fieldReaderInt("Id", (Bean1 obj, int id) -> obj.id = id),
                ObjectReaders.fieldReader("Name", String.class, (Bean1 obj, String name) -> obj.name = name)
        );
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.id = 101;
        bean.name = "DataWorks";
        JSON.mixIn(Bean2.class, Bean2Mixin.class);

        String str = JSON.toJSONString(bean);
        assertEquals("{\"Id\":101,\"Name\":\"DataWorks\"}", str);

        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.id, bean1.id);
        assertEquals(bean.name, bean1.name);
    }

    public static class Bean2 {
        int id;
        String name;
    }

    @JSONAutowired(reader = "jsonObjectReader", writer = "jsonObjectWriter")
    public static class Bean2Mixin {
        static final ObjectWriter jsonObjectWriter = ObjectWriters.objectWriter(
                Bean2.class,
                ObjectWriters.fieldWriter("Id", (Bean2 e) -> e.id),
                ObjectWriters.fieldWriter("Name", (Bean2 e) -> e.name)
        );

        static final ObjectReader jsonObjectReader = ObjectReaders.of(
                Bean2::new,
                ObjectReaders.fieldReaderInt("Id", (Bean2 obj, int id) -> obj.id = id),
                ObjectReaders.fieldReader("Name", String.class, (Bean2 obj, String name) -> obj.name = name)
        );
    }
}
