package com.alibaba.fastjson2.support;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.redission.JSONBCodec;
import com.alibaba.fastjson2.support.redission.JSONCodec;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONCodecTest {
    @Test
    public void json() throws Exception {
        JSONCodec codec = new JSONCodec(Bean.class);

        Bean bean = new Bean();
        bean.name = "abc";

        ByteBuf encoded = codec.getValueEncoder()
                .encode(bean);

        Bean decoded = (Bean) codec.getValueDecoder().decode(encoded, null);
        assertEquals(bean.name, decoded.name);
    }

    @Test
    public void jsonAutoType() throws Exception {
        JSONCodec codec = new JSONCodec(
                JSONFactory.createWriteContext(JSONWriter.Feature.WriteClassName),
                JSONFactory.createReadContext(JSONReader.autoTypeFilter(Bean.class))
        );

        Bean bean = new Bean();
        bean.name = "abc";

        ByteBuf encoded = codec.getValueEncoder()
                .encode(bean);

        Bean decoded = (Bean) codec.getValueDecoder().decode(encoded, null);
        assertEquals(bean.name, decoded.name);
    }

    @Test
    public void jsonb() throws Exception {
        JSONBCodec codec = new JSONBCodec(Bean.class);

        Bean bean = new Bean();
        bean.name = "abc";

        ByteBuf encoded = codec.getValueEncoder()
                .encode(bean);

        Bean decoded = (Bean) codec.getValueDecoder().decode(encoded, null);
        assertEquals(bean.name, decoded.name);
    }

    @Test
    public void jsonbAutoType() throws Exception {
        JSONBCodec codec = new JSONBCodec(
                JSONFactory.createWriteContext(JSONWriter.Feature.WriteClassName),
                JSONFactory.createReadContext(JSONReader.autoTypeFilter(Bean.class))
        );

        Bean bean = new Bean();
        bean.name = "abc";

        ByteBuf encoded = codec.getValueEncoder()
                .encode(bean);

        Bean decoded = (Bean) codec.getValueDecoder()
                .decode(encoded, null);
        assertEquals(bean.name, decoded.name);
    }

    public static class Bean {
        public String name;
    }
}
