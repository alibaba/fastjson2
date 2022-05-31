package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumSetTest {
    @Test
    public void test0() {
        EnumSet<TimeUnit> enumSet = EnumSet.of(TimeUnit.DAYS, TimeUnit.SECONDS);

        {
            String json = JSON.toJSONString(enumSet);
            assertEquals("[\"SECONDS\",\"DAYS\"]", json);

            EnumSet<TimeUnit> enumSet2 = JSON.parseObject(json, new TypeReference<EnumSet<TimeUnit>>() {
            });
            assertEquals(enumSet.size(), enumSet2.size());
        }

        {
            byte[] jsonbBytes = JSONB.toBytes(enumSet);
            EnumSet<TimeUnit> enumSet2 = JSONB.parseObject(jsonbBytes, new TypeReference<EnumSet<TimeUnit>>() {
            });
            assertEquals(enumSet.size(), enumSet2.size());
        }
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.units = EnumSet.of(TimeUnit.DAYS, TimeUnit.SECONDS);
        String json = JSON.toJSONString(bean);
        assertEquals("{\"units\":[\"SECONDS\",\"DAYS\"]}", json);
        Bean bean1 = JSON.parseObject(json, Bean.class);
        assertEquals(bean.units.size(), bean1.units.size());
    }

    @Test
    public void test1_jsonb() {
        Bean bean = new Bean();
        bean.units = EnumSet.of(TimeUnit.DAYS, TimeUnit.SECONDS);
        byte[] jsonbBytes = JSONB.toBytes(bean);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class);
        assertEquals(bean.units.size(), bean1.units.size());
    }

    public static class Bean {
        public EnumSet<TimeUnit> units;
    }
}
