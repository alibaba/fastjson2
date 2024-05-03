package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Issue2489 {
    @Test
    public void test() {
        String str = "{\"registry\":{\"cn.xx.UserService\":[{\"schema\":\"http\",\"host\":\"127.0.0.1\",\"port\":8082,\"context\":\"trpc\",\"status\":true,\"parameters\":{\"env\":\"dev\",\"tag\":\"red\"}}]},\"timestamps\":{\"cn.xx.UserService@http://127.0.0.1:8082/trpc\":1714442794280},\"versions\":{\"cn.xx.UserService\":1},\"version\":1}\n";
        Snapshot snapshot = JSON.parseObject(str, Snapshot.class);
        assertEquals(1, snapshot.registry.size());
        snapshot.registry.forEach((k, v) -> {
            assertEquals(String.class, k.getClass());
            for (InstanceMeta instanceMeta : v) {
                assertNotNull(instanceMeta);
            }
        });

        List<InstanceMeta> instanceMetas = snapshot.registry.get("cn.xx.UserService");
        assertEquals(1, instanceMetas.size());
        InstanceMeta instanceMeta = instanceMetas.get(0);
        assertEquals("http", instanceMeta.schema);
    }

    @Data
    @AllArgsConstructor
    public static class Snapshot {
        /**
         * 注册上来的服务集
         */
//    @JSONField(deserializeUsing = LinkedMultiValueMapDeserializer.class)
        LinkedMultiValueMap<String, InstanceMeta> registry;
        /**
         * 实例时间戳  - 实例级别
         * (service + "@" + instance.toUrl(),时间戳)
         */
        Map<String, Long> timestamps;
        /**
         * 服务的版本 - 服务级别
         */
        Map<String, Long> versions;

        /**
         * VERSIONS#value
         */
        long version;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(of = {"schema", "host", "port", "context"})
    public static class InstanceMeta {
        private String schema;
        private String host;
        private Integer port;
        private String context;
        private Boolean status;
    }
}
