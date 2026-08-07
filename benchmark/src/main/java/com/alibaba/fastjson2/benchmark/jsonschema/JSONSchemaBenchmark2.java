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

public class JSONSchemaBenchmark2 {
    @State(Scope.Thread)
    public static class BenchmarkState {
        // everit
        private Schema jsonSchemaEverit;
        private org.json.JSONObject schemasEverit;
        private List<String> schemaNames;

        // fastjson
        private JSONSchema jsonSchemaFastjson2;
        private JSONObject schemasFastjson2;

        private JsonSchema jsonSchemaNetworknt;
        private JsonNode schemasNetworknt;

        public BenchmarkState() {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            {
                org.json.JSONObject root = new org.json.JSONObject(new org.json.JSONTokener(classLoader.getResourceAsStream("schema/perftest.json")));
                org.json.JSONObject schemaObject = new org.json.JSONObject(new org.json.JSONTokener(classLoader.getResourceAsStream("schema/schema-draft4.json")));
                jsonSchemaEverit = SchemaLoader.load(schemaObject);
                schemasEverit = root.getJSONObject("schemas");

                schemaNames = Arrays.asList(org.json.JSONObject.getNames(schemasEverit));
            }

            {
                JSONObject root = JSON.parseObject(classLoader.getResource("schema/perftest.json"));
                JSONObject schemaObject = JSON.parseObject(classLoader.getResource("schema/schema-draft4.json"));
                jsonSchemaFastjson2 = JSONSchema.of(schemaObject);
                schemasFastjson2 = root.getJSONObject("schemas");
            }

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonSchemaFactory factory = JsonSchemaFactory.getInstance();

                ObjectReader reader = objectMapper.reader();
                JsonNode schemaNode = reader.readTree(classLoader.getResourceAsStream("schema/schema-draft4.json"));
                jsonSchemaNetworknt = factory.getSchema(schemaNode);

                JsonNode root = reader.readTree(classLoader.getResourceAsStream("schema/perftest.json"));
                schemasNetworknt = root.get("schemas");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Benchmark
    public void everit(BenchmarkState state) {
        for (String name : state.schemaNames) {
            org.json.JSONObject json = (org.json.JSONObject) state.schemasEverit.get(name);
            state.jsonSchemaEverit.validate(json);
        }
    }

    @Benchmark
    public void fastjson2(BenchmarkState state) {
        for (String name : state.schemaNames) {
            JSONObject json = state.schemasFastjson2.getJSONObject(name);
            state.jsonSchemaFastjson2.validate(json);
        }
    }

    @Benchmark
    public void networknt(BenchmarkState state) {
        for (String name : state.schemaNames) {
            JsonNode json = state.schemasNetworknt.get(name);
            state.jsonSchemaNetworknt.validate(json);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(JSONSchemaBenchmark2.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
