package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComplexTest {
    @Test
    public void test() {
        Function function = JSONObject.of("name", "cos", "category", "numeric")
                .to(Function.class);
        assertEquals("cos", function.name);
        assertTrue(function.category.contains(Function.Category.Numeric));
    }

    @Test
    public void testList() {
        Function function = JSONObject.of(
                        "name", "concat",
                        "category", JSONArray.of("array", "string"))
                .to(Function.class);
        assertEquals("concat", function.name);
        assertEquals(2, function.category.size());
        assertTrue(function.category.contains(Function.Category.Array));
        assertTrue(function.category.contains(Function.Category.String));

        JSONObject root = JSONObject.of(
                "functions",
                JSONArray.of(
                        function,
                        JSONObject.of(
                                "name", "count",
                                "category", "aggregate"),
                        JSONObject.of(
                                "name", "count",
                                "category", "window")
                )
        );

        String json = JSON.toJSONString(root);

        {
            Bean bean = JSON.parseObject(json, Bean.class);
            assertEquals(2, bean.functions.size());
            assertEquals(2, bean.functions.get("count").category.size());
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Aggregate));
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Window));
        }
        {
            Bean1 bean = JSON.parseObject(json, Bean1.class);
            assertEquals(2, bean.functions.size());
            assertEquals(2, bean.functions.get("count").category.size());
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Aggregate));
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Window));
        }
        {
            Bean2 bean = JSON.parseObject(json, Bean2.class);
            assertEquals(2, bean.functions.size());
            assertEquals(2, bean.functions.get("count").category.size());
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Aggregate));
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Window));
        }
        {
            Bean3 bean = JSON.parseObject(json, Bean3.class);
            assertEquals(2, bean.functions.size());
            assertEquals(2, bean.functions.get("count").category.size());
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Aggregate));
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Window));
        }

        {
            Bean3 bean = root.to(Bean3.class);
            assertEquals(2, bean.functions.size());
        }
        {
            Bean2 bean = root.to(Bean2.class);
            assertEquals(2, bean.functions.size());
            assertEquals(2, bean.functions.get("count").category.size());
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Aggregate));
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Window));
        }
        {
            Bean1 bean = root.to(Bean1.class);
            assertEquals(2, bean.functions.size());
            assertEquals(2, bean.functions.get("count").category.size());
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Aggregate));
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Window));
        }
        {
            Bean bean = root.to(Bean.class);
            assertEquals(2, bean.functions.size());
            assertEquals(2, bean.functions.get("count").category.size());
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Aggregate));
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Window));
        }

        {
            Bean bean = new Bean();
            root.copyTo(bean);
            assertEquals(2, bean.functions.size());
            assertEquals(2, bean.functions.get("count").category.size());
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Aggregate));
            assertTrue(bean.functions.get("count").category.contains(Function.Category.Window));
        }
    }

    public static class Bean {
        private final Map<String, Function> functions = new LinkedHashMap<>();

        @JSONField(arrayToMapKey = "name", arrayToMapDuplicateHandler = FunctionDuplicateHandler.class)
        public Map<String, Function> getFunctions() {
            return functions;
        }
    }

    public static class Bean1 {
        @JSONField(arrayToMapKey = "name", arrayToMapDuplicateHandler = FunctionDuplicateHandler.class)
        public final Map<String, Function> functions = new LinkedHashMap<>();
    }

    public static class Bean2 {
        @JSONField(arrayToMapKey = "name", arrayToMapDuplicateHandler = FunctionDuplicateHandler.class)
        private Map<String, Function> functions;

        public Map<String, Function> getFunctions() {
            return functions;
        }

        public void setFunctions(Map<String, Function> functions) {
            this.functions = functions;
        }
    }

    public static class Bean3 {
        @JSONField(arrayToMapKey = "name", arrayToMapDuplicateHandler = FunctionDuplicateHandler.class)
        public Map<String, Function> functions;
    }

    public static class Function {
        final String name;
        final Set<Category> category = new LinkedHashSet<>();

        public Function(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Set<Category> getCategory() {
            return category;
        }

        public enum Category {
            Array,
            Aggregate,
            Window,
            Numeric,
            String,
            Conversation,
            Encrypt,
            Datetime
        }
    }

    static class FunctionDuplicateHandler
            implements BiConsumer<Function, Function> {
        @Override
        public void accept(Function a, Function b) {
            a.category.addAll(b.category);
        }
    }
}
