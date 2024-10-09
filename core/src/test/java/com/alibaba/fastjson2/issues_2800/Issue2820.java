package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2820 {
    static class CityGMLVersionMixin {
        @JSONField(value = true)
        public String toValue() {
            return null;
        }
    }

    @Test
    public void test() {
        JSON.mixIn(CityGMLVersion.class, CityGMLVersionMixin.class);
        FOO foo = JSON.parseObject("{\n" +
                "  \"version\": \"2.0\"\n" +
                "}", FOO.class);

        assertEquals(CityGMLVersion.v2_0, foo.getVersion());
    }

    public class FOO {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteEnumUsingToString)
        private CityGMLVersion version;

        public CityGMLVersion getVersion() {
            return version;
        }

        public FOO setVersion(CityGMLVersion version) {
            this.version = version;
            return this;
        }

        public FOO setVersion(String version) {
            this.version = CityGMLVersion.fromValue(version);
            return this;
        }
    }

    public enum CityGMLVersion {
        v3_0("3.0"),
        v2_0("2.0"),
        v1_0("1.0");

        private final String value;

        CityGMLVersion(String value) {
            this.value = value;
        }

        public String toValue() {
            return this.value;
        }

        public static CityGMLVersion fromValue(String value) {
            for (CityGMLVersion v : values()) {
                if (v.value.equals(value)) {
                    return v;
                }
            }
            return null;
        }

        public String toString() {
            return this.value;
        }
    }
}
