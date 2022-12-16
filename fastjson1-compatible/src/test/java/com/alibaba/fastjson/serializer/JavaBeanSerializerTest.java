package com.alibaba.fastjson.serializer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaBeanSerializerTest {
    @Test
    public void test() {
        SerializeConfig config = new SerializeConfig();
        JavaBeanSerializer serializer = (JavaBeanSerializer) config.get(Bean.class);
        config.put(Bean.class, (Object) serializer);

        Bean bean = new Bean();
        bean.id = 123;
        assertEquals(123, serializer.getFieldValue(bean, "id"));
    }

    @Test
    public void test1() {
        JSONSerializer serializer = new JSONSerializer(SerializeConfig.global);
        assertNotNull(serializer.getObjectWriter(Bean.class));
        serializer.getContext();
        serializer.setContext(new SerialContext(null, null, null, 0, 0));

        Bean bean = new Bean();
        bean.id = 123;

        SerializeWriter out = new SerializeWriter();
        JSONSerializer.write(out, bean);
        assertEquals("{\"id\":123}", out.toString());
    }

    public static class Bean {
        public int id;
    }
}
