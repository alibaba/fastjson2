package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONTypeRootNameTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 123;

        ObjectWriter w = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class);
        String str = w.toJSONString(bean);
        String expected = "{\"built-in\":{\"id\":123}}";
        assertEquals(expected, str);

        str = JSON.toJSONString(bean);
        assertEquals(expected, str);

        byte[] jsonb = JSONB.toBytes(bean);
        assertEquals(expected,
                JSON.parseObject(
                        JSONB.toJSONString(jsonb)
                ).toJSONString());

        JSONObject jsonObject = (JSONObject) JSON.toJSON(bean);
        str = jsonObject.toString();
        assertEquals(expected, str);

        ObjectReader<Bean> r = ObjectReaderCreator.INSTANCE.createObjectReader(Bean.class);
        Bean bean1 = r.readObject(str);
        assertEquals(bean.id, bean1.id);

        Bean bean2 = r.readJSONBObject(JSONReader.ofJSONB(jsonb), null, null, 0);
        assertEquals(bean.id, bean2.id);

        Bean bean3 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.id, bean3.id);

        Bean bean4 = r.createInstance(jsonObject, 0);
        assertEquals(bean.id, bean4.id);
    }

    @JSONType(rootName = "built-in")
    public static class Bean {
        public int id;
    }
}
