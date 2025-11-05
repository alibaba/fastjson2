package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3842 {
    @Test
    public void test() {
        assertEquals(0, test(0));
    }

    private static int test(int errorCode) {
        UserData u1 = new UserData();
        u1.setErrorCode(errorCode);

        byte[] bytes = JSONB.toBytes(
                u1,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);

        System.out.println(JSONB.toJSONString(bytes));

        UserData u2 = JSONB.parseObject(
                bytes,
                UserData.class,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased
        );
        return u2.getErrorCode();
    }

    public static class UserData
            implements Serializable {
        private static final long serialVersionUID = 8521716552408936209L;

        private int errorCode = -1;
        // private int errorCode;

        public int getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }
    }
}
