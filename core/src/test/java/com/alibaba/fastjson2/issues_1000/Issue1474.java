package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1474 {
    @Test
    public void test() {
        JSON.mixIn(ProducerRecord.class, ProducerRecordMixin.class);

        ProducerRecord record = new ProducerRecord("abc");
        assertEquals("{\"topic\":\"abc\"}", JSON.toJSONString(record));
    }

    public interface ProducerRecordMixin {
        @JSONField
        String topic();
    }

    public static class ProducerRecord {
        private final String topic;

        public ProducerRecord(String topic) {
            this.topic = topic;
        }

        public String topic() {
            return topic;
        }
    }

    @Test
    public void test1() {
        JSON.mixIn(ProducerRecord1.class, ProducerRecordMixin1.class);

        ProducerRecord1 record = new ProducerRecord1("abc");
        assertEquals("{\"topic\":\"abc\"}", JSON.toJSONString(record));
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.FieldBased)
    public interface ProducerRecordMixin1 {
    }

    public static class ProducerRecord1 {
        private final String topic;

        public ProducerRecord1(String topic) {
            this.topic = topic;
        }

        public String topic() {
            return topic;
        }
    }

    @Test
    public void test2() {
        JSON.mixIn(ProducerRecord2.class, ProducerRecordMixin2.class);

        ProducerRecord2 record = new ProducerRecord2("abc");
        assertEquals("{\"topic\":\"abc\"}", JSON.toJSONString(record));
    }

    public interface ProducerRecordMixin2 {
        @JSONField
        String topic();
    }

    static class ProducerRecord2 {
        private final String topic;

        public ProducerRecord2(String topic) {
            this.topic = topic;
        }

        public String topic() {
            return topic;
        }
    }
}
