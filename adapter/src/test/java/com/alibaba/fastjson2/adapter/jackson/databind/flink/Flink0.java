package com.alibaba.fastjson2.adapter.jackson.databind.flink;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonCreator;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonProperty;
import com.alibaba.fastjson2.adapter.jackson.core.JacksonException;
import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import com.alibaba.fastjson2.adapter.jackson.databind.DeserializationContext;
import com.alibaba.fastjson2.adapter.jackson.databind.annotation.JsonDeserialize;
import com.alibaba.fastjson2.adapter.jackson.databind.deser.std.StdDeserializer;
import com.alibaba.fastjson2.adapter.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Flink0 {
    @Test
    public void test() {
        JsonJobResultEntry entry = JSON.parseObject("{\"result\":{\"xid\":123}}", JsonJobResultEntry.class);
        assertNotNull(entry);
        assertEquals(123, entry.getJobResult().id);
    }

    static class JsonJobResultEntry {
        private JobResult result;

        @JsonCreator
        private JsonJobResultEntry(JobResult result) {
            this.result = result;
        }

        @JsonProperty("result")
        @JsonDeserialize(using = JobResultDeserializer.class)
        public JobResult getJobResult() {
            return result;
        }
    }

    public static class JobResultDeserializer
            extends StdDeserializer<JobResult> {
        public JobResultDeserializer() {
            super(JobResult.class);
        }

        @Override
        public JobResult deserialize(JsonParser p, DeserializationContext ctx) throws IOException, JacksonException {
            ObjectNode treeNode = p.readValueAsTree();
            int jobId = treeNode.get("xid").asInt();
            return new JobResult(jobId);
        }
    }

    static class JobResult {
        public int id;

        private JobResult(int id) {
            this.id = id;
        }
    }
}
