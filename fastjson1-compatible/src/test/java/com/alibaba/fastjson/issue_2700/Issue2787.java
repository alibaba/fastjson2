package com.alibaba.fastjson.issue_2700;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2787 {
    @Test
    public void test_for_issue() throws Exception {
        Model m = new Model();
        String str = JSON.toJSONString(m, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty);
        assertEquals("{\"value\":[]}", str);
    }

    public static class Model {
        public int[] value;
    }

    public static class Issue2752 {
        @Test
        public void test_for_issue() {
            Pageable pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "id");
            SerializeConfig config = new SerializeConfig();
//                    config.register(new MyModule());
            String result = JSON.toJSONString(pageRequest, config);
            assertTrue(result.indexOf("\"property\":\"id\"") != -1);
        }
    }
}
