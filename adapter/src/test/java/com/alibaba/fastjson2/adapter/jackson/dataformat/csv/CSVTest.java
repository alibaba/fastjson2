package com.alibaba.fastjson2.adapter.jackson.dataformat.csv;

import com.alibaba.fastjson2.adapter.jackson.core.JsonFactory;
import com.alibaba.fastjson2.adapter.jackson.core.JsonGenerator;
import com.alibaba.fastjson2.adapter.jackson.databind.MappingIterator;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectMapper;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class CSVTest {
    @Test
    public void test() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
        assertSame(jsonFactory, objectMapper.getFactory());
    }

    @Test
    public void test1() throws Exception {
        String csv = "1,DataWorks\n2,MaxCompute\n3,EMR";

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        ObjectReader oReader = csvMapper.reader(Product.class).with(schema);
        List<Product> products = new ArrayList<>();

        try (Reader reader = new StringReader(csv)) {
            MappingIterator<Product> mi = oReader.readValues(reader);
            while (mi.hasNext()) {
                Product current = mi.next();
                products.add(current);
            }
            mi.close();
        }

        assertEquals(3, products.size());
        assertEquals(1, products.get(0).id);
        assertEquals(2, products.get(1).id);
        assertEquals(3, products.get(2).id);
    }

    public static class Product {
        public int id;
        public String name;
    }
}
