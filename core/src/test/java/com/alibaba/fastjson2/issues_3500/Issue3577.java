package com.alibaba.fastjson2.issues_3500;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

public class Issue3577 {
    @Test
    public void test() {
        String json = "{            \n" +
                "            \"certifiedMaterials\": {\n" +
                "              \"show\": false,\n" +
                "              \"materials\": []\n" +
                "            }\n" +
                "          }";

        ColorBrandsProduct dto = JSON.parseObject(json, ColorBrandsProduct.class);
        assertNotNull(dto);
        assertNull(dto.getCertifiedMaterials().getContainsAtLeastTitle());
    }

    @JsonPropertyOrder(ColorBrandsProduct.JSON_PROPERTY_CERTIFIED_MATERIALS)
    @JsonTypeName("Color")
    public static class ColorBrandsProduct
            implements Serializable {
        private static final long serialVersionUID = 1L;
        public static final String JSON_PROPERTY_CERTIFIED_MATERIALS = "certifiedMaterials";
        private CertifiedMaterialsBrandsProduct certifiedMaterials;
        public ColorBrandsProduct certifiedMaterials(CertifiedMaterialsBrandsProduct certifiedMaterials) {
            this.certifiedMaterials = certifiedMaterials;
            return this;
        }

        /**
         * Get certifiedMaterials
         *
         * @return certifiedMaterials
         **/
        @JsonProperty(JSON_PROPERTY_CERTIFIED_MATERIALS)
        @JsonInclude(value = JsonInclude.Include.ALWAYS)

        public CertifiedMaterialsBrandsProduct getCertifiedMaterials() {
            return certifiedMaterials;
        }

        @JsonProperty(JSON_PROPERTY_CERTIFIED_MATERIALS)
        @JsonInclude(value = JsonInclude.Include.ALWAYS)
        public void setCertifiedMaterials(CertifiedMaterialsBrandsProduct certifiedMaterials) {
            this.certifiedMaterials = certifiedMaterials;
        }
    }

    @JsonPropertyOrder(CertifiedMaterialsBrandsProduct.JSON_PROPERTY_CONTAINS_AT_LEAST_TITLE)
    @JsonTypeName("CertifiedMaterials")
    public static class CertifiedMaterialsBrandsProduct
            implements Serializable {
        private static final long serialVersionUID = 1L;

        public static final String JSON_PROPERTY_CONTAINS_AT_LEAST_TITLE = "containsAtLeastTitle";
        private String containsAtLeastTitle;

        public CertifiedMaterialsBrandsProduct containsAtLeastTitle(String containsAtLeastTitle) {
            this.containsAtLeastTitle = containsAtLeastTitle;
            return this;
        }

        /**
         * Title that says \&quot;contains at least\&quot;, translated to the given store language.
         *
         * @return containsAtLeastTitle
         **/
        @JsonProperty(JSON_PROPERTY_CONTAINS_AT_LEAST_TITLE)
        // @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

        public String getContainsAtLeastTitle() {
            return containsAtLeastTitle;
        }

        @JsonProperty(JSON_PROPERTY_CONTAINS_AT_LEAST_TITLE)
        @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
        public void setContainsAtLeastTitle(String containsAtLeastTitle) {
            this.containsAtLeastTitle = containsAtLeastTitle;
        }
    }
}
