package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptionalWriterFeaturesTest {
    @Test
    public void optionalPassesExplicitFeaturesToItsValueWriter() {
        ObjectWriter writer = new ObjectWriterProvider().getObjectWriter(Optional.class);
        try (JSONWriter json = JSONWriter.of()) {
            writer.write(json, Optional.of(123), null, null, JSONWriter.Feature.WriteNonStringValueAsString.mask);
            assertEquals("\"123\"", json.toString());
        }
    }
}
