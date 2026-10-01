package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathFieldComparisonReviewTest {
    @Test
    public void mapAndBeanFieldComparisons() {
        Bean small = new Bean(1, 2);
        Bean equal = new Bean(2, 2);
        Bean large = new Bean(3, 2);
        for (JSONArray input : new JSONArray[]{JSONArray.of(small, equal, large),
                JSONArray.of(JSONObject.from(small), JSONObject.from(equal), JSONObject.from(large))}) {
            String[] operators = {"==", "!=", "<", "<=", ">", ">="};
            int[][] matches = {{1}, {0, 2}, {0}, {0, 1}, {2}, {1, 2}};
            for (int i = 0; i < operators.length; i++) {
                JSONArray expected = new JSONArray();
                for (int index : matches[i]) {
                    expected.add(input.get(index));
                }
                assertEquals(expected, JSONPath.eval(input, "$[?(@.left " + operators[i] + " @.right)]"));
            }
        }
    }

    @Test
    public void nullAndMissingFieldsDoNotMatchRelationalComparisons() {
        for (String operator : new String[]{"<", "<=", ">", ">="}) {
            String expression = "$[?(@.left " + operator + " @.right)]";
            JSONArray beans = JSONArray.of(new NullableBean(null, 2), new NullableBean(2, null),
                    new NullableBean(null, null));
            JSONArray maps = JSONArray.of(JSONObject.of("left", null, "right", 2),
                    JSONObject.of("left", 2, "right", null), JSONObject.of("right", 2),
                    JSONObject.of("left", 2), new JSONObject());
            assertEquals(new JSONArray(), JSONPath.eval(beans, expression));
            assertEquals(new JSONArray(), JSONPath.eval(maps, expression));
            assertEquals(new JSONArray(), JSONPath.extract(maps.toString(), expression));
        }
    }

    public static class NullableBean {
        public Integer left;
        public Integer right;

        public NullableBean(Integer left, Integer right) {
            this.left = left;
            this.right = right;
        }
    }

    public static class Bean {
        public int left;
        public int right;

        public Bean(int left, int right) {
            this.left = left;
            this.right = right;
        }
    }
}
