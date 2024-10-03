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
package com.alibaba.fastjson2.support.solon.test.requirement.case2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author noear 2023/10/29 created
 */
public class TypeTest {
    @Test
    public void test() throws Throwable {
        Bean data = new Bean();
        data.value = 12L;

        String output = JSON.toJSONString(data, JSONWriter.Feature.WriteClassName);

        System.out.println(output); //{"@type":"features.type0.TypeTest$Bean","value":12L}
        assertEquals("{\"@type\":\"features.type0.TypeTest$Bean\",\"value\":12}", output);
    }

    public static class Bean {
        private Long value;

        public Long getValue() {
            return value;
        }
    }
}
