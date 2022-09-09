package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Issue647 {
    @Test
    public void test() {
        URL url = Issue647.class.getClassLoader().getResource("issues/issue647.json");
        JSONObject jsonObject = JSON.parseObject(url);
        assertNotNull(jsonObject);
    }

    @Test
    public void test1() {
        String str = "{\"item\":[{\"id\":101}]";

        Exception error = null;
        try {
            char[] chars = str.toCharArray();
            JSON.parseObject(chars, 0, chars.length, Bean.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("item"));

        error = null;
        try {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSON.parseObject(bytes, 0, bytes.length, Bean.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("item"));

        error = null;
        try {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
            jsonReader.read(Bean.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("item"));
    }

    public static class Bean {
        public Item item;
    }

    public static class Item {
        public int id;
    }

    @Test
    public void test2() {
        String str = "{\"@type\":\"com.alibaba.fastjson2.issues.Issue647$TradeDTO\"," +
                "\"storeCoupons\": {\n" +
                "        \"@type\": \"java.util.HashMap\"\n" +
                "    },\n" +
                "    \"storeRemark\": [\n" +
                "        {\n" +
                "            \"remark\": \"\",\n" +
                "            \"storeId\": \"1514499072599498753\"\n" +
                "        }\n" +
                "    ]" +
                "}";
        TradeDTO bean2 = (TradeDTO) JSON.parseObject(str, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals("1514499072599498753", bean2.storeRemark.get(0).storeId);
    }

    public static class TradeDTO {
        public Object storeCoupons;
        public List<StoreRemarkDTO> storeRemark;
    }

    @Data
    public static class StoreRemarkDTO {
        private String storeId;
        private String remark;
    }
}
