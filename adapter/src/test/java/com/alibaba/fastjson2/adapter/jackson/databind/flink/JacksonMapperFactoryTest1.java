package com.alibaba.fastjson2.adapter.jackson.databind.flink;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JacksonMapperFactoryTest1 {
    @Test
    public void test() throws Exception {
        final CsvMapper mapper = new CsvMapper();
        mapper.registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module().configureAbsentsAsNulls(true))
                .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        final String instantString = "2022-08-07T12:00:33.107787800Z";
        final Instant instant = Instant.parse(instantString);
        final String instantCsv = String.format("\"%s\"\n", instantString);

        final ObjectWriter writer = mapper.writerWithSchemaFor(TypeWithInstant.class);

        TypeWithInstant bean = new TypeWithInstant(instant);
        String str = writer.writeValueAsString(bean);
        assertEquals(instantCsv, str);

        final ObjectReader reader = mapper.readerWithSchemaFor(TypeWithInstant.class);
        TypeWithInstant bean1 = reader.readValue(instantCsv, TypeWithInstant.class);
        assertEquals(bean.data, bean1.data);
    }

    @Test
    void testCsvMapperOptionalSupportedEnabled() throws Exception {
        final CsvMapper mapper = new CsvMapper();
        mapper.registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module().configureAbsentsAsNulls(true))
                .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        final ObjectWriter writer = mapper.writerWithSchemaFor(TypeWithOptional.class);

        assertEquals("value\n", writer.writeValueAsString(new TypeWithOptional(Optional.of("value"))));
        assertEquals("\n", writer.writeValueAsString(new TypeWithOptional(Optional.empty())));

        final ObjectReader reader = mapper.readerWithSchemaFor(TypeWithOptional.class);

        assertTrue((reader.readValue("value\n", TypeWithOptional.class).data).get().contains("value"));
        assertTrue((reader.readValue("null\n", TypeWithOptional.class).data).get().contains("null"));
        assertTrue((reader.readValue("\n", TypeWithOptional.class).data).get().isEmpty());
    }

    public static class TypeWithOptional {
        public Optional<String> data;

        @JsonCreator
        public TypeWithOptional(@JsonProperty("data") Optional<String> data) {
            this.data = data;
        }
    }

    public static class TypeWithInstant {
        public Instant data;

        @JsonCreator
        public TypeWithInstant(@JsonProperty("data") Instant data) {
            this.data = data;
        }
    }
}
