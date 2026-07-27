package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue7685 {
    @Test
    public void test() {
        String str = "{\"Test\":[{\"message\":\"中\\n\"},{\"message\":\"s Howard), dedicated to dinosaur conservation, was invited by Benjamin Lockwood. Benjamin and his subordinate Eli Mills (played by Rafi Spo) hope that she and relevant technicians will go to Isla Nublar to save the dinosaurs there and relocate them to a new habitat. Claire, who is infected, manages to bring Owen (played by Chris Pratt) along to go, while Owen hopes to rescue the velociraptor Blue that he has personally tamed. When they set foot on this isolated island, they found that things were not that simple.\\nVolcanoes are restless and ready to erupt, while ugly conspiracies are quietly brewing amidst chaos.\"}]}";
        JSONObject result = JSON.parseObject(str);
        assertNotNull(result);
        assertEquals(2, result.getJSONArray("Test").size());
    }

    @Test
    public void testVariousLengths() {
        for (int len = 1; len <= 600; len++) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"Test\":[{\"message\":\"中\\n\"},{\"message\":\"");
            for (int i = 0; i < len; i++) {
                sb.append('a');
            }
            sb.append("\\n");
            for (int i = 0; i < len; i++) {
                sb.append('b');
            }
            sb.append("\"}]}");

            JSONObject result = JSON.parseObject(sb.toString());
            assertNotNull(result);
            String message = result.getJSONArray("Test")
                    .getJSONObject(1)
                    .getString("message");
            assertEquals(len * 2 + 1, message.length());
        }
    }
}
