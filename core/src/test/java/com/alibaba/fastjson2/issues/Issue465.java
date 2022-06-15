package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue465 {
    @Test
    public void test() {
        String data = "{\"pNum\":2,\"pSize\":10}";
        PageParameterData queryCondition = JSON.parseObject(data, PageParameterData.class);
        assertEquals(2, queryCondition.pNum);

        PageParameterData queryCondition2 = JSON.parseObject(data.getBytes(StandardCharsets.UTF_8), PageParameterData.class);
        assertEquals(2, queryCondition2.pNum);
    }

    @Data
    public class PageParameterData {
        private int pSize = 10;
        private int pNum = 1;
        private int sRows;
        private int eRows = 10;
        private List<PageCondition> paramList = new ArrayList<>();

        public PageParameterData() {
        }

        public PageParameterData(final int pNum, final int pSize) {
            this.pNum = pNum;
            this.pSize = pSize;
        }
    }

    public class PageCondition {
    }
}
