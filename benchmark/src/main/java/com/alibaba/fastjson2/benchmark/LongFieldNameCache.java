package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSON;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Benchmark for long field name caching in fastjson2.
 * Compares parsing performance with standard OTLP metrics JSON containing long property names
 * (e.g., startTimeUnixNano, aggregationTemporality, exponentialHistogram) versus a modified
 * version where those names are shortened to <=16 characters.
 *
 * Data source: OpenTelemetry metrics example (https://github.com/open-telemetry/opentelemetry-proto/blob/b363281e5c30f343bfd2405519cc3db93d11f252/examples/metrics.json)
 * Licensed under Apache License 2.0.
 */
@State(Scope.Benchmark)
public class LongFieldNameCache {
    @Param({"OTLP", "SHORT"})
    public String payload;

    private byte[] utf8Bytes;

    @Setup
    public void setup() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream input = LongFieldNameCache.class.getClassLoader().getResourceAsStream("data/otlp-metrics.json")) {
            if (input == null) {
                throw new IOException("Missing data/otlp-metrics.json");
            }
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
        }
        String json = new String(output.toByteArray(), StandardCharsets.UTF_8);

        if ("SHORT".equals(payload)) {
            // Replace long property names with shorter equivalents, preserving JSON structure.
            // We only replace complete keys including quotes and colon to avoid affecting values.
            json = json.replace("\"startTimeUnixNano\":", "\"startTime\":");
            json = json.replace("\"aggregationTemporality\":", "\"temporality\":");
            json = json.replace("\"exponentialHistogram\":", "\"expHistogram\":");
        }
        utf8Bytes = json.getBytes(StandardCharsets.UTF_8);
    }

    @Benchmark
    public Object parse() {
        return JSON.parseObject(utf8Bytes);
    }
}
