package com.alibaba.fastjson2.example.spring6test.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

/**
 * @author Xi.Liu
 */
@Data
@Builder
public class WebFluxMockBean {
    private String name;
    @JSONField(name = "UpperName")
    private String upperName;

    private InnerBean innerBean;
    @JSONField(name = "UpperInnerBean")
    private InnerBean upperInnerBean;

    @Data
    @Builder
    public static class InnerBean {
        private String id;
        @JSONField(name = "UpperId")
        private String upperId;
    }
}
