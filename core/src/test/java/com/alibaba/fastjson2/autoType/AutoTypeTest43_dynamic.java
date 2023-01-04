package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest43_dynamic {
    @Test
    public void test_0() throws Exception {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        JSONObject object = new JSONObject();

        Model proxy = (Model) Proxy.newProxyInstance(Model.class.getClassLoader(), new Class<?>[]{Model.class, Map.class}, object);

        proxy.setId(101);
        assertEquals(101, proxy.getId());
        assertEquals(101, object.get("id"));

        object.put("id", 102);
        assertEquals(102, proxy.getId());
        assertEquals(1, ((Map) proxy).size());
        assertEquals(false, ((Map) proxy).isEmpty());

        byte[] bytes = JSONB.toBytes(proxy,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest43_dynamic$Model\",\n" +
                "\t\"id\":102\n" +
                "}", JSONB.toJSONString(bytes));

        Model value2 = (Model) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(102, value2.getId());
    }

    public interface Model {
        int getId();

        void setId(int id);
    }
}
