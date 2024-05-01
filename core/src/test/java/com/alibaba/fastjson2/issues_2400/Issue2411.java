package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.Fnv;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2411 {
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
        RmsFinishCourseDTO course = new RmsFinishCourseDTO();
        course.setCourseId("6bad799a1c894893bedade17215244a1");
        course.setUserId("4b99d48f87f84868a59aa3b3ce82fd56");

        byte[] bytes = JSONB.toBytes(course, writerFeatures);

        RmsFinishCourseDTO result =
                JSONB.parseObject(bytes, RmsFinishCourseDTO.class, readerFeatures);

        assertEquals(course.userId, result.userId);
        assertEquals(course.courseId, result.courseId);
    }

    @Test
    public void testJSON() {
        RmsFinishCourseDTO course = new RmsFinishCourseDTO();
        course.setCourseId("6bad799a1c894893bedade17215244a1");
        course.setUserId("4b99d48f87f84868a59aa3b3ce82fd56");

        String json = JSON.toJSONString(course, writerFeatures);

        RmsFinishCourseDTO result =
                JSON.parseObject(json, RmsFinishCourseDTO.class, readerFeatures);
        System.out.println(JSON.toJSONString(result, JSONWriter.Feature.FieldBased));

        assertEquals(course.userId, result.userId);
        assertEquals(course.courseId, result.courseId);
    }

    @Test
    public void getFieldReader() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(RmsFinishCourseDTO.class);
        assertNotNull(
                objectReader.getFieldReader("courseId"));
        assertNotNull(
                objectReader.getFieldReader(Fnv.hashCode64("courseId")));
    }

    @Data
    public static class RmsFinishCourseDTO
            implements Serializable {
        private String courseId;
        private String userId;
        private String studyRate;
        private String resourceId;
        private String providerCorpCode;
        private String userAgent;
        private String sourceId;
    }
}
