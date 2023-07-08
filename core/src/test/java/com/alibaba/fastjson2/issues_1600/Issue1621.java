package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1621 {
    @Test
    public void test() {
        final OffsetDateTime defaultDateTime = OffsetDateTime.MIN;
        DemoMetadata demoMetadata = DemoMetadata.builder()
                .metadataType("TestType")
                .lastMetadataRefreshTime(defaultDateTime)
                .build();
        Map<String, DemoMetadata> map = new HashMap<>();
        map.put(demoMetadata.getMetadataType(), demoMetadata);
        DemoRoot demoRoot = DemoRoot.builder()
                .rootName("Root")
                .lastMetadataRefreshTime(defaultDateTime)
                .dmoToDaoTypeMapping(map)
                .build();
        JSONObject jsonObject = (JSONObject) JSON.toJSON(demoRoot);
        DemoRoot updatedRoot = jsonObject.toJavaObject(DemoRoot.class);
        assertEquals(defaultDateTime, updatedRoot.getLastMetadataRefreshTime());
    }

    @Data
    @SuperBuilder
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class DemoRoot
            extends DemoAbstract {
        private static final long serialVersionUID = 1L;
        private String rootName;
        private Map<String, DemoMetadata> dmoToDaoTypeMapping;

        @Override
        public void someFunction() {
        }
    }

    @Data
    @SuperBuilder
    @Generated
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @ToString
    public static class DemoMetadata
            extends DemoAbstract {
        private static final long serialVersionUID = 1L;
        private String metadataType;

        @Override
        public void someFunction() {
        }
    }

    @SuperBuilder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public abstract static class DemoAbstract
            implements Serializable {
        private static final long serialVersionUID = 1L;
        @EqualsAndHashCode.Exclude
        private OffsetDateTime lastMetadataRefreshTime;

        public abstract void someFunction();
    }
}
