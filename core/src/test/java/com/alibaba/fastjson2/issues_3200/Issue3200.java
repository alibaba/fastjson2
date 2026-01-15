package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3200 {
    @Test
    public void testArray() {
        SecondMenuGroupUI secondMenuGroupUI = new SecondMenuGroupUI();
        secondMenuGroupUI.setGroupId("group1");
        secondMenuGroupUI.setGroupName("分组");
        String expected = "{\"groupId\":\"group1\",\"groupName\":\"分组\",\"secondList\":[]}";
        assertEquals(expected, JSON.toJSONString(secondMenuGroupUI));
        assertEquals(expected, new String(JSON.toJSONBytes(secondMenuGroupUI), StandardCharsets.UTF_8));
    }

    @Test
    public void testArray_reflect() {
        SecondMenuGroupUI secondMenuGroupUI = new SecondMenuGroupUI();
        secondMenuGroupUI.setGroupId("group1");
        secondMenuGroupUI.setGroupName("分组");
        assertEquals("{\"groupId\":\"group1\",\"groupName\":\"分组\",\"secondList\":[]}",
                ObjectWriterCreator.INSTANCE.createObjectWriter(SecondMenuGroupUI.class).toJSONString(secondMenuGroupUI));
    }

    @Getter
    @Setter
    public static class SecondMenuGroupUI {
        @JSONField(ordinal = 2)
        private String groupName;
        @JSONField(ordinal = 1)
        private String groupId;
        @JSONField(ordinal = 3, serializeFeatures = JSONWriter.Feature.WriteNullListAsEmpty)
        private List<String> secondList;
    }

    @Test
    public void testArray1() {
        SecondMenuGroupUI1 secondMenuGroupUI = new SecondMenuGroupUI1();
        secondMenuGroupUI.groupId = "group1";
        secondMenuGroupUI.groupName = "分组";
        assertEquals("{\"groupId\":\"group1\",\"groupName\":\"分组\",\"secondList\":[]}", JSON.toJSONString(secondMenuGroupUI));
    }

    @Test
    public void testArray1_reflect() {
        SecondMenuGroupUI1 secondMenuGroupUI = new SecondMenuGroupUI1();
        secondMenuGroupUI.groupId = "group1";
        secondMenuGroupUI.groupName = "分组";
        assertEquals("{\"groupId\":\"group1\",\"groupName\":\"分组\",\"secondList\":[]}",
                ObjectWriterCreator.INSTANCE.createObjectWriter(SecondMenuGroupUI1.class)
                        .toJSONString(secondMenuGroupUI));
    }

    public static class SecondMenuGroupUI1 {
        @JSONField(ordinal = 2)
        public String groupName;
        @JSONField(ordinal = 1)
        public String groupId;
        @JSONField(ordinal = 3, serializeFeatures = JSONWriter.Feature.WriteNullListAsEmpty)
        public List<String> secondList;
    }
}
