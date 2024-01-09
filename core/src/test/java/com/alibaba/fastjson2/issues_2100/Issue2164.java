package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.*;
import org.junit.jupiter.api.Test;

/**
 * @author 张治保
 * @since 2024/1/9
 */
public class Issue2164 {
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class TestVO {
        private String Ref;
        private Double Width;
        private Double High;
    }

    /**
     * Fastjson默认读特性
     */
    public static final JSONReader.Feature[] FASTJSON_DEFAULT_READER_FEATURES = {JSONReader.Feature.SupportSmartMatch, JSONReader.Feature.SupportArrayToBean, JSONReader.Feature.UseNativeObject, JSONReader.Feature.SupportClassForName, JSONReader.Feature.IgnoreSetNullValue, JSONReader.Feature.AllowUnQuotedFieldNames, JSONReader.Feature.IgnoreCheckClose, JSONReader.Feature.IgnoreAutoTypeNotMatch};

    @Test
    public void test() {
        String json = "{\r\n"
                + "	\"Ref\": \"\",\r\n"
                + "	\"Width\": \"1.01\",\r\n"
                + "	\"High\": \"\"\r\n"
                + "}";
//		String json = "{\"Ref\": \"\",\"Width\": \"\",\"High\": \"\"}";
        TestVO jsonRootBean = JSON.parseObject(json, TestVO.class, FASTJSON_DEFAULT_READER_FEATURES);
        System.out.println(jsonRootBean);
    }
}
