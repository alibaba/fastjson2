package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONReader.Feature.NonZeroNumberCastToBooleanAsTrue;
import static org.junit.jupiter.api.Assertions.*;

public class BooleanTest {
    @Test
    public void test() {
        assertFalse(JSON.parseObject("{\"value\":0}", Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertFalse(JSON.parseObject("{\"value\":false}", Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":1}", Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":3}", Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":true}", Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
    }

    @Test
    public void test1() {
        assertFalse(JSON.parseObject("{\"value\":0}").toJavaObject(Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertFalse(JSON.parseObject("{\"value\":false}").toJavaObject(Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":1}").toJavaObject(Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":3}").toJavaObject(Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":true}").toJavaObject(Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
    }

    @Test
    public void testPath() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$.value"},
                new Type[]{int.class, boolean.class},
                new String[2],
                new long[]{0, 0},
                DateUtils.SHANGHAI_ZONE_ID,
                NonZeroNumberCastToBooleanAsTrue
        );
        Object[] values = (Object[]) path.extract("{\"id\":123,\"value\":3}");
        assertEquals(Boolean.TRUE, values[1]);
    }

    @Test
    public void testPath1() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$.value"},
                new Type[]{int.class, boolean.class},
                new String[2],
                new long[]{0, 0},
                DateUtils.SHANGHAI_ZONE_ID
        );
        assertEquals(Boolean.TRUE, ((Object[]) path.extract("{\"id\":123,\"value\":\"true\"}"))[1]);
        assertEquals(Boolean.TRUE, ((Object[]) path.extract("{\"id\":123,\"value\":\"TRUE\"}"))[1]);
        assertEquals(Boolean.TRUE, ((Object[]) path.extract("{\"id\":123,\"value\":\"1\"}"))[1]);
        assertEquals(Boolean.FALSE, ((Object[]) path.extract("{\"id\":123,\"value\":\"false\"}"))[1]);
        assertEquals(Boolean.FALSE, ((Object[]) path.extract("{\"id\":123,\"value\":\"FALSE\"}"))[1]);
        assertEquals(Boolean.FALSE, ((Object[]) path.extract("{\"id\":123,\"value\":\"0\"}"))[1]);
    }

    public static class Bean {
        public boolean value;
    }
}
