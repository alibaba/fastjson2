/*
 * Copyright 2017-2024 noear.org and authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
