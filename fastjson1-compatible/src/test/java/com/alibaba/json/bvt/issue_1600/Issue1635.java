package com.alibaba.json.bvt.issue_1600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PascalNameFilter;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1635 {
    public static class Foo {
        public String name;
        public Integer BarCount;
        public Boolean flag;
        public List list;

        public Foo(String name, Integer barCount) {
            this.name = name;
            BarCount = barCount;
        }
    }

    @Test
    public void test_issue() throws Exception {
        SerializeConfig config = new SerializeConfig();
        config.setAsmEnable(false);
        Foo foo = new Foo(null, null);
        String json = JSON.toJSONString(foo,
                new SerializeFilter[]{new PascalNameFilter()},
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullListAsEmpty
        );
        assertEquals("{\"BarCount\":0,\"Flag\":false,\"List\":[],\"Name\":\"\"}", json);
    }

    @Test
    public void test_issue_1() throws Exception {
        SerializeConfig config = new SerializeConfig();
        config.setAsmEnable(false);
        Foo foo = new Foo(null, null);
        String json = JSON.toJSONString(foo,
                new SerializeFilter[]{new PascalNameFilter()},
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.BeanToArray
        );
        assertEquals("[0,false,[],\"\"]", json);
    }
}
