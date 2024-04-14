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
        JSONWriter.Feature[] writerFeatures = {JSONWriter.Feature.WriteClassName};
        JSONReader.Feature[] readerFeatures = {};
        JSONReader.AutoTypeBeforeHandler autoTypeFilter = JSONReader.autoTypeFilter(Bean.class.getName()); // 配置反序列化支持的类，支持前缀配置
        JSONCodec codec = new JSONCodec(
                JSONFactory.createWriteContext(writerFeatures),
                JSONFactory.createReadContext(autoTypeFilter, readerFeatures)
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
        JSONWriter.Feature[] writerFeatures = {JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased};
        JSONReader.Feature[] readerFeatures = {JSONReader.Feature.FieldBased};
        JSONReader.AutoTypeBeforeHandler autoTypeFilter = JSONReader.autoTypeFilter(Bean.class.getName()); // 配置反序列化支持的类，支持前缀配置
        JSONBCodec codec = new JSONBCodec(
                JSONFactory.createWriteContext(writerFeatures),
                JSONFactory.createReadContext(autoTypeFilter, readerFeatures)
        );

        Bean bean = new Bean();
        bean.name = "abc";

        ByteBuf encoded = codec.getValueEncoder()
                .encode(bean);

        Bean decoded = (Bean) codec.getValueDecoder()
                .decode(encoded, null);
        assertEquals(bean.name, decoded.name);
    }

    @Test
    public void jsonb1() throws Exception {
        JSONBCodec codec = new JSONBCodec(Bean.class.getName()); // 配置反序列化支持的类，支持前缀配置

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
