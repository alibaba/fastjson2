package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 张治保
 * @since 2024/1/13
 */
public class Issue2180 {
    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @Accessors(chain = true)
    public static class Balance {
        private Double amount;
        private String currency;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Accessors(chain = true)
    public static class Recovery {
        private String name;
        private Balance balance;
        private Balance balance2;
        private Balance recovery;
    }

    @Test
    public void refTest() {
        Balance usd = new Balance()
                .setAmount(100.12)
                .setCurrency("USD");
        Recovery recovery = new Recovery()
                .setName("hello")
                .setBalance(usd)
                .setBalance2(usd)
                .setRecovery(usd);
        String jsonString = JSON.toJSONString(recovery, JSONWriter.Feature.ReferenceDetection);
        //完全相等 （在 JSONObject 中是完全相等的两个对象）
        JSONObject jsonObject = JSON.parseObject(jsonString);
        assertSame(jsonObject.get("balance"), jsonObject.get("balance2"));
        assertSame(jsonObject.get("balance"), jsonObject.get("recovery"));
        //原数据转成 jsonObject 然后转为对应类型 这里不完全相等，
        //todo 使用JSONObject 转换之后不完全相等(不是同一个对象) 可优化
        Recovery recovery2 = jsonObject.to(Recovery.class);
        assertEquals(recovery2, recovery);
        //原数据转换 完全相等
        Recovery recovery3 = JSON.parseObject(jsonString, Recovery.class);
        assertEquals(recovery3, recovery);
    }
}
