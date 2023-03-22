package com.alibaba.fastjson;

import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeReferenceTest4 {
    @Test
    public void test() {
        ExtInfoBean extInfoBean = new ExtInfoBean();
        extInfoBean.setInfoId("id");
        extInfoBean.setInfoName(ExtInfoBean.class.getName());
        extInfoBean.setInfoData(null);

        String json = com.alibaba.fastjson2.JSON.toJSONString(extInfoBean);
        assertEquals("{\"infoId\":\"id\",\"infoName\":\"com.alibaba.fastjson.TypeReferenceTest4$ExtInfoBean\"}", json);

        ExtInfoBean result = testTypeReference(json, null);
        assertEquals(json, com.alibaba.fastjson2.JSON.toJSONString(result));
    }

    private static <T> ExtInfoBean<T> testTypeReference(String extJson, Class<T> clz) {
        ExtInfoBean extInfoBean = JSON.parseObject(extJson,
                new TypeReference<ExtInfoBean<T>>(clz) {
                });

        return extInfoBean;
    }

    /**
     * @author weizhiyu
     * @since 2022/07/06 2:35 PM
     */
    @Data
    public static class ExtInfoBean<T> {
        private String infoId;
        private String infoName;
        private T infoData;
    }
}
