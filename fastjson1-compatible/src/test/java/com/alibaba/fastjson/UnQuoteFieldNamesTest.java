package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 11/01/2017.
 */
public class UnQuoteFieldNamesTest {
    @Test
    public void test_for_issue() throws Exception {
        Map map = Collections.singletonMap("value", 123);

        String json = JSON.toJSONString(
                map,
                SerializeConfig.globalInstance,
                new SerializeFilter[0],
                null,
                JSON.DEFAULT_GENERATE_FEATURE & ~SerializerFeature.QuoteFieldNames.mask
        );
        assertEquals("{value:123}", json);
    }

    @Test
    public void test_bean() throws Exception {
        Bean bean = new Bean();
        bean.value = 123;
        String json = JSON.toJSONString(
                bean,
                SerializeConfig.globalInstance,
                new SerializeFilter[0],
                null,
                JSON.DEFAULT_GENERATE_FEATURE & ~SerializerFeature.QuoteFieldNames.mask
        );
        assertEquals("{value:123}", json);
    }

    public static class Bean {
        public int value;
    }

    @Test
    public void test_bean1() throws Exception {
        Bean1 bean = new Bean1();
        bean.value = 123;
        String json = JSON.toJSONString(
                bean,
                SerializeConfig.globalInstance,
                new SerializeFilter[0],
                null,
                JSON.DEFAULT_GENERATE_FEATURE & ~SerializerFeature.QuoteFieldNames.mask
        );
        assertEquals("{value:123}", json);
    }

    public static class Bean1 {
        public long value;
    }
}
