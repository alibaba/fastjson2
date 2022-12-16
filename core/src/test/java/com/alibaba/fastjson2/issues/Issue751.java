package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue751 {
    @Test
    public void test() {
        byte[] bytes = JSONB.toBytes(new Data());
        Object o = JSONB.parseObject(bytes, Data.class, JSONReader.Feature.FieldBased);
        assertEquals(Data.class, o.getClass());
    }

    static class Data
            implements Serializable {
        private String mData = "";

        public Data() {
        }

        public String getData() {
            return mData;
        }

        public void setData(String data) {
            mData = data;
        }
    }
}
