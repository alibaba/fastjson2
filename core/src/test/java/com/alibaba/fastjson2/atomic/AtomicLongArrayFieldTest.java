package com.alibaba.fastjson2.atomic;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.concurrent.atomic.AtomicLongArray;

public class AtomicLongArrayFieldTest extends TestCase {

    public void test_codec_null() throws Exception {
        V0 v = new V0();


        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls);
        Assert.assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        Assert.assertEquals(v1.getValue(), v.getValue());
    }

    public void test_codec_null_1() throws Exception {
        V0 v = new V0();

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
        Assert.assertEquals("{\"value\":[]}", text);
    }
    
    public void test_codec_null_2() throws Exception {
        V0 v = JSON.parseObject("{\"value\":[1,2]}", V0.class);

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
        Assert.assertEquals("{\"value\":[1,2]}", text);
    }

    public static class V0 {

        private AtomicLongArray value;

        public AtomicLongArray getValue() {
            return value;
        }

        public void setValue(AtomicLongArray value) {
            this.value = value;
        }

    }
}
