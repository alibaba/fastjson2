package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListRefTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.items = new ArrayList<>();

        Item item = new Item();
        bean.items.add(item);
        bean.items.add(null);
        bean.items.add(item);

        assertEquals("{\"items\":[{},null,{\"$ref\":\"$.items[0]\"}]}",
                JSON.toJSONString(bean, JSONWriter.Feature.ReferenceDetection)
        );
    }

    @Test
    public void testLambda() {
        Bean bean = new Bean();
        bean.items = new ArrayList<>();

        Item item = new Item();
        bean.items.add(item);
        bean.items.add(null);
        bean.items.add(item);

        ObjectWriter objectWriter = TestUtils.createObjectWriterLambda(Bean.class);
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.ReferenceDetection);
        jsonWriter.setRootObject(bean);

        objectWriter.write(jsonWriter, bean);

        assertEquals("{\"items\":[{},null,{\"$ref\":\"$.items[0]\"}]}",
                jsonWriter.toString()
        );
    }

    public static class Bean {
        public List<Item> items;
    }

    public static class Item {
    }
}
