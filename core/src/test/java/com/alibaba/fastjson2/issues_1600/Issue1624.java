package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1624 {
    @Test
    public void test() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();

        jsonWriter.startObject();
        jsonWriter.writeName("list");
        jsonWriter.writeTypeName("com.alibaba.fastjson2.issues_1600.Issue1624$PList");
        jsonWriter.write(new ArrayList());
        jsonWriter.endObject();

        byte[] jsonbBytes = jsonWriter.getBytes();

        Bean bean = JSONB.parseObject(jsonbBytes, Bean.class);
        assertEquals(ArrayList.class, bean.list.getClass());
    }

    public static class Bean {
        public List list;
    }

    @Test
    public void test1() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeTypeName("com.alibaba.fastjson2.issues_1600.Issue1624$Bean1");
        jsonWriter.startObject();
        jsonWriter.writeName("id");
        jsonWriter.writeInt32(123);
        jsonWriter.endObject();

        byte[] jsonbBytes = jsonWriter.getBytes();
        JSONB.dump(jsonbBytes);

        IBean1 bean = JSONB.parseObject(jsonbBytes, IBean1.class);
        assertEquals(123, bean.getId());
    }

    public interface IBean1 {
        int getId();
    }
}
