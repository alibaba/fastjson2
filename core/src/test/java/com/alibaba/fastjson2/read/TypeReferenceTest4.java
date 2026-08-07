package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.Data;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("reader")
public class TypeReferenceTest4 {
    @Test
    public void test() {
        ExtInfoBean extInfoBean = new ExtInfoBean();
        extInfoBean.setInfoId("id");
        extInfoBean.setInfoName(ExtInfoBean.class.getName());
        extInfoBean.setInfoData(null);

        String json = JSON.toJSONString(extInfoBean);
        assertEquals("{\"infoId\":\"id\",\"infoName\":\"com.alibaba.fastjson2.read.TypeReferenceTest4$ExtInfoBean\"}", json);

        ExtInfoBean result = testTypeReference(json, null);
        assertEquals(json, JSON.toJSONString(result));
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
