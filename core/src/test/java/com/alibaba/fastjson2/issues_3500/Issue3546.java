package com.alibaba.fastjson2.issues_3500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3546 {
    @Test
    public void test() {
        TextMessage textMsg = new TextMessage();

        // 第一次设置父类实例
        textMsg.setData(new Message());
        assertEquals("{\"data\":{\"channel\":null}}", JSON.toJSONString(textMsg, JSONWriter.Feature.WriteNulls));

        // 第二次设置子类实例
        AssignMessage am = new AssignMessage();
        am.setAssignee("assignee");
        textMsg.setData(am);

        assertEquals("{\"data\":{\"assignee\":\"assignee\",\"channel\":null}}", JSON.toJSONString(textMsg, JSONWriter.Feature.WriteNulls));
    }

    @Test
    public void test_reflect() {
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(TextMessage.class);

        TextMessage textMsg = new TextMessage();

        // 第一次设置父类实例
        textMsg.setData(new Message());
        assertEquals("{\"data\":{\"channel\":null}}",
                objectWriter.toJSONString(textMsg, JSONWriter.Feature.WriteNulls));

        // 第二次设置子类实例
        AssignMessage am = new AssignMessage();
        am.setAssignee("assignee");
        textMsg.setData(am);

        assertEquals("{\"data\":{\"assignee\":\"assignee\",\"channel\":null}}", objectWriter.toJSONString(textMsg, JSONWriter.Feature.WriteNulls));
    }

    @Getter
    @Setter
    class Message {
        private String channel;
        // getter/setter...
    }

    @Getter
    @Setter
    class AssignMessage
            extends Message {
        private String assignee;
    }

    @Getter
    @Setter
    class TextMessage {
        private Object data;
    }
}
