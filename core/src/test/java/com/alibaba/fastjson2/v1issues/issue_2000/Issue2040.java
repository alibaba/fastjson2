package com.alibaba.fastjson2.v1issues.issue_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Permissions;
import org.gitlab4j.api.models.Visibility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue2040 {
    @Test
    public void test_for_issue_2040() {
        Model model = JSON.parseObject("{\"accessLevel\":30,\"visibility\":\"PUBLIC\"}", Model.class);
        assertSame(AccessLevel.DEVELOPER, model.accessLevel);
    }

    @Test
    public void test_for_issue_2040_2() {
        String json = "{\n" +
                "      \"project_access\": null,\n" +
                "      \"group_access\": {\n" +
                "        \"access_level\": 50,\n" +
                "        \"notification_level\": 3\n" +
                "      }\n" +
                "    }";

        Permissions permissions = JSON.parseObject(json, Permissions.class, JSONReader.Feature.SupportSmartMatch);
        String json2 = JSON.toJSONString(permissions);
        assertEquals("{\"groupAccess\":{\"accessLevel\":50,\"notificationLevel\":3}}", json2);
    }

    public static class Model {
        public AccessLevel accessLevel;
        public Visibility visibility;
    }
}
