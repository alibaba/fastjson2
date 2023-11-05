package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1985 {
    @Test
    public void test() throws Exception {
        DemoBean demoBean = new DemoBean();
        demoBean.setType("test");
        Map map = new HashMap();
        map.put("a", 1);
        demoBean.setMap(map);
        assertEquals("{\"a\":1}", JSON.toJSONString(demoBean));
    }

    @Data
    @JSONType(serializer = DemoFastJsonSerializer.class)
    @JsonSerialize(using = DemoJacksonSerializer.class)
    public static class DemoBean {
        private String type;
        private Map<String, Object> map;
    }

    public static class DemoJacksonSerializer
            extends JsonSerializer<DemoBean> {
        @Override
        public void serialize(DemoBean demoBean, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            for (Map.Entry<String, Object> entry : demoBean.getMap().entrySet()) {
                gen.writeFieldName(entry.getKey());
                gen.writeObject(entry.getValue());
            }
            gen.writeEndObject();
        }
    }

    public static class DemoFastJsonSerializer
            implements ObjectWriter<DemoBean> {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            jsonWriter.startObject();
            DemoBean demoBean = (DemoBean) object;
            for (Map.Entry<String, Object> entry : demoBean.getMap().entrySet()) {
                jsonWriter.writeName(entry.getKey());
                jsonWriter.writeColon();
                jsonWriter.writeAny(entry.getValue());
            }
            jsonWriter.endObject();
        }
    }
}
