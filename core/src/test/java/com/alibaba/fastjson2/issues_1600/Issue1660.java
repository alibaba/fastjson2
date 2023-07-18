package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1660 {
    @Test
    public void test() {
        ConsumerUserCollectionVO collectionVO = new ConsumerUserCollectionVO();
        collectionVO.setVisitorSum(123);
        assertEquals(
                "{\"visitorSum\":123}",
                JSON.toJSONString(collectionVO, JSONWriter.Feature.NotWriteDefaultValue)
        );
    }

    @Data
    public static class ConsumerUserCollectionVO
            implements Serializable {
        private static final long serialVersionUID = 7707169390289072582L;
        private int visitorSum;
        private int visitSum;
    }
}
