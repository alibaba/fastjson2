package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1699 {
    @Test
    public void test() {
        assertEquals("{\"code\":\"Big\",\"desc\":\"大\"}", JSON.toJSONString(MyEnum.Big));
    }

    @JSONType(writeEnumAsJavaBean = true)
    public interface MapEnum {
        String getCode();
        String getDesc();
    }

    public enum MyEnum
            implements MapEnum {
        Big("Big", "大"),
        Small("Small", "小");

        MyEnum(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        final String code;
        final String desc;

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getDesc() {
            return desc;
        }
    }
}
