package com.alibaba.fastjson2.v1issues.issue_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1879 {
    @Test
    public void test_for_issue() throws Exception {
        String json = "{\n" +
                "   \"ids\" : \"1,2,3\"\n" +
                "}";
        M1 m = JSON.parseObject(json, M1.class);
    }

    @Test
    public void test_for_issue_2() throws Exception {
        String json = "{\n" +
                "   \"ids\" : \"1,2,3\"\n" +
                "}";
        M2 m = JSON.parseObject(json, M2.class);
        assertNotNull(m);
    }

    @Test
    public void test_for_issue_2_creators() throws Exception {
        ObjectReaderCreator[] creators = new ObjectReaderCreator[]{
                ObjectReaderCreator.INSTANCE,
//                ObjectReaderCreatorLambda.INSTANCE,
//                ObjectReaderCreatorASM.INSTANCE,
        };

        String json = "{\n" +
                "   \"ids\" : \"1,2,3\"\n" +
                "}";

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<M2> objectReader = creator.createObjectReader(M2.class);
            M2 m = objectReader.readObject(
                    JSONReader.of(json));
            assertNotNull(m);
            assertNotNull(m.ids);
            assertEquals(3, m.ids.size());
        }
    }

    public static class M1 {
        private List<Long> ids;

        @JSONCreator
        public M1(@JSONField(name = "ids") String ids) {
            this.ids = new ArrayList<Long>();
            for (String id : ids.split(",")) {
                this.ids.add(Long.valueOf(id));
            }
        }
    }

    public static class M2 {
        private List<Long> ids;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
    }
}
