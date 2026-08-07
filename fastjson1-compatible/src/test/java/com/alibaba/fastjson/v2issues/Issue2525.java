package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2525 {
    @Test
    public void testMutated() {
        StandardGetQry standardGetQry = new StandardGetQry();
        StandardBaseModel standardBaseModel = new StandardBaseModel();
        standardBaseModel.setCode("2");
        standardBaseModel.setCategory("NEW_CALL_STATUS");
        standardGetQry.setStandardBaseModel(standardBaseModel);
        String jsonStr = JSON.toJSONString(standardGetQry);
        assertEquals("{\"standardCategory\":\"NEW_CALL_STATUS\",\"standardCode\":\"2\"}", jsonStr);

        StandardGetQry standardGetQry2 = JSON.parseObject(jsonStr, StandardGetQry.class);
        assertEquals("{\"standardCategory\":\"NEW_CALL_STATUS\",\"standardCode\":\"2\"}", JSON.toJSONString(standardGetQry2));
    }

    @Data
    public static class StandardGetQry {
        /**
         * 基础数据
         */
        @JSONField(unwrapped = true)
        private StandardBaseModel standardBaseModel;
    }

    @Data
    public static class StandardBaseModel {
        /**
         * 逻辑编码-标准KEY
         */
        @JSONField(name = "standardCode")
        private String code;

        /**
         * 标准类别
         */
        @JSONField(name = "standardCategory")
        private String category;
    }

    @Test
    public void testMutated1() {
        StandardGetQry1 standardGetQry = new StandardGetQry1();
        StandardBaseModel1 standardBaseModel = new StandardBaseModel1();
        standardBaseModel.setCode("2");
        standardBaseModel.setCategory("NEW_CALL_STATUS");
        standardGetQry.setStandardBaseModel(standardBaseModel);
        String jsonStr = com.alibaba.fastjson.JSON.toJSONString(standardGetQry);
        assertEquals("{\"standardCategory\":\"NEW_CALL_STATUS\",\"standardCode\":\"2\"}", jsonStr);

        StandardGetQry1 standardGetQry2 = com.alibaba.fastjson.JSON.parseObject(jsonStr, StandardGetQry1.class);
        assertEquals("{\"standardCategory\":\"NEW_CALL_STATUS\",\"standardCode\":\"2\"}", com.alibaba.fastjson.JSON.toJSONString(standardGetQry2));
    }

    @Data
    public static class StandardGetQry1 {
        /**
         * 基础数据
         */
        @com.alibaba.fastjson.annotation.JSONField(unwrapped = true)
        private StandardBaseModel1 standardBaseModel;
    }

    @Data
    public static class StandardBaseModel1 {
        /**
         * 逻辑编码-标准KEY
         */
        @com.alibaba.fastjson.annotation.JSONField(name = "standardCode")
        private String code;

        /**
         * 标准类别
         */
        @com.alibaba.fastjson.annotation.JSONField(name = "standardCategory")
        private String category;
    }
}
