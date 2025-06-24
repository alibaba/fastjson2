package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2642 {
    @Test
    public void test() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(Long.class, (out, obj, fieldName, fieldType, features) -> {
            String val = obj.toString();
            out.writeString(val);
        });

        JSONWriter.Context context = new JSONWriter.Context(provider,
                JSONWriter.Feature.WriteNullStringAsEmpty,
                JSONWriter.Feature.WriteNulls);

        DemoDo demoDo = new DemoDo();
        String json = JSON.toJSONString(demoDo, context);

        System.out.println(json); //实际上 s1 输出为 null

        assertEquals("{\"n0\":null,\"n1\":\"1\",\"s0\":\"\",\"s1\":\"noear\"}", json);
    }

    @Getter
    @Setter
    public static class DemoDo
            implements Serializable {
        String s0;
        String s1 = "noear";

        Long n0;
        Long n1 = 1L; //当有 Long 字段时，才触发这个问题
    }
}
