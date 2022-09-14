package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue764 {
    @Test
    public void test() {
        DockerTest compose = new DockerTest();
        compose.setVersion("3.9");

        Service realtimeSvc = new Service();
        realtimeSvc.setImage("real-svc:1.0.21");
        realtimeSvc.putEnv("NETWORK_NAME", "eth0");
        realtimeSvc.putEnv("DEBUG_VERBOSE", "false");
        realtimeSvc.putEnv("SPRING_PROFILES_ACTIVE", "test");
        realtimeSvc.putEnv("SERVER_IP", "172.21.32.180");
        Deploy realtimeSvcDeploy = new Deploy();
        realtimeSvcDeploy.setReplicas(1);
        realtimeSvc.setDeploy(realtimeSvcDeploy);
        compose.addService("realtime-svc", realtimeSvc);
        Service realtimePipe = new Service();
        realtimePipe.setImage("realpipe:1.0.21");
        realtimePipe.putEnv("NETWORK_NAME", "eth0");
        realtimePipe.putEnv("DEBUG_VERBOSE", "false");
        realtimePipe.putEnv("SPRING_PROFILES_ACTIVE", "test");
        realtimePipe.putEnv("SERVER_IP", "172.21.22.180");
        Deploy realtimePipeDeploy = new Deploy();
        realtimePipeDeploy.setReplicas(1);
        realtimePipeDeploy.setResources(new Deploy.Resource().limit(2.5));
        realtimePipe.setDeploy(realtimePipeDeploy);
        compose.addService("realtime-pipe", realtimePipe);

        Service platformManagerSvc = new Service();
        platformManagerSvc.setImage("platform:1.0.21");
        platformManagerSvc.putEnv("NETWORK_NAME", "eth0");
        platformManagerSvc.putEnv("DEBUG_VERBOSE", "false");
        platformManagerSvc.putEnv("SPRING_PROFILES_ACTIVE", "test");
        platformManagerSvc.putEnv("SERVER_IP", "172.21.32.180");
        platformManagerSvc.putEnv("HOLLYSYS_LOGIC_IMAGE=nodered:1.3.7_v1.0.21");
        platformManagerSvc.putEnv("HOLLYSYS_UDPIO_ENV[MINIO_FILE_PATH]=/opt/deploy/data/udpio/");
        platformManagerSvc.putEnv("HOLLYSYS_UDPIO_ENV[HOLLYSYS_SERVER_ENABLED]=false");
        platformManagerSvc.putEnv("HOLLYSYS_UDPIO_ENV[HOLLYSYS_PROTOCOL]=udp");
        platformManagerSvc.putEnv("HOLLYSYS_UDPIO_ENV[HOLLYSYS_UDP_PACKAGE_LENGTH]=1000");
        platformManagerSvc.putEnv("hollysys.logic.data=/opt/m7it/logic/");
        Deploy platformManagerSvcDeploy = new Deploy();
        platformManagerSvcDeploy.setReplicas(1);
        platformManagerSvcDeploy.setResources(new Deploy.Resource().limit("4096MB"));
        platformManagerSvc.setDeploy(platformManagerSvcDeploy);
        compose.addService("platform-manager-svc", platformManagerSvc);

        JSONWriter.Feature[] FASTJSON_IGNORE_WRITER_FEATURES = {
                JSONWriter.Feature.BrowserCompatible,
                JSONWriter.Feature.WriteNullBooleanAsFalse,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.IgnoreErrorGetter,
                JSONWriter.Feature.IgnoreNonFieldGetter
        };

        String json = JSON.toJSONString(compose, FASTJSON_IGNORE_WRITER_FEATURES);
        assertFalse("{}".equals(json));
    }

    @Data
    public static class DockerTest {
        private String version = "3.8";
        private Map<String, Service> services = new HashMap();

        public Map<String, Service> addService(String name, Service service) {
            if (services == null) {
                services = new HashMap();
            }
            services.put(name, service);
            return services;
        }
    }

    @Getter
    public static class Service {
        private String image;
        private Set<String> environment;
        private Deploy deploy;

        public Service putEnv(String env) {
            if (environment == null) {
                environment = new HashSet();
            }
            if (StringUtils.isNotBlank(env) && env.contains("=")) {
                environment.add(env);
            }
            return this;
        }

        public Service putEnv(String key, String value) {
            if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
                return this;
            }
            return putEnv(key + "=" + value);
        }

        public final void setImage(String image) {
            this.image = image;
        }

        public final void setDeploy(Deploy deploy) {
            this.deploy = deploy;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Deploy {
        private String mode = DeployMode.REPLICATED.getValue();
        private Integer replicas = 1;
        private RestartPolicy restartPolicy;
        private Resource resources;
        private Map<String, String> labels;

        public Deploy putLabel(String label, String value) {
            if (labels == null) {
                labels = new HashMap();
            }
            labels.put(label, value);
            return this;
        }

        @Getter
        public static enum DeployMode {
            REPLICATED("replicated"),
            GLOBAL("global");
            private String value;

            private DeployMode(String value) {
                this.value = value;
            }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RestartPolicy {
            private String condition;
            private Duration delay;
            private Integer maxAttempts;
            private Duration window;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Resource {
            private Limit limits;
            private Limit reservations;

            public Resource limit(Number cpus, DataSize memory) {
                limits = new Limit(cpus, memory);
                return this;
            }

            public Resource limit(Number cpus, String memory) {
                return limit(cpus, StringUtils.isNotBlank(memory) ? DataSize.parse(memory) : null);
            }

            public Resource limit(Number cpus) {
                limits = new Limit(cpus);
                return this;
            }

            public Resource limit(String memory) {
                if (StringUtils.isNotBlank(memory)) {
                    limits = new Limit(DataSize.parse(memory));
                }
                return this;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Limit {
                private Number cpus;
                private DataSize memory;

                public Limit(Number cpus) {
                    this.cpus = cpus;
                }

                public Limit(DataSize memory) {
                    this.memory = memory;
                }

                public final String getMemory() {
                    return memory != null ? memory.toMegabytes() + "M" : null;
                }

                public final String getCpus() {
                    return cpus != null ? cpus.toString() : null;
                }
            }
        }
    }
}
