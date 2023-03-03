package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1184 {
    @Test
    public void test() {
        final String source = "[null]";
        List<VerifyDevice> strings = JSON.parseArray(source, VerifyDevice.class);
        assertEquals("[null]", JSON.toJSONString(strings));
    }

    public enum VerifyDevice {
        FACE_DETECT("face_1", "人脸"),
        IC_CARD("ic_2", "IC卡");

        private final String code;
        private final String disc;

        VerifyDevice(String code, String disc) {
            this.code = code;
            this.disc = disc;
        }

        public String getCode() {
            return code;
        }

        public String getDisc() {
            return disc;
        }
    }

    @Test
    public void test1() {
        final String source = "[null]";
        List<Type> strings = JSON.parseArray(source, Type.class);
        assertEquals("[null]", JSON.toJSONString(strings));
    }

    public enum Type {
        A, B, C
    }
}
