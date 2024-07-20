package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import sun.misc.VM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author yanxutao89
 * @author poo00
 */
public class Issue2682 {
    @Test
    public void test() {
        String str = "{\"bizType\":\"common\"}";
        VM vm = JSON.parseObject(str, VM.class);
        assertEquals(BizType.COMMON, vm.getBizType());
    }

    @Test
    public void test1() throws IOException {
        String str = "{'bizType':'common','baseType':'123啊'}";
        VM vm = JSON.parseObject(str, VM.class);
        assertEquals(RobotActEnum.CARTON_SCAN, vm.getBaseType());
    }

    @Data
    public static class VM {
        private BizType bizType;

        private RobotActEnum baseType;
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


    public enum RobotActEnum implements IEnum<String> {
        /**
         * 123啊
         */
        CARTON_SCAN("123啊", "我是123"),

        /**
         * 456啊
         */
        PALLET_ON_SHELF("456啊", "我是456"),

        /**
         * 789啊
         */
        OUTBOUND_BIND_DOCK("789啊", "我是789"),
        /**
         *147啊
         */
        GET_PICK_BILL("147啊", "我是147"),
        ;


        @JSONField(value = true)
        private final String value;

        private final String message;


        RobotActEnum(String value, String message) {
            this.value = value;
            this.message = message;
        }

        @Override
        public String getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public RobotActEnum getEnum() {
            return this;
        }

        public static String staticMessage(RobotActEnum robotActEnum) {
            if (null == robotActEnum) {
                return null;
            }
            return robotActEnum.getMessage();
        }

        public static String staticValue(RobotActEnum robotActEnum) {
            if (null == robotActEnum) {
                return null;
            }
            return robotActEnum.getValue();
        }
    }
}
