package com.alibaba.fastjson2.benchmark.jsonschema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JSONSchemaBenchmark {
    @State(Scope.Thread)
    public static class BenchmarkState {
        // everit
        private Schema jsonSchema_everit;
        private org.json.JSONObject schemas_everit;
        private List<String> schemaNames;

        // fastjson
        private JSONSchema jsonSchema_fastjson2;
        private JSONObject schemas_fastjson2;

        private JsonSchema jsonSchema_networknt;
        private JsonNode schemas_networknt;

        public BenchmarkState() {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            {
                org.json.JSONObject root = new org.json.JSONObject(new org.json.JSONTokener(classLoader.getResourceAsStream("schema/perftest.json")));
                org.json.JSONObject schemaObject = new org.json.JSONObject(new org.json.JSONTokener(classLoader.getResourceAsStream("schema/schema-draft4.json")));
                jsonSchema_everit = SchemaLoader.load(schemaObject);
                schemas_everit = root.getJSONObject("schemas");

                schemaNames = Arrays.asList(org.json.JSONObject.getNames(schemas_everit));
            }

            {
                JSONObject root = JSON.parseObject(classLoader.getResource("schema/perftest.json"));
                JSONObject schemaObject = JSON.parseObject(classLoader.getResource("schema/schema-draft4.json"));
                jsonSchema_fastjson2 = JSONSchema.of(schemaObject);
                schemas_fastjson2 = root.getJSONObject("schemas");
            }

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonSchemaFactory factory = JsonSchemaFactory.getInstance();

                ObjectReader reader = objectMapper.reader();
                JsonNode schemaNode = reader.readTree(classLoader.getResourceAsStream("schema/schema-draft4.json"));
                jsonSchema_networknt = factory.getSchema(schemaNode);

                JsonNode root = reader.readTree(classLoader.getResourceAsStream("schema/perftest.json"));
                schemas_networknt = root.get("schemas");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Benchmark
    public void everit(BenchmarkState state) {
        for (String name : state.schemaNames) {
            org.json.JSONObject json = (org.json.JSONObject) state.schemas_everit.get(name);
            state.jsonSchema_everit.validate(json);
        }
    }

    @Benchmark
    public void fastjson2(BenchmarkState state) {
        for (String name : state.schemaNames) {
            JSONObject json = state.schemas_fastjson2.getJSONObject(name);
            state.jsonSchema_fastjson2.validate(json);
        }
    }

    @Benchmark
    public void networknt(BenchmarkState state) {
        for (String name : state.schemaNames) {
            JsonNode json = state.schemas_networknt.get(name);
            state.jsonSchema_networknt.validate(json);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(JSONSchemaBenchmark.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
