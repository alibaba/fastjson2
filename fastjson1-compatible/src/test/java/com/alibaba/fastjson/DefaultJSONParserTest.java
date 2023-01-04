/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultJSONParserTest {
    @Test
    public void test_double() {
        DefaultJSONParser parser = new DefaultJSONParser("3.4");
        parser.config(Feature.UseBigDecimal, false);
        assertEquals("3.4", parser.getInput());
        assertEquals(false, parser.isEnabled(Feature.UseBigDecimal));
        Object result = parser.parse();
        assertEquals(3.4D, result);
    }

    @Test
    public void test_double_in_object() {
        DefaultJSONParser parser = new DefaultJSONParser("{\"double\":3.4}");
        parser.config(Feature.UseBigDecimal, false);
        assertEquals("{\"double\":3.4}", parser.getInput());
        Object result = parser.parse();
        assertEquals(3.4D, ((Map) result).get("double"));
    }

    @Test
    public void test_error() {
        Exception error = null;
        try {
            DefaultJSONParser parser = new DefaultJSONParser("{\"name\":3]");
            parser.parse();
        } catch (Exception ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error2() {
        Exception error = null;
        try {
            DefaultJSONParser parser = new DefaultJSONParser("ttr");
            parser.parse();
        } catch (Exception ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error3() {
        Exception error = null;
        try {
            DefaultJSONParser parser = new DefaultJSONParser("33");
            parser.parseObject(new HashMap());
        } catch (Exception ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error4() {
        Exception error = null;
        try {
            DefaultJSONParser parser = new DefaultJSONParser("]");
            parser.parse();
        } catch (Exception ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error6() {
        Exception error = null;
        try {
            DefaultJSONParser parser = new DefaultJSONParser("{\"a\"33");
            parser.parse();
        } catch (Exception ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error7() {
        Exception error = null;
        try {
            DefaultJSONParser parser = new DefaultJSONParser("{\"a\":{}3");
            parser.parse();
        } catch (Exception ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error11() {
        Exception error = null;
        try {
            DefaultJSONParser parser = new DefaultJSONParser("{]");
            parser.parse();
        } catch (Exception ex) {
            error = ex;
        }
        assertNotNull(error);
    }
}
