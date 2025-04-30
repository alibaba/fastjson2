package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3200 {
    @Test
    public void testArray() {
        SecondMenuGroupUI secondMenuGroupUI = new SecondMenuGroupUI();
        secondMenuGroupUI.setGroupId("group1");
        secondMenuGroupUI.setGroupName("分组");
        assertEquals("{\"groupId\":\"group1\",\"groupName\":\"分组\",\"secondList\":[]}", JSON.toJSONString(secondMenuGroupUI));
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
}
