package com.alibaba.fastjson2.internal.processor.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2411Bean {
    static JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.WriteNameAsSymbol
    };
    static JSONReader.Feature[] readerFeatures = {
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.ErrorOnNoneSerializable, JSONReader.Feature.IgnoreAutoTypeNotMatch,
            JSONReader.Feature.UseNativeObject, JSONReader.Feature.FieldBased
    };

    @Test
    public void test() {
        Bean course = new Bean();
        course.courseId = "6bad799a1c894893bedade17215244a1";
        course.userId = "4b99d48f87f84868a59aa3b3ce82fd56";

        String json = JSON.toJSONString(course, writerFeatures);

        Bean result = JSON.parseObject(json, Bean.class, readerFeatures);

        assertEquals(course.userId, result.userId);
        assertEquals(course.courseId, result.courseId);
    }

    @JSONCompiled
    public static class Bean {
        public String courseId;
        public String userId;
        public String studyRate;
        public String resourceId;
        public String providerCorpCode;
        public String userAgent;
        public String sourceId;
    }
}
