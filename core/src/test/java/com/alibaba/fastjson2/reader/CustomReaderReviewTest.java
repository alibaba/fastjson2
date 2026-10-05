package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomReaderReviewTest {
    @Test
    public void listUsesExplicitItemReader() {
        ObjectReader<ListBean> reader = ObjectReaders.of(ListBean.class, ListBean::new,
                ObjectReaders.fieldReaderList("values", String.class, ArrayList::new,
                        (ListBean bean, List<String> values) -> bean.values = values,
                        ObjectReaders.ofString(value -> "custom:" + value)));
        try (JSONReader json = JSONReader.of("{\"values\":[\"a\",\"b\"]}")) {
            assertEquals(Arrays.asList("custom:a", "custom:b"), reader.readObject(json).values);
        }
    }

    @Test
    public void threeFieldsSmartMatchJsonb() {
        ObjectReader<Names> reader = ObjectReaderCreator.INSTANCE.createObjectReader(Names.class);
        byte[] bytes = JSONB.toBytes(JSONObject.of("FIRST_NAME", 1, "SECOND_NAME", 2, "THIRD_NAME", 3));
        try (JSONReader json = JSONReader.ofJSONB(bytes)) {
            Names bean = reader.readJSONBObject(json, null, null, JSONReader.Feature.SupportSmartMatch.mask);
            assertEquals(1, bean.firstName);
            assertEquals(2, bean.secondName);
            assertEquals(3, bean.thirdName);
        }
    }

    @Test
    public void twoFieldArrayMappingAppliesBuilderAndConsumesComma() {
        ObjectReader2<Pair> reader = new ObjectReader2<>(Pair.class, null, null, 0, Pair::new,
                value -> {
                    Pair pair = (Pair) value;
                    pair.first += 10;
                    return pair;
                }, ObjectReaders.fieldReaderInt("first", (Pair pair, int value) -> pair.first = value),
                ObjectReaders.fieldReaderInt("second", (Pair pair, int value) -> pair.second = value));
        try (JSONReader json = JSONReader.of("[1,2],3")) {
            Pair pair = reader.readObject(json, null, null, JSONReader.Feature.SupportArrayToBean.mask);
            assertEquals(11, pair.first);
            assertEquals(2, pair.second);
            assertEquals(3, json.readInt32Value());
        }
    }

    @Test
    public void adapterAppliesJsonbBuilder() {
        ObjectReaderAdapter<Pair> reader = new ObjectReaderAdapter<>(Pair.class, null, null, 0, Pair::new,
                value -> {
                    Pair pair = (Pair) value;
                    pair.first += 10;
                    return pair;
                }, ObjectReaders.fieldReaderInt("first", (Pair pair, int value) -> pair.first = value));
        try (JSONReader json = JSONReader.ofJSONB(JSONB.toBytes(JSONObject.of("first", 1)))) {
            assertEquals(11, reader.readJSONBObject(json, null, null, 0).first);
        }
    }

    public static class ListBean {
        public List<String> values;
    }

    public static class Names {
        public int firstName;
        public int secondName;
        public int thirdName;
    }

    public static class Pair {
        public int first;
        public int second;
    }
}
