package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONB;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Issue1812 {
    @Test
    public void test() throws Exception {
        List<DriverEntity> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            DriverEntity entity = new DriverEntity();
            entity.values = new ArrayList<>();
            entity.values.add("a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890a1234567890" + i);
            list.add(entity);
        }
        JSONB.toBytes(list);
    }

    @Data
    public class DriverEntity
            implements Serializable {
        public List<String> values;
    }
}
