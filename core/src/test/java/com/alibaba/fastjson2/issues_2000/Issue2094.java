package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2094 {
    @Test
    public void test() {
        String str = "{\"items\":{1:{\"config\":{2:{\"id\":123}}}}}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertEquals(1, bean.items.size());
        assertEquals(123, bean.items.get(1).getConfig().get(2).id);
    }

    @Data
    public static class Bean {
        private Map<Integer, XXXConfig> items;
    }

    public static class XXXConfig {
        private SortedMap<Integer, AckSampleConfig> config;

        public SortedMap<Integer, AckSampleConfig> getConfig() {
            return config;
        }

        public void setConfig(SortedMap<Integer, AckSampleConfig> config) {
            this.config = config;
        }
    }

    public static class AckSampleConfig {
        public int id;
    }
}
