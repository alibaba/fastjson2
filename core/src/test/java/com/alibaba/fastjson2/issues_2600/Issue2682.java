package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2682 {
    @Test
    public void test() {
        String str = "{\"bizType\":\"common\"}";
        VM vm = JSON.parseObject(str, VM.class);
        assertEquals(BizType.COMMON, vm.getBizType());
    }

    @Data
    public static class VM {
        private BizType bizType;
    }

    public enum BizType
            implements IEnum<String> {
        COMMON("common", "通用");

        BizType(String value, String name) {
            this.value = value;
            this.name = name;
        }

        @EnumValue
        private final String value;
        private final String name;

        @JSONField(value = true)
        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public static void test123() {
        }
    }
}
