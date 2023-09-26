package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Issue1866 {
    @Test
    public void test() {
        User hello = new User();
        hello.setAge1(null);  // WriteNullNumberAsZero, 期望为0
        hello.setAge2(null);  // WriteNulls, 期望为null
        hello.setAge3(null);  // NullAsDefaultValue, 期望为0
        hello.setAge4(null);  // 期望不被序列化
        // age5, age6不为null应不受影响
        hello.setAge5(10);
        hello.setAge6(10);
        System.out.println(JSON.toJSONString(hello));
        assertEquals("{\"age1\":0,\"age2\":null,\"age3\":0,\"age5\":10,\"age6\":10}", JSON.toJSONString(hello));
    }

    public static class User {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Integer age1;

        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        private Integer age2;

        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Integer age3;

        private Integer age4;

        private Integer age5;

        private Integer age6;

        public Integer getAge1() {
            return age1;
        }

        public void setAge1(Integer age1) {
            this.age1 = age1;
        }

        public Integer getAge2() {
            return age2;
        }

        public void setAge2(Integer age2) {
            this.age2 = age2;
        }

        public Integer getAge3() {
            return age3;
        }

        public void setAge3(Integer age3) {
            this.age3 = age3;
        }

        public Integer getAge4() {
            return age4;
        }

        public void setAge4(Integer age4) {
            this.age4 = age4;
        }

        public Integer getAge5() {
            return age5;
        }

        public void setAge5(Integer age5) {
            this.age5 = age5;
        }

        public Integer getAge6() {
            return age6;
        }

        public void setAge6(Integer age6) {
            this.age6 = age6;
        }
    }
}
