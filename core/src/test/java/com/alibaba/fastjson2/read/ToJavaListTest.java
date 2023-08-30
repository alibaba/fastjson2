package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ToJavaListTest {
    @Test
    public void test() {
        PermissionsBoundaryBaseline baseline = new PermissionsBoundaryBaseline();

        PermissionsBoundaryBaseline.APIConfig apiConfig = new PermissionsBoundaryBaseline.APIConfig("abc");
        apiConfig.parameters = new ArrayList<>();
        apiConfig.parameters.add(new PermissionsBoundaryBaseline.ParameterConfig("k", "v"));

        baseline.apis = new ArrayList<>();
        baseline.apis.add(apiConfig);

        String str = JSON.toJSONString(baseline);
        PermissionsBoundaryBaseline baseline1 = JSON.parseObject(str)
                .toJavaObject(PermissionsBoundaryBaseline.class);
        PermissionsBoundaryBaseline.APIConfig apiConfig1 = baseline1.getApis().get(0);
        PermissionsBoundaryBaseline.ParameterConfig parameterConfig = apiConfig1.getParameters().get(0);
    }

    @Data
    public static class PermissionsBoundaryBaseline
            implements Serializable {
        private static final long serialVersionUID = 2018385906703413152L;
        private String productName;
        private String clusterName;
        private String serviceName;
        private String k8sName;
        private String regionId;
        private String aliyunId;
        private List<APIConfig> apis;

        @Data
        public static class APIConfig
                implements Serializable {
            private static final long serialVersionUID = 4071579360627137437L;
            private String coordinate;
            private String description;
            private List<ParameterConfig> parameters;

            public APIConfig(String coordinate) {
                this.coordinate = coordinate;
            }

            public APIConfig(String coordinate, String description, List<ParameterConfig> parameters) {
                this.coordinate = coordinate;
                this.description = description;
                this.parameters = parameters;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof APIConfig)) {
                    return false;
                }
                APIConfig apiConfig = (APIConfig) o;
                return getCoordinate().equals(apiConfig.getCoordinate()) && Objects.equals(getParameters(), apiConfig.getParameters());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getCoordinate(), getParameters());
            }
        }

        @Data
        public static class ParameterConfig
                implements Serializable {
            private static final long serialVersionUID = 6361869534198523451L;
            //当key为InnerVpc且value为true时，进行vpc配置
            String key;
            Boolean required = false; //True代表强校验参数是否存在
            String value;

            public ParameterConfig(String key, String value) {
                this.key = key;
                this.value = value;
            }

            public ParameterConfig(String key, Boolean required, String value) {
                this.key = key;
                this.required = required;
                this.value = value;
            }

            public ParameterConfig() {
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof ParameterConfig)) {
                    return false;
                }
                ParameterConfig that = (ParameterConfig) o;
                return Objects.equals(getKey(), that.getKey()) && Objects.equals(getRequired(), that.getRequired())
                        && Objects.equals(getValue(), that.getValue());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getKey(), getRequired(), getValue());
            }
        }
    }
}
