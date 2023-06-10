package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue942 {
    @Test
    public void test1() {
        User user = JSON.parseObject("{\"id\":123,\"name\":\"xx\",\"user_name\":\"uu\"}", User.class);
        assertEquals("uu", user.userName);
    }

    @Value
    public static class User {
        @JSONField(name = "id")
        int id;
        @JSONField(name = "name")
        String name;
        @JSONField(name = "user_name")
        String userName;

        public User(@JSONField(name = "id") int id, @JSONField(name = "name") String name, @JSONField(name = "user_name") String userName) {
            this.id = id;
            this.name = name;
            this.userName = userName;
        }
    }

    @Value
    public static class CPUDTO {
        double max;
        double avg;
    }

    @Value
    @AllArgsConstructor
    public static class ServerDTO {
        @JSONField(name = "server_id")
        String id;

        @JSONField(name = "cpu")
        CPUDTO cpu;

        @JSONField(name = "total_mem")
        double totalMem;

        @JSONField(name = "free_mem")
        double freeMem;

        @JSONField(name = "ip")
        String ip;

        @JSONField(name = "external_ip")
        String externalIp;

        @JSONField(name = "external_interface")
        String externalInterface;

        @JSONField(name = "network_interfaces")
        Map<String, String> netInterfaces;

        @JSONField(name = "version")
        String version;

        @JSONField(name = "graphics")
        List<GpuCardDTO> graphics;

        @JSONField(name = "analytics")
        List<RunningAnalyticsDTO> runningAnalytics;

        @JSONField(name = "services")
        List<RunningServiceDTO> runningServices;

        @JSONField(name = "support")
        List<String> support;

        @JSONField(name = "params")
        JSONObject params;

        @JSONField(name = "analytics_changed", defaultValue = "true")
        boolean analyticsChanged;

        @JSONField(name = "preview_port")
        int previewPort;

        @JSONField(name = "analytics_gpu_binding")
        Map<Integer, Set<String>> analyticsGpuBinding;
    }

    @Value
    @AllArgsConstructor
    public static class RunningAnalyticsDTO {
        @JSONField(name = "id")
        int id;

        @JSONField(name = "up_time")
        long upTime;

        @JSONField(name = "gpu_id")
        int gpuId;
    }

    @Value
    @AllArgsConstructor
    public static class RunningServiceDTO {
        @JSONField(name = "type")
        String type;

        @JSONField(name = "ready")
        boolean isReady;
    }

    @Value
    @AllArgsConstructor
    public static class GpuCardDTO {
        @JSONField(name = "id")
        int id;

        @JSONField(name = "name")
        String name;

        /**
         * GPU usage percent
         */
        @JSONField(name = "gpu")
        double gpu;

        /**
         * NVDEC usage percent
         */
        @JSONField(name = "nv_dec")
        double nvDec;

        /**
         * NVENC usage percent
         */
        @JSONField(name = "nv_enc")
        double nvEnc;

        /**
         * total GPU memory
         */
        @JSONField(name = "total_mem")
        double totalMem;

        /**
         * free GPU memory
         */
        @JSONField(name = "free_mem")
        double freeMem;
    }
}
