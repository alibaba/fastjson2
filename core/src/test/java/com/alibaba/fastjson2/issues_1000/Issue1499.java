package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1499 {
    @Test
    public void test() {
        ConfigItem configItem = new ConfigItem();
        byte[] jsonBytes = JSON.toJSONBytes(configItem);
        assertEquals("{\"handleTimeout\":\"PT10S\",\"name\":\"li4\"}", new String(jsonBytes));
        ConfigItem configItem2 = configItem.clone();
        assertEquals(configItem.handleTimeout, configItem2.handleTimeout);
    }

    @Data
    static class ConfigItem {
        @JSONField(ordinal = 30)
        private Duration handleTimeout = Duration.ofSeconds(10);

        @JSONField(ordinal = 60)
        private String name = "li4";

        public ConfigItem clone() {
            return JSON.parseObject(JSON.toJSONBytes(this), ConfigItem.class);
        }
    }
}
