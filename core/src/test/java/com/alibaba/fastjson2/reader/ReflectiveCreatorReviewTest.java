package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ReflectiveCreatorReviewTest {
    @Test
    void parameterCollectionRetainsItemClass() throws Exception {
        Constructor<?> constructor = ListBean.class.getConstructor(List.class);
        FieldReader[] readers = ObjectReaderCreator.INSTANCE.createFieldReaders(
                new ObjectReaderProvider(), ListBean.class, ListBean.class,
                constructor, constructor.getParameters(), "values");
        assertSame(String.class, readers[0].getItemClass());
    }

    @Test
    void fieldOverloadRetainsOrdinal() throws Exception {
        FieldReader reader = ObjectReaderCreator.INSTANCE.createFieldReader(
                Bean.class, Bean.class, "value", 7, 0, null, null, null, null,
                int.class, int.class, Bean.class.getField("value"), null);
        assertEquals(7, reader.ordinal);
    }

    @Test
    void smallReaderRetainsCustomTypeKey() throws Exception {
        FieldReader field = ObjectReaderCreator.INSTANCE.createFieldReader("value", Bean.class.getField("value"));
        ObjectReader<Bean> reader = ObjectReaderCreator.INSTANCE.createObjectReader(
                Bean.class, "kind", 0, null, Bean::new, null, field);
        assertEquals("kind", reader.getTypeKey());
    }

    @Test
    void alternateFieldRetainsFormat() {
        ObjectReader<DateBean> reader = ObjectReaderCreator.INSTANCE.createObjectReader(DateBean.class);
        try (JSONReader input = JSONReader.of("{\"alias\":2}")) {
            assertEquals(2000, reader.readObject(input).value.getTime());
        }
    }

    public static class Bean {
        public int value;
    }

    public static class ListBean {
        public final List<String> values;

        public ListBean(List<String> values) {
            this.values = values;
        }
    }

    public static class DateBean {
        @JSONField(format = "unixtime", alternateNames = "alias")
        public Date value;
    }
}
