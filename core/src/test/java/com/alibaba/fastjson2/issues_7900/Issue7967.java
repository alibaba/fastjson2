package com.alibaba.fastjson2.issues_7900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue7967 {
    static class PayloadMessage {
        public String bizContent;
    }

    static class ChannelMessage {
        public PayloadMessage bizContent;
    }

    static class IntelligenceMessage {
        public byte[] payload;
        public ChannelMessage payloadObject;
    }

    static class ClusterMessage {
        public IntelligenceMessage message;
    }

    static final String JSON_TEXT = "{\"message\":{"
            + "\"payload\":\"eyJiaXpDb250ZW50Ijp7ImJpekNvbnRlbnQiOiJ7XCJhY3Rpb25cIjpcInRlc3RBY3Rpb25cIixcIm9yZGVyX2lkXCI6MTIzMTIzMTIzMTIzfSJ9fQ==\","
            + "\"payloadObject\":{\"bizContent\":{\"bizContent\":\"{\\\"action\\\":\\\"testAction\\\",\\\"order_id\\\":123123123123}\"}}"
            + "}}";

    @Test
    public void testBase64FieldFollowedByEscapedContentBytes() {
        ClusterMessage clusterMessage = JSON.parseObject(
                JSON_TEXT.getBytes(StandardCharsets.UTF_8), ClusterMessage.class,
                JSONReader.Feature.Base64StringAsByteArray);

        assertEquals("{\"bizContent\":{\"bizContent\":\"{\\\"action\\\":\\\"testAction\\\",\\\"order_id\\\":123123123123}\"}}",
                new String(clusterMessage.message.payload, StandardCharsets.UTF_8));
        assertEquals("{\"action\":\"testAction\",\"order_id\":123123123123}",
                clusterMessage.message.payloadObject.bizContent.bizContent);
    }

    @Test
    public void testBase64FieldFollowedByEscapedContentString() {
        ClusterMessage clusterMessage = JSON.parseObject(JSON_TEXT, ClusterMessage.class,
                JSONReader.Feature.Base64StringAsByteArray);

        assertArrayEquals(clusterMessage.message.payload,
                JSON.parseObject(JSON_TEXT, ClusterMessage.class,
                        JSONReader.Feature.Base64StringAsByteArray).message.payload);
        assertEquals("{\"action\":\"testAction\",\"order_id\":123123123123}",
                clusterMessage.message.payloadObject.bizContent.bizContent);
    }

    @Test
    public void testBase64WithBackslashInsideStringStillRejected() {
        // a backslash inside the base64 string itself is still invalid base64
        String broken = "{\"message\":{"
                + "\"payload\":\"abc\\\\def==\""
                + "}}";

        try {
            JSON.parseObject(broken.getBytes(StandardCharsets.UTF_8), ClusterMessage.class,
                    JSONReader.Feature.Base64StringAsByteArray);
        } catch (Exception e) {
            // reject is expected; must not succeed with corrupted content
            return;
        }
        throw new IllegalStateException("expected failure for backslash inside base64 string");
    }
}
