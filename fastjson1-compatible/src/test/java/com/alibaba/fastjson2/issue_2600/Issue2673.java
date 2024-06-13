package com.alibaba.fastjson2.issue_2600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author 张治保
 * @since 2024/6/13
 */
public class Issue2673 {
    @Test
    void test() {
        String jsonStr = "{\r\n"
                + "  \"id\":\"01\",\r\n"
                +"\"list\":[1,2,3],"
                + "  \"data\":{\r\n"
                + "      \"key\":\"test\"\r\n"
                + "  }\r\n"
                + "}";
        JSONObject json = JSON.parseObject(jsonStr);
        Bean bean = JSON.toJavaObject(json, Bean.class);
        Assertions.assertDoesNotThrow(() -> (JSONObject) bean.getData());
        Assertions.assertDoesNotThrow(() -> (JSONArray) bean.getList());
    }

    @Getter
    @Setter
    static class Bean {
        private String id;
        private Object data;
        private Object list;
    }
}
