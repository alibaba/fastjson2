package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

public class Issue3499WithoutFix {
    /**
     * This test demonstrates the issue that was fixed in commit c1b0b6382.
     * Before the fix, using ErrorOnNoneSerializable with generic types would throw
     * "not support none-Serializable" even when the generic type implements Serializable.
     */
    @Test
    public void testGenericMessageWithSerializable() {
        // This should work fine - Message implements Serializable and String is serializable
        Message<String> obj = new Message<>("Hello");
        byte[] bytes = JSONB.toBytes(obj, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable); // This feature was causing the issue

        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(true, obj.getClass());
        Object result = JSONB.parseObject(bytes, obj.getClass(), handler,
                JSONReader.Feature.ErrorOnNoneSerializable); // This feature was causing the issue

        assertEquals(obj.getMsg(), ((Message<String>) result).getMsg());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class Message<T>
            implements Serializable {
        T msg;
    }
}
