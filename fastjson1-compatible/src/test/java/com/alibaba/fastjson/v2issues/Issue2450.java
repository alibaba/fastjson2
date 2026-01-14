package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue2450 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.self = bean;

        JSONSerializer serializer = new JSONSerializer(SerializeConfig.global);
        assertFalse(serializer.containsReference(bean));

        serializer.incrementIndent();
        serializer.decrementIdent();

        serializer.write(bean);

        assertEquals("{\"self\":{\"$ref\":\"..\"}}", serializer.toString());
        serializer.println();
        serializer.writeReference(bean);
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.self = bean;

        JSONSerializer serializer = new JSONSerializer(SerializeConfig.global);
        JSONWriter jsonWriter = serializer.out.getJSONWriter();
        jsonWriter.setPath("$", bean);
        jsonWriter.writeReference(bean);
        assertEquals("{\"$ref\":\"$\"}", serializer.toString());
    }

    public class Bean {
        public String name;

        public Bean self;
    }
}
