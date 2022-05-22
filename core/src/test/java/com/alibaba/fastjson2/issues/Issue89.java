package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue89 {
    @Test
    public void fetchReward() {
        String str = "{ \n" +
                "    \"_id\" : \"6195f08f5b210000a7002c14\", \n" +
                "    \"activityContent\" : {\n" +
                "        \"awardRule\" : {\n" +
                "            \"ruleType\" : \"RANDOM\", \n" +
                "            \"ruleData\" : [\n" +
                "                {\n" +
                "                    \"amount\" : \"0.33\", \n" +
                "                    \"percentage\" : \"0.70\"\n" +
                "                }, \n" +
                "                {\n" +
                "                    \"amount\" : \"0.66\", \n" +
                "                    \"percentage\" : \"0.15\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    }\n" +
                "}";

        JSONObject object = JSON.parseObject(str);

        ActivityDO activityDO = object.toJavaObject(ActivityDO.class);
        JSONObject activityContent = activityDO.getActivityContent();
        // 这里getJSONObject就会报错
        JSONObject awardRule = activityContent.getJSONObject("awardRule");
        assertNotNull(awardRule);
    }

    public static class BaseDO {
    }

    @Getter
    @Setter
    public static class ActivityDO
            extends BaseDO {
        private JSONObject activityContent;
    }
}
