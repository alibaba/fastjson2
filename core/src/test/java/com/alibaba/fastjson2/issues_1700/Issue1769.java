package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1769 {
    @Test
    public void testOnInvokeJsonGenericParse() {
        AlimeFromConfig alimeFromConfig = new AlimeFromConfig();
        alimeFromConfig.setDefaultFromId("birtney");

        List<AbstractConfig> sourceConfigs = new ArrayList<>();
        sourceConfigs.add(alimeFromConfig);
        Map<String, List<AbstractConfig>> configMap = new HashMap<>();
        configMap.put("FROM", sourceConfigs);

        BirtneyModel sourceModel = new BirtneyModel();
        sourceModel.setConfigs(configMap);

        String json = JSON.toJSONString(sourceModel, JSONWriter.Feature.WriteByteArrayAsBase64);
        System.out.println(json);

        BirtneyModel birtneyModel =
                JSONObject.parseObject(json, BirtneyModel.class, JSONReader.Feature.Base64StringAsByteArray);

        assertEquals(
                AlimeFromConfig.class,
                birtneyModel.configs.get("FROM").get(0).getClass()
        );
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class AlimeFromConfig
            extends AbstractConfig {
        private String defaultFromId;
        private Map<String, String> urlMap;
    }

    @JSONType(builder = AlimeFromConfig.class)
    public abstract static class AbstractConfig
            implements Serializable {
        private Long id;
        private String type;
        private String subType;
        private String sceneId;
        private Integer version;
    }

    @Data
    public static class BirtneyModel
            implements Serializable {
        private Map<String, List<AbstractConfig>> configs;
    }
}
