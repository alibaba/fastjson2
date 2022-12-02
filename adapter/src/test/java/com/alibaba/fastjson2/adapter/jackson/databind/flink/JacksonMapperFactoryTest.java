package com.alibaba.fastjson2.adapter.jackson.databind.flink;

import com.alibaba.fastjson2.adapter.jackson.annotation.JsonCreator;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonProperty;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectWriter;
import com.alibaba.fastjson2.adapter.jackson.dataformat.csv.CsvMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JacksonMapperFactoryTest {
    @Test
    public void test() throws Exception {
        final CsvMapper mapper = JacksonMapperFactory.createCsvMapper();

        final String instantString = "2022-08-07T12:00:33.1077878Z";
        final Instant instant = Instant.parse(instantString);
        final String instantCsv = String.format("\"%s\"\n", instantString);

        final ObjectWriter writer = mapper.writerWithSchemaFor(TypeWithInstant.class);

        TypeWithInstant bean = new TypeWithInstant(instant);
        assertEquals(instantCsv, writer.writeValueAsString(bean));

        final ObjectReader reader = mapper.readerWithSchemaFor(TypeWithInstant.class);
        TypeWithInstant bean1 = reader.readValue(instantCsv, TypeWithInstant.class);
        assertEquals(bean.data, bean1.data);
    }

    @Test
    public void testFile() throws Exception {
        final CsvMapper mapper = JacksonMapperFactory.createCsvMapper();

        final String instantString = "2022-08-07T12:00:33.1077878Z";
        final Instant instant = Instant.parse(instantString);
        final String instantCsv = String.format("\"%s\"\n", instantString);

        final ObjectWriter writer = mapper.writerWithSchemaFor(TypeWithInstant.class);

        TypeWithInstant bean = new TypeWithInstant(instant);
        File file = File.createTempFile("tmp", "csv");
        writer.writeValue(file, bean);
        String str = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        assertEquals(instantCsv, str);

        final ObjectReader reader = mapper.readerWithSchemaFor(TypeWithInstant.class);
        TypeWithInstant bean1 = reader.readValue(instantCsv, TypeWithInstant.class);
        assertEquals(bean.data, bean1.data);
    }

    @Test
    public void test1() throws Exception {
        final CsvMapper mapper = JacksonMapperFactory.createCsvMapper();

        final ObjectWriter writer = mapper.writerWithSchemaFor(TypeWithOptional.class);

        assertEquals("value\n", writer.writeValueAsString(new TypeWithOptional(Optional.of("value"))));
        assertEquals("\n", writer.writeValueAsString(new TypeWithOptional(Optional.empty())));

        final ObjectReader reader = mapper.readerWithSchemaFor(TypeWithOptional.class);

        assertTrue((reader.readValue("value\n", TypeWithOptional.class).data).get().contains("value"));
        assertTrue((reader.readValue("null\n", TypeWithOptional.class).data).get().contains("null"));
        assertFalse((reader.readValue("\n", TypeWithOptional.class).data).isPresent());
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
