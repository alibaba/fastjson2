package com.alibaba.fastjson.issue_2700;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.MiscCodec;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.spi.Module;
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

        public class MyModule implements Module {

            @Override
            public ObjectDeserializer createDeserializer(ParserConfig config, Class type) {
                if (type.getName().equals("org.springframework.data.domain.Sort")) {
                    return MiscCodec.instance;
                }
                return null;
            }

            @Override
            public ObjectSerializer createSerializer(SerializeConfig config, Class type) {
                if (type.getName().equals("org.springframework.data.domain.Sort")) {
                    return MiscCodec.instance;
                }
                return null;
            }
        }

    }
}
