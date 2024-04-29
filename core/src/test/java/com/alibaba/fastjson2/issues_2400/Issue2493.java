package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2493 {
    @Test
    public void jsonFastSerializationTest() {
        String json = "{\n" +
                "    \"port\":[\"5683\"],\n" +
                "    \"sslPort\":[\"5684\"],\n" +
                "    \"protocol\":[\"CoAP\"],\n" +
                "    \"sslProtocl\":[\"CoAQ\"],\n" +
                "    \"heartbeatTime\":[\"30\"],\n" +
                "    \"connectionRequired\":[\"true\"]\n" +
                "}";
        Bean bean = JSON.parseObject(json, Bean.class);
        assertTrue(bean.getConnectionRequired());
        bean = JSON.parseObject(json.getBytes(StandardCharsets.UTF_8), Bean.class);
        assertTrue(bean.getConnectionRequired());
    }

    @Data
    public static class Bean {
        private Integer port;
        private Integer sslPort;
        private String protocol;
        private String sslProtocol;
        private Long heartbeatTime;
        private Boolean connectionRequired;
    }
}
