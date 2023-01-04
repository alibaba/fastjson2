package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderDateFieldTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");

        assertNotNull(fieldReader.field);

        fieldReader.accept(bean, null);
        assertNull(bean.value);

        fieldReader.accept(bean, "");
        assertNull(bean.value);

        fieldReader.accept(bean, "2017-07-03 12:13:14");
        assertEquals(1499055194000L, bean.value.getTime());

        fieldReader.accept(bean, "null");
        assertNull(bean.value);

        assertNotNull(fieldReader.getObjectReader(JSONReader.of("")));

        fieldReader.accept(bean, "1499055194000");
        assertEquals(1499055194000L, bean.value.getTime());

        fieldReader.accept(bean, 1499055194000L);
        assertEquals(1499055194000L, bean.value.getTime());

        assertThrows(Exception.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                1499055194000L,
                objectReader.readObject(
                        JSONReader.of("{\"value\":\"2017-07-03 12:13:14\"}"),
                        0
                ).value.getTime()
        );
    }

    private static class Bean {
        public Date value;
    }

    @Test
    public void test3() {
        ObjectReader<Bean3> objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean3.class);
        assertEquals(
                1499055194000L,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":\"2017-07-03 12:13:14\"}")
                ).value.getTime()
        );
        assertEquals(
                1499055194000L,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":1499055194000}")
                ).value.getTime()
        );
        assertNull(
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":null}")
                ).value
        );
    }

    private static class Bean3 {
        public Date value;
        public final int id;

        public Bean3(@JSONField(name = "id") int id) {
            this.id = id;
        }
    }

    @Test
    public void test4() {
        ObjectReader<Bean4> objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean4.class);
        assertEquals(
                1499055194000L,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":1499055194}")
                ).value.getTime()
        );
    }

    private static class Bean4 {
        @JSONField(format = "unixtime")
        public Date value;
        public final int id;

        public Bean4(@JSONField(name = "id") int id) {
            this.id = id;
        }
    }

    @Test
    public void testMillis() {
        ObjectReader<BeanMillis> objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(BeanMillis.class);
        assertEquals(
                1499055194000L,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":1499055194000}")
                ).value.getTime()
        );
    }

    private static class BeanMillis {
        @JSONField(format = "millis")
        public Date value;
        public final int id;

        public BeanMillis(@JSONField(name = "id") int id) {
            this.id = id;
        }
    }

    @Test
    public void test5() {
        ObjectReader<Bean5> objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean5.class);
        assertEquals(
                1499055194000L,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":\"20170703121314000+0800\"}")
                ).value.getTime()
        );
        assertThrows(
                JSONException.class,
                () -> objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":\"xxx\"}")
                )
        );
    }

    private static class Bean5 {
        @JSONField(format = "yyyyMMddHHmmssSSSZ")
        public Date value;
        public final int id;

        public Bean5(@JSONField(name = "id") int id) {
            this.id = id;
        }
    }

    @Test
    public void testISO8601() {
        ObjectReader<BeanISO8601> objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(BeanISO8601.class);
        assertEquals(
                1499055194000L,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":\"2017-07-03T12:13:14\"}")
                ).value.getTime()
        );
        assertThrows(
                Exception.class,
                () -> objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":\"xxx\"}")
                )
        );
    }

    private static class BeanISO8601 {
        @JSONField(format = "iso8601")
        public Date value;
        public final int id;

        public BeanISO8601(@JSONField(name = "id") int id) {
            this.id = id;
        }
    }
}
