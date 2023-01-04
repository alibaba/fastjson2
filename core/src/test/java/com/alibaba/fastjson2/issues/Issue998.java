package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue998 {
    @Test
    public void test() {
        String str = "{\"cloudServiceName\":\"xxx\",\"enterpriseCode\":\"xxx\",\"enterpriseName\":\"xxx\",\"serviceCode\":\"IT_MONITOR_MANAGER_SYSTEM\",\"cloudStyle\":\"\"}";
        EnterpriseCloudServiceVO enterpriseCloudServiceVO = JSON.parseObject(str, EnterpriseCloudServiceVO.class);
        assertNull(enterpriseCloudServiceVO.cloudStyle);
    }

    @Test
    public void test1() {
        String str = "{\"cloudServiceName\":\"xxx\",\"enterpriseCode\":\"xxx\",\"enterpriseName\":\"xxx\",\"serviceCode\":\"IT_MONITOR_MANAGER_SYSTEM\",\"cloudStyle\":\"\"}";
        EnterpriseCloudServiceVO1 enterpriseCloudServiceVO = JSON.parseObject(str, EnterpriseCloudServiceVO1.class);
        assertNull(enterpriseCloudServiceVO.cloudStyle);
    }

    public static class EnterpriseCloudServiceVO {
        private String enterpriseCode;
        private String enterpriseName;
        private String serviceCode;
        private String cloudServiceName;
        private CloudStyle cloudStyle;

        public String getEnterpriseCode() {
            return enterpriseCode;
        }

        public void setEnterpriseCode(String enterpriseCode) {
            this.enterpriseCode = enterpriseCode;
        }

        public String getEnterpriseName() {
            return enterpriseName;
        }

        public void setEnterpriseName(String enterpriseName) {
            this.enterpriseName = enterpriseName;
        }

        public String getServiceCode() {
            return serviceCode;
        }

        public void setServiceCode(String serviceCode) {
            this.serviceCode = serviceCode;
        }

        public String getCloudServiceName() {
            return cloudServiceName;
        }

        public void setCloudServiceName(String cloudServiceName) {
            this.cloudServiceName = cloudServiceName;
        }

        public CloudStyle getCloudStyle() {
            return cloudStyle;
        }

        public void setCloudStyle(CloudStyle cloudStyle) {
            this.cloudStyle = cloudStyle;
        }
    }

    private static class EnterpriseCloudServiceVO1 {
        public String enterpriseCode;
        public String enterpriseName;
        public String serviceCode;
        public String cloudServiceName;
        public CloudStyle cloudStyle;
    }

    public static class CloudStyle
            implements Serializable {
        private Integer modelId;

        public Integer getModelId() {
            return modelId;
        }

        public void setModelId(Integer modelId) {
            this.modelId = modelId;
        }
    }
}
