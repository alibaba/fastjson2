package com.alibaba.fastjson2.support.solon.test.requirement.case1;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

/**
 * @author noear 2023/10/29 created
 */
public class AsNumberTest2 {
    @Test
    public void test() throws Exception {
        ObjectWriterProvider writerProvider = new ObjectWriterProvider();

        JSONWriter.Context writeContext = new JSONWriter.Context(writerProvider,
                JSONWriter.Feature.WriteNullNumberAsZero);

        Demo demo = new Demo();
        String tmp = JSON.toJSONString(demo, writeContext);
        System.out.println(tmp);

        assert "{\"a\":0,\"b\":0,\"c\":1}".equals(tmp);


        writeContext = new JSONWriter.Context(writerProvider,
                JSONWriter.Feature.WriteNullNumberAsZero,
                JSONWriter.Feature.WriteLongAsString);

        demo = new Demo();
        tmp = JSON.toJSONString(demo, writeContext);
        System.out.println(tmp);

        assert "{\"a\":\"0\",\"b\":\"0\",\"c\":\"1\"}".equals(tmp);
    }

    public static class Demo {
        public long a;
        public Long b;
        public Long c = 1L;
    }
}
