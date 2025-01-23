package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
                + "    \"Ref\": \"\",\r\n"
                + "    \"Width\": \"1.01\",\r\n"
                + "    \"High\": \"\"\r\n"
                + "}";
        TestVO jsonRootBean = JSON.parseObject(json, TestVO.class, FASTJSON_DEFAULT_READER_FEATURES);
        assertEquals("",jsonRootBean.Ref);
        assertEquals(1.01,jsonRootBean.Width);
        assertNull(jsonRootBean.High);

    }

    @Test
    public void testWhiteSpaceComma(){
        String json = "{\r\n"
                + "    \"Ref\": \"\"\r\n,\r\n"
                + "    \"Width\": \"1.01\"\r\n , \r\n"
                + "    \"High\": \"\"\r\n"
                + ",\"test2\": \"\"\r\n}";
        TestVO jsonRootBean = JSON.parseObject(json, TestVO.class, FASTJSON_DEFAULT_READER_FEATURES);
        assertEquals("",jsonRootBean.Ref);
        assertEquals(1.01,jsonRootBean.Width);
        assertNull(jsonRootBean.High);
    }
}
