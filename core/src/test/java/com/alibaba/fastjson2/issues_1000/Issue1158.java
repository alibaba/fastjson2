package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1158 {
    @Test
    public void test() {
        StandardGetQry standardGetQry = new StandardGetQry();
        StandardBaseModel standardBaseModel = new StandardBaseModel();
        standardBaseModel.setCode("1");
        standardBaseModel.setCategory("RECORD_CALL_STATUS");
        standardGetQry.setStandardBaseModel(standardBaseModel);
        String jsonStr = JSON.toJSONString(standardGetQry);
        assertEquals("{\"standardCategory\":\"RECORD_CALL_STATUS\",\"standardCode\":\"1\"}", jsonStr);

        StandardGetQry standardGetQry2 = JSON.parseObject(jsonStr, StandardGetQry.class);
        assertEquals("{\"standardCategory\":\"RECORD_CALL_STATUS\",\"standardCode\":\"1\"}", JSON.toJSONString(standardGetQry2));
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
}
