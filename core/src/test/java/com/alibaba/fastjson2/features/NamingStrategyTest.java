package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamingStrategyTest {
    ObjectWriterProvider writerProvider;
    ObjectReaderProvider readerProvider;

    @BeforeEach
    public void setUp() {
        writerProvider = new ObjectWriterProvider(PropertyNamingStrategy.SnakeCase);
    }

    @Test
    public void test() {
        Bean bean = new Bean();
        bean.userId = 123;
        bean.item = new Item();
        bean.item.itemId = 1001;

        JSONWriter.Context writeContext = JSONFactory.createWriteContext(writerProvider);
        assertEquals("{\"item\":{\"itemId\":1001},\"userId\":123}", JSON.toJSONString(bean));

        String snakeExpected = "{\"item\":{\"item_id\":1001},\"user_id\":123}";
        assertEquals(snakeExpected, JSON.toJSONString(bean, writeContext));

        Bean bean1 = JSON.parseObject(snakeExpected, Bean.class, NameFilter.of(PropertyNamingStrategy.SnakeCase));
        assertEquals(bean.userId, bean1.userId);
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.userId = 123;
        bean.item = new Item1();
        bean.item.itemId = 1001;

        assertEquals("{\"item\":{\"item_id\":1001},\"user_id\":123}", JSON.toJSONString(bean));
    }

    public static class Bean {
        public int userId;
        public Item item = new Item();
    }

    public static class Item {
        public int itemId;
    }

    @JSONType(naming = PropertyNamingStrategy.SnakeCase)
    public static class Bean1 {
        public int userId;
        public Item1 item = new Item1();
    }

    @JSONType(naming = PropertyNamingStrategy.SnakeCase)
    public static class Item1 {
        public int itemId;
    }
}
